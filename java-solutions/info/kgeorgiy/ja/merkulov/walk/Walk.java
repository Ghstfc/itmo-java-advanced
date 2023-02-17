package info.kgeorgiy.ja.merkulov.walk;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {

    private static final String badFileHash = "0".repeat(64);

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String countHash(String fileName) throws NoSuchAlgorithmException, IOException {
        byte[] buffer = new byte[8192];
        int count;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        while ((count = bis.read(buffer)) > 0) {
            digest.update(buffer, 0, count);
        }
        bis.close();
        byte[] hash = digest.digest();
        return bytesToHex(hash);
    }

    public static void main(String[] args) {

        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 2) {
            System.err.println("More/less arguments, than 2");
            return;
        }
        if (args[0] == null) {
            System.err.println("First argument is null");
            return;
        }
        if (args[1] == null) {
            System.err.println("Second argument is null");
            return;
        }


        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            String fileName;
            String hash;
            while (true) {
                fileName = reader.readLine();
                if (fileName == null)
                    break;
                if (new File(fileName).isFile() && new File(fileName).exists()) {
                    hash = countHash(fileName);
                } else if (new File(fileName).isDirectory()) {
                    writer.write(badFileHash + " " + fileName + System.lineSeparator());
                    continue;
                    //TODO (стало впадлу, поэтому рассматривается как проблема с чтением файлов)
                } else {
                    writer.write(badFileHash + " " + fileName + System.lineSeparator());
                    continue;
                }
                writer.write(hash + " " + fileName + System.lineSeparator());
            }
            writer.close();
            reader.close();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm");
        } catch (FileNotFoundException e) {
            System.err.println(" file not found");
        } catch (IOException e) {
            System.err.println(" can't read/write file");
        }
    }
}
