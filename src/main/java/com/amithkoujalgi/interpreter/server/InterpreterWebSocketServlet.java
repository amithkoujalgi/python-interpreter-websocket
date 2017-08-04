package com.amithkoujalgi.interpreter.server;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/interpreter")
public class InterpreterWebSocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(InterpreterWebSocket.class);
    }
}
