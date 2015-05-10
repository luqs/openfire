package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

/**
 * Created by zhangzhi on 2014/9/15.
 */
public abstract class OwnerIQHandler extends PermissionRequirementIQHandler {
    @Override
    public boolean checkPermission(Group group, IQ packet) {
        return group.getOwner().equals(packet.getFrom().asBareJID());
    }
}
