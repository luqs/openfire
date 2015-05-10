package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import junit.framework.TestCase;
import mockit.*;
import org.jivesoftware.openfire.PacketRouter;
import org.xmpp.packet.*;

import java.util.List;

public class GroupImplTest extends TestCase {
    private JID userJid = new JID("user@skyaea.com");
    private JID groupJid = new JID("100@group.skysea.com");
    @Mocked
    GroupMemberPersistenceManager groupMemberPersistenceManager;
    @Mocked
    GroupPersistenceManager groupPersistenceManager;
    @Mocked
    GroupIQDispatcher dispatcher;
    @Mocked
    PacketRouter packetRouter;

    private GroupInfo groupInfo;
    private GroupImpl group;

    @Override
    public void setUp() throws Exception {
        groupInfo = new GroupInfo();
        groupInfo.setId(100);
        groupInfo.setOwner("owner");
        groupInfo.setOpennessType(GroupInfo.OpennessType.PUBLIC);

        group = new GroupImpl(
                groupJid,
                groupInfo,
                "skysea.com",
                dispatcher,
                packetRouter,
                groupPersistenceManager,
                groupMemberPersistenceManager);

        new NonStrictExpectations() {
            {
                groupMemberPersistenceManager.addMembers(groupInfo.getId(), (List<GroupMemberInfo>)any);
                result = true;
            }
        };

        group.getChatUserManager().addUser("user", "用户");
        group.getChatUserManager().addUser("user1", "用户1");
        group.getChatUserManager().addUser("user2", "用户2");
    }

    public void testConstructor() {
        // Assert
        assertEquals("100", group.getId());
        assertEquals(new JID("owner@skysea.com"), group.getOwner());
        assertEquals(groupInfo, group.getGroupInfo());
        assertNotNull(group.getChatUserManager());
    }

    public void testUpdateGroupInfo() throws Exception {
        // Arrange
        final GroupInfo updater = new GroupInfo();
        updater.setName("new group");
        updater.setOpennessType(GroupInfo.OpennessType.AFFIRM_REQUIRED);
        new NonStrictExpectations() {
            {
                groupPersistenceManager.updateGroup(updater);
                result = true;
                times = 1;
            }
        };

        // Act
        group.updateGroupInfo(userJid, updater);

        // Assert
        assertFalse(group.getGroupInfo() == updater);
        assertFalse(group.getGroupInfo() == groupInfo);

        GroupInfo retGroupInfo = group.getGroupInfo();
        assertEquals(updater.getName(), retGroupInfo.getName());
        assertEquals(updater.getOpennessType(), retGroupInfo.getOpennessType());
    }

    public void testApplyToJoin_When_Openness_Type_Is_Public() throws Exception {
        // Arrange
        final String nickName = "碧眼狐狸";
        final String reason = "我也是80后";
        new NonStrictExpectations(ApplyStrategy.IMMEDIATE_PROCESS) {{
            ApplyStrategy.IMMEDIATE_PROCESS.applyToJoin(group, userJid, nickName, reason);
            times = 1;
        }};
        // Act
        group.applyJoin(userJid, nickName, reason);

        // Assert

    }

    public void testApplyToJoin_When_Openness_Type_Is_AFFIRM_REQUIRED() throws Exception {
        // Arrange
        final GroupInfo updater = new GroupInfo();
        updater.setOpennessType(GroupInfo.OpennessType.AFFIRM_REQUIRED);

        final String nickName = "碧眼狐狸";
        final String reason = "我也是80后";
        new NonStrictExpectations(ApplyStrategy.FORWARDING_TO_OWNER) {
            {
                groupPersistenceManager.updateGroup(updater);
                result = true;
                times = 1;
            }
        };
        group.updateGroupInfo(userJid, updater);

        // Act
        group.applyJoin(userJid, nickName, reason);

        // Assert
        new Verifications() {
            {
                ApplyStrategy.FORWARDING_TO_OWNER.applyToJoin(group, userJid, nickName, reason);
                times = 1;
            }
        };
    }

    public void testSend_When_Packet_Is_Message() throws Exception {
        // Arrange
        final Message msg = new Message();
        msg.setFrom(userJid);
        msg.setType(Message.Type.groupchat);

        final JID sender = new JID(groupJid.toString() + "/" + userJid.getNode());
        for (final ChatUser user : group.getChatUserManager().getUsers()) {
            new NonStrictExpectations(user) {
                {
                    user.send(packetRouter, with(new Delegate<Packet>() {

                        public boolean validate(Packet p) {
                            assertEquals(
                                    "<message type=\"groupchat\" from=\"100@group.skysea.com/user\">" +
                                            "<x xmlns=\"http://skysea.com/protocol/group#member\">" +
                                            "<member nickname=\"用户\"/>" +
                                            "</x></message>", p.toXML().trim());
                            assertEquals(sender, p.getFrom());
                            return true;
                        }
                    }));
                    times = 1;
                }
            };
        }

        // Act
        group.send(msg);

        // Assert
    }

