package egovframework.com.edu.prm.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import egovframework.com.edu.prm.service.EgovProgramManageService;
import egovframework.com.edu.prm.service.ProgramManage;
import egovframework.com.edu.prm.service.ProgramManageVO;

@Service("egovProgramManageService")
public class EgovProgramManageServiceImpl extends EgovAbstractServiceImpl implements EgovProgramManageService {

    @Resource(name = "programManageDAO")
    private ProgramManageDAO programManageDAO;

    @Override
    public ProgramManageVO selectProgram(ProgramManageVO programManageVO) throws Exception {
        return programManageDAO.selectProgram(programManageVO);
    }

    @Override
    public List<ProgramManageVO> selectProgramList(ProgramManageVO programManageVO) throws Exception {
        return programManageDAO.selectProgramList(programManageVO);
    }

    @Override
    public List<ProgramManageVO> selectProgramListAll(ProgramManageVO programManageVO) throws Exception {
        return programManageDAO.selectProgramListAll(programManageVO);
    }

    @Override
    public int selectProgramListTotCnt(ProgramManageVO programManageVO) throws Exception {
        return programManageDAO.selectProgramListTotCnt(programManageVO);
    }

    @Override
    public void insertProgram(ProgramManage programManage) throws Exception {
        programManageDAO.insertProgram(programManage);
    }

    @Override
    public void updateProgram(ProgramManage programManage) throws Exception {
        programManageDAO.updateProgram(programManage);
    }

    @Override
    public void deleteProgram(ProgramManage programManage) throws Exception {
        programManageDAO.deleteProgram(programManage);
    }
}
