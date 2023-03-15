package com.example.api.controller;

import com.example.stage.core.system.domain.AjaxResult;
import com.example.api.service.VimDeptApiService;
import com.example.api.service.VimUserApiService;
import com.example.api.vo.Dept;
import com.example.api.vo.TreeConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/sys/depts")
public class DeptController {

    private final Logger logger = LoggerFactory.getLogger(DeptController.class);

    /**
     * 顶级的 PARENT_ID
     */
    public static final String DEFAULT_PARENT_ID = "0";

    @Resource
    private VimDeptApiService vimDeptApiService;


    @Resource
    private VimUserApiService vimUserApiService;


    @GetMapping
    public AjaxResult list(){
        TreeConvert convert = new TreeConvert();
        return AjaxResult.success(convert.listToTree(vimDeptApiService.getDepts(),DEFAULT_PARENT_ID));
    }

    @GetMapping("parent")
    public AjaxResult parent(String deptId) {
        Dept dept = vimDeptApiService.get(deptId);
        List<Dept> depts = vimDeptApiService.getDepts(dept.getParentIds());
        depts.add(dept);
        return AjaxResult.success(depts);
    }

    /**
     * 单个部门 用户
     *
     * @param deptId deptId
     * @return ImDept
     */
    @GetMapping("{deptId}/users")
    public AjaxResult users(@PathVariable(value = "deptId") String deptId) {
        return AjaxResult.success(vimUserApiService.getByDept(deptId));
    }
}
