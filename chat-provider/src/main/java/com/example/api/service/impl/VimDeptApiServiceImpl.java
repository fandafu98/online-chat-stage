package com.example.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.stage.core.system.domain.entity.SysDept;
import com.example.stage.core.system.service.ISysDeptService;
import com.example.api.service.VimDeptApiService;
import com.example.api.vo.Dept;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * vim 部门操作类，如果需要对接其他的系统，重新下面的方法即可
 *
 * @author 乐天
 */
@Service
public class VimDeptApiServiceImpl implements VimDeptApiService {

    private static final String CACHE_KEY = "dept";

    @Resource
    private ISysDeptService iSysDeptService;

    /**
     * 获取所有的部门
     *
     * @return List<Dept>
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":list:all")
    public List<Dept> getDepts() {
        return iSysDeptService.list().stream().map(this::transform).collect(Collectors.toList());
    }

    /**
     * 获取多个部门
     *
     * @param deptIds 多个部门id,逗号分隔
     * @return List<Dept>
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":list:parent", key = "#deptIds")
    public List<Dept> getDepts(String deptIds) {
        if (StringUtils.isNotBlank(deptIds)) {
            QueryWrapper<SysDept> wrapper = new QueryWrapper<>();
            wrapper.in("dept_id", deptIds.split(","));
            List<SysDept> sysDepts = iSysDeptService.list(wrapper);
            return sysDepts.stream().map(this::transform).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取部门
     *
     * @param deptId 门id
     * @return Dept
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":one", key = "#deptId")
    public Dept get(String deptId) {
        return transform(iSysDeptService.selectDeptById(Long.parseLong(deptId)));
    }

    /**
     * 部门类型转换 SysDept -> Dept
     *
     * @param dept 部门
     * @return Dept
     */
    private Dept transform(SysDept dept) {
        return new Dept(String.valueOf(dept.getDeptId()), String.valueOf(dept.getParentId()), dept.getAncestors(), dept.getDeptName(), String.valueOf(dept.getOrderNum()));
    }
}
