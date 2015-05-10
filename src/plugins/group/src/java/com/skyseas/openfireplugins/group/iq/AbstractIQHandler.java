package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.GroupManager;
import com.skyseas.openfireplugins.group.GroupService;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

/**
 * Created by apple on 14-9-14.
 */
public abstract class AbstractIQHandler implements IQHandler {
    protected RoutingTable routingTable;
    protected PacketRouter packetRouter;
    protected GroupService groupService;
    protected GroupManager groupManager;

    @Override
    public void initialize(GroupService groupService) {
        this.groupService = groupService;
        XMPPServer server = groupService.getServer();
        packetRouter = server.getPacketRouter();
        groupManager = groupService.getGroupManager();
    }

    @Override
    public void process(IQContext context) {
        Packet packet = filter(context);
        if(packet == null) {
            dispatch(context);
        }else {
            routePacket(packet);
        }
    }
    protected abstract void dispatch(IQContext context);
    protected Packet filter(IQContext context){
        return null;
    }

    protected void replyError(IQ packet, PacketError.Condition condition) {
        IQ reply = IQ.createResultIQ(packet);
        reply.setError(condition);
        routePacket(reply);
    }

    protected void replyOK(IQ packet) {
        routePacket(IQ.createResultIQ(packet));
    }

    public void routePacket(Packet packet) {
        packetRouter.route(packet);
    }

    protected void handleException(Throwable throwable,  String format, Object... args) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.error(String.format(format, args), throwable);
    }

}
