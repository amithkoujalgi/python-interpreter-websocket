package com.amithkoujalgi.interpreter.util;

import java.io.IOException;
import java.util.Properties;


public class Config {
    private static Config instance;
    private Properties p;

    private Config() {
    }

    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public Properties getConfig() throws IOException {
        if (p == null) {
            p = new Properties();
            p.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
        }
        return p;
    }
}
