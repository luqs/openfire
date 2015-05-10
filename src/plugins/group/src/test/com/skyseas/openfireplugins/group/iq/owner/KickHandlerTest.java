package com.skyseas.openfireplugins.group.iq.owner;

import com.skyseas.openfireplugins.group.ChatUserManager;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import com.skyseas.openfireplugins.group.iq.group.MockChatUser;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class KickHandlerTest extends IQHandlerTest<KickHandler> {

    public KickHandlerTest(){
        super(new KickHandler());
    }

    public void testProcess() throws Exception {
        // Arrange
        final IQ packet = IQ(
                "<iq from='owner@skysea.com' to='100@group.skysea.com' id='v11' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#owner'>\n" +
                "    <kick username='user'>\n" +
                "        <reason>抱歉！你总是发送广告信息。</reason>\n" +
                "    </kick>\n" +
                "  </x>\n" +
                "</iq>");
        final MockChatUser user = new MockChatUser("user", "碧眼狐狸");
        new NonStrictExpectations(){
            {
                userManager.removeUser(ChatUserManager.RemoveType.KICK, "user",packet.getFrom(), "抱歉！你总是发送广告信息。" );
                result = user;
                times = 1;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        assertEquals(
                                "<iq type=\"result\" id=\"v11\" from=\"100@group.skysea.com\" to=\"owner@skysea.com\"/>",
                                packet.toString().trim());
                    }
                }));
                times = 1;
            }
        };
    }
}