package egovframework.com.edu.prm.service.impl;

import java.util.Map;

import org.springframework.stereotype.Repository;

import egovframework.com.cmm.service.impl.EgovComAbstractDAO;

@Repository("programUploadDAO")
public class ProgramUploadDAO extends EgovComAbstractDAO {

    public void insertUploadJob(Map<String, Object> params) {
        insert("programUploadDAO.insertUploadJob", params);
    }

    public void updateUploadJob(Map<String, Object> params) {
        update("programUploadDAO.updateUploadJob", params);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectUploadJob(String jobId) {
        return (Map<String, Object>) selectOne("programUploadDAO.selectUploadJob", jobId);
    }

    public void insertUploadError(Map<String, Object> params) {
        insert("programUploadDAO.insertUploadError", params);
    }

    public void insertProgramStaging(Map<String, Object> params) {
        insert("programUploadDAO.insertProgramStaging", params);
    }

    public void deleteProgramStaging(String jobId) {
        delete("programUploadDAO.deleteProgramStaging", jobId);
    }

    public void mergeProgramFromStaging(String jobId) {
        insert("programUploadDAO.mergeProgramFromStaging", jobId);
    }
}
