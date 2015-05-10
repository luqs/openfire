package com.skyseas.openfireplugins.push;

import org.xmpp.packet.Packet;

/**
 * XMPP包发送器。
 * Created by zhangzhi on 2014/11/11.
 */
public interface PacketSender {

    /* 发送XMPP包。 */
    void send(Packet packet);
}
