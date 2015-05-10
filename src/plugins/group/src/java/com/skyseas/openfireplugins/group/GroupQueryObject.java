package com.skyseas.openfireplugins.group;

/**
* Created by apple on 14-9-13.
*/
public class GroupQueryObject {
    private int groupId;
    private int category;
    private String name;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasValue() {
        return  groupId > 0 ||
                category > 0 ||
                name != null && name.length() > 0;
    }
}
