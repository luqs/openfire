package com.skyseas.openfireplugins.group;

import java.util.Date;

/**
* Created by apple on 14-9-3.
*/
public class GroupMemberInfo {
    private int id;
    private int groupId;
    private String userName;
    private String nickName;
    private Date joinTime;

    public GroupMemberInfo(){}

    public GroupMemberInfo(String userName, String nickName) {
        this.userName = userName;
        this.nickName = nickName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date createTime) {
        this.joinTime = createTime;
    }
}
