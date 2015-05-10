package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

/**
 * Created by zhangzhi on 2014/10/9.
 */
public abstract class PermissionRequirementIQHandler extends GroupIQHandler implements PermissionRequirement {
    @Override
    protected Packet filter(IQContext context) {
        assert context != null;
        assert context.getPacket() != null;

        IQ packet = context.getPacket();
        Group group = context.getItem(IQContext.ITEM_GROUP);

        if(group == null || checkPermission(group, packet)){
            return null;
        }

        /* 只有圈子所有者才有权限操作 */
        packet = IQ.createResultIQ(packet);
        packet.setError(PacketError.Condition.not_authorized);
        return packet;
    }

}
