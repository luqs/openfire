package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class OwnerIQHandlerTest extends IQHandlerTest<OwnerIQHandlerTest.MockOwnerIQHandler> {

    public OwnerIQHandlerTest(){
        super(new MockOwnerIQHandler());
    }

    public void testFilter_When_User_Is_Owner() throws Exception {
        // Arrange
        IQ packet = IQ(
                "<iq from='owner@skysea.com' to='100@group.skysea.com' id='v12' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                "    <destroy>\n" +
                "        <reason>再见了各位！</reason>\n" +
                "    </destroy>\n" +
                "  </x>\n" +
                "</iq>");

        IQContext context = new IQContext(packet);
        context.setItem(IQContext.ITEM_GROUP, group);

        // Act
        Packet result = handler.filter(context);

        // Assert
        assertNull(result);
    }

    public void testFilter_When_User_Is_Not_Owner() throws Exception {
        // Arrange
        IQ packet = IQ(
                "<iq from='user@skysea.com' to='100@group.skysea.com' id='v12' type='set'>\n" +
                        "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                        "    <destroy>\n" +
                        "        <reason>再见了各位！</reason>\n" +
                        "    </destroy>\n" +
                        "  </x>\n" +
                        "</iq>");

        IQContext context = new IQContext(packet);
        context.setItem(IQContext.ITEM_GROUP, group);

        // Act
        Packet result = handler.filter(context);

        // Assert
        assertEquals(
                "<iq type=\"error\" id=\"v12\" from=\"100@group.skysea.com\" to=\"user@skysea.com\">\n" +
                "  <error code=\"401\" type=\"auth\">\n" +
                "    <not-authorized xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>\n" +
                "  </error>\n" +
                "</iq>", result.toString().trim());
    }

    public static class MockOwnerIQHandler extends OwnerIQHandler {
        @Override
        protected void process(IQ packet, Group group) {

        }
    }
}