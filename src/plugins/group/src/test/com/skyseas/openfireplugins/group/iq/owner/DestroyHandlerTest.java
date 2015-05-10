package com.skyseas.openfireplugins.group.iq.owner;

import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class DestroyHandlerTest extends IQHandlerTest<DestroyHandler> {

    public DestroyHandlerTest() {
        super(new DestroyHandler());
    }

    public void testProcess() throws Exception {
        // Arrange
        final IQ packet = IQ(
                "<iq from='owner@skysea.com' to='100@group.skysea.com' id='v12' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                "  \t<destroy>\n" +
                "  \t\t<reason>再见了各位！</reason>\n" +
                "  \t</destroy>\n" +
                "  </x>\n" +
                "</iq>");

        new NonStrictExpectations(){
            {
                groupManager.remove(group, packet.getFrom(), "再见了各位！");
                result = true;
                times = 1;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet p)
                    {
                        assertEquals("<iq type=\"result\" id=\"v12\" from=\"100@group.skysea.com\" to=\"owner@skysea.com\"/>",
                                p.toString().trim());
                    }
                }));
                times = 1;

            }
        };
    }
}