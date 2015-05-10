package com.skyseas.openfireplugins.group;


import java.util.Date;

/**
 * Created by zhangzhi on 2014/8/27.
 */
public class GroupInfo {
    /**
     * 圈子的开放类型。
     */
    public enum OpennessType {

        /**
         * 完全开放的，无论是谁都可加入。
         */
        PUBLIC,

        /**
         * 需要验证的，加入前需要经过圈子所有者验证同意。
         */
        AFFIRM_REQUIRED,


        /**
         * 私有圈子。
         */
        PRIVATE
    }
    
    private String owner;
    private String name;
    private String description;
    private OpennessType opennessType;
    private int category;
    private String logo;
    private Date createTime;
    private String subject;
    private int numberOfMembers;
    private int id;
    private String status ;//0代表正常，1代表禁止一切消息收发功能

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getCategory() {
        return category;
    }

    public OpennessType getOpennessType() {
        return opennessType;
    }


    public void setOpennessType(OpennessType opennessType) {
        this.opennessType = opennessType;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getOwner() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJID(String groupServiceName) {
        return String.valueOf(id) + "@" + groupServiceName;
    }

    public int getNumberOfMembers() {
        return numberOfMembers;
    }

    public void setNumberOfMembers(int numberOfMembers) {
        this.numberOfMembers = numberOfMembers;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static GroupInfo combine(GroupInfo orgInfo, GroupInfo updater) {
        GroupInfo newGroupInfo = new GroupInfo();

        newGroupInfo.setId(updater.getId() > 0
                ? updater.getId()
                : orgInfo.getId());

        newGroupInfo.setName(updater.getName() != null
                ? updater.getName()
                : orgInfo.getName());

        newGroupInfo.setOwner(updater.getOwner() != null
                ? updater.getOwner()
                : orgInfo.getOwner());

        newGroupInfo.setSubject(updater.getSubject() != null
                ? updater.getSubject()
                : orgInfo.getSubject());

        newGroupInfo.setNumberOfMembers(updater.getNumberOfMembers() > 0
                ? updater.getNumberOfMembers()
                : orgInfo.getNumberOfMembers());

        newGroupInfo.setDescription(updater.getDescription() != null
                ? updater.getDescription()
                : orgInfo.getDescription());

        newGroupInfo.setLogo(updater.getLogo() != null
                ? updater.getLogo()
                : orgInfo.getLogo());

        newGroupInfo.setCategory(updater.getCategory() > 0
                ? updater.getCategory()
                : orgInfo.getCategory());

        newGroupInfo.setOpennessType(updater.getOpennessType() != null
                ? updater.getOpennessType()
                : orgInfo.getOpennessType());

        newGroupInfo.setCreateTime(updater.getCreateTime() != null
                ? updater.getCreateTime()
                :orgInfo.getCreateTime());
        
        newGroupInfo.setStatus(updater.getStatus() !=null
                ? updater.getStatus()
                :orgInfo.getStatus());

        return newGroupInfo;
    }
}
