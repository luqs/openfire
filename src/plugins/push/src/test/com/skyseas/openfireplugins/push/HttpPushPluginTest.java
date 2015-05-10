package com.skyseas.openfireplugins.push;

import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.container.PluginServlet;

import java.io.File;

public class HttpPushPluginTest extends TestCase {
    private HttpPushPlugin plugin;
    @Mocked PushServlet pushServlet;
    @Mocked PluginManager pluginManager;

    @Override
    protected void setUp() {
        plugin = new HttpPushPlugin();
        new NonStrictExpectations(PluginServlet.class) {
            {
                pluginManager.getPluginDirectory(plugin);
                result = new File("/push");
            }
        };
    }

    public void testInitializePlugin() throws Exception {

        // Arrange
        new NonStrictExpectations(AuthCheckFilter.class){ };
        new NonStrictExpectations(PluginServlet.class){ };

        // Act
        plugin.initializePlugin(pluginManager, null);

        // Assert
        new Verifications(){
            {
                PluginServlet.registerServlet(
                        pluginManager,
                        plugin,
                        withAny((PushServlet)null),
                        HttpPushPlugin.PUSH_SERVLET_REL_PATH);
                times = 1;

                AuthCheckFilter.addExclude(plugin.PLUGIN_NAME + "/packet");
                times = 1;
            }
        };
    }

    public void testDestroyPlugin() throws Exception {
        // Arrange
        new NonStrictExpectations(AuthCheckFilter.class){};
        plugin.initializePlugin(pluginManager, null);

        // Act
        plugin.destroyPlugin();

        // Assert
        new Verifications(){
            {
                PluginServlet.unregisterServlet(plugin, HttpPushPlugin.PUSH_SERVLET_REL_PATH);
                times = 1;

                AuthCheckFilter.removeExclude(plugin.PLUGIN_NAME + "/packet");
                times = 1;
            }
        };
    }
}