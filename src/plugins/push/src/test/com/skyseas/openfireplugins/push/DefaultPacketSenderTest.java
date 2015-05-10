package com.skyseas.openfireplugins.push;

import com.skyseas.openfireplugins.push.DefaultPacketSender;
import junit.framework.TestCase;
import mockit.Mocked;
import mockit.Verifications;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.RoutingTable;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class DefaultPacketSenderTest extends TestCase {
    @Mocked PacketRouter packetRouter;
    @Mocked RoutingTable routingTable;
    DefaultPacketSender sender;

    @Override
    protected void setUp() {
        sender = new DefaultPacketSender(packetRouter, routingTable);
    }

    public void testSend_When_Packet_Recipient_Is_Set() throws Exception {
        // Arrange
        final Message message = new Message();
        message.setTo("user@skysea.com");
        message.setBody("ok");

        // Act
        sender.send(message);

        // Assert
        new Verifications(){
            {
                packetRouter.route((Packet)message);
                times  = 1;
            }
        };
    }

    public void testSend_When_Broadcast_Packet_Is_Not_Message() throws Exception {
        // Arrange
        final IQ iq = new IQ(IQ.Type.set);
        iq.setFrom("skysea.com/event");

        // Act && Assert
        try {
            sender.send(iq);
            fail();
        }catch (IllegalArgumentException exp) {
            assertEquals("broadcast packet must be message.", exp.getMessage());
            return;
        }
        fail();
    }

    public void testSend_When_Packet_Recipient_Is_Not_Set() throws Exception {
        // Arrange
        final Message message = new Message();
        message.setBody("ok");

        // Act
        sender.send(message);

        // Assert
        new Verifications(){
            {
                routingTable.broadcastPacket(message, true);
                times  = 1;
            }
        };
    }
}