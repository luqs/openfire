package com.skyseas.openfireplugins.push;

import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

/**
 * 默认的XMPP包发送器。
 * Created by zhangzhi on 2014/11/11.
 */
public class DefaultPacketSender implements PacketSender {
    private final PacketRouter packetRouter;
    private final RoutingTable routingTable;

    public DefaultPacketSender() {
        this(XMPPServer.getInstance().getPacketRouter(),
             XMPPServer.getInstance().getRoutingTable());
    }

    DefaultPacketSender(PacketRouter packetRouter, RoutingTable routingTable) {
        if (packetRouter == null) {
            throw new NullPointerException("packetRouter");
        }
        if (routingTable == null) {
            throw new NullPointerException("routingTable");
        }

        this.packetRouter = packetRouter;
        this.routingTable = routingTable;
    }

    @Override
    public void send(Packet packet) {

        /**
         * 如果packet收件人地址不为空，则将该packet单独路由。
         *
         * 否则，认为packet是广播消息，广播需要将packet发送至
         * 当前在线的所有客户端，并且系统只接收message类型的广播消息。
         */
        if (packet.getTo() != null) {
            route(packet);
        } else {
            if (packet instanceof Message) {
                broadcast((Message) packet);
            }else {
                throw new IllegalArgumentException("broadcast packet must be message.");
            }
        }
    }

    private void broadcast(Message packet) {
        routingTable.broadcastPacket((Message) packet, true);
    }

    private void route(Packet packet) {
        packetRouter.route(packet);
    }
}
