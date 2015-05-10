package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.ChatUserManager;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.iq.group.MockChatUser;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class ApplyStrategyTest extends TestCase {
    @Mocked Group group;
    @Mocked
    ChatUserManager userManager;
    public static final JID PROPOSER = new JID("user@skysea.com");
    public static final String NICKNAME = "碧眼狐狸";
    public static final String REASON = "我也是80后啊";

    @Override
    public void setUp() {
        new NonStrictExpectations() {
            {
                group.getId();
                result = "100";

                group.getJid();
                result = new JID("100@group.skysea.com");

                group.getOwner();
                result = new JID("owner@skysea.com");

                group.getChatUserManager();
                result = userManager;
            }
        };
    }

    public void test_Apply_IMMEDIATE_PROCESS() throws Exception {
        // Arrange
        new NonStrictExpectations() {
            {
                userManager.addUser(PROPOSER.getNode(), NICKNAME);
                result = new MockChatUser(PROPOSER.getNode(), NICKNAME);
                times = 1;

            }
        };

        // Act
        ApplyStrategy.IMMEDIATE_PROCESS.applyToJoin(group, PROPOSER, NICKNAME, REASON);

        // Assert
        new Verifications() {
            {
                group.send(PROPOSER, with(new Delegate<Message>() {
                    public void validate(Message msg) {
                        assertEquals("<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#user\">" +
                                        "<apply><agree from=\"100@group.skysea.com\"/>" +
                                        "</apply></x>" +
                                        "</message>",
                                msg.toXML().toString());
                    }
                }));
                times = 1;
            }
        };
    }

    public void test_Apply_FORWARDING_TO_OWNER() throws Exception {
        // Arrange
        final String groupId = group.getId();
        new NonStrictExpectations(ApplyStrategy.FORWARDING_TO_OWNER) {
            {

                invoke(ApplyStrategy.FORWARDING_TO_OWNER, "createApplyTrans", PROPOSER.getNode(), groupId);
                result = "transId_ok";
            }
        };

        // Act
        ApplyStrategy.FORWARDING_TO_OWNER.applyToJoin(group, PROPOSER, NICKNAME, REASON);

        // Assert
        new Verifications() {
            {
                group.send(group.getOwner(), with(new Delegate<Message>() {
                    public void validate(Message msg) {
                        assertEquals("<message>" +
                                        "<x xmlns=\"http://skysea.com/protocol/group#owner\">" +
                                        "<apply id=\"transId_ok\">" +
                                        "<member username=\"user\" nickname=\"碧眼狐狸\"/>" +
                                        "<reason>我也是80后啊</reason>" +
                                        "</apply></x>" +
                                        "</message>",
                                msg.toXML().toString());
                    }
                }));
                times = 1;
            }
        };
    }


}