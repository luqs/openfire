package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import org.xmpp.packet.IQ;

/**
 * 圈子成员IQ处理程序。
 * Created by zhangzhi on 2014/10/9.
 */
public abstract class MemberIQHandler extends PermissionRequirementIQHandler {
    @Override
    public boolean checkPermission(Group group, IQ packet) {
        return group.getChatUserManager().hasUser(packet.getFrom().getNode());
    }
}
