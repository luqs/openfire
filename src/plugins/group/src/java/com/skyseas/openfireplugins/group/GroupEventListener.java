package com.skyseas.openfireplugins.group;

import org.xmpp.packet.JID;

/**
 * 圈子事件监听器。
 * Created by zhangzhi on 2014/9/29.
 */
public interface GroupEventListener {

    /**
     * 当用户已退出。
     * @param group
     * @param user
     * @param reason
     */
    void userExited(Group group, ChatUser user, String reason);

    /**
     * 当用户已被踢出
     * @param group
     * @param user
     * @param from
     * @param reason
     */
    void userKicked(Group group, ChatUser user, JID from, String reason);

    /**
     * 当用户昵称已修改。
     * @param group
     * @param user
     * @param oldNickname
     */
    void userNicknameChanged(Group group, ChatUser user, String oldNickname);

    /**
     * 当用户已加入圈子。
     * @param group
     * @param user
     */
    void userJoined(Group group, ChatUser user);

    /**
     * 当圈子已创建。
     * @param group
     */
    void groupCreated(Group group);

    /**
     * 当圈子已销毁。
     * @param group
     * @param from
     * @param reason
     */
    void groupDestroyed(Group group, JID from, String reason);


    /**
     * 当圈子信息已更新。
     * @param group
     */
    void groupInfoChanged(Group group, JID from);
}
