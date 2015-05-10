package com.skyseas.openfireplugins.group.iq.owner;

import com.skyseas.openfireplugins.group.FullMemberException;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.OwnerIQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import com.skyseas.openfireplugins.group.util.StringUtils;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;

/**
 * 申请处理程序。
 * Created by apple on 14-9-15.
 */
@XHandler(namespace = IQHandler.OWNER_NAMESPACE, elementName = "apply")
public class ApplyHandler extends OwnerIQHandler {

    @Override
    public void process(IQ packet, Group group) {
        assert group != null;
        assert packet != null;

        ApplyProcessObject processor = new ApplyProcessObject(packet, group);
        processor.execute();
    }

    private final class ApplyProcessObject {
        private final IQ packet;
        private final Group group;
        private final ApplyProcessPacket appPacket;
        private Exception exp;

        public ApplyProcessObject(IQ packet, Group group) {
            this.packet = packet;
            this.group = group;
            this.appPacket = new ApplyProcessPacket(packet.getChildElement());
        }

        public void execute() {
            if (appPacket.isAgree()) {
                if (addChatUser()) {
                    replyOK(packet);
                    notifyProposer(true);
                } else {
                    replyErrorToOwner();
                }
            } else {
                replyOK(packet);
                notifyProposer(false);
            }
        }

        private boolean addChatUser() {
            try {
                group.getChatUserManager().addUser(
                        appPacket.getUserName(),
                        StringUtils.ifNullReturnDefaultValue(
                                appPacket.getNickname(),
                                appPacket.getUserName()));
                return true;
            } catch (FullMemberException e) {
                exp = e;
                return false;
            }
        }

        private void replyErrorToOwner() {
            replyError(packet, exp instanceof FullMemberException
                            ? PacketError.Condition.service_unavailable
                            : PacketError.Condition.internal_server_error);
        }

        private void notifyProposer(boolean result) {
            /* 被拒绝时向申请者发送消息 */
            Message msg = ApplyProcessPacket.newInstanceForApplyResult(
                    result,
                    packet.getFrom().toBareJID(),
                    appPacket.getReason());
            group.send(groupService.getServer().createJID(appPacket.getUserName(), null), msg);
        }
    }


}
