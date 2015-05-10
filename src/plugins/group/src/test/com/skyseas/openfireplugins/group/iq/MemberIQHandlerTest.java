package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import mockit.NonStrictExpectations;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class MemberIQHandlerTest extends IQHandlerTest<MemberIQHandlerTest.MockMemberIQHandler> {

    public MemberIQHandlerTest() {
        super(new MockMemberIQHandler());
    }

    public void testFilter_When_User_Is_Group_Member() throws Exception {
        // Arrange
        new NonStrictExpectations(){
            {
                userManager.hasUser("user");
                result = true;
            }
        };
        IQ packet = IQ(
                "<iq from='user@skysea.com' to='100@group.skysea.com' id='v8' type='set'>\n" +
                        "  <x xmlns='http://skysea.com/protocol/group#member'>\n" +
                        "  \t<invite>\n" +
                        "  \t\t<member username='user100' nickname='独孤求败' />\n" +
                        "  \t</invite>\n" +
                        "  </x>\n" +
                        "</iq>");

        IQContext context = new IQContext(packet);
        context.setItem(IQContext.ITEM_GROUP, group);

        // Act
        Packet result = handler.filter(context);

        // Assert
        assertNull(result);
    }

    public void testFilter_When_User_Is_Not_Group_Member() throws Exception {
        // Arrange
        new NonStrictExpectations(){
            {
                userManager.hasUser("user");
                result = false;
            }
        };
        IQ packet = IQ(
                "<iq from='user@skysea.com' to='100@group.skysea.com' id='v8' type='set'>\n" +
                        "  <x xmlns='http://skysea.com/protocol/group#member'>\n" +
                        "  \t<invite>\n" +
                        "  \t\t<member username='user100' nickname='独孤求败' />\n" +
                        "  \t</invite>\n" +
                        "  </x>\n" +
                        "</iq>");

        IQContext context = new IQContext(packet);
        context.setItem(IQContext.ITEM_GROUP, group);

        // Act
        Packet result = handler.filter(context);

        // Assert
        assertEquals(
                "<iq type=\"error\" id=\"v8\" from=\"100@group.skysea.com\" to=\"user@skysea.com\">\n" +
                        "  <error code=\"401\" type=\"auth\">\n" +
                        "    <not-authorized xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/>\n" +
                        "  </error>\n" +
                        "</iq>", result.toString().trim());
    }

    public static class MockMemberIQHandler extends MemberIQHandler{
        @Override
        protected void process(IQ packet, Group group) {

        }
    }
}