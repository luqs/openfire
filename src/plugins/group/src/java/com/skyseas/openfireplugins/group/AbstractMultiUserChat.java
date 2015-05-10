package com.skyseas.openfireplugins.group;

import com.skyseas.openfireplugins.group.util.ContractUtils;
import org.xmpp.packet.*;

/**
 * Created by apple on 14-9-14.
 */
public abstract class AbstractMultiUserChat implements MultiUserChat {
    protected final String id;
    protected final JID jid;

    protected AbstractMultiUserChat(String id, JID jid) {
        ContractUtils.requiresNotEmpty(id, "id");
        ContractUtils.requiresNotNull(jid, "jid");

        this.id = id;
        this.jid = jid;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final JID getJid() {
        return this.jid;
    }

    @Override
    public void send(JID recipients, Message msg) {
        ContractUtils.requiresNotNull(recipients, "recipients");
        ContractUtils.requiresNotNull(msg, "msg");

        msg.setFrom(this.jid);
        msg.setTo(recipients);
        routePacket(msg);
    }

    @Override
    public void send(Packet packet) {
        ContractUtils.requiresNotNull(packet, "packet");

        if (packet instanceof Message) {
            broadcast((Message) packet);
        } else if (packet instanceof Presence) {
            broadcast((Presence) packet);
        } else if (packet instanceof IQ) {
            handle((IQ) packet);
        } else {
            throw new IllegalArgumentException("packet");
        }
    }

    @Override
    public void broadcast(Packet packet) {
        ContractUtils.requiresNotNull(packet, "packet");

        if(packet.getFrom() == null) {
            packet.setFrom(this.jid);
        }

        for (ChatUser user : getChatUserManager().getUsers()) {
            routePacket(packet, user);
        }
    }


    protected void broadcast(Message message) {
        ChatUser user = checkMsg(message);
        if (user != null) {
            message = MessageFactory.newInstanceForGroupChat(message, user.getNickname());
            message.setFrom(createGroupUserJid(user.getUserName()));
            broadcast((Packet) message);
        }
    }

    private ChatUser checkMsg(Message message) {
        if (message.getType() == Message.Type.groupchat) {
            ChatUser user = getChatUserManager().getUser(message.getFrom().getNode());
            if (user != null) {
                return user;
            } else {
                Message errorMsg = new Message();
                errorMsg.setFrom(jid);
                errorMsg.setTo(message.getFrom());
                errorMsg.setError(PacketError.Condition.not_authorized);
                routePacket(errorMsg);
            }
        } else {
            Message errorMsg = new Message();
            errorMsg.setTo(message.getFrom());
            replyNotAcceptable(errorMsg);
        }
        return null;
    }

    protected JID createGroupUserJid(String userName) {
        return new JID(jid.getNode(), jid.getDomain(), userName);
    }

    protected void broadcast(Presence packet) {
        Presence replyError = new Presence();
        replyError.setTo(packet.getFrom());
        replyNotAcceptable(replyError);
    }

    private void replyNotAcceptable(Packet packet) {
        packet.setError(PacketError.Condition.not_acceptable);
        packet.setFrom(jid);
        routePacket(packet);
    }

    protected abstract void handle(IQ packet);

    protected abstract void routePacket(Packet packet, ChatUser user);

    protected abstract void routePacket(Packet packet);

}
