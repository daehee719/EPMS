package egovframework.com.edu.prm.service;

import java.util.List;

public interface EgovProgramManageService {

    ProgramManageVO selectProgram(ProgramManageVO programManageVO) throws Exception;

    List<ProgramManageVO> selectProgramList(ProgramManageVO programManageVO) throws Exception;

    List<ProgramManageVO> selectProgramListAll(ProgramManageVO programManageVO) throws Exception;

    int selectProgramListTotCnt(ProgramManageVO programManageVO) throws Exception;

    void insertProgram(ProgramManage programManage) throws Exception;

    void updateProgram(ProgramManage programManage) throws Exception;

    void deleteProgram(ProgramManage programManage) throws Exception;
}
