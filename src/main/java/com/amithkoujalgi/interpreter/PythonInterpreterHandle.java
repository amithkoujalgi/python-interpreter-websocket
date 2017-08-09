package com.amithkoujalgi.interpreter;

import com.amithkoujalgi.interpreter.util.Config;

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
            String pythonPath = Config.getInstance().getConfig().getProperty("PYTHON_BINARY_PATH");
            ProcessBuilder pb;
            if (OSUtils.isMac()) {
                pb = new ProcessBuilder(
                        "/usr/bin/script", "-q", "/dev/null", pythonPath);
            } else if (OSUtils.isUnix()) {
                pb = new ProcessBuilder(
                        "/usr/bin/script", "-qfc", pythonPath, "/dev/null");
            } else {
                throw new Exception("Platform not supported!");
            }
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
        while (true) {
            if (!pythonCommandWriter.isAlive()) {
                break;
            }
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
                Thread.sleep(1);
                if (messageAdded && !executed) {
                    writer.write(msg);
                    // writer.newLine();
                    writer.flush();
                    messageAdded = false;
                    executed = true;
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Stream closed")) {
                    break;
                } else {
                    e.printStackTrace();
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

class OSUtils {
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }
}