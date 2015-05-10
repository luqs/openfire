package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.ChatUser;
import org.jivesoftware.openfire.PacketRouter;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * Created by zhangzhi on 2014/9/26.
 */
class ChatUserImpl implements ChatUser {
    private final String userName;
    private volatile String nickname;
    private final JID jid;

    public ChatUserImpl(String userName, String nickName, JID jid) {
        assert userName != null;
        assert nickName != null;
        assert jid != null;

        this.userName = userName;
        this.nickname = nickName;
        this.jid = jid;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    @Override
    public void send(PacketRouter router, Packet packet) {
        assert router != null;
        assert packet != null;

        packet.setTo(jid);
        router.route(packet);
    }

    @Override
    public String getUserName() {
        return userName;
    }


    @Override
    public JID getJid(){
        return this.jid;
    }
}
