package com.example.vim.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.example.stage.core.annotation.Excel;
import com.example.stage.core.system.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 群管理对象 im_group
 * 
 * @author 乐天
 * @since 2022-01-26
 */
public class ImGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private String id;

    /** 群名称 */
    @Excel(name = "群名称")
    private String name;

    /** 群头像 */
    @Excel(name = "群头像")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String avatar;

    /** 群主 */
    @Excel(name = "群主")
    private Long master;

    /** 审核 */
    @Excel(name = "审核")
    private String needCheck;

    /** 公告 */
    @Excel(name = "公告")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String announcement;

    @TableLogic(value = "0", delval = "1")
    private String delFlag;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
    }
    public void setMaster(Long master) 
    {
        this.master = master;
    }

    public Long getMaster() 
    {
        return master;
    }
    public void setNeedCheck(String needCheck) 
    {
        this.needCheck = needCheck;
    }

    public String getNeedCheck() 
    {
        return needCheck;
    }
    public void setAnnouncement(String announcement) 
    {
        this.announcement = announcement;
    }

    public String getAnnouncement() 
    {
        return announcement;
    }
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("avatar", getAvatar())
            .append("master", getMaster())
            .append("needCheck", getNeedCheck())
            .append("announcement", getAnnouncement())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
