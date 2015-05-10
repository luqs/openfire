package com.skyseas.openfireplugins.group;

/**
 * Created by apple on 14-9-14.
 */

import org.xmpp.packet.JID;

/**
 * 圈子基础接口。
 * Created by apple on 14-9-13.
 */
public interface Group extends MultiUserChat {

    /**
     * 获得圈子所有者。
     * @return
     */
    JID getOwner();

    /**
     * 获得圈子描述信息。
     * @return
     */
    GroupInfo getGroupInfo();

    /**
     * 更新圈子描述
     */
    boolean updateGroupInfo(JID operator, GroupInfo groupInfo);

    /**
     * 申请加入圈子。
     * @param proposer
     * @param nickname
     * @param reason
     * @throws FullMemberException
     */
    void applyJoin(JID proposer, String nickname, String reason) throws FullMemberException;

    boolean destroy(JID operator, String reason);
}
