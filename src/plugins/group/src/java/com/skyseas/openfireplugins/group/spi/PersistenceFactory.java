package com.skyseas.openfireplugins.group.spi;

/**
 * 持久化工厂。
 * Created by zhangzhi on 2014/9/29.
 */
interface PersistenceFactory {

    /**
     * 获得圈子成员持久化管理器。
     * @return
     */
    GroupMemberPersistenceManager getGroupMemberPersistenceManager();

    /**
     * 获得圈子持久化管理器。
     * @return
     */
    GroupPersistenceManager getGroupPersistenceManager();
}
