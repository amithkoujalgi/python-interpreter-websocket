package com.amithkoujalgi.interpreter;

import java.io.*;
import java.util.Map;

public class PythonInterpreterHandle extends Thread {
    private InputStream pyOutputStream;
    private InputStream pyErrorStream;
    private Process p;
    private PythonCommandWriter pythonCommandWriter;

    @Override
    public void run() {
        startInterpreter();
    }

    public void startInterpreter() {
        try {
            System.out.println("Starting Python interpreter...");
            ProcessBuilder pb = new ProcessBuilder(
                    "/usr/bin/script", "-q", "/dev/null", "/usr/local/bin/python3");
            Map<String, String> map = pb.environment();
            p = pb.start();

            pyOutputStream = p.getInputStream();
            pyErrorStream = p.getErrorStream();

            pythonCommandWriter = new PythonCommandWriter(p.getOutputStream());
            pythonCommandWriter.setName("commander");
            pythonCommandWriter.start();

            int errorCode = p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        pythonCommandWriter.kill();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        p.destroy();
    }

    public void write(String msg) throws IOException {
        pythonCommandWriter.write(msg);
    }

    public InputStream getPythonOutputStream() {
        return pyOutputStream;
    }
}

class PythonCommandWriter extends Thread {
    private BufferedWriter writer;
    private boolean messageAdded = false, executed = true;
    private String msg;
    private boolean shouldInterrupt = false;

    public PythonCommandWriter(OutputStream os) {
        this.writer = new BufferedWriter(new OutputStreamWriter(os));
    }

    public void run() {
        while (true) {
            try {
                if (shouldInterrupt) {
                    writer.write("exit()");
                    writer.newLine();
                    writer.flush();
                    writer.close();
                    System.out.println("Terminating Python interpreter...");
                    break;
                }
                Thread.sleep(100);
                if (messageAdded && !executed) {
                    writer.write(msg);
                    writer.newLine();
                    writer.flush();
                    messageAdded = false;
                    executed = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("Stream closed")) {
                    break;
                }
            }
        }
    }

    public void write(String msg) throws IOException {
        this.msg = msg;
        messageAdded = true;
        executed = false;
    }

    public void kill() {
        shouldInterrupt = true;
    }
}