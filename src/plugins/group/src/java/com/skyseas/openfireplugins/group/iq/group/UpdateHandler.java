package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.OwnerIQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

/**
 * 圈子更新处理程序。
 * Created by apple on 14-9-14.
 */
@XHandler(namespace = IQHandler.GROUP_NAMESPACE, elementName = "x")
public class UpdateHandler extends OwnerIQHandler {
    @Override
    public void process(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        GroupInfo updateInfo = GroupInfoPacket.getGroupInfo(packet);
        if (updateInfo != null) {
            if (group.updateGroupInfo(packet.getFrom(), updateInfo)) {
                replyOK(packet);
            } else {
                replyError(packet, PacketError.Condition.internal_server_error);
            }
        } else {
            replyError(packet, PacketError.Condition.bad_request);
        }


    }
}
