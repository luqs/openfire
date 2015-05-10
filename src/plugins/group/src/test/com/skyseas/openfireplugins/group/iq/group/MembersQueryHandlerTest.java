package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import java.util.ArrayList;

/**
 * Created by apple on 14-9-14.
 */
public class MembersQueryHandlerTest extends IQHandlerTest<MembersQueryHandler> {
    public MembersQueryHandlerTest(){
        super(new MembersQueryHandler());
    }

    public void testProcess() throws  Exception {
        //Arrange
        IQ packet  = IQ(
                "<iq from='user@skysea.com' to='100@group.skysea.com'  id='v3' type='get'>\n" +
                "  <query xmlns='http://skysea.com/protocol/group' node='members' />\n" +
                "</iq>");
        final ArrayList<MockChatUser> items = new ArrayList<MockChatUser>(2);
        final MockChatUser member1 = new MockChatUser("user1", "小李飞刀");
        final MockChatUser member2 = new MockChatUser("user10", "大刀关胜");
        items.add(member1);
        items.add(member2);

        new NonStrictExpectations(){
            {
                userManager.getUsers();
                result = items;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet){
                        String expectXml = "<iq type=\"result\" id=\"v3\" from=\"100@group.skysea.com\" to=\"user@skysea.com\">\n" +
                                "  <query xmlns=\"http://skysea.com/protocol/group\" node=\"members\">\n" +
                                "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                                "      <reported>\n" +
                                "        <field var=\"username\"/>\n" +
                                "        <field var=\"nickname\"/>\n" +
                                "      </reported>\n" +
                                "      <item>\n" +
                                "        <field var=\"nickname\">\n" +
                                "          <value>小李飞刀</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"username\">\n" +
                                "          <value>user1</value>\n" +
                                "        </field>\n" +
                                "      </item>\n" +
                                "      <item>\n" +
                                "        <field var=\"nickname\">\n" +
                                "          <value>大刀关胜</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"username\">\n" +
                                "          <value>user10</value>\n" +
                                "        </field>\n" +
                                "      </item>\n" +
                                "    </x>\n" +
                                "  </query>\n" +
                                "</iq>";

                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
            }
        };
    }
}
