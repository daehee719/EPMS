package egovframework.com.edu.prm.web;

import jakarta.annotation.Resource;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.edu.prm.service.EgovProgramManageService;
import egovframework.com.edu.prm.service.EgovProgramUploadService;
import egovframework.com.edu.prm.service.ProgramManage;
import egovframework.com.edu.prm.service.ProgramManageVO;

@Controller
public class EgovProgramManageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovProgramManageController.class);

    @Resource(name = "egovProgramManageService")
    private EgovProgramManageService egovProgramManageService;

    @Resource(name = "egovProgramUploadService")
    private EgovProgramUploadService egovProgramUploadService;

    @RequestMapping("/adm/program/manage.do")
    public String selectProgramManage(@ModelAttribute("programVO") ProgramManageVO programManageVO,
            ModelMap model) throws Exception {
        bindPaging(programManageVO, model);
        model.addAttribute("mode", "admin");
        return "egovframework/com/edu/prm/ProgramManage";
    }

    @RequestMapping("/edu/program/list.do")
    public String selectProgramList(@ModelAttribute("programVO") ProgramManageVO programManageVO,
            ModelMap model) throws Exception {
        bindPaging(programManageVO, model);
        return "egovframework/com/edu/prm/ProgramList";
    }

    @RequestMapping("/adm/program/form.do")
    public String programForm(@ModelAttribute("programVO") ProgramManageVO programManageVO, ModelMap model)
            throws Exception {
        if (programManageVO.getProgramCode() != null && !programManageVO.getProgramCode().trim().isEmpty()) {
            ProgramManageVO result = egovProgramManageService.selectProgram(programManageVO);
            model.addAttribute("program", result);
        }
        return "egovframework/com/edu/prm/ProgramForm";
    }

    @RequestMapping("/adm/program/insert.do")
    public String insertProgram(@ModelAttribute("program") ProgramManage programManage) throws Exception {
        LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
        if (user != null) {
            programManage.setRegId(user.getId());
            programManage.setUpdId(user.getId());
        }
        egovProgramManageService.insertProgram(programManage);
        return "redirect:/adm/program/manage.do";
    }

    @RequestMapping("/adm/program/upload.do")
    public String uploadView() {
        return "egovframework/com/edu/prm/ProgramUpload";
    }

    @RequestMapping("/adm/program/upload/submit.do")
    @ResponseBody
    public Map<String, Object> uploadSubmit(@RequestParam("uploadFile") MultipartFile uploadFile) throws Exception {
        LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
        String regId = user != null ? user.getId() : "SYSTEM";
        String jobId = egovProgramUploadService.startUpload(uploadFile, regId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("jobId", jobId);
        return result;
    }

    @RequestMapping("/adm/program/upload/status.do")
    @ResponseBody
    public Map<String, Object> uploadStatus(@RequestParam("jobId") String jobId) throws Exception {
        return egovProgramUploadService.getUploadStatus(jobId);
    }

    @RequestMapping("/adm/program/export.csv")
    public void exportCsv(@ModelAttribute("programVO") ProgramManageVO programManageVO,
                          HttpServletResponse response) throws Exception {
        try {
            List<ProgramManageVO> rows = egovProgramManageService.selectProgramListAll(programManageVO);
            String fileName = buildFileName("programs", "csv");
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write("programCode,programName,startDate,endDate,capacity,useYn,regDt\n");
                for (ProgramManageVO row : rows) {
                    writer.write(csv(row.getProgramCode()));
                    writer.write(",");
                    writer.write(csv(row.getProgramName()));
                    writer.write(",");
                    writer.write(csv(row.getStartDate()));
                    writer.write(",");
                    writer.write(csv(row.getEndDate()));
                    writer.write(",");
                    writer.write(csv(row.getCapacity() == null ? "" : String.valueOf(row.getCapacity())));
                    writer.write(",");
                    writer.write(csv(row.getUseYn()));
                    writer.write(",");
                    writer.write(csv(row.getRegDt()));
                    writer.write("\n");
                }
                writer.flush();
            }
        } catch (Exception e) {
            LOGGER.error("CSV export failed", e);
            throw e;
        }
    }

    @RequestMapping("/adm/program/export.xlsx")
    public void exportXlsx(@ModelAttribute("programVO") ProgramManageVO programManageVO,
                           HttpServletResponse response) throws Exception {
        try {
            if (!isClassPresent("org.apache.poi.xssf.usermodel.XSSFWorkbook")) {
                writePlainError(response, "XLSX 라이브러리(poi-ooxml)가 누락되었습니다. 빌드/배포를 확인하세요.");
                return;
            }
            List<ProgramManageVO> rows = egovProgramManageService.selectProgramListAll(programManageVO);
            String fileName = buildFileName("programs", "xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("programs");
                int r = 0;
                Row header = sheet.createRow(r++);
                header.createCell(0).setCellValue("programCode");
                header.createCell(1).setCellValue("programName");
                header.createCell(2).setCellValue("startDate");
                header.createCell(3).setCellValue("endDate");
                header.createCell(4).setCellValue("capacity");
                header.createCell(5).setCellValue("useYn");
                header.createCell(6).setCellValue("regDt");

                for (ProgramManageVO row : rows) {
                    Row data = sheet.createRow(r++);
                    data.createCell(0).setCellValue(nz(row.getProgramCode()));
                    data.createCell(1).setCellValue(nz(row.getProgramName()));
                    data.createCell(2).setCellValue(nz(row.getStartDate()));
                    data.createCell(3).setCellValue(nz(row.getEndDate()));
                    data.createCell(4).setCellValue(row.getCapacity() == null ? "" : String.valueOf(row.getCapacity()));
                    data.createCell(5).setCellValue(nz(row.getUseYn()));
                    data.createCell(6).setCellValue(nz(row.getRegDt()));
                }
                wb.write(response.getOutputStream());
            }
        } catch (Throwable e) {
            LOGGER.error("XLSX export failed", e);
            writePlainError(response, "엑셀 다운로드 중 오류가 발생했습니다: "
                    + e.getClass().getName() + (e.getMessage() == null ? "" : (" - " + e.getMessage())));
        }
    }

    @RequestMapping("/adm/program/update.do")
    public String updateProgram(@ModelAttribute("program") ProgramManage programManage) throws Exception {
        LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
        if (user != null) {
            programManage.setUpdId(user.getId());
        }
        egovProgramManageService.updateProgram(programManage);
        return "redirect:/adm/program/manage.do";
    }

    @RequestMapping("/adm/program/delete.do")
    public String deleteProgram(@ModelAttribute("program") ProgramManage programManage) throws Exception {
        egovProgramManageService.deleteProgram(programManage);
        return "redirect:/adm/program/manage.do";
    }

    private void bindPaging(ProgramManageVO programManageVO, ModelMap model) throws Exception {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(programManageVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(programManageVO.getPageUnit());
        paginationInfo.setPageSize(programManageVO.getPageSize());

        programManageVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        programManageVO.setLastIndex(paginationInfo.getLastRecordIndex());
        programManageVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        model.addAttribute("programList", egovProgramManageService.selectProgramList(programManageVO));

        int totCnt = egovProgramManageService.selectProgramListTotCnt(programManageVO);
        paginationInfo.setTotalRecordCount(totCnt);
        model.addAttribute("paginationInfo", paginationInfo);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private String buildFileName(String prefix, String ext) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + ts + "." + ext;
    }

    private boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private void writePlainError(HttpServletResponse response, String message) {
        try {
            if (!response.isCommitted()) {
                response.reset();
                // Avoid web.xml 500 error-page override so the real message is returned.
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(message);
                response.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to write error response", e);
        }
    }
}
