package com.skyseas.openfireplugins.group.spi;

import junit.framework.TestCase;
import org.xmpp.packet.JID;

public class ChatUserImplTest extends TestCase {

    private ChatUserImpl chatUser;
    private JID userJid;

    @Override
    protected void setUp() {
        userJid = new JID("user@skysea.com");
        chatUser = new ChatUserImpl("user", "张三", userJid);
    }

    public void testConstructor() throws Exception {
        // Assert
        assertEquals("user", chatUser.getUserName());
        assertEquals("张三", chatUser.getNickname());
        assertEquals(userJid, chatUser.getJid());
    }

    public void testChange() throws  Exception{
        // Act
        chatUser.setNickname("喵咪");

        // Assert
        assertEquals("喵咪", chatUser.getNickname());
    }


}