package com.skyseas.openfireplugins.group;

import org.jivesoftware.openfire.PacketRouter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * 聊天用户接口。
* Created by apple on 14-9-13.
*/
public interface ChatUser {

    /**
     * 获得用户名。
     * @return
     */
    String getUserName();

    /**
     * 获得用户昵称。
     * @return
     */
    String getNickname();

    /**
     * 向用户发送一个XMPP包。
     * @param router
     * @param packet
     */
    void send(PacketRouter router, Packet packet);

    JID getJid();
}
