package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import org.xmpp.packet.IQ;

/**
 * Created by apple on 14-9-15.
 */
public interface PermissionRequirement {
    boolean checkPermission(Group group, IQ packet);
}
