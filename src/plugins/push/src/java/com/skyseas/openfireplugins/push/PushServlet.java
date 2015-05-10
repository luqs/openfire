package com.skyseas.openfireplugins.push;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * 推送XMPP消息的servlet。
 * Created by zhangzhi on 2014/11/11.
 */
public class PushServlet extends HttpServlet {
    private final static Logger LOGGER                  = LoggerFactory.getLogger(PushServlet.class);
    public static String PACKET_CONTENT_PARAMETER_NAME  = "packet_content";
    public final static String ALLOW_IP_LIST_KEY        = "push.allow_ip_list";
    private final PacketSender sender;

    public PushServlet() { this(new DefaultPacketSender()); }
    PushServlet(PacketSender sender) {
        this.sender = sender;
    }

    /**
     * 调用API之前需要先验证，客户端是否有权调用。
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(doCheck(request)) {
            super.service(request, response);
        }else {
            finish(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized");
        }
    }

    /**
     * 检测客户端是否有权限访问。
     * @param request
     * @return
     */
    protected boolean doCheck(HttpServletRequest request) {
        String ip       = request.getRemoteAddr();
        String allowIps = JiveGlobals.getProperty(ALLOW_IP_LIST_KEY);
        boolean allow   = false;

        if(allowIps != null) {
            /* 例如: 192.168.1.102;192.168.1.104 */
            StringTokenizer tokenizer = new StringTokenizer(allowIps, ";");
            while (tokenizer.hasMoreTokens()) {
                if(ip.equals(tokenizer.nextToken())) {
                    allow = true;
                    break;
                }
            }
        }
        return allow;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /**
         * 接收来自客户端的post请求，
         * 从请求中取得XMPP数据包，并将数据包通过PacketSender发送出去。
         */
        String rawPacket    = getRawPacketContent(request);
        Packet packet       = createPacket(rawPacket);

        if (packet != null && sendPacket(packet)) {
            finish(response, HttpServletResponse.SC_OK, "ok");
        } else {
            finish(response, HttpServletResponse.SC_BAD_REQUEST,
                    "invalid xmpp data.");
        }
    }

    /**
     * 获得原始的XMPP包内容。
     *
     * @param request
     * @return
     */
    private String getRawPacketContent(HttpServletRequest request) {
        String packetContent = request.getParameter(PACKET_CONTENT_PARAMETER_NAME);
        return packetContent != null ? packetContent.trim() : null;
    }

    /**
     * 用原始的packet xml文本创建包对象实例。
     *
     * @param rawPacket
     * @return
     */
    private Packet createPacket(String rawPacket) {
        if (rawPacket == null) { return null; }

        Element element;
        try {
            /* 将内容主题分析为XML元素 */
            element = DocumentHelper.parseText(rawPacket).getRootElement();
        } catch (DocumentException e) {
            LOGGER.error("parse xml fail", e);
            return null;
        }

        if ("message".equals(element.getName())) {
            return new Message(element);
        } else if ("iq".equals(element.getName())) {
            return new IQ(element);
        } else if ("presence".equals(element.getName())) {
            return new Presence(element);
        } else {
            return null;
        }
    }

    /**
     * 完成当前请求，并设置响应状态信息。
     *
     * @param response
     * @param status
     * @param message
     * @throws IOException
     */
    protected void finish(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        if (message != null) {
            try {
                response.getWriter().write(message);
                response.getWriter().flush();
            } finally {
                response.getWriter().close();
            }
        }
    }

    /**
     * 发送数据包。
     *
     * @param packet
     * @return
     */
    private boolean sendPacket(Packet packet) {
        try {
            sender.send(packet);
            return true;
        } catch (Exception exp) {
            LOGGER.error("send packet fail.", packet);
        }
        return false;
    }
}
