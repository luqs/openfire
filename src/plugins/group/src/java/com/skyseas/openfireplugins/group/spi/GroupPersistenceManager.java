package com.skyseas.openfireplugins.group.spi;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.GroupQueryObject;
import com.skyseas.openfireplugins.group.util.Paging;

import java.util.List;

/**
 * 圈子持久化管理器。
 * Created by zhangzhi on 2014/9/26.
 */
interface GroupPersistenceManager {

    /**
     * 添加圈子。
     * @param groupInfo
     * @return
     * @throws PersistenceException
     */
    void addGroup(GroupInfo groupInfo)  throws PersistenceException;

    /**
     * 删除圈子。
     * @param id
     * @return
     * @throws PersistenceException
     */
    boolean removeGroup(int id) throws PersistenceException;

    /**
     * 更新圈子。
     * @param groupInfo
     * @return
     * @throws PersistenceException
     */
    boolean updateGroup(GroupInfo groupInfo) throws PersistenceException;

    /**
     * 获得圈子信息。
     * @param groupId
     * @return
     * @throws PersistenceException
     */
    GroupInfo getGroup(int groupId) throws PersistenceException;

    /**
     * 获得成员加入的圈子列表。
     * @param userName
     * @return
     * @throws PersistenceException
     */
    List<GroupInfo> getMemberJoinedGroups(String userName) throws PersistenceException;

    /**
     * 查询圈子列表。
     * @param queryObject
     * @param offset
     * @param limit
     * @return
     * @throws PersistenceException
     */
    Paging<GroupInfo> queryGroups(GroupQueryObject queryObject, int offset, int limit) throws PersistenceException;

}
