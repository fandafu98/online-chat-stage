package com.example.api.vo;

import java.io.Serializable;

public class Dept implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private String id;

    /** 父部门ID */
    private String parentId;

    /** 祖级列表 */
    private String parentIds;

    /** 部门名称 */
    private String name;

    /** 显示顺序 */
    private String orderNum;

    public Dept() {
    }

    public Dept(String id, String parentId, String parentIds, String name, String orderNum) {
        this.id = id;
        this.parentId = parentId;
        this.parentIds = parentIds;
        this.name = name;
        this.orderNum = orderNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
