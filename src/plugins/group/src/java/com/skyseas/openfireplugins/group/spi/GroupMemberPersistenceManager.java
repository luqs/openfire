package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.GroupMemberInfo;

import java.util.List;

/**
 * 圈子成员持久化管理器。
 * Created by apple on 14-9-13.
 */
interface GroupMemberPersistenceManager {

    /**
     * 修改成员的圈子名片。
     * @param id
     * @param userName
     * @param nickname
     * @return
     * @throws PersistenceException
     */
    boolean changeGroupProfile(int id, String userName, String nickname)throws PersistenceException;

    /**
     * 添加一系列圈子成员。
     * @param groupId
     * @param members
     * @return
     * @throws PersistenceException
     */
    boolean addMembers(int groupId, List<GroupMemberInfo> members) throws PersistenceException;


        /**
         * 删除圈子成员。
         * @param groupId
         * @param userName
         * @return
         * @throws PersistenceException
         */
    boolean removeMember(int groupId, String userName) throws PersistenceException;

    /**
     * 指定用户名是否是圈子成员。
     * @param groupId
     * @param userName
     * @return
     * @throws PersistenceException
     */
    boolean isGroupMember(int groupId, String userName) throws PersistenceException;

    /**
     * 获得圈子成员列表。
     * @param groupId
     * @param userName
     * @return
     * @throws PersistenceException
     */
    List<GroupMemberInfo> getGroupMembers(int groupId, String userName) throws PersistenceException;

}
