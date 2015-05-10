package com.skyseas.openfireplugins.group.iq.member;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import com.skyseas.openfireplugins.group.iq.group.MockChatUser;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class ProfileHandlerTest extends IQHandlerTest<ProfileHandler> {
    public ProfileHandlerTest() {
        super(new ProfileHandler());
    }

    public void testProcess() throws Exception {
        // Arrange
        final MockChatUser user = new MockChatUser("user", "碧眼狐狸");
        IQ packet = IQ(
                "<iq from='user@skysea.com' to='100@group.skysea.com' id='v7' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#member'>\n" +
                "    <profile>\n" +
                "        <nickname>金轮法王</nickname>\n" +
                "    </profile>\n" +
                " </x>\n" +
                "</iq>");
        new NonStrictExpectations(){
            {
                userManager.getUser("user");
                result = user;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                userManager.changeNickname("user", "金轮法王");
                times = 1;


                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        String expectXml = "<iq type=\"result\" id=\"v7\" from=\"100@group.skysea.com\" to=\"user@skysea.com\"/>";
                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
                times = 1;
            }
        };
    }
}