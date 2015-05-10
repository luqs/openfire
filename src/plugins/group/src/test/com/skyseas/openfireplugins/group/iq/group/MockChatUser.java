package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.ChatUser;
import org.jivesoftware.openfire.PacketRouter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
* Created by zhangzhi on 2014/9/15.
*/
public class MockChatUser implements ChatUser {

    private final String userName;
    private final String nickname;

    public MockChatUser(String userName, String nickname) {

        this.userName = userName;
        this.nickname = nickname;
    }
    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void send(PacketRouter router, Packet packet) {

    }

    public JID getJid() {
        return null;
    }

    @Override
    public String getUserName() {
        return userName;
    }
}
