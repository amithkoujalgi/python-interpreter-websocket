package com.amithkoujalgi.interpreter.server;

import com.amithkoujalgi.interpreter.PythonInterpreterHandle;
import com.amithkoujalgi.interpreter.util.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@WebSocket
public class InterpreterWebSocket {
    private InterpreterSession interpreterSession;

    public InterpreterWebSocket() {
        //System.out.println("Created object of " + InterpreterWebSocket.class);
    }

    @OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
        if (message.contains("\n")) {
            List<String> lines = getStringListSeparatedChar(message, '\n');
            JSONUtils.print(lines);
            for (String line : lines) {
                interpreterSession.getPythonInterpreterHandle().write(line);
                interpreterSession.setReader(new PythonOutputReader(interpreterSession.getPythonInterpreterHandle().getPythonOutputStream(), session));
                Thread reader = new Thread(interpreterSession.getReader(), "reader");
                reader.start();
                while (true) {
                    if (!reader.isAlive()) {
                        break;
                    }
                }
            }
        } else {
            interpreterSession.getPythonInterpreterHandle().write(message);
            interpreterSession.setReader(new PythonOutputReader(interpreterSession.getPythonInterpreterHandle().getPythonOutputStream(), session));
            Thread reader = new Thread(interpreterSession.getReader(), "reader");
            reader.start();
            while (true) {
                if (!reader.isAlive()) {
                    break;
                }
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Client connected!");
        interpreterSession = new InterpreterSession();
        interpreterSession.setPythonInterpreterHandle(new PythonInterpreterHandle());
        interpreterSession.getPythonInterpreterHandle().start();
        while (true) {
            if (interpreterSession.getPythonInterpreterHandle().getPythonOutputStream() != null) {
                break;
            }
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

    private List<String> getStringListSeparatedChar(String text, char character) {
        // a hack to fetch lines separated by newline characters
        text = StringUtils.replace(text, "\n", "@@##");
        String[] s = StringUtils.split(text, "##");
        List<String> l = Arrays.asList(s);
        l.replaceAll(x -> x.replace("@@", "\n"));
        return l;
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
                if (sb.toString().endsWith(">>>") || sb.toString().endsWith("...")) {
                    break;
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Stream closed")) {
                    break;
                }
                if (e.getMessage().contains("RemoteEndpoint unavailable")) {
                    break;
                } else {
                    e.printStackTrace();
                }
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