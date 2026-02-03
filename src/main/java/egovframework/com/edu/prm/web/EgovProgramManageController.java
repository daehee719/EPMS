package egovframework.com.edu.prm.web;

import javax.annotation.Resource;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.edu.prm.service.EgovProgramManageService;
import egovframework.com.edu.prm.service.ProgramManage;
import egovframework.com.edu.prm.service.ProgramManageVO;

@Controller
public class EgovProgramManageController {

    @Resource(name = "egovProgramManageService")
    private EgovProgramManageService egovProgramManageService;

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
}
