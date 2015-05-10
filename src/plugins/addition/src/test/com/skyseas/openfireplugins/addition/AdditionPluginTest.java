package com.skyseas.openfireplugins.addition;

import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.container.PluginManager;

import java.io.File;

public class AdditionPluginTest extends TestCase {

    @Mocked
    PluginManager pluginManager;
    @Mocked
    ResourceServlet servlet;
    private AdditionPlugin plugin;

    @Override
    protected void setUp() {
        plugin = new AdditionPlugin();
    }

    public void testInitializePlugin() throws Exception {
        // Arrange
        final String servletUrl = "/test";
        new NonStrictExpectations(plugin) {
            {
                plugin.createServlet();
                result = servlet;
                times = 1;

                plugin.installServlet(pluginManager, servlet);
                result = servletUrl;
                times = 1;

                AuthCheckFilter.addExclude(AdditionPlugin.RESOURCE_SERVLET_PATH);
                times = 1;
            }
        };

        // Act
        plugin.initializePlugin(pluginManager, new File(""));
    }

    public void testDestroyPlugin() throws Exception {
        // Arrange
        final String servletUrl = "/test";
        new NonStrictExpectations(plugin) {
            {
                plugin.createServlet();
                result = servlet;
                times = 1;

                plugin.installServlet(pluginManager, servlet);
                result = servletUrl;
                times = 1;

                AuthCheckFilter.addExclude(AdditionPlugin.RESOURCE_SERVLET_PATH);
                times = 1;

                plugin.uninstallServlet();
                times = 1;

                AuthCheckFilter.removeExclude(AdditionPlugin.RESOURCE_SERVLET_PATH);
                times = 1;
            }
        };
        plugin.initializePlugin(pluginManager, new File(""));

        // Act
        plugin.destroyPlugin();

    }
}