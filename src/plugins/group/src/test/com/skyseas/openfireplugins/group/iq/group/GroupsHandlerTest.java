package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import com.skyseas.openfireplugins.group.iq.group.GroupsHandler;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import java.util.ArrayList;

public class GroupsHandlerTest extends IQHandlerTest<GroupsHandler> {

    public GroupsHandlerTest(){
        super(new GroupsHandler());
    }

    public void testProcess() throws Exception {
        //Arrange
        IQ packet = IQ(
                "<iq from='user@skysea.com' to='group.skysea.com' id='v6' type='get'>\n" +
                "    <query xmlns='http://skysea.com/protocol/group#user' node='groups' />\n" +
                "</iq>");
        final ArrayList<GroupInfo> items = getGroupInfos();
        new NonStrictExpectations(){
            {
                groupManager.getMemberJoinedGroups("user");
                result = items;
                times = 1;
            }
        };

        // Act
        handler.process(packet);

        // Assert
        new Verifications(){
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet){
                        String expectXml = "<iq type=\"result\" id=\"v6\" from=\"group.skysea.com\" to=\"user@skysea.com\">\n" +
                                "  <query xmlns=\"http://skysea.com/protocol/group#user\" node=\"groups\">\n" +
                                "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                                "      <reported>\n" +
                                "        <field var=\"jid\"/>\n" +
                                "      </reported>\n" +
                                "      <item>\n" +
                                "        <field var=\"jid\">\n" +
                                "          <value>1@group.skysea.com</value>\n" +
                                "        </field>\n" +
                                "      </item>\n" +
                                "      <item>\n" +
                                "        <field var=\"jid\">\n" +
                                "          <value>2@group.skysea.com</value>\n" +
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

    private ArrayList<GroupInfo> getGroupInfos() {
        ArrayList<GroupInfo> items = new ArrayList<GroupInfo>(2);
        GroupInfo group = new GroupInfo();
        group.setId(1);
        group.setOpennessType(GroupInfo.OpennessType.AFFIRM_REQUIRED);
        group.setName("我的1");
        group.setSubject("主题1");
        items.add(group);

        GroupInfo group2 = new GroupInfo();
        group2.setId(2);
        group.setOpennessType(GroupInfo.OpennessType.PUBLIC);
        group2.setName("我的2");
        group2.setSubject("主题2");
        group2.setNumberOfMembers(100);
        items.add(group2);
        return items;
    }
}