package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class CreateHandlerTest extends IQHandlerTest<CreateHandler> {
    public CreateHandlerTest() {
        super(new CreateHandler());
    }

    public void testProcess() throws Exception {
        // Arrange
        IQ packet = IQ("<iq from='user@skysea.com' to='group.skysea.com' id='v9' type='set'>\n" +
                "    <x xmlns='http://skysea.com/protocol/group'>\n" +
                "        <x xmlns='jabber:x:data' type='submit'>\n" +
                "            <field var='name' type='text-single'>\n" +
                "              <value>圈子名称</value>\n" +
                "            </field>\n" +
                "            <field var='category' type='list-single'>\n" +
                "              <value>1</value>\n" +
                "            </field>\n" +
                "            <field var='subject' type='text-single'>\n" +
                "              <value>圈子主题</value>\n" +
                "            </field>\n" +
                "            <field var='description' type='text-multi'>\n" +
                "              <value>圈子描述</value>\n" +
                "            </field>\n" +
                "            <field var='logo' type='text-single'>\n" +
                "              <value>logo</value>\n" +
                "            </field>\n" +
                "            <field var='openness' type='list-single'>\n" +
                "              <value>PUBLIC</value>\n" +
                "            </field>\n" +
                "        </x>\n" +
                "    </x>\n" +
                "</iq>");


        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                groupManager.create(with(new Delegate<GroupInfo>() {
                    public void validate(GroupInfo info) {
                        assertEquals("圈子名称", info.getName());
                        assertEquals(1, info.getCategory());
                        assertEquals("圈子主题", info.getSubject());
                        assertEquals("圈子描述", info.getDescription());
                        assertEquals(GroupInfo.OpennessType.PUBLIC, info.getOpennessType());
                        assertEquals("logo", info.getLogo());
                        assertEquals("user", info.getOwner());
                        assertNotNull(info.getCreateTime());
                        assertEquals(0, info.getId());
                    }
                }));
                result = group;

                //GroupEventDispatcher.fireGroupCreated(group);
                //times = 1;
            }
        };

        // Act
        handler.process(packet);

        // Assert
        new Verifications() {
            {
                packetRouter.route(with(new Delegate<Packet>() {

                    public void validate(Packet packet) {
                        String expectXml = "<iq type=\"result\" id=\"v9\" from=\"group.skysea.com\" to=\"user@skysea.com\">\n" +
                                "  <x xmlns=\"http://skysea.com/protocol/group\">\n" +
                                "    <x xmlns=\"jabber:x:data\" type=\"result\">\n" +
                                "      <field var=\"jid\">\n" +
                                "        <value>100@group.skysea.com</value>\n" +
                                "      </field>\n" +
                                "    </x>\n" +
                                "  </x>\n" +
                                "</iq>";
                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
                times = 1;
            }
        };
    }
}