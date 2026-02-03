package egovframework.com.edu.prm.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EgovProgramUploadService {

    String startUpload(MultipartFile file, String regId) throws Exception;

    Map<String, Object> getUploadStatus(String jobId) throws Exception;
}
