package com.sekoor.client;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for accessing keybase. Uses terminal commands to do it.
 */
public class KeybaseConnector {

    private static void validateKeybaseInstalled() throws IOException, InterruptedException {

        Process p = executeCommandWithReturnString("keybase version");
        if (p.exitValue() != 0) {
            throw new IllegalStateException("Keybase not installed (exit value = " + p.exitValue() + ")" );
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = reader.readLine();
        if (line == null) {
            throw new IllegalStateException("Keybase not installed");
        }
    }

    private static Process executeCommandWithReturnString(String command)  throws IOException, InterruptedException {
        System.out.println("About to execute command: " + command);
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        return p;
    }

    public static List<String> getContacts() throws IOException, InterruptedException {
        List<String> names = new ArrayList<>();

        validateKeybaseInstalled();

        Process p = executeCommandWithReturnString("keybase list-tracking");
        if (p.exitValue() != 0) {
            throw new IllegalStateException("Keybase could not find tracking list (exit value = " + p.exitValue() + ")" );
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            names.add(line);
        }
        return names;
    }

    public static BufferedReader encryptToContact(String contact, String message, String newFileAbsolutePath) throws IOException, InterruptedException {

        // Encrypt it
        Process p = executeCommandWithReturnString("keybase encrypt " + contact + " " + newFileAbsolutePath);
        if (p.exitValue() != 0) {
            throw new IllegalStateException("Keybase could not encrypt the file (exit value = " + p.exitValue() + ")" );
        }
        // Normal output doesn't hold anything, have to read from error
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        return errReader;
    }


}
