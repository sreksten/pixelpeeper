package com.threeamigos.pixelpeeper;

/**
 * A class designed to help the application understand if we are in a
 * production or development environment.
 *
 * @author Stefano Reksten
 */
public class Environment {

    private Environment() {
    }

    public static boolean isDev() {
        return "dev".equalsIgnoreCase(System.getProperty("env"));
    }

}
