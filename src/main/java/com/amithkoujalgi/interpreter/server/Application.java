package com.amithkoujalgi.interpreter.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class Application {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        Server server = new Server(port);

        ResourceHandler h = new ResourceHandler();
        h.setWelcomeFiles(new String[]{"index.html"});
        h.setDirectoriesListed(true);
        h.setResourceBase("src/main/webapp/");

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/interpreter/*");
        context.setHandler(new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory webSocketServletFactory) {
                webSocketServletFactory.register(InterpreterWebSocket.class);
                int timeoutMinutes = 30;
                webSocketServletFactory.getPolicy().setIdleTimeout(60000 * timeoutMinutes);
            }
        });

        FilterHolder cors = context.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{h, context});
        server.setHandler(handlers);
        server.start();
    }
}

