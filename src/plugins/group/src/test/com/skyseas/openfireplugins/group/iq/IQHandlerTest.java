package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.ChatUserManager;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupManager;
import com.skyseas.openfireplugins.group.GroupService;
import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

/**
 * Created by apple on 14-9-14.
 */
public abstract class IQHandlerTest<T extends IQHandler> extends TestCase {
    protected final T handler;
    @Mocked protected GroupService groupService;
    @Mocked protected XMPPServer xmppServer;
    @Mocked protected PacketRouter packetRouter;
    @Mocked protected GroupManager groupManager;
    @Mocked protected SessionManager sessionManager;
    @Mocked protected Group group;
    @Mocked protected ChatUserManager userManager;

    protected IQHandlerTest(T handler) {
        this.handler = handler;
    }

    protected IQ IQ(String xml) {
        try {
            return new IQ(DocumentHelper.parseText(xml).getRootElement());
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return null;
    }
    @Override
    public void setUp() {
        new NonStrictExpectations() {
            {
                groupService.getServer();
                result = xmppServer;

                xmppServer.getPacketRouter();
                result = packetRouter;

                xmppServer.getSessionManager();
                result = sessionManager;

                groupService.getGroupManager();
                result = groupManager;

                groupService.getServiceDomain();
                result = "group.skysea.com";

                group.getJid();
                result = new JID("100@group.skysea.com");

                group.getChatUserManager();
                result = userManager;

                group.getOwner();
                result = new JID("owner@skysea.com");
            }
        };

        handler.initialize(groupService);
    }
}
