package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import com.skyseas.openfireplugins.group.util.Paging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangzhi on 2014/9/26.
 */
class GroupManagerImpl implements GroupManager {
    private final static Logger LOG = LoggerFactory.getLogger(GroupManagerImpl.class);
    private final ConcurrentHashMap<String, GroupImpl> groups = new ConcurrentHashMap<String, GroupImpl>(256);
    private final GroupFacade facade;

    GroupManagerImpl(GroupService groupService, GroupIQDispatcher dispatcher) {
        assert groupService != null;
        assert dispatcher != null;

        GroupFacade facade = new GroupFacade();
        facade.setGroupService(groupService);
        facade.setGroupIQDispatcher(dispatcher);
        facade.setPersistenceFactory(PersistenceManagerImpl.INSTANCE);
        this.facade = facade;
    }

    GroupManagerImpl(GroupFacade facade) {
        assert facade != null;
        this.facade = facade;
    }

    /**
     * 搜索圈子列表。
     */
    @Override
    public Paging<GroupInfo> search(GroupQueryObject query, int offset, int limit) {
        assert query != null;
        return facade.search(query, offset, limit);
    }

    /**
     * 获得用户加入的圈子列表。
     *
     * @param userName
     * @return
     */
    @Override
    public List<GroupInfo> getMemberJoinedGroups(String userName) {
        assert userName != null && userName.length() > 0;
        return facade.getMemberJoinedGroups(userName);
    }

    /**
     * 创建圈子。
     */
    @Override
    public Group create(GroupInfo groupInfo) {
        assert groupInfo != null;

        GroupImpl group = facade.create(groupInfo);
        if (group != null) {
            activeGroup(group);

            GroupEventDispatcher.fireGroupCreated(group);
        }
        return group;
    }

    /**
     * 获得圈子对象。
     */
    @Override
    public Group getGroup(String groupId) {
        assert groupId != null;

        /* 从内存获取失败，尝试从持久化层加载，并激活在内存中 */
        GroupImpl group = groups.get(groupId);
        if (group != null) {
            return group;
        }

        group = facade.load(groupId);
        if (group != null) {
            return activeGroup(groupId, group);
        }
        return null;
    }

    /**
     * 删除圈子。
     */
    @Override
    public boolean remove(Group group, JID operator, String reason) {
        assert group != null;

        /* 从内存中移除圈子对象，并销毁该圈子 */
        if (groups.remove(group.getId(), group)) {
            return group.destroy(operator, reason);
        }
        return false;
    }

    /**
     * 获得已经在内存中激活的Group对象实例。
     *
     * @param groupId
     * @return
     */
    public Group getActivatedGroup(String groupId) {
        assert groupId != null;
        return groups.get(groupId);
    }

    /**
     * 清理圈子管理器，将空闲状态的圈子从内存中释放出来。
     *
     * @return
     */
    public void clean() {
        Iterator<GroupImpl> groupIterator = groups.values().iterator();
        while (groupIterator.hasNext()) {
            GroupImpl group = groupIterator.next();
            if (group.isIdleState()) {
                groupIterator.remove();
            }
        }
    }


    /**
     * 直接将圈子激活在内存中
     *
     * @param group
     */
    private void activeGroup(GroupImpl group) {
        groups.put(group.getId(), group);
    }

    /**
     * 激活圈子，并考虑会与内存中相同groupId冲突的情况。
     *
     * @param groupId
     * @return
     */
    private Group activeGroup(String groupId, GroupImpl newGroup) {

        /* 再次确认圈子不在内存中 */
        Group group = groups.get(groupId);
        if (group != null) {
            return group;
        }

        /**
         * 在此我们并没有使用锁定的方式保证内存中Group的唯一性，
         * 而是使用乐观的并发控制方式，假设针对当前Group的初始化
         * 并没有其他的并发线程。
         *
         * 创建好圈子的Group对象后通过putIfAbsent写入groups，并根据返回值
         * 判断是否写入成功。 如果返回null则代表groups中并不存在该Group，
         * 否则说明遇到并发，这时候返回已存在的Group即可。
         *
         *
         * 同时也要注意，如果创建Group对象的代价较高，并且并发较高时可能并不适合。
         */
        Group orgGroup = groups.putIfAbsent(groupId, newGroup);

        return orgGroup == null /* 返回null代表原先并不存在该Group对象 */
                ? newGroup
                : orgGroup;
    }


}
