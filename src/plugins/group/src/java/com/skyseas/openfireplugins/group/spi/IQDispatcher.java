package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupService;
import com.skyseas.openfireplugins.group.iq.*;
import com.skyseas.openfireplugins.group.iq.group.*;
import com.skyseas.openfireplugins.group.iq.member.ExitGroupHandler;
import com.skyseas.openfireplugins.group.iq.group.GroupsHandler;
import com.skyseas.openfireplugins.group.iq.member.InviteHandler;
import com.skyseas.openfireplugins.group.iq.member.ProfileHandler;
import com.skyseas.openfireplugins.group.iq.owner.ApplyHandler;
import com.skyseas.openfireplugins.group.iq.owner.DestroyHandler;
import com.skyseas.openfireplugins.group.iq.owner.KickHandler;
import com.skyseas.openfireplugins.group.iq.user.ApplyJoinGroupHandler;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by apple on 14-9-14.
 */
final class IQDispatcher implements GroupIQDispatcher {
    private Logger LOG = LoggerFactory.getLogger(IQDispatcher.class);
    private final HashMap<StringPair, IQHandler> queryHandlers = new HashMap<StringPair, IQHandler>();
    private final HashMap<StringPair, IQHandler> xHandlers = new HashMap<StringPair, IQHandler>();
    private final GroupService groupService;

    public IQDispatcher(GroupService groupService) {
        assert groupService != null;
        this.groupService = groupService;
    }

    /**
     * 安装一个IQHandler。
     * @param klass
     */
    public void installHandler(Class<?> klass) {
        if (klass == null) {
            throw new NullPointerException("klass");
        }

        if (!IQHandler.class.isAssignableFrom(klass)) {
            throw new IllegalArgumentException("class is not IQHandler.");
        }

        if (!tryAddHandler(klass)) {
            throw new IllegalArgumentException("IQHandler lack of definition");
        }
    }

    /**
     * 将IQ请求分派到IQHandler进行处理。
     * @param packet
     */
    public void dispatch(IQ packet) {
        assert packet != null;

        IQHandler handler = getHandler(packet);
        if (handler != null) {
            process(handler, packet);
        } else {
            /* 回复客户端IQ不被接受 */
            replyNoAcceptable(packet);
        }
    }

    /**
     * 将特定圈子的IQ请求分派到IQHandler进行处理。
     * @param packet
     * @param group
     */
    @Override
    public void dispatch(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        IQHandler handler = getHandler(packet);
        if (handler != null) {
            process((GroupIQHandler) handler, packet, group);
        } else {
            /* 回复客户端IQ不被接受 */
            replyNoAcceptable(packet);
        }
    }

    /**
     * 获得所有IQHandler列表。
     * @return
     */
    Collection<IQHandler> getHandlers() {
        int size = queryHandlers.size() + xHandlers.size();
        ArrayList<IQHandler> handlers = new ArrayList<IQHandler>(size);
        handlers.addAll(queryHandlers.values());
        handlers.addAll(xHandlers.values());
        return handlers;
    }


    public IQHandler getHandler(IQ packet) {
        Element extElement = packet.getChildElement();
        if (extElement != null) {
            String extName = extElement.getName();
            if ("query".equals(extName)) {
                return getQueryHandler(packet, extElement);
            } else if ("x".equals(extName)) {
                return getXHandler(packet, extElement);
            }
        }
        return null;
    }


    private boolean tryAddHandler(Class<?> klass) {
        XHandler        xDesc   = klass.getAnnotation(XHandler.class);
        QueryHandler    qDesc   = klass.getAnnotation(QueryHandler.class);
        StringPair      key;

        if (xDesc != null) {
            key = new StringPair(xDesc.namespace(), xDesc.elementName());
            return addHandler(xHandlers, key, klass);
        } else if (qDesc != null) {
            key = new StringPair(qDesc.namespace(), qDesc.node());
            return addHandler(queryHandlers, key, klass);
        }
        return false;
    }

    private boolean addHandler(HashMap<StringPair, IQHandler> handlers, StringPair key, Class<?> klass) {
        IQHandler handler = createAndInitHandler(klass);
        if (handler != null) {
            handlers.put(key, handler);
            return true;
        }
        return false;
    }


    protected void process(IQHandler handler, IQ packet) {
        handler.process(new IQContext(packet));
    }

    protected void process(IQHandler handler, IQ packet, Group group) {
        IQContext context = new IQContext(packet);
        context.setItem(IQContext.ITEM_GROUP, group);
        handler.process(context);
    }

    protected IQHandler createAndInitHandler(Class<?> klass) {
        IQHandler handler;
        try {
            handler = (IQHandler) klass.newInstance();
        } catch (Exception e) {
            LOG.error("创建Handler实例失败 class:" + klass, e);
            throw new IllegalArgumentException("IQHandler class invalid.", e);
        }

        if (handler != null) {
            try {
                handler.initialize(groupService);
            } catch (Exception e) {
                LOG.error("初始化Handler失败，class:" + handler.getClass(), e);
                return null;
            }
        }
        return handler;
    }

    private IQHandler getXHandler(IQ packet, Element extElement) {
        String firstElementName = null;
        if (extElement.elements().size() > 0) {
            firstElementName = ((Element) extElement.elements().get(0)).getName();
        }
        StringPair key = new StringPair(extElement.getNamespaceURI(), firstElementName);
        return xHandlers.get(key);
    }

    private IQHandler getQueryHandler(IQ packet, Element extElement) {
        StringPair key = new StringPair(
                extElement.getNamespaceURI(),
                extElement.attributeValue("node"));
        return queryHandlers.get(key);
    }

    private void replyNoAcceptable(IQ packet) {
        IQ replyError = IQ.createResultIQ(packet);
        replyError.setError(PacketError.Condition.not_acceptable);
        groupService.getServer().getPacketRouter().route(replyError);
    }


    /**
     * 服务IQ分配器配置。
     * @param dispatcher
     */
    public static void serviceIQConfig(IQDispatcher dispatcher) {
        assert dispatcher != null;

        dispatcher.installHandler(CreateHandler.class);
        dispatcher.installHandler(SearchHandler.class);
        dispatcher.installHandler(GroupsHandler.class);
    }

    /**
     * 圈子IQ分派器配置。
     * @param dispatcher
     */
    public static void groupIQConfig(IQDispatcher dispatcher) {
        assert dispatcher != null;

        // Group
        dispatcher.installHandler(InfoQueryHandler.class);
        dispatcher.installHandler(MembersQueryHandler.class);
        dispatcher.installHandler(UpdateHandler.class);

        // Member
        dispatcher.installHandler(ExitGroupHandler.class);
        dispatcher.installHandler(ProfileHandler.class);
        dispatcher.installHandler(InviteHandler.class);

        // Owner
        dispatcher.installHandler(ApplyHandler.class);
        dispatcher.installHandler(DestroyHandler.class);
        dispatcher.installHandler(KickHandler.class);

        // User
        dispatcher.installHandler(ApplyJoinGroupHandler.class);
    }


    /**
     * 字符串对
     */
    private final static class StringPair {
        private final String str1;
        private final String str2;

        public StringPair(String str1, String str2) {
            this.str1 = str1 == null ? "" : str1;
            this.str2 = str2 == null ? "" : str2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof StringPair) {
                StringPair other = (StringPair) obj;

                return
                        str1.equals(other.str1) &&
                        str2.equals(other.str2);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = result * 31 + str1.hashCode();
            result = result * 31 + str2.hashCode();
            return result;
        }
    }
}
