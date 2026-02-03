package egovframework.com.edu.prm.service.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import egovframework.com.cmm.service.impl.EgovComAbstractDAO;
import egovframework.com.edu.prm.service.ProgramManage;
import egovframework.com.edu.prm.service.ProgramManageVO;

@Repository("programManageDAO")
public class ProgramManageDAO extends EgovComAbstractDAO {

    public ProgramManageVO selectProgram(ProgramManageVO programManageVO) throws Exception {
        return (ProgramManageVO) selectOne("programManageDAO.selectProgram", programManageVO);
    }

    public List<ProgramManageVO> selectProgramList(ProgramManageVO programManageVO) throws Exception {
        return selectList("programManageDAO.selectProgramList", programManageVO);
    }

    public int selectProgramListTotCnt(ProgramManageVO programManageVO) throws Exception {
        return (Integer) selectOne("programManageDAO.selectProgramListTotCnt", programManageVO);
    }

    public void insertProgram(ProgramManage programManage) throws Exception {
        insert("programManageDAO.insertProgram", programManage);
    }

    public void updateProgram(ProgramManage programManage) throws Exception {
        update("programManageDAO.updateProgram", programManage);
    }

    public void deleteProgram(ProgramManage programManage) throws Exception {
        delete("programManageDAO.deleteProgram", programManage);
    }
}
