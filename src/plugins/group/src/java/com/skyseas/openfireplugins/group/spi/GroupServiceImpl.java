package com.skyseas.openfireplugins.group.spi;

import java.util.Map;
import java.util.TimerTask;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventListener;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.GroupManager;
import com.skyseas.openfireplugins.group.GroupService;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.group.CreateHandler;
import com.skyseas.openfireplugins.group.iq.group.InfoQueryHandler;
import com.skyseas.openfireplugins.group.util.StringUtils;

/**
 * Created by apple on 14-9-14.
 */
public final class GroupServiceImpl implements GroupService, Component, PropertyEventListener {
    private final static int CLEAN_TASK_PERIOD = 60 * 1000 * 5; // 5分钟一次
    private final static Logger LOG = LoggerFactory.getLogger(GroupServiceImpl.class);
    private final String serviceName;
    private final String description;
    private final XMPPServer server;
    private final IQDispatcher groupIQDispatcher;
    private IQDispatcher iqDispatcher;
    private JID jid;
    private GroupManagerImpl groupManager;
    private CleanTask cleanTask;

    public GroupServiceImpl(String serviceName, String description, XMPPServer server) {
        if (serviceName == null) {
            throw new NullPointerException("serviceName is null.");
        }
        if (server == null) {
            throw new NullPointerException("server is null.");
        }
        this.serviceName = serviceName;
        this.description = description;
        this.server = server;

        iqDispatcher = new IQDispatcher(this);
        groupIQDispatcher = new IQDispatcher(this);
        groupManager = new GroupManagerImpl(this, groupIQDispatcher);
    }

    /**
     * 获得组件名称。
     *
     * @return
     */
    @Override
    public String getName() {
        return getServiceName();
    }

    /**
     * 获得组件描述。
     *
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * 组件初始化事件
     *
     * @param jid
     * @param componentManager
     * @throws org.xmpp.component.ComponentException
     */
    @Override
    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
        this.jid = jid;

        /* 初始化服务IQ处理程序。 */
        IQDispatcher.serviceIQConfig(iqDispatcher);

        /* 初始化圈子IQ处理程序。 */
        IQDispatcher.groupIQConfig(groupIQDispatcher);

        /* 添加圈子事件监听 */
        GroupEventDispatcher.addEventListener(GroupEventBroadcastListener.INSTANCE);

        /* 内存清理任务 */
        cleanTask = new CleanTask();
    }

    /**
     * 处理组件接收的协议包。
     *
     * @param packet
     */
    @Override
    public void processPacket(Packet packet) {
        JID to = packet.getTo();

        /**
         * 如果packet目标地址是某个Group，则将packet发送到相应的Group，
         * 否则就地处理。
         */
        if (!StringUtils.isNullOrEmpty(to.getNode())) {
            processGroupPacket(to.getNode(), packet);
        } else {
            processServicePacket(packet);
        }
    }


    private void replyError(Packet packet, PacketError.Condition condition) {
        Packet reply;
        if (packet instanceof IQ) {
            reply = IQ.createResultIQ((IQ) packet);
        } else if (packet instanceof Message) {
            reply = new Message();
        } else {
            reply = new Presence();
        }
        reply.setFrom(packet.getTo());
        reply.setTo(packet.getFrom());
        reply.setError(condition);
        routePacket(reply);
    }

    private void processGroupPacket(String groupId, Packet packet) {
        Group group = groupManager.getGroup(groupId);
        if (group != null) {
        	String status = group.getGroupInfo().getStatus();
        	if("0".equals(status)){
        		//群的状态如果是0：非禁言，则想下一层路由消息
        		group.send(packet);
        	} else if(packet instanceof IQ && groupIQDispatcher.getHandler((IQ)packet) instanceof InfoQueryHandler) {
        		//群的状态是禁言，只允许查询群详情的请求通过，
        		group.send(packet);
        	} else {
        		System.out.println(packet);
        		System.out.println("====================not_allowed");
        		//其他一切请求不允许通过
        		replyError(packet, PacketError.Condition.not_allowed);
        	}
        } else {
            replyError(packet, PacketError.Condition.item_not_found);
        }
    }

    private void processServicePacket(Packet packet) {
        if (packet instanceof IQ) {
        	IQHandler iqHandler = iqDispatcher.getHandler((IQ) packet);
        	if("false".equals(JiveGlobals.getProperty(ALLOW_CREATE_GROUP,"true"))&&iqHandler instanceof CreateHandler){
        		System.out.println(packet);
        		System.out.println("====================not_allowed==============");
        		replyError(packet, PacketError.Condition.not_allowed);
        		return ;
        	}
            iqDispatcher.dispatch((IQ) packet);
        } else {
            replyError(packet, PacketError.Condition.not_acceptable);
        }
    }

    void routePacket(Packet packet) {
        server.getPacketRouter().route(packet);
    }

    @Override
    public void start() {
        /* 调度清理任务 */
        TaskEngine.getInstance().schedule(cleanTask, CLEAN_TASK_PERIOD, CLEAN_TASK_PERIOD);
        LOG.info("start.");
    }

    @Override
    public void shutdown() {
        GroupEventDispatcher.removeEventListener(GroupEventBroadcastListener.INSTANCE);
        TaskEngine.getInstance().cancelScheduledTask(cleanTask);
        LOG.info("shutdown");
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public XMPPServer getServer() {
        return server;
    }

    @Override
    public GroupManager getGroupManager() {
        return groupManager;
    }

    @Override
    public JID getGroupJid(String groupId) {
        return new JID(groupId, jid.getDomain(), null, true);
    }

    @Override
    public String getServiceDomain() {
        return jid.getDomain();
    }

    /**
     * 圈子管理器的定时清理任务。
     */
    private class CleanTask extends TimerTask {
        @Override
        public void run() {
            LOG.info("start cleaning.");
            try {
                groupManager.clean();
            } catch (Exception exp) {
                LOG.error("清理GroupManager失败", exp);
            }
        }
    }

	@Override
	public void propertySet(String property, Map<String, Object> params) {
		if(FIRE_PROPERTY_LISTENER_TMP.equals(property)){
			String value =(String)params.get("value");
			TypeToken<Map<String,String>> mapType =new TypeToken<Map<String,String>>(){};
			Map<String,String> valueMap = new Gson().fromJson(value, mapType.getType());
			if("GroupSatatusChange".equals(valueMap.get("event_name"))){
				String groupId = valueMap.get("groupid");
				String status = valueMap.get("status");
				groupManager.getGroup(groupId).getGroupInfo().setStatus(status);
			}
		}
		System.out.println(property);
	}

	@Override
	public void propertyDeleted(String property, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void xmlPropertySet(String property, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void xmlPropertyDeleted(String property, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

}
