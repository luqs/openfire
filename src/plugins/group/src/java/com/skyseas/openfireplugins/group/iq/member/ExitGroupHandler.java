package com.skyseas.openfireplugins.group.iq.member;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.ChatUserManager;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.iq.GroupIQHandler;
import com.skyseas.openfireplugins.group.util.HasReasonPacket;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import com.skyseas.openfireplugins.group.util.StringUtils;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

/**
 * 用户退出圈子处理程序。
 * Created by zhangzhi on 2014/9/15.
 */
@XHandler(namespace = IQHandler.MEMBER_NAMESPACE, elementName = "exit")
public class ExitGroupHandler extends GroupIQHandler {
    @Override
    protected void process(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        /* 退出者必须是某个用户所以需要携带node */
        String exitUserName = packet.getFrom().getNode();
        if(StringUtils.isNullOrEmpty(exitUserName)) {
            replyError(packet, PacketError.Condition.bad_request);
            return;
        }

        /* 圈子所有者不能退出圈子 */
        if(group.getOwner().equals(packet.getFrom().asBareJID())){
            replyError(packet, PacketError.Condition.not_allowed);
            return;
        }

        ExitGroupPacket exitGroupPacket = new ExitGroupPacket(packet.getChildElement());
        ChatUser user;
        try {

            /*从聊天用户管理器删除用户 */
            user = group.getChatUserManager().removeUser(
                    ChatUserManager.RemoveType.EXIT,
                    exitUserName,
                    packet.getFrom(),
                    exitGroupPacket.getReason());
        }catch (Exception exp) {
            handleException(exp, "用户退出圈子失败,GroupId:%s, UserName:%s", group.getId(), exitUserName);
            replyError(packet, PacketError.Condition.internal_server_error);
            return;
        }

        if(user != null) {
            replyOK(packet);
        }else {
            replyError(packet, PacketError.Condition.not_authorized);
        }
    }

    private static class ExitGroupPacket extends HasReasonPacket {
        public ExitGroupPacket(Element element) {
            super(element, "exit");
        }
    }

}
