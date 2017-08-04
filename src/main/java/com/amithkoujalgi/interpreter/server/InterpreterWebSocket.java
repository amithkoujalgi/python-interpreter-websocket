package com.amithkoujalgi.interpreter.server;

import com.amithkoujalgi.interpreter.PythonInterpreterHandle;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@WebSocket
public class InterpreterWebSocket {
    private InterpreterSession interpreterSession;

    public InterpreterWebSocket() {
        System.out.println("Created object of " + InterpreterWebSocket.class);
    }

    @OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
        System.out.println("Command: " + message);
        interpreterSession.getPythonInterpreterHandle().write(message);
        interpreterSession.setReader(new PythonOutputReader(interpreterSession.getPythonInterpreterHandle().getPythonOutputStream(), session));
        Thread reader = new Thread(interpreterSession.getReader(), "reader");
        reader.start();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Client connected!");
        interpreterSession = new InterpreterSession();
        interpreterSession.setPythonInterpreterHandle(new PythonInterpreterHandle());
        interpreterSession.getPythonInterpreterHandle().start();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        interpreterSession.setReader(new PythonOutputReader(interpreterSession.getPythonInterpreterHandle().getPythonOutputStream(), session));
        Thread reader = new Thread(interpreterSession.getReader(), "reader");
        reader.start();
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        System.out.println("Client closed!");
        interpreterSession.getPythonInterpreterHandle().kill();
    }
}


class PythonOutputReader implements Runnable {

    private InputStream is;
    private Session session;

    public PythonOutputReader(InputStream is, Session session) {
        this.is = is;
        this.session = session;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        while (true) {
            try {
                char c = (char) reader.read();
                sb.append(c);
                session.getRemote().sendString(c + "");
                if (sb.toString().endsWith(">>>")) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class InterpreterSession {
    private PythonOutputReader reader;
    private PythonInterpreterHandle pythonInterpreterHandle;

    public PythonOutputReader getReader() {
        return reader;
    }

    public void setReader(PythonOutputReader reader) {
        this.reader = reader;
    }

    public PythonInterpreterHandle getPythonInterpreterHandle() {
        return pythonInterpreterHandle;
    }

    public void setPythonInterpreterHandle(PythonInterpreterHandle pythonInterpreterHandle) {
        this.pythonInterpreterHandle = pythonInterpreterHandle;
    }
}