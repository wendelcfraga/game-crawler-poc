package com.wendel.gamecrawler.creds;

import java.io.FileInputStream;
import java.util.Properties;

public class Credenciais {
    
    private static String getGoogleKey() {
        Properties props = new Properties();
        try {
            FileInputStream file = new FileInputStream("./src/main/resources/application.properties");
            props.load(file);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return props.getProperty("google.api.key");
    }

    private static String getGoogleId() {
        Properties props = new Properties();
        try {
            FileInputStream file = new FileInputStream("./src/main/resources/application.properties");
            props.load(file);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return props.getProperty("google.api.id");
    }

    public final String googleKey = getGoogleKey();
    public final String googleId = getGoogleId();

    public static void main(String[] args) {
        Credenciais opa = new Credenciais();
        System.out.println(opa.googleId + opa.googleKey);
    }
    
}
