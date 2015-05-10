package com.skyseas.openfireplugins.group;

import junit.framework.TestCase;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * Created by apple on 14-8-30.
 */
public class MessageFactoryTest extends TestCase {

    // Arrange
    String userName = "user";
    String nickName = "碧眼狐狸";
    JID ownerJid = new JID("owner@skysea.com");

    public void testNewInstanceForMemberJoined() {
        // Act
        Message msg = MessageFactory.newInstanceForMemberJoin(userName, nickName);

        // Assert
        assertEquals("<message>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group#member\">\n" +
                        "    <join>\n" +
                        "      <member username=\"user\" nickname=\"碧眼狐狸\"/>\n" +
                        "    </join>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }


    public void testNewInstanceForGroupDestroyed() {
        // Act
        Message msg = MessageFactory.newInstanceForGroupDestroy(ownerJid, "再见了各位！");

        // Assert
        assertEquals("<message>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group\">\n" +
                        "    <destroy from=\"owner@skysea.com\">\n" +
                        "      <reason>再见了各位！</reason>\n" +
                        "    </destroy>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }

    public void testNewInstanceForGroupChanged() {
        // Act
        Message msg = MessageFactory.newInstanceForGroupInfoChange(ownerJid);

        // Assert
        assertEquals("<message>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group\">\n" +
                        "    <change from=\"owner@skysea.com\"/>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }

    public void testNewInstanceForMemberExit() {
        // Act
        Message msg = MessageFactory.newInstanceForMemberExit(
                userName,
                nickName,
                "大家太吵了，不好意思，我退了先！");

        // Assert
        assertEquals("<message>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group#member\">\n" +
                        "    <exit>\n" +
                        "      <member username=\"user\" nickname=\"碧眼狐狸\"/>\n" +
                        "      <reason>大家太吵了，不好意思，我退了先！</reason>\n" +
                        "    </exit>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }

    public void testNewInstanceForMemberKick() {
        // Act
        Message msg = MessageFactory.newInstanceForMemberKick(
                userName,
                nickName,
                ownerJid,
                "抱歉！你总是发送广告信息。");

        // Assert
        assertEquals("<message>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group#member\">\n" +
                        "    <kick from=\"owner@skysea.com\">\n" +
                        "      <member username=\"user\" nickname=\"碧眼狐狸\"/>\n" +
                        "      <reason>抱歉！你总是发送广告信息。</reason>\n" +
                        "    </kick>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }

    public void testNewInstanceForGroupChat() {
        // Act
        Message msg = MessageFactory.newInstanceForGroupChat("大家好啊，一起出来喝酒吧！", "碧眼狐狸");

        // Assert
        assertEquals("<message type=\"groupchat\">\n" +
                        "  <body>大家好啊，一起出来喝酒吧！</body>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group#member\">\n" +
                        "    <member nickname=\"碧眼狐狸\"/>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }

    public void testNewInstanceForMemberUpdateProfile() {
        // Act
        Message msg = MessageFactory.newInstanceForMemberUpdateProfile("user", "碧眼狐狸", "金轮法王");

        // Assert
        assertEquals("<message>\n" +
                        "  <x xmlns=\"http://skysea.com/protocol/group#member\">\n" +
                        "    <profile>\n" +
                        "      <member username=\"user\" nickname=\"碧眼狐狸\"/>\n" +
                        "      <nickname>金轮法王</nickname>\n" +
                        "    </profile>\n" +
                        "  </x>\n" +
                        "</message>",
                msg.toString().trim());
    }


}
