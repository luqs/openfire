package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupManager;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.*;

public class GroupServiceImplTest extends TestCase {
    @Mocked
    XMPPServer server;
    @Mocked
    Group group;
    private GroupServiceImpl groupService;
    JID serviceJid = new JID("group.skysea.com");
    JID userJid = new JID("user@skysea.com");
    JID groupJid = new JID("100@group.skysea.com");

    @Override
    protected void setUp() throws Exception {
        groupService = new GroupServiceImpl("group", "圈子服务", server);
    }

    public void testInitialize() throws Exception {

        // Arrange
        new NonStrictExpectations(IQDispatcher.class) {
            {
                IQDispatcher.groupIQConfig(withAny((IQDispatcher) null));
                times = 1;

                IQDispatcher.serviceIQConfig(withAny((IQDispatcher) null));
                times = 1;
            }
        };

        // Act
        groupService.initialize(serviceJid, null);
    }

    public void testProcessPacket_When_IQ_Packet_Send_To_Service() throws Exception {

        // Arrange
        final IQ packet = new IQ();
        packet.setTo(serviceJid);
        groupService.initialize(serviceJid, null);
        new NonStrictExpectations() {
            {
                final IQDispatcher dispatcher = getField(groupService, "iqDispatcher");
                new NonStrictExpectations(dispatcher) {
                    {
                        dispatcher.dispatch(packet);
                        times = 1;
                    }
                };

            }
        };

        // Act
        groupService.processPacket(packet);

        // Assert
    }

    public void testProcessPacket_When_Message_Packet_Send_To_Service() throws Exception {

        // Arrange
        final Message packet = new Message();
        packet.setFrom(userJid);
        packet.setTo(serviceJid);
        groupService.initialize(serviceJid, null);
        new NonStrictExpectations(groupService) {
            {
                groupService.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(serviceJid, packet.getFrom());
                        assertEquals(userJid, packet.getTo());
                        assertEquals(PacketError.Condition.not_acceptable, packet.getError().getCondition());
                        assertEquals(Message.class, packet.getClass());
                    }
                }));
                times = 1;
            }
        };

        // Act
        groupService.processPacket(packet);

        // Assert
    }

    public void testProcessPacket_When_Packet_Send_To_Group() throws Exception {
        // Arrange
        final Message packet = new Message();
        packet.setFrom(userJid);
        packet.setTo(groupJid);
        groupService.initialize(serviceJid, null);
        new NonStrictExpectations() {
            {
                final GroupManager groupManager = getField(groupService, "groupManager");
                new NonStrictExpectations(groupManager) {
                    {
                        groupManager.getGroup(groupJid.getNode());
                        result = group;
                        times = 1;
                    }
                };
            }
        };

        new NonStrictExpectations() {
            {
                group.send(packet);
                times = 1;
            }
        };

        // Act
        groupService.processPacket(packet);
    }

    public void testProcessPacket_When_Packet_Send_To_Group_But_Group_No_Found() throws Exception {
        // Arrange
        final Presence packet = new Presence();
        packet.setFrom(userJid);
        packet.setTo(groupJid);
        groupService.initialize(serviceJid, null);

        new NonStrictExpectations() {
            {
                final GroupManager groupManager = getField(groupService, "groupManager");
                new NonStrictExpectations(groupManager) {
                    {
                        groupManager.getGroup(groupJid.getNode());
                        result = null;
                        times = 1;
                    }
                };
            }
        };
        new NonStrictExpectations(groupService) {
            {
                groupService.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(groupJid, packet.getFrom());
                        assertEquals(userJid, packet.getTo());
                        assertEquals(PacketError.Condition.item_not_found, packet.getError().getCondition());
                        assertEquals(Presence.class, packet.getClass());
                    }
                }));
                times = 1;
            }
        };

        // Act
        groupService.processPacket(packet);
    }
}