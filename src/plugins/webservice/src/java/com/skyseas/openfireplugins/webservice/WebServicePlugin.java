package com.skyseas.openfireplugins.webservice;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.server.http.XFireHttpServer;
import org.codehaus.xfire.service.Endpoint;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * openfire消息的HTTP推送插件。 Created by zhangzhi on 2014/11/11.
 */
public class WebServicePlugin implements Plugin {
	private XFireHttpServer httpServer;
	private static Logger LOGGER = LoggerFactory
			.getLogger(WebServicePlugin.class);

	@Override
	public void initializePlugin(PluginManager pluginManager, File file) {
		try {
//			ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
//
//			Service service = serviceFactory.create(OpenfireWebService.class);
//			service.setInvoker(new BeanInvoker(new OpenfireWebServiceImpl()));
//
//			XFire xfire = XFireFactory.newInstance().getXFire();
//			xfire.getServiceRegistry().register(service);
//			httpServer = new XFireHttpServer();
//			httpServer.setPort(3030);
//			httpServer.start();
			javax.xml.ws.Endpoint.publish("/ws", new OpenfireWebServiceImpl());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("WebService Server start faile", e);
		}
	}

	@Override
	public void destroyPlugin() {
		try {
			if (httpServer != null && httpServer.isStarted()) {
				httpServer.stop();
			}
		} catch (Exception e) {
			LOGGER.error("WebService Server stop faile", e);
		}

	}

}
