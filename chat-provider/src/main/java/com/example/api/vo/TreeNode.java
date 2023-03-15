package com.example.api.vo;


import java.io.Serializable;
import java.util.List;

/**
 * 树状菜单
 * @author 乐天kp
 */
public class TreeNode implements Serializable {


    private String id;
    private String label;
    private String parentId;
    private List<TreeNode> children;

    private int count;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
