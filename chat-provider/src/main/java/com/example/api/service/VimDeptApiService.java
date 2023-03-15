package com.example.api.service;

import com.example.api.vo.Dept;

import java.util.List;

public interface VimDeptApiService {

    List<Dept> getDepts();

    List<Dept> getDepts(String parentIds);

    Dept get(String deptId);
}
