package com.skyseas.openfireplugins.group.iq.member;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.FullMemberException;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupMemberInfo;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.MemberIQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import com.skyseas.openfireplugins.group.util.ModelPacket;
import com.skyseas.openfireplugins.group.util.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;

import java.util.ArrayList;
import java.util.List;

/**
 * 邀请处理程序。
 * Created by zhangzhi on 2014/10/9.
 */
@XHandler(namespace = IQHandler.MEMBER_NAMESPACE, elementName = "invite")
public class InviteHandler extends MemberIQHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(InviteHandler.class);

    @Override
    protected void process(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        InvitePacket invPacket = new InvitePacket(packet.getChildElement());
        List<GroupMemberInfo> newMembers = invPacket.getMembers();

        if (newMembers.size() < 1) {
            replyError(packet, PacketError.Condition.bad_request);
            return;
        }

        List<ChatUser> addedUsers = addUsers(group, newMembers);
        if (addedUsers != null) {
            replyOK(packet);
            /* 广播邀请消息 */
            broadcastInviteMessage(group, packet.getFrom().toBareJID(), addedUsers);
        } else {
            replyError(packet, PacketError.Condition.internal_server_error);
        }

    }

    private void broadcastInviteMessage(final Group group, final String from, final List<ChatUser> newMembers) {
        if (newMembers.size() > 0) {
            TaskEngine.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    Message msg = createInviteMessage(from, newMembers);
                    group.broadcast(msg);
                }
            });
        }
    }

    /**
     * <message from='100@group.skysea.com' to='user1@skysea.com'>
     * <x xmlns='http://skysea.com/protocol/group#member'>
     * <invite from='user@skysea.com'>
     * <member username='user100' nickname='独孤求败' />
     * <member username='user101' nickname='雁过留声' />
     * <member username='user102' nickname='圆月弯刀' />
     * </invite>
     * </x>
     * </message>
     *
     * @return
     */
    private Message createInviteMessage(String from, List<ChatUser> users) {
        Message msg = new Message();
        Element invElement = msg
                .addChildElement("x", IQHandler.MEMBER_NAMESPACE)
                .addElement("invite")
                .addAttribute("from", from);

        for (ChatUser user : users) {
            invElement.addElement("member")
                    .addAttribute("username", user.getUserName())
                    .addAttribute("nickname", user.getNickname());
        }

        return msg;
    }

    private List<ChatUser> addUsers(Group group, List<GroupMemberInfo> newMembers) {
        try {
            return group.getChatUserManager().addUsers(newMembers);
        } catch (FullMemberException exp) {
            LOGGER.error("invite members buf group full member.", exp);
        } catch (Throwable throwable) {
            LOGGER.error("add Users fail.", throwable);
        }
        return null;
    }


    private static class InvitePacket extends ModelPacket {
        public InvitePacket(Element element) {
            super(element, "invite");
        }

        public List<GroupMemberInfo> getMembers() {
            List<Element> memberElements = modeElement.elements("member");
            ArrayList<GroupMemberInfo> members = new ArrayList<GroupMemberInfo>(memberElements.size());

            for (Element memElement : memberElements) {
                String userName = memElement.attributeValue("username");
                String userNickname = memElement.attributeValue("nickname");

                if (!StringUtils.isNullOrEmpty(userName)) {
                    members.add(new GroupMemberInfo(
                            userName,
                            StringUtils.ifNullReturnDefaultValue(userNickname, userName)));
                }
            }
            return members;
        }
    }
}

