package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import com.skyseas.openfireplugins.group.util.Paging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.util.Collections;
import java.util.List;

/**
 * 圈子的外观类，提供简单易用的接口。
 * Created by zhangzhi on 2014/9/29.
 */
final class GroupFacade {
    private final static Logger LOG = LoggerFactory.getLogger(GroupFacade.class);
    private GroupService            groupService;
    private PersistenceFactory      persistenceFactory;
    private GroupIQDispatcher       dispatcher;
    private GroupPersistenceManager groupPerMgr;

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setPersistenceFactory(PersistenceFactory persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
        this.groupPerMgr = persistenceFactory.getGroupPersistenceManager();
    }

    public void setGroupIQDispatcher(GroupIQDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * 搜索圈子列表。
     * @param query
     * @param offset
     * @param limit
     * @return
     */
    public Paging<GroupInfo> search(GroupQueryObject query, int offset, int limit) {
        try {
            return groupPerMgr.queryGroups(query, offset, limit);
        } catch (PersistenceException exp) {
            LOG.error("查询圈子列表失败", exp);
            return new Paging<GroupInfo>();
        }
    }

    /**
     * 获得用户已经加入的圈子列表。
     * @param userName
     * @return
     */
    public List<GroupInfo> getMemberJoinedGroups(String userName) {
        try {
            return groupPerMgr.getMemberJoinedGroups(userName);
        }catch (PersistenceException exp) {
            LOG.error("获取成员圈子列表失败。", exp);
            return Collections.emptyList();
        }
    }

    /**
     * 创建圈子。
     * @param groupInfo
     * @return
     */
    public GroupImpl create(GroupInfo groupInfo) {
        try {
            /* 创建持久化圈子数据 */
            groupPerMgr.addGroup(groupInfo);
        } catch (PersistenceException e) {
            LOG.error("持久化添加圈子数据失败", e);
            return null;
        }

        GroupImpl group = newInstance(groupInfo);
        try {
            /* 将圈子所有者添加到圈子成员列表 */
            group.getChatUserManager().addUser(groupInfo.getOwner(), groupInfo.getOwner());
        } catch (FullMemberException e) {
            LOG.error("创建圈子成功但添加所有者到圈子失败", e);
        }
        return group;
    }

    /**
     * 加载特定圈子。
     * @param groupId
     * @return
     */
    public GroupImpl load(String groupId) {
        GroupInfo groupInfo;
        try {
            groupInfo = groupPerMgr.getGroup(Integer.parseInt(groupId));
        }catch (PersistenceException e){
            LOG.error("获得圈子持久化数据失败", e);
            return null;
        }
        if(groupInfo != null) {
            return newInstance(groupInfo);
        }else {
            return null;
        }
    }

    GroupImpl newInstance(GroupInfo groupInfo) {
        return new GroupImpl(
                groupService.getGroupJid(String.valueOf(groupInfo.getId())),
                groupInfo,
                groupService.getServer().getServerInfo().getXMPPDomain(),
                dispatcher,
                groupService.getServer().getPacketRouter(),
                groupPerMgr,
                persistenceFactory.getGroupMemberPersistenceManager());
    }

}
