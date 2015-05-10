package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.Group;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.Verifications;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class GroupEventBroadcastListenerTest extends TestCase {
    @Mocked Group group;
    ChatUserImpl user = new ChatUserImpl("user", "碧眼狐狸", new JID("user@skysea.com"));

    public void testUserExited() throws Exception {
        // Act
        GroupEventBroadcastListener.INSTANCE.userExited(group, user, "reason");

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                group.broadcast(with(new Delegate<Packet>() {

                    public void validate(Packet packet) {
                        assertEquals(
                                "<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#member\"><exit>" +
                                        "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                                "<reason>reason</reason></exit></x></message>", packet.toXML().trim());
                    }
                }));
                times = 1;

                group.send(user.getJid(), with(new Delegate<Message>() {

                    public void validate(Message packet) {
                        assertEquals(
                                "<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#member\"><exit>" +
                                        "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                                        "<reason>reason</reason></exit></x></message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testUserJoined() throws Exception {
        // Act
        GroupEventBroadcastListener.INSTANCE.userJoined(group, user);

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                group.broadcast(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#member\"><join>" +
                                        "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                                        "</join></x></message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testUserKicked() throws Exception {
        // Act
        GroupEventBroadcastListener.INSTANCE.userKicked(group, user, new JID("owner@skysea.com"), "reason");

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                group.broadcast(with(new Delegate<Packet>() {

                    public void validate(Packet packet) {
                        assertEquals(
                                "<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#member\">" +
                                        "<kick from=\"owner@skysea.com\">" +
                                        "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                                        "</kick></x>" +
                                        "</message>", packet.toXML().trim());
                    }
                }));
                times = 1;

                group.send(user.getJid(), with(new Delegate<Message>() {

                    public void validate(Message msg) {
                        assertEquals(
                                "<message><x xmlns=\"http://skysea.com/protocol/group#member\">" +
                                        "<kick from=\"owner@skysea.com\">" +
                                        "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                                        "<reason>reason</reason></kick></x>" +
                                        "</message>", msg.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testUserNicknameChanged() throws Exception {

        // Act
        GroupEventBroadcastListener.INSTANCE.userNicknameChanged(group, user, "old");

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                group.broadcast(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#member\">" +
                                        "<profile>" +
                                        "<member username=\"user\" nickname=\"old\"/>" +
                                        "<nickname>碧眼狐狸</nickname></profile></x>" +
                                        "</message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testGroupDestroyed() throws Exception {

        // Act
        GroupEventBroadcastListener.INSTANCE.groupDestroyed(group, new JID("owner@skysea.com"), "reason");

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                group.broadcast(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message><x xmlns=\"http://skysea.com/protocol/group\">" +
                                        "<destroy from=\"owner@skysea.com\">" +
                                        "<reason>reason</reason></destroy></x>" +
                                        "</message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testGroupChanged() throws Exception {

        // Act
        GroupEventBroadcastListener.INSTANCE.groupInfoChanged(group, new JID("owner@skysea.com"));

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                group.broadcast(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message><x xmlns=\"http://skysea.com/protocol/group\">" +
                                        "<change from=\"owner@skysea.com\"/>" +
                                        "</x></message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }


}