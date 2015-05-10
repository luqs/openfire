package com.skyseas.openfireplugins.addition;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.container.PluginServlet;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by zhangzhi on 2014/11/21.
 */
public class AdditionPlugin implements Plugin {
    private static Logger LOGGER = LoggerFactory.getLogger(AdditionPlugin.class);
    public final static String RESOURCE_BASEPATH = "resource.basepath";
    public final static String PLUGIN_NAME = "addition";
    final static String RESOURCE_SERVLET_REL_PATH = "/resource";
    final static String RESOURCE_SERVLET_PATH = "/" + PLUGIN_NAME + RESOURCE_SERVLET_REL_PATH;
    private ResourceServlet servlet;
    private String servletUrl;

    @Override
    public void initializePlugin(PluginManager pluginManager, File file) {
        ResourceServlet ser = createServlet();
        if (ser != null) {
            try {
                this.servletUrl = installServlet(pluginManager, ser);
                this.servlet = ser;

            /* 将servlet排除在认证检测之外，这样http服务器才能直接访问 */
                AuthCheckFilter.addExclude(RESOURCE_SERVLET_PATH);
            } catch (ServletException e) {
                LOGGER.error("register servlet fail", e);
            }
        }
    }

    @Override
    public void destroyPlugin() {
        if (servlet != null) {
            try {
                uninstallServlet();
            } catch (ServletException e) {
                LOGGER.error("unregister servlet fail", e);
            } finally {
                AuthCheckFilter.removeExclude(RESOURCE_SERVLET_PATH);
            }
        }
    }

    String installServlet(PluginManager pluginManager, ResourceServlet servlet) throws ServletException {
        return PluginServlet.registerServlet(pluginManager, this, servlet, RESOURCE_SERVLET_REL_PATH);
    }

    void uninstallServlet() throws ServletException {
        PluginServlet.unregisterServlet(this, RESOURCE_SERVLET_PATH);
    }

    ResourceServlet createServlet() {
        File basePath = new File(JiveGlobals.getProperty(RESOURCE_BASEPATH, ""));
        LOGGER.info("basePath:{}", basePath);
        try {
            return new ResourceServlet(new DefaultResourceStorage(basePath));
        } catch (Exception exp) {
            LOGGER.error("initialize storage fail. basePath: " + basePath);
            return null;
        }
    }

    public String getServletUrl() {
        return servletUrl;
    }
}
