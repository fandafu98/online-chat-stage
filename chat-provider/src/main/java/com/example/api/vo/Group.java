package com.example.api.vo;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String avatar;
    private String master;
    private String needCheck;
    private List<User> userList;

    public Group() {
    }

    public Group(String id, String name, String avatar, String master,String needCheck) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.master = master;
        this.needCheck = needCheck;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getNeedCheck() {
        return needCheck;
    }

    public void setNeedCheck(String needCheck) {
        this.needCheck = needCheck;
    }
}
