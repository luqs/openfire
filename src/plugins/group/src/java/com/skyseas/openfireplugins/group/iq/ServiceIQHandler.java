package com.skyseas.openfireplugins.group.iq;

import org.xmpp.packet.IQ;

/**
 * Created by zhangzhi on 2014/9/15.
 */
public abstract class ServiceIQHandler extends AbstractIQHandler {
    @Override
    protected void dispatch(IQContext context) {
        assert context != null;
        process(context.getPacket());
    }

    protected abstract void process(IQ packet);
}
