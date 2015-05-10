package com.skyseas.openfireplugins.group;

import com.skyseas.openfireplugins.group.spi.GroupServiceImpl;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManagerFactory;

import java.io.File;

/**
 * 圈子插件。
 * Created by zhangzhi on 2014/9/30.
 */
public final class GroupPlugin implements Plugin {
    private final static Logger LOG = LoggerFactory.getLogger(GroupPlugin.class);
    private GroupServiceImpl groupService;
    @Override
    public void initializePlugin(PluginManager pluginManager, File file) {
        String serviceName = JiveGlobals.getProperty("group.servicename", "group");
        GroupServiceImpl service = createService(serviceName);
        PropertyEventDispatcher.addListener(service);
        installService(service);
    }

    private GroupServiceImpl createService(String serviceName) {
        return new GroupServiceImpl(serviceName,
                "this is skysea multi user chat service.",
                XMPPServer.getInstance());
    }

    @Override
    public void destroyPlugin() {
        if(groupService != null) {
            uninstallService();
        }
    }

    private void installService(GroupServiceImpl groupService) {
        try {
            ComponentManagerFactory.getComponentManager().addComponent(groupService.getServiceName(), groupService);
            this.groupService = groupService;
        } catch (ComponentException e) {
            LOG.error("安装GroupService到组件管理器失败。", e);
        }
    }
    private void uninstallService() {
        try {
            ComponentManagerFactory.getComponentManager().removeComponent(groupService.getServiceName());
            PropertyEventDispatcher.removeListener(groupService);
        } catch (ComponentException e) {
            LOG.error("从组件管理器卸载GroupService失败。", e);
        }
    }
}