    public void testSend_When_Packet_Is_Message_But_Sender_Is_Not_Group_User() throws Exception {
        // Arrange
        final JID otherJid = new JID("other@skysea.com");
        final Message msg = new Message();
        msg.setType(Message.Type.groupchat);
        msg.setFrom(otherJid);

        // Act
        group.send(msg);

        // Assert
        new Verifications() {
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message from=\"100@group.skysea.com\" to=\"other@skysea.com\" type=\"error\">" +
                                        "<error code=\"401\" type=\"auth\">" +
                                        "<not-authorized xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>" +
                                        "</error>" +
                                        "</message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testSend_When_Packet_Is_Message_But_Type_Is_Invalid() throws Exception {
        // Arrange
        final Message msg = new Message();
        msg.setFrom(userJid);

        // Act
        group.send(msg);

        // Assert
        new Verifications() {
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<message to=\"user@skyaea.com\" type=\"error\" from=\"100@group.skysea.com\">" +
                                        "<error code=\"406\" type=\"modify\">" +
                                        "<not-acceptable xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>" +
                                        "</error>" +
                                        "</message>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testSend_When_Packet_Is_Presence() throws Exception {
        // Arrange
        Presence presence = new Presence();
        presence.setFrom(userJid);

        // Act
        group.send(presence);

        // Assert
        new Verifications() {
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<presence to=\"user@skyaea.com\" type=\"error\" from=\"100@group.skysea.com\">" +
                                        "<error code=\"406\" type=\"modify\">" +
                                        "<not-acceptable xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>" +
                                        "</error>" +
                                        "</presence>", packet.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testDestroy() throws Exception {
        // Arrange
        final String reason = "再见了各位";
        new NonStrictExpectations() {
            {
                groupPersistenceManager.removeGroup(groupInfo.getId());
                result = true;
                times = 1;
            }
        };
        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                GroupEventDispatcher.fireGroupDestroyed(group, group.getOwner(), reason);
                times = 1;
            }
        };

        // Act
        group.destroy(group.getOwner(), reason);

        // Assert
    }

    public void testSend_When_Packet_Is_IQ() throws Exception {
        // Arrange
        final IQ iq = new IQ();
        iq.setFrom(userJid);

        // Act
        group.send(iq);

        // Assert
        new Verifications() {
            {
                dispatcher.dispatch(iq, group);
                times = 1;
            }
        };
    }

    public void testBroadcast() throws Exception {
        // Arrange
        final Packet packet = new Message();
        for (final ChatUser user : group.getChatUserManager().getUsers()) {
            new NonStrictExpectations(user) {
                {
                    user.send(packetRouter, packet);
                    times = 1;
                }
            };
        }

        // Act
        group.broadcast(packet);
    }

    public void testNumberOfUsersChanged() throws Exception {
        // Arrange
        int orgNum = group.getGroupInfo().getNumberOfMembers();

        // Act
        group.numberOfUsersChanged(100);

        // Assert
        assertEquals(100, group.getGroupInfo().getNumberOfMembers());
        assertFalse(orgNum == group.getGroupInfo().getNumberOfMembers());
    }

    public void testIsIdleState() throws Exception {
        // Arrange
        final long currentTime = 100;
        new NonStrictExpectations(group) {
            {
                setField(group, "lastReceivedPacketTime", currentTime);
            }
        };
        new MockUp<System>(){
            private int idx;
            private long[] times = new long[] {
                    currentTime,
                    currentTime + GroupImpl.IDLE_TIME_OUT - 1,
                    currentTime + GroupImpl.IDLE_TIME_OUT,
                    currentTime + GroupImpl.IDLE_TIME_OUT + 1,
                    currentTime + GroupImpl.IDLE_TIME_OUT + 2,
                    currentTime + GroupImpl.IDLE_TIME_OUT + 3};
            @Mock
            public long currentTimeMillis()
            {
                return times[idx++];
            }
        };

        // Act && Assert
        assertFalse(group.isIdleState());
        assertFalse(group.isIdleState());
        assertFalse(group.isIdleState());
        assertTrue(group.isIdleState());
        group.send(new IQ());
        assertFalse(group.isIdleState());
    }
}