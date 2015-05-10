package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandlerTest;
import mockit.Delegate;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public class UpdateHandlerTest extends IQHandlerTest<UpdateHandler> {

    private IQ packet;

    public UpdateHandlerTest() {
        super(new UpdateHandler());
    }

    public void setUp() {
        super.setUp();
        packet = IQ("<iq from='owner@skysea.com' to='100@group.skysea.com' id='v1' type='set'>\n" +
                        "\t<x xmlns='http://skysea.com/protocol/group'>\n" +
                        "\t\t<x xmlns='jabber:x:data' type='submit'>\n" +
                        "\t\t    <field var='name'  type='text-single'>\n" +
                        "\t\t      <value>圈子名称</value>\n" +
                        "\t\t    </field>\n" +
                        "\t\t    <field var='category' type='list-single'>\n" +
                        "\t\t      <value>56</value>\n" +
                        "\t\t    </field>\n" +
                        "\t\t    <field var='subject' type='text-single'>\n" +
                        "\t\t      <value>圈子主题</value>\n" +
                        "\t\t    </field>\n" +
                        "\t\t    <field var='description' type='text-multi'>\n" +
                        "\t\t      <value>圈子描述</value>\n" +
                        "\t\t    </field>\n" +
                        "\t\t    <field var='logo' type='text-single'>\n" +
                        "\t\t      <value>logo</value>\n" +
                        "\t\t    </field>\n" +
                        "\t\t    <field var='openness' type='list-single'>\n" +
                        "\t\t      <value>AFFIRM_REQUIRED</value>\n" +
                        "\t\t    </field>\n" +
                        "\t  \t</x>\n" +
                        "  \t</x>\n" +
                        "</iq>");
    }

    public void testProcess() throws Exception {
        //Arrange
        new NonStrictExpectations(){
            {
                group.updateGroupInfo(packet.getFrom(), with(new Delegate<GroupInfo>() {
                    public void validate(GroupInfo groupInfo) {
                        //assertEquals(0, groupInfo.getId());
                        assertEquals("圈子名称", groupInfo.getName());
                        assertEquals("圈子描述", groupInfo.getDescription());
                        assertEquals(GroupInfo.OpennessType.AFFIRM_REQUIRED, groupInfo.getOpennessType());
                        assertEquals("圈子主题", groupInfo.getSubject());
                        assertEquals(56, groupInfo.getCategory());
                        assertEquals("logo", groupInfo.getLogo());
                    }
                }));
                result = true;
                times = 1;
            }
        };

        // Act
        handler.process(packet, group);

        // Assert
        new Verifications(){
            {
                packetRouter.route(with(new Delegate<Packet>() {
                    public void validate(Packet packet) {
                        String expectXml = "<iq type=\"result\" id=\"v1\" from=\"100@group.skysea.com\" to=\"owner@skysea.com\"/>";
                        assertEquals(expectXml, packet.toString().trim());
                    }
                }));
            }
        };
    }

}