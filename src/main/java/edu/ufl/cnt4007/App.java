package edu.ufl.cnt4007;

import java.io.InputStream;
import java.util.Properties;

public class App {
    public static void main(String[] args) {

        InputStream input = App.class.getClassLoader()
                .getResourceAsStream("application.properties"); // ✅

        Properties props = new Properties();
        try {
            props.load(input);
        } catch (Exception e) {
            // Fail and exit
            e.printStackTrace();
            System.exit(1);
        }

        boolean featureEnabled = Boolean.parseBoolean(props.getProperty("cli.enabled", "true"));
        System.out.println(featureEnabled);
    }
}
