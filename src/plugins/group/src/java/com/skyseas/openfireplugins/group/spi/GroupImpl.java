package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import org.jivesoftware.openfire.PacketRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * 圈子实现。
 * Created by apple on 14-9-14.
 */

final class GroupImpl extends AbstractMultiUserChat implements Group, NumberOfUsersListener {
    private final static Logger LOG = LoggerFactory.getLogger(GroupImpl.class);
    public static final long IDLE_TIME_OUT = 60 * 1000 * 10; /* 空闲超时毫秒数，10分钟 */
    private final JID                           owner;
    private final GroupPersistenceManager       persistenceMgr;
    private final GroupMemberPersistenceManager memberPersistenceMgr;
    private final String                        xmppDomain;
    private final GroupIQDispatcher             dispatcher;
    private volatile GroupInfo                  groupInfo;
    private volatile ChatUserManager            chatUserManager;
    private ApplyStrategy                       applyStrategy;
    private PacketRouter                        packetRouter;
    /* 圈子最后一次收到数据包的时间 */
    private long                                lastReceivedPacketTime = System.currentTimeMillis();

    GroupImpl(JID                           jid,
              GroupInfo                     groupInfo,
              String                        xmppDomain,
              GroupIQDispatcher             dispatcher,
              PacketRouter                  packetRouter,
              GroupPersistenceManager       persistenceMgr,
              GroupMemberPersistenceManager memberPersistenceMgr) {
        super(String.valueOf(groupInfo.getId()), jid);
        assert jid                  != null;
        assert groupInfo            != null;
        assert xmppDomain           != null;
        assert dispatcher           != null;
        assert persistenceMgr       != null;
        assert memberPersistenceMgr != null;

        this.groupInfo              = groupInfo;
        this.xmppDomain             = xmppDomain;
        this.dispatcher             = dispatcher;
        this.packetRouter           = packetRouter;
        this.persistenceMgr         = persistenceMgr;
        this.memberPersistenceMgr   = memberPersistenceMgr;
        this.owner                  = new JID(groupInfo.getOwner(), xmppDomain, null, true);

        setApplyStrategy(groupInfo.getOpennessType());
    }

    /**
     * 获得圈子所有者。
     * @return
     */
    @Override
    public JID getOwner() {
        return owner;
    }

    /**
     * 获得圈子信息。
     * @return
     */
    @Override
    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    /**
     * 更新圈子信息。
     *
     * @param operator
     * @param groupInfo
     * @return
     */
    @Override
    public boolean updateGroupInfo(JID operator, GroupInfo groupInfo) {
        assert groupInfo != null;

        if (!persistenceUpdate(groupInfo)) {
            return false;
        }

        /* 合并更新内存中的Group副本，注意：这和持久化GroupInfo不是原子的 */
        combineUpdate(groupInfo);

        /* 触发圈子修改 */
        GroupEventDispatcher.fireGroupInfoChanged(this, operator);
        return true;
    }

    /**
     * 申请加入圈子。
     * @param proposer
     * @param nickname
     * @param reason
     * @throws FullMemberException
     */
    @Override
    public void applyJoin(JID proposer, String nickname, String reason) throws FullMemberException {
        assert proposer != null;
        assert nickname != null && nickname.length() > 0;
        applyStrategy.applyToJoin(this, proposer, nickname, reason);
    }

    /**
     * 处理IQ请求。
     * @param packet
     */
    @Override
    protected void handle(IQ packet) {
        dispatcher.dispatch(packet, this);
    }

    /**
     * 将Packet路由到特定用户。
     * @param packet
     * @param user
     */
    @Override
    protected void routePacket(Packet packet, ChatUser user) {
        user.send(packetRouter, packet);
    }

    /**
     * 路由Packet
     * @param packet
     */
    @Override
    protected void routePacket(Packet packet) {
        packetRouter.route(packet);
    }

    /**
     * 获得聊天用户管理器。
     * @return
     */
    @Override
    public ChatUserManager getChatUserManager() {
        if (chatUserManager != null) {
            return chatUserManager;
        }
        synchronized (this) {
            if (chatUserManager == null) {
                chatUserManager = new ChatUserManagerImpl(this, xmppDomain, this, memberPersistenceMgr);
            }
        }
        return chatUserManager;
    }

    /**
     * 监听用户聊天用户人数修改。
     * @param newNumberOfUsers
     */
    @Override
    public void numberOfUsersChanged(int newNumberOfUsers) {
        synchronized (this) {
            groupInfo.setNumberOfMembers(newNumberOfUsers);
        }
    }

    /**
     * 销毁圈子。
     * @param operator
     * @param reason
     * @return
     */
    @Override
    public boolean destroy(JID operator, String reason) {
        /* 触发聊天用户管理器初始化 */
        getChatUserManager();
        if(persistenceDestroy()) {
            GroupEventDispatcher.fireGroupDestroyed(this, operator, reason);
            return true;
        }
        return false;
    }

    @Override
    public void send(Packet packet) {
        /* 更新最后一次接收数据包的时间 */
        lastReceivedPacketTime = System.currentTimeMillis();
        super.send(packet);
    }

    /**
     * 返回当前圈子是否处于空闲状态。
     * @return 10分钟内没有收到任何数据包则返回：{@code true}。
     */
    public boolean isIdleState() {
        return System.currentTimeMillis() - lastReceivedPacketTime > IDLE_TIME_OUT;
    }

    /**
     * 持久化更新圈子信息。
     * @param updater
     * @return
     */
    private boolean persistenceUpdate(GroupInfo updater) {
        updater.setId(groupInfo.getId());
        try {
            return persistenceMgr.updateGroup(updater);
        } catch (PersistenceException e) {
            handleException(e, "更新圈子到数据库失败");
        }
        return false;
    }

    /**
     * 合并更新内存中的圈子信息。
     * @param updater
     */
    private void combineUpdate(GroupInfo updater) {
        synchronized (this) {
            if (updater.getOpennessType() != null) {
                setApplyStrategy(updater.getOpennessType());
            }
            groupInfo = GroupInfo.combine(groupInfo, updater);
        }
    }

    /**
     * 持久化销毁圈子。
     * @return
     */
    private boolean persistenceDestroy() {
        try {
            return persistenceMgr.removeGroup(groupInfo.getId());
        } catch (PersistenceException e) {
            handleException(e, "删除圈子失败");
        }
        return false;
    }

    private void setApplyStrategy(GroupInfo.OpennessType opennessType) {
        applyStrategy = ApplyStrategy.getStrategyFor(opennessType);
    }

    private void handleException(Throwable exp, String format, Object... args) {
        LOG.error(String.format(format, args) + ", GroupId:" + id, exp);
    }


}
