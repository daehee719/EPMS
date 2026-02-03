package egovframework.com.edu.prm.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import egovframework.com.edu.prm.service.EgovProgramUploadService;

@Service("egovProgramUploadService")
public class EgovProgramUploadServiceImpl extends EgovAbstractServiceImpl implements EgovProgramUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovProgramUploadServiceImpl.class);
    private static final int MAX_ROWS = 10000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ProgramUploadDAO programUploadDAO;
    private final TransactionTemplate transactionTemplate;

    public EgovProgramUploadServiceImpl(ProgramUploadDAO programUploadDAO, PlatformTransactionManager transactionManager) {
        this.programUploadDAO = programUploadDAO;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public String startUpload(MultipartFile file, String regId) throws Exception {
        String originalName = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String ext = getFileExt(originalName);
        if (!("csv".equalsIgnoreCase(ext) || "xlsx".equalsIgnoreCase(ext))) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (csv, xlsx)");
        }

        String jobId = UUID.randomUUID().toString();
        Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "epms_upload");
        Files.createDirectories(uploadDir);
        Path saved = uploadDir.resolve(jobId + "." + ext.toLowerCase());
        file.transferTo(saved.toFile());

        Map<String, Object> job = new HashMap<>();
        job.put("jobId", jobId);
        job.put("fileName", originalName);
        job.put("fileType", ext.toLowerCase());
        job.put("status", "PENDING");
        job.put("message", "업로드 대기");
        job.put("regId", regId);
        programUploadDAO.insertUploadJob(job);

        processUploadAsync(jobId, saved.toString(), ext.toLowerCase(), regId);
        return jobId;
    }

    @Override
    public Map<String, Object> getUploadStatus(String jobId) throws Exception {
        Map<String, Object> job = programUploadDAO.selectUploadJob(jobId);
        if (job == null) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("status", "NOT_FOUND");
            notFound.put("message", "작업을 찾을 수 없습니다.");
            return notFound;
        }
        return job;
    }

    @Async("uploadTaskExecutor")
    public void processUploadAsync(String jobId, String filePath, String fileType, String regId) {
        try {
            updateJob(jobId, "RUNNING", "처리 중", 0, 0, 0, new Timestamp(System.currentTimeMillis()), null);
            UploadResult result;
            if ("csv".equalsIgnoreCase(fileType)) {
                result = parseCsv(jobId, Paths.get(filePath), regId);
            } else {
                result = parseXlsx(jobId, Paths.get(filePath), regId);
            }

            if (result.totalCount > MAX_ROWS) {
                programUploadDAO.deleteProgramStaging(jobId);
                updateJob(jobId, "FAILED", "최대 10,000건을 초과했습니다.", result.totalCount, 0, result.errorCount, null, new Timestamp(System.currentTimeMillis()));
                return;
            }

            if (result.errorCount > 0) {
                updateJob(jobId, "FAILED", "오류가 발견되어 반영되지 않았습니다.", result.totalCount, result.successCount, result.errorCount, null, new Timestamp(System.currentTimeMillis()));
                return;
            }

            mergeAndClean(jobId);
            updateJob(jobId, "SUCCESS", "정상 반영 완료", result.totalCount, result.successCount, result.errorCount, null, new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            LOGGER.error("Upload failed: {}", jobId, e);
            updateJob(jobId, "FAILED", "처리 중 오류가 발생했습니다.", 0, 0, 0, null, new Timestamp(System.currentTimeMillis()));
        }
    }

    protected void mergeAndClean(String jobId) {
        transactionTemplate.execute(status -> {
            programUploadDAO.mergeProgramFromStaging(jobId);
            programUploadDAO.deleteProgramStaging(jobId);
            return null;
        });
    }

    private UploadResult parseCsv(String jobId, Path file, String regId) throws Exception {
        int total = 0;
        int success = 0;
        int errors = 0;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int rowNo = 0;
            while ((line = reader.readLine()) != null) {
                rowNo++;
                if (rowNo == 1 && line.toLowerCase().contains("program")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                total++;
                String[] cols = parseCsvLine(line);
                UploadRow row = toRow(cols);
                List<String> rowErrors = validateRow(row);
                if (!rowErrors.isEmpty()) {
                    errors += rowErrors.size();
                    for (String err : rowErrors) {
                        insertError(jobId, rowNo, "", err, line);
                    }
                    continue;
                }
                insertStaging(jobId, row, regId);
                success++;
            }
        }
        return new UploadResult(total, success, errors);
    }

    private UploadResult parseXlsx(String jobId, Path file, String regId) throws Exception {
        int total = 0;
        int success = 0;
        int errors = 0;
        DataFormatter formatter = new DataFormatter();

        try (InputStream is = Files.newInputStream(file); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int rowIdx = 0;
            for (Row row : sheet) {
                rowIdx++;
                if (rowIdx == 1) {
                    continue;
                }
                if (row == null) {
                    continue;
                }
                String[] cols = new String[7];
                for (int i = 0; i < cols.length; i++) {
                    Cell cell = row.getCell(i);
                    cols[i] = getCellValue(cell, formatter);
                }
                if (isRowEmpty(cols)) {
                    continue;
                }
                total++;
                UploadRow data = toRow(cols);
                List<String> rowErrors = validateRow(data);
                if (!rowErrors.isEmpty()) {
                    errors += rowErrors.size();
                    for (String err : rowErrors) {
                        insertError(jobId, rowIdx, "", err, String.join(",", cols));
                    }
                    continue;
                }
                insertStaging(jobId, data, regId);
                success++;
            }
        }
        return new UploadResult(total, success, errors);
    }

    private String getCellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return DATE_FMT.format(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        return formatter.formatCellValue(cell).trim();
    }

    private UploadRow toRow(String[] cols) {
        UploadRow row = new UploadRow();
        row.programCode = get(cols, 0);
        row.programName = get(cols, 1);
        row.startDateRaw = get(cols, 2);
        row.endDateRaw = get(cols, 3);
        row.startDate = parseDate(row.startDateRaw);
        row.endDate = parseDate(row.endDateRaw);
        row.capacityRaw = get(cols, 4);
        row.capacity = parseInt(row.capacityRaw);
        row.useYn = get(cols, 5);
        row.description = get(cols, 6);
        return row;
    }

    private List<String> validateRow(UploadRow row) {
        List<String> errors = new ArrayList<>();
        if (row.programCode.isEmpty()) {
            errors.add("프로그램 코드는 필수입니다.");
        }
        if (row.programName.isEmpty()) {
            errors.add("프로그램 명칭은 필수입니다.");
        }
        if (!(row.useYn.equalsIgnoreCase("Y") || row.useYn.equalsIgnoreCase("N"))) {
            errors.add("사용여부는 Y 또는 N이어야 합니다.");
        }
        if (!row.capacityRaw.isEmpty() && row.capacity == null) {
            errors.add("정원은 숫자여야 합니다.");
        }
        if (!row.startDateRaw.isEmpty() && row.startDate == null) {
            errors.add("시작일 형식이 올바르지 않습니다. (yyyy-MM-dd)");
        }
        if (!row.endDateRaw.isEmpty() && row.endDate == null) {
            errors.add("종료일 형식이 올바르지 않습니다. (yyyy-MM-dd)");
        }
        if (row.startDate != null && row.endDate != null && row.startDate.after(row.endDate)) {
            errors.add("시작일은 종료일보다 클 수 없습니다.");
        }
        if (row.capacity != null && row.capacity < 0) {
            errors.add("정원은 0 이상이어야 합니다.");
        }
        return errors;
    }

    private void insertStaging(String jobId, UploadRow row, String regId) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobId", jobId);
        params.put("programCode", row.programCode);
        params.put("programName", row.programName);
        params.put("startDate", row.startDate);
        params.put("endDate", row.endDate);
        params.put("capacity", row.capacity);
        params.put("useYn", row.useYn.toUpperCase());
        params.put("description", row.description);
        params.put("regId", regId);
        params.put("regDt", new Timestamp(System.currentTimeMillis()));
        params.put("updId", regId);
        params.put("updDt", new Timestamp(System.currentTimeMillis()));
        programUploadDAO.insertProgramStaging(params);
    }

    private void insertError(String jobId, int rowNo, String fieldName, String message, String rawData) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobId", jobId);
        params.put("rowNo", rowNo);
        params.put("fieldName", fieldName);
        params.put("errorMessage", message);
        params.put("rawData", rawData != null && rawData.length() > 2000 ? rawData.substring(0, 2000) : rawData);
        programUploadDAO.insertUploadError(params);
    }

    private void updateJob(String jobId, String status, String message, int total, int success, int error, Timestamp startDt, Timestamp endDt) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobId", jobId);
        params.put("status", status);
        params.put("message", message);
        params.put("totalCount", total);
        params.put("successCount", success);
        params.put("errorCount", error);
        params.put("startDt", startDt);
        params.put("endDt", endDt);
        programUploadDAO.updateUploadJob(params);
    }

    private String getFileExt(String name) {
        int idx = name.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return name.substring(idx + 1);
    }

    private boolean isRowEmpty(String[] cols) {
        for (String col : cols) {
            if (col != null && !col.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String get(String[] cols, int idx) {
        if (cols == null || cols.length <= idx || cols[idx] == null) {
            return "";
        }
        return cols[idx].trim();
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate d = LocalDate.parse(value.trim(), DATE_FMT);
            return Date.valueOf(d);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        result.add(sb.toString());
        while (result.size() < 7) {
            result.add("");
        }
        return result.toArray(new String[0]);
    }

    private static class UploadRow {
        String programCode;
        String programName;
        String startDateRaw;
        String endDateRaw;
        String capacityRaw;
        Date startDate;
        Date endDate;
        Integer capacity;
        String useYn;
        String description;
    }

    private static class UploadResult {
        final int totalCount;
        final int successCount;
        final int errorCount;

        UploadResult(int totalCount, int successCount, int errorCount) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.errorCount = errorCount;
        }
    }
}
