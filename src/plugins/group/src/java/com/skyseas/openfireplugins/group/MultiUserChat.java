package com.skyseas.openfireplugins.group;

/**
 * Created by apple on 14-9-14.
 */

import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

/**
 * 多用户聊天基础接口。
 * Created by apple on 14-9-13.
 *
 */
public interface MultiUserChat {

    /**
     * 获得多用户聊天房间的ID。
     * @return
     */
    String getId();

    /**
     * 获得多用户聊天房间的地址。
     * @return
     */
    JID getJid();

    /**
     * 向聊天房间发送数据包。
     * @param packet
     */
    void send(Packet packet);

    /**
     * 已圈子的身份发送一条消息到指定jid。
     * @param jid
     * @param msg
     */
    void send(JID jid, Message msg);

    /**
     * 向聊天房间广播数据包。
     * @param packet
     */
    void broadcast(Packet packet);

    /**
     * 获得聊天用户管理器。
     */
    ChatUserManager getChatUserManager();
}
