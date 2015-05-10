package com.skyseas.openfireplugins.push;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.container.PluginServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;

/**
 * openfire消息的HTTP推送插件。
 * Created by zhangzhi on 2014/11/11.
 */
public class HttpPushPlugin implements Plugin {
    public final static String PLUGIN_NAME          = "push";
    final static String PUSH_SERVLET_REL_PATH = "/packet";
    final static String PUSH_SERVLET_PATH           = "/" + PLUGIN_NAME + PUSH_SERVLET_REL_PATH;
    private static Logger LOGGER = LoggerFactory.getLogger(HttpPushPlugin.class);
    private PushServlet servlet;
    private String servletUrl;

    @Override
    public void initializePlugin(PluginManager pluginManager, File file) {
        PushServlet ser = createServlet();

        try {
            /* 注册push servlet实例到当前插件路径下 */
            this.servletUrl             = PluginServlet.registerServlet(pluginManager, this, ser, PUSH_SERVLET_REL_PATH);
            this.servlet                = ser;

            /* 将push servlet排除在认证检测之外，这样http服务器才能直接访问 */
            AuthCheckFilter.addExclude(PUSH_SERVLET_PATH);
        } catch (ServletException e) {
            LOGGER.error("register servlet fail", e);
        }
    }

    @Override
    public void destroyPlugin() {
        if (servlet != null) {
            try {
                /* 插件销毁时，卸载注册的servlet */
                PluginServlet.unregisterServlet(this, PUSH_SERVLET_REL_PATH);
                servlet = null;
            } catch (ServletException e) {
                LOGGER.error("unregister servlet fail", e);
            } finally {
                    AuthCheckFilter.removeExclude(PUSH_SERVLET_PATH);
            }
        }
    }

    /**
     * 获得推送servlet的url地址。
     * @return
     */
    public String getServletUrl() {
        return servletUrl;
    }

    PushServlet createServlet() {
        return new PushServlet();
    }


}
