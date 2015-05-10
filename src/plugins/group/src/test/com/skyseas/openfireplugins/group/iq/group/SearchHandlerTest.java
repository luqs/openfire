package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.GroupQueryObject;
import com.skyseas.openfireplugins.group.util.Paging;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import java.util.ArrayList;

public class SearchHandlerTest extends IQHandlerTest<SearchHandler> {
    public SearchHandlerTest() {
        super(new SearchHandler());
    }

    public void testProcess() throws Exception {
        //Arrange
        IQ packet = IQ("<iq from='user@skysea.com' to='group.skysea.com' id='v1' type='set'>\n" +
                        "    <query xmlns='jabber:iq:search'>\n" +
                        "        <x xmlns='jabber:x:data' type='submit'>\n" +
                        "            <field var='id' type='text-single'>\n" +
                        "             <value>1</value>\n" +
                        "            </field>\n" +
                        "            <field var='name'  type='text-single'>\n" +
                        "              <value>圈子名称</value>\n" +
                        "            </field>\n" +
                        "            <field var='category' type='list-single'>\n" +
                        "              <value>100</value>\n" +
                        "            </field>\n" +
                        "        </x>\n" +
                        "        <set xmlns='http://jabber.org/protocol/rsm'>\n" +
                        "          <max>10</max>\n" +
                        "          <index>20</index>\n" +
                        "        </set>                  \n" +
                        "    </query>\n" +
                        "</iq>");

        final Paging<GroupInfo> groups = getGroupInfos();
        new NonStrictExpectations(){
            {
                groupManager.search(with(new Delegate<GroupQueryObject>() {
                    public void validate(GroupQueryObject queryObject) {
                        assertEquals(1, queryObject.getGroupId());
                        assertEquals("圈子名称", queryObject.getName());
                        assertEquals(100, queryObject.getCategory());
                    }

                }), 20, 10);
                result = groups;
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
                        String expectXml = "<iq type=\"result\" id=\"v1\" from=\"group.skysea.com\" to=\"user@skysea.com\">\n" +
                                "  <query xmlns=\"jabber:iq:search\">\n" +
                                "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                                "      <reported>\n" +
                                "        <field var=\"jid\"/>\n" +
                                "        <field var=\"openness\"/>\n" +
                                "        <field var=\"owner\"/>\n" +
                                "        <field var=\"name\"/>\n" +
                                "        <field var=\"logo\"/>\n" +
                                "        <field var=\"memberCount\"/>\n" +
                                "        <field var=\"subject\"/>\n" +
                                "      </reported>\n" +
                                "      <item>\n" +
                                "        <field var=\"openness\">\n" +
                                "          <value>AFFIRM_REQUIRED</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"logo\">\n" +
                                "          <value>a.jpg</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"subject\">\n" +
                                "          <value>主题1</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"name\">\n" +
                                "          <value>我的1</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"owner\">\n" +
                                "          <value>owner1</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"jid\">\n" +
                                "          <value>1@group.skysea.com</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"memberCount\">\n" +
                                "          <value>100</value>\n" +
                                "        </field>\n" +
                                "      </item>\n" +
                                "      <item>\n" +
                                "        <field var=\"openness\">\n" +
                                "          <value>PUBLIC</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"logo\">\n" +
                                "          <value>b.jpg</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"subject\">\n" +
                                "          <value>主题2</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"name\">\n" +
                                "          <value>我的2</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"owner\">\n" +
                                "          <value>owner2</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"jid\">\n" +
                                "          <value>2@group.skysea.com</value>\n" +
                                "        </field>\n" +
                                "        <field var=\"memberCount\">\n" +
                                "          <value>0</value>\n" +
                                "        </field>\n" +
                                "      </item>\n" +
                                "    </x>\n" +
                                "    <set xmlns=\"http://jabber.org/protocol/rsm\">\n" +
                                "      <count>100</count>\n" +
                                "      <first index=\"20\">1</first>\n" +
                                "      <last>2</last>\n" +
                                "    </set>\n" +
                                "  </query>\n" +
                                "</iq>";


                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
                times = 1;
            }
        };
    }

    private Paging<GroupInfo> getGroupInfos() {

        final Paging<GroupInfo> groups = new Paging<GroupInfo>();
        groups.setCount(100);
        groups.setLimit(10);
        groups.setOffset(20);

        groups.setItems(new ArrayList<GroupInfo>(2));

        GroupInfo group = new GroupInfo();
        group.setId(1);
        group.setOpennessType(GroupInfo.OpennessType.AFFIRM_REQUIRED);
        group.setName("我的1");
        group.setSubject("主题1");
        group.setNumberOfMembers(100);
        group.setOwner("owner1");
        group.setLogo("a.jpg");
        groups.getItems().add(group);

        GroupInfo group2 = new GroupInfo();
        group2.setId(2);
        group2.setOpennessType(GroupInfo.OpennessType.PUBLIC);
        group2.setName("我的2");
        group2.setSubject("主题2");
        group2.setOwner("owner2");
        group2.setLogo("b.jpg");
        groups.getItems().add(group2);
        return groups;
    }
}