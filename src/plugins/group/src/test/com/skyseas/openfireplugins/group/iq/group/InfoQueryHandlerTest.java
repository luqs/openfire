package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import java.util.Calendar;

public class InfoQueryHandlerTest extends IQHandlerTest<InfoQueryHandler> {


    public InfoQueryHandlerTest() {
        super(new InfoQueryHandler());
    }

    public void testProcess() throws Exception {
        // Arrange
        final GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(100);
        groupInfo.setOwner("ok");
        groupInfo.setLogo("test.jpg");
        groupInfo.setName("fun");
        groupInfo.setNumberOfMembers(122);
        groupInfo.setOpennessType(GroupInfo.OpennessType.AFFIRM_REQUIRED);
        groupInfo.setCategory(23);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014,1,1,1,1,1);
        calendar.set(Calendar.MILLISECOND, 1);
        groupInfo.setCreateTime(calendar.getTime());
        IQ packet = IQ("<iq id='v3' from='user@skyseas.com' to='1@group.skysea.com' type='get'>\n" +
                        "  <query xmlns='http://jabber.org/protocol/group'  node='info'/>\n" +
                        "</iq>");

        new NonStrictExpectations(){
            {
                group.getGroupInfo();
                result = groupInfo;
            }
        };

        // Act
        handler.process( packet, group);

        // Assert
        new Verifications(){
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {

                        String expectXml =
                                "<iq type=\"result\" id=\"v3\" from=\"1@group.skysea.com\" to=\"user@skyseas.com\">\n" +
                                        "  <query xmlns=\"http://skysea.com/protocol/group\" node=\"info\">\n" +
                                        "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                                        "      <field var=\"jid\">\n" +
                                        "        <value>100@group.skysea.com</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"owner\">\n" +
                                        "        <value>ok</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"name\">\n" +
                                        "        <value>fun</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"logo\">\n" +
                                        "        <value>test.jpg</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"memberCount\">\n" +
                                        "        <value>122</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"subject\"/>\n" +
                                        "      <field var=\"description\"/>\n" +
                                        "      <field var=\"openness\">\n" +
                                        "        <value>AFFIRM_REQUIRED</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"category\">\n" +
                                        "        <value>23</value>\n" +
                                        "      </field>\n" +
                                        "      <field var=\"createTime\">\n" +
                                        "        <value>2014-01-31T17:01:01Z</value>\n" +
                                        "      </field>\n" +
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