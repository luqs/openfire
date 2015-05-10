package com.skyseas.openfireplugins.group.iq.member;

import com.skyseas.openfireplugins.group.GroupMemberInfo;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import com.skyseas.openfireplugins.group.iq.group.MockChatUser;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import java.util.Arrays;
import java.util.List;

public class InviteHandlerTest extends IQHandlerTest<InviteHandler> {

    public InviteHandlerTest() {
        super(new InviteHandler());
    }

    public void testProcess() throws Exception {
        // Arrange
        new NonStrictExpectations(){
            {
                userManager.addUsers(with(new Delegate<List<GroupMemberInfo>>() {
                    public void validate(List<GroupMemberInfo> memberInfos) {
                        assertEquals(3, memberInfos.size());

                        assertEquals("user100", memberInfos.get(0).getUserName());
                        assertEquals("独孤求败", memberInfos.get(0).getNickName());

                        assertEquals("user101", memberInfos.get(1).getUserName());
                        assertEquals("雁过留声", memberInfos.get(1).getNickName());

                        assertEquals("user102", memberInfos.get(2).getUserName());
                        assertEquals("圆月弯刀", memberInfos.get(2).getNickName());
                    }
                }));
                result = Arrays.asList(
                        new MockChatUser("user100", "独孤求败"),
                        new MockChatUser("user101", "雁过留声"),
                        new MockChatUser("user102", "圆月弯刀"));
                times = 1;
            }
        };

        IQ packet = IQ("<iq from='user@skysea.com/spark' to='100@group.skysea.com' id='v8' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#member'>\n" +
                "    <invite>\n" +
                "        <member username='user100' nickname='独孤求败' />\n" +
                "        <member username='user101' nickname='雁过留声' />\n" +
                "        <member username='user102' nickname='圆月弯刀' />\n" +
                "    </invite>\n" +
                "  </x>\n" +
                "</iq>\n");

        // Act
        handler.process(packet, group);

        // Assert
        Thread.sleep(100);
        new Verifications(){
            {
                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet p) {
                        assertEquals(
                                "<iq type=\"result\" " +
                                        "id=\"v8\" " +
                                        "from=\"100@group.skysea.com\" " +
                                        "to=\"user@skysea.com/spark\"/>",
                                p.toXML().trim());
                    }
                }));
                times = 1;

                group.broadcast(with(new Delegate<Packet>() {
                    public void validate(Packet p) {
                        assertEquals(
                                "<message><x xmlns=\"http://skysea.com/protocol/group#member\">" +
                                        "<invite from=\"user@skysea.com\">" +
                                        "<member username=\"user100\" nickname=\"独孤求败\"/>" +
                                        "<member username=\"user101\" nickname=\"雁过留声\"/>" +
                                        "<member username=\"user102\" nickname=\"圆月弯刀\"/>" +
                                        "</invite></x></message>",
                                p.toXML().trim());
                    }
                }));
                times = 1;
            }
        };

    }

    public void testProcess_When_Add_User_Fail() throws Exception {
        // Arrange
        new NonStrictExpectations(){
            {
                userManager.addUsers(with(new Delegate<List<GroupMemberInfo>>() {
                    public void validate(List<GroupMemberInfo> memberInfos) {
                        assertEquals(1, memberInfos.size());
                        assertEquals("user100", memberInfos.get(0).getUserName());
                        assertEquals("独孤求败", memberInfos.get(0).getNickName());
                    }
                }));
                result = new RuntimeException();
                times = 1;
            }
        };

        IQ packet = IQ("<iq from='user@skysea.com' to='100@group.skysea.com' id='v8' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#member'>\n" +
                "  \t<invite>\n" +
                "  \t\t<member username='user100' nickname='独孤求败' />\n" +
                "  \t</invite>\n" +
                "  </x>\n" +
                "</iq>");

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet p) {
                        assertEquals(
                                "<iq type=\"error\" id=\"v8\" from=\"100@group.skysea.com\" to=\"user@skysea.com\">" +
                                        "<error code=\"500\" type=\"wait\">" +
                                        "<internal-server-error xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>" +
                                        "</error>" +
                                        "</iq>",
                                p.toXML().trim());
                    }
                }));
                times = 1;
            }
        };
    }
}