package com.skyseas.openfireplugins.group;

import com.skyseas.openfireplugins.group.iq.IQHandler;
import org.dom4j.Element;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
* Created by zhangzhi on 2014/9/10.
*/
public class MessageFactory {

    public static Message newInstanceForMemberJoin(String userName, String nickname) {
       return new MessageBuilder(ActionType.JOIN)
                .setUserName(userName)
                .setNickName(nickname)
                .build();
    }


    public static Message newInstanceForGroupDestroy(JID operator, String reason) {
        return new MessageBuilder(ActionType.DESTROY)
                .setReason(reason)
                .setOperatorFrom(operator.asBareJID())
                .setNamespace(IQHandler.GROUP_NAMESPACE)
                .build();
    }

    public static Message newInstanceForMemberExit(String userName, String nickname, String reason) {
        return new MessageBuilder(ActionType.EXIT)
                .setUserName(userName)
                .setNickName(nickname)
                .setReason(reason)
                .build();
    }

    public static Message newInstanceForMemberKick(String userName, String nickname, JID operator, String reason) {
        return new MessageBuilder(ActionType.KICK)
                .setUserName(userName)
                .setNickName(nickname)
                .setReason(reason)
                .setOperatorFrom(operator)
                .build();
    }

    public static Message newInstanceForGroupChat(Message message, String nickName) {
       return new MessageBuilder(message, ActionType.MESSAGE)
                .setNickName(nickName)
                .build();
    }

    public static Message newInstanceForMemberUpdateProfile(String userName, String nickname, String newNickname) {
        Message msg = new MessageBuilder(ActionType.PROFILE)
                .setUserName(userName)
                .setNickName(nickname)
                .build();

        Element profile = msg.getChildElement("x", IQHandler.MEMBER_NAMESPACE);
        profile.element("profile").addElement("nickname").setText(newNickname);
        return msg;
    }

    public static Message newInstanceForGroupInfoChange(JID ownerJid) {
        return new MessageBuilder(ActionType.CHANGE)
                .setOperatorFrom(ownerJid)
                .setNamespace(IQHandler.GROUP_NAMESPACE)
                .build();
    }


    private enum ActionType{
        JOIN("join"), EXIT("exit"), KICK("kick"),DESTROY("destroy"), PROFILE("profile"), CHANGE("change"), MESSAGE("");

        private final String eleName;
        ActionType(String eleName) {
            this.eleName = eleName;
        }
    }

    /**
     * Message生成器
     */
    private static class MessageBuilder {

        private final ActionType type;
        private final Message msg;
        private String userName;
        private String nickName;
        private JID memJid;
        private String reason;
        private JID operatorFrom;
        private String namespace = IQHandler.MEMBER_NAMESPACE;

        public MessageBuilder(Message msg, ActionType type){
            assert  msg != null;
            assert  type != null;

            this.msg = msg;
            this.type = type;
        }

        public MessageBuilder(ActionType type) {
            this(new Message(), type);
        }

        public MessageBuilder setFrom(JID from) {
            this.msg.setFrom(from);
            return this;
        }

        public MessageBuilder setUserName(String userName){
            this.userName = userName;
            return this;
        }

        public MessageBuilder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public MessageBuilder setNickName(String nickName){
            this.nickName = nickName;
            return this;
        }

        public MessageBuilder setJid(JID jid){
            this.memJid = jid;
            return this;
        }

        public MessageBuilder setOperatorFrom(JID from) {
            this.operatorFrom = from;
            return this;
        }

        public MessageBuilder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }
        public Message build() {

            Element extElement = msg.addChildElement("x", namespace);

            if(type.eleName.length() > 0) {
                extElement = extElement.addElement(type.eleName);
            }

            if(operatorFrom != null) {
                extElement.addAttribute("from", operatorFrom.toString());
            }

            Element memElement = extElement.addElement("member");
            if(userName != null) {
                memElement.addAttribute("username", userName);
            }

            if(nickName != null) {
                memElement.addAttribute("nickname", nickName);
            }

            if(memJid != null) {
                memElement.addAttribute("jid", memJid.toString());
            }

            if(memElement.attributeCount() < 1) {
                extElement.remove(memElement);
            }

            if(reason != null) {
                extElement.addElement("reason").setText(reason);
            }

            if(type == ActionType.MESSAGE) {
                msg.setType(Message.Type.groupchat);
            }

            return msg;
        }


    }
}
