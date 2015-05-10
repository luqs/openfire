package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.iq.AbstractIQHandler;
import com.skyseas.openfireplugins.group.iq.IQContext;
import org.xmpp.packet.IQ;

/**
 * Created by apple on 14-9-14.
 */
public abstract class GroupIQHandler extends AbstractIQHandler {
    @Override
    protected void dispatch(IQContext context) {
        assert context != null;

        Group group = context.getItem(IQContext.ITEM_GROUP);
        if(group != null) {
            process(context.getPacket(), group);
        }else {
            throw new IllegalArgumentException("group is null");
        }
    }

    protected abstract void process(IQ packet , Group group);
}

