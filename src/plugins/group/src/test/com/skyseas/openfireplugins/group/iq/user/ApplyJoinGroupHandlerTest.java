package com.skyseas.openfireplugins.group.iq.user;

import com.skyseas.openfireplugins.group.FullMemberException;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class ApplyJoinGroupHandlerTest extends IQHandlerTest<ApplyJoinGroupHandler> {

    private IQ packet;

    public ApplyJoinGroupHandlerTest(){
        super(new ApplyJoinGroupHandler());
    }

    public void setUp(){
        super.setUp();
        packet = IQ("<iq from='user@skysea.com' to='100@group.skysea.com' id='v4' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#user'>\n" +
                "    <apply>\n" +
                "        <member nickname=\"碧眼狐狸\" /> "+
                "        <reason>我也是80后，请让我加入吧！</reason>\n" +
                "    </apply>\n" +
                "  </x>\n" +
                "</iq>");
    }

    public void testProcess() throws Exception {
        // Arrange
        new NonStrictExpectations(){
            {
                userManager.hasUser("user");
                result = false;
                times = 1;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                group.applyJoin(packet.getFrom(), "碧眼狐狸", "我也是80后，请让我加入吧！");
                times = 1;

                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet p) {
                        assertEquals(
                                "<iq type=\"result\" id=\"v4\" from=\"100@group.skysea.com\" " +
                                        "to=\"user@skysea.com\"/>",
                                p.toString().trim());
                    }

                }));
            }
        };

    }

    public void testProcess_When_User_Was_Joined() throws Exception{
        IQ packet = IQ("<iq from='user@skysea.com' to='100@group.skysea.com' id='v4' type='set'>\n" +
                "  <x xmlns='http://skysea.com/protocol/group#user'>\n" +
                "    <apply>\n" +
                "        <reason>我也是80后，请让我加入吧！</reason>\n" +
                "    </apply>\n" +
                "  </x>\n" +
                "</iq>");
        new NonStrictExpectations(){
            {
                userManager.hasUser("user");
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
                    public void validate(Packet p) {
                        assertEquals("<iq type=\"result\" id=\"v4\" from=\"100@group.skysea.com\" to=\"user@skysea.com\"/>",
                        p.toString().trim());
                    }

                }));
            }
        };
    }

    public void testProcess_When_Grou_Full_Member() throws Exception{
        new NonStrictExpectations(){
            {
                userManager.hasUser("user");
                result = false;
                times = 1;

                group.applyJoin(packet.getFrom(), "碧眼狐狸", "我也是80后，请让我加入吧！");
                result = new FullMemberException("");
                times = 1;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                handler.routePacket(with(new Delegate<Packet>() {
                    public void validate(Packet p) {
                        assertEquals("<iq type=\"error\" id=\"v4\" from=\"100@group.skysea.com\" to=\"user@skysea.com\">\n" +
                                        "  <error code=\"503\" type=\"cancel\">\n" +
                                        "    <service-unavailable xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>\n" +
                                        "  </error>\n" +
                                        "</iq>",
                                p.toString().trim());
                    }

                }));
                times = 1;
            }
        };
    }
}