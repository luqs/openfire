package com.skyseas.openfireplugins.group.iq.owner;

import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class ApplyHandlerTest extends IQHandlerTest<ApplyHandler> {

    public ApplyHandlerTest() {
        super(new ApplyHandler());
    }

    @Override
    public void setUp(){
        super.setUp();

        new NonStrictExpectations() {
            {
                xmppServer.createJID("user", null);
                result = new JID("user@skysea.com");
            }
        };
    }

    public void testProcess_When_Owner_Agree_Apply() throws Exception {

        // Arrange

        IQ packet = IQ("<iq from='owner@skysea.com' to='100@group.skysea.com' id='v5' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                "    <apply id='s2fd1'>\n" +
                "        <agree />\n" +
                "        <member username='user' nickname='碧眼狐狸' />" +
                "        <reason>欢迎加入</reason>\n" +
                "    </apply>\n" +
                "  </x>\n" +
                "</iq>");


        // Act
        handler.process(packet, group);

        // Assert
        new Verifications() {
            {
                userManager.addUser("user", "碧眼狐狸");
                times = 1;

                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals("<iq type=\"result\" id=\"v5\" " +
                                        "from=\"100@group.skysea.com\" to=\"owner@skysea.com\"/>",
                                packet.toString().trim());
                    }
                }));
                times = 1;

                group.send(new JID("user@skysea.com"),
                        with(new Delegate<Message>() {
                            public void validate(Message packet) {
                                assertEquals("<message>\n" +
                                                "  <x xmlns=\"http://skysea.com/protocol/group#user\">\n" +
                                                "    <apply>\n" +
                                                "      <agree from=\"owner@skysea.com\"/>\n" +
                                                "      <reason>欢迎加入</reason>\n" +
                                                "    </apply>\n" +
                                                "  </x>\n" +
                                                "</message>",
                                        packet.toString().trim());
                            }
                        }));
                times = 1;
            }
        };

    }

    public void testProcess_When_Owner_Decline_Apply() throws Exception {

        // Arrange
        IQ packet = IQ("<iq from='owner@skysea.com' to='100@group.skysea.com' id='v5' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                "    <apply id='s2fd1' from='user@skysea.com' >\n" +
                "        <decline />\n" +
                "        <member username='user' nickname='碧眼狐狸' />" +
                "        <reason>目前不考虑新人加入，不好意思！</reason>\n" +
                "    </apply>\n" +
                "  </x>\n" +
                "</iq>");


        // Act
        handler.process(packet, group);

        // Assert
        new Verifications() {
            {
                handler.routePacket(with(new Delegate<IQ>() {
                    public void validate(IQ packet) {
                        assertEquals("<iq type=\"result\" id=\"v5\" " +
                                        "from=\"100@group.skysea.com\" to=\"owner@skysea.com\"/>",
                                packet.toString().trim());
                    }
                }));
                times = 1;

                group.send(new JID("user@skysea.com"),
                        with(new Delegate<Message>() {
                            public void validate(Message packet) {
                                assertEquals("<message>\n" +
                                                "  <x xmlns=\"http://skysea.com/protocol/group#user\">\n" +
                                                "    <apply>\n" +
                                                "      <decline from=\"owner@skysea.com\"/>\n" +
                                                "      <reason>目前不考虑新人加入，不好意思！</reason>\n" +
                                                "    </apply>\n" +
                                                "  </x>\n" +
                                                "</message>",
                                        packet.toString().trim());
                            }
                        }));
                times = 1;
            }
        };

    }
}