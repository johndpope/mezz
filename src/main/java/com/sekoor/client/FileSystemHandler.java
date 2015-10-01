package com.sekoor.client;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class FileSystemHandler {

    public static File baseDir = new File("/home/pelle/sekoor_dir");

    // A map from contact name to its corresponding directory
    public static Map<String, File> dirMap = new HashMap<>();

    public static void checkDir(File baseDir) {
        if (!baseDir.isDirectory()) {
            String err = baseDir.toString() + " is not a directory";
            throw new IllegalStateException(err);
        }
    }

    public static Map<String, File> buildFoldersIfNeeded(List<String> newContacts, File baseDir) {
        checkDir(baseDir);

        final File[] files = baseDir.listFiles();

        dirMap = new HashMap<>(); // Start from fresh
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    dirMap.put(f.getName(), f);
                } else {
                    // Ignore files
                }
            }
        }

        String parentDirPath = baseDir.getAbsolutePath();
        System.out.println("Nr of existing dirs: " + dirMap.keySet().size() + ", in " + parentDirPath);
        for (String contact : newContacts) {
            File dirName = dirMap.get(contact);
            if (dirName == null) {
                File newDir = new File(baseDir.getAbsolutePath(), contact);
                if (newDir.exists()) {
                    // This must be a file with the same name, should not be here
                    String msg = "Please remove the file " + contact + " in " + parentDirPath;
                    System.out.println("Error: " + msg);
                    throw new IllegalStateException(msg);
                } else {
                    System.out.println("Creating dir for contact " + contact);
                    newDir.mkdir();
                    dirMap.put(contact, newDir);
                }
            } else {
                // The dir has already been created, We check the content of a folder later when user clicks the contact
            }
        }

        return dirMap;
    }

    /**
     *
     * @param contact - the receiver
     * @param message - the message to be sent
     * @param baseDir - base dir for all the files
     * @param dt - time to be used in the filename
     * @return the absolute path of the new file
     */
    public static String createFileWhenSendingMessageToContact(String contact, String message, File baseDir, DateTime dt) throws IOException {
        checkDir(baseDir);

        File dirName = dirMap.get(contact);
        if (dirName != null) {
            if (dirName.exists()) {
                String dateStr = getDateString(dt);
                File newFile = new File(dirName.getAbsolutePath() + File.separator + dateStr + "_out.html");
                if (newFile.createNewFile()) {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(newFile));
                        writer.write(message);
                        return newFile.getAbsolutePath();
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                } else {
                    throw new IllegalStateException("File exists: " + newFile.getAbsolutePath());
                }
            } else {
                throw new IllegalStateException("Incorrect state! No folder created for " + contact);
            }
        } else {
            throw new IllegalStateException("Incorrect state! No folder cashed for " + contact);
        }
    }


    private static String getDateString(DateTime dt) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
        return fmt.print(dt);
    }
}
