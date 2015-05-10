package com.skyseas.openfireplugins.group.iq.member;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.ChatUserManager;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class ExitGroupHandlerTest extends IQHandlerTest<ExitGroupHandler> {
    @Mocked ChatUser user;
    private IQ packet;

    public ExitGroupHandlerTest(){
        super(new ExitGroupHandler());
    }

    public void setUp() {
        super.setUp();
        packet = IQ("<iq from='user@skysea.com' to='100@group.skysea.com' id='v7' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#member'>\n" +
                "    <exit>\n" +
                "        <reason>大家太吵了，不好意思，我退了先！</reason>\n" +
                "    </exit>\n" +
                "  </x>\n" +
                "</iq>");
    }

    public void testProcess() throws Exception {

        // Arrange
        new NonStrictExpectations() {
            {
               userManager.removeUser(ChatUserManager.RemoveType.EXIT, "user", packet.getFrom(), "大家太吵了，不好意思，我退了先！");
               result = user;
               times = 1;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications() {
            {
                packetRouter.route(with(new Delegate<Packet>() {

                    public void validate(Packet packet) {
                        String expectXml = "<iq type=\"result\" id=\"v7\" " +
                                "from=\"100@group.skysea.com\" to=\"user@skysea.com\"/>";
                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    public void testProcess_When_User_Is_Owner() throws Exception {
        // Arrange
        packet.setFrom("owner@skysea.com");

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications() {
            {
                packetRouter.route(with(new Delegate<Packet>() {

                    public void validate(Packet packet) {
                        String expectXml =
                                "<iq type=\"error\" id=\"v7\" from=\"100@group.skysea.com\" to=\"owner@skysea.com\">\n" +
                                "  <error code=\"405\" type=\"cancel\">\n" +
                                "    <not-allowed xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>\n" +
                                "  </error>\n" +
                                "</iq>";
                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
                times = 1;
            }
        };
    }


}