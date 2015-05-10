package com.skyseas.openfireplugins.group.iq.owner;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.ChatUserManager;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.util.HasReasonPacket;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.OwnerIQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

/**
 * 踢出成员处理程序。
 * Created by zhangzhi on 2014/9/9.
 */
@XHandler(namespace = IQHandler.OWNER_NAMESPACE, elementName = "kick")
public class KickHandler extends OwnerIQHandler {
    @Override
    public void process(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        KickPacket kickPacket = new KickPacket(packet.getChildElement());

        /* 所有者不能将自己踢出 */
        if (group.getOwner().getNode().equals(kickPacket.getUserName())) {
            replyError(packet, PacketError.Condition.not_allowed);
            return;
        }

        /* 从聊天用户管理器将用户删除 */
        ChatUser user = group.getChatUserManager().removeUser(
                ChatUserManager.RemoveType.KICK,
                kickPacket.getUserName(),
                packet.getFrom(),
                kickPacket.getReason());

        if (user != null) {
            replyOK(packet);
        } else {
            replyError(packet, PacketError.Condition.internal_server_error);
        }
    }

    private static class KickPacket extends HasReasonPacket {
        public KickPacket(Element element) {
            super(element, "kick");
        }

        public String getUserName() {
            return modeElement.attributeValue("username", null);
        }
    }
}
