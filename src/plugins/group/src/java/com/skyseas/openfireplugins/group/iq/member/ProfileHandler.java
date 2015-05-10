package com.skyseas.openfireplugins.group.iq.member;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.iq.GroupIQHandler;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import com.skyseas.openfireplugins.group.util.ModelPacket;
import com.skyseas.openfireplugins.group.util.StringUtils;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

/**
 * 用户个人信息处理程序。
 * Created by zhangzhi on 2014/9/15.
 */
@XHandler(namespace = IQHandler.MEMBER_NAMESPACE, elementName = "profile")
public class ProfileHandler extends GroupIQHandler {
    @Override
    protected void process(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        String userName = packet.getFrom().getNode();
        String newNickname = new ProfilePacket(packet.getChildElement()).getNickname();

        if(StringUtils.isNullOrEmpty(newNickname)) {
            replyError(packet, PacketError.Condition.bad_request);
            return;
        }

        ChatUser user = group.getChatUserManager().getUser(userName);
        if(user == null) {
            replyError(packet, PacketError.Condition.not_authorized);
            return;
        }

        if(changeNickname(group, userName, newNickname)) {
            replyOK(packet);
        }else {
            replyError(packet, PacketError.Condition.internal_server_error);
        }
    }

    private boolean changeNickname(Group group, String userName, String newNickname) {
        try {
            group.getChatUserManager().changeNickname(userName, newNickname);
            return true;
        }catch (Exception exp) {
            handleException(exp, "修改用户昵称失败，GroupId:%s, UserName:%s, NewNickname:%s",
                    group.getId(),
                    userName,
                    newNickname);
            return false;
        }
    }

    private static class ProfilePacket extends ModelPacket {
        public ProfilePacket(Element element) {
            super(element, "profile");
        }
        public String getNickname() {
            return getElementValue("nickname", null);
        }
    }
}
