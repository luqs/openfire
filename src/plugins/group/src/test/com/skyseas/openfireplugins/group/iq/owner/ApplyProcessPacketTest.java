package com.skyseas.openfireplugins.group.iq.owner;

import junit.framework.TestCase;
import org.xmpp.packet.Message;

public class ApplyProcessPacketTest extends TestCase {

    public void testNewInstanceForwardingToOwner() throws Exception {
        Message msg = ApplyProcessPacket.newInstanceForwardingToOwner("123", "user", "碧眼狐狸", "我也是80后");
        assertEquals(
                "<message>" +
                "<x xmlns=\"http://skysea.com/protocol/group#owner\">" +
                "<apply id=\"123\">" +
                "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                "<reason>我也是80后</reason></apply></x>" +
                "</message>",
                msg.toXML().trim());
    }

    public void testNewInstanceForApplyResult_When_Agree_Apply() throws Exception {
        Message msg = ApplyProcessPacket.newInstanceForApplyResult(true, "owner@skysea.com", "欢迎加入");
        assertEquals("<message>" +
                "<x xmlns=\"http://skysea.com/protocol/group#user\">" +
                "<apply>" +
                "<agree from=\"owner@skysea.com\"/>" +
                "<reason>欢迎加入</reason></apply></x>" +
                "</message>",
                msg.toXML().trim());
    }


    public void testNewInstanceForApplyResult_When_Decline_Apply() throws Exception {
        Message msg = ApplyProcessPacket.newInstanceForApplyResult(false, "owner@skysea.com", "不收人了");
        assertEquals("<message>" +
                        "<x xmlns=\"http://skysea.com/protocol/group#user\">" +
                        "<apply>" +
                        "<decline from=\"owner@skysea.com\"/>" +
                        "<reason>不收人了</reason></apply></x>" +
                        "</message>",
                msg.toXML().trim());
    }

}