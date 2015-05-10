package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.Group;
import org.xmpp.packet.IQ;

/**
 * Created by zhangzhi on 2014/9/28.
 */
public interface GroupIQDispatcher {
    void dispatch(IQ packet, Group group);
}
