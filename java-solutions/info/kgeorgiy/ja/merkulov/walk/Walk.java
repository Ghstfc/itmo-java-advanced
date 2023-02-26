package info.kgeorgiy.ja.merkulov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static String countHash(String fileName) throws NoSuchAlgorithmException {
        byte[] buffer = new byte[8192];
        int count;
        byte[] hash = null;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName))) {
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();
            hash = digest.digest();

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage() + " File not found");
        } catch (IOException e) {
            System.err.println(e.getMessage() + "Can't read file");
        }
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

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            Path path = Paths.get(args[1]);
            if (path.getParent() != null && Files.notExists(path.getParent())) {
                try {
                    Files.createDirectories(path.getParent());
                } catch (IOException e) {
                    System.err.println("Parent directory isn't exists");
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                String fileName;
                String hash;
                while (true) {
                    fileName = reader.readLine();
                    if (fileName == null)
                        break;
                    try {
                        Path pathOfSource = Paths.get(fileName);
                        if (Files.exists(pathOfSource) && !Files.isDirectory(pathOfSource)) {
                            hash = countHash(fileName);
                            writer.write(hash + " " + fileName + System.lineSeparator());
                        } else {
                            writer.write(badFileHash + " " + fileName + System.lineSeparator());
                        }
                    } catch (InvalidPathException ignored) {
                        writer.write(badFileHash + " " + fileName + System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage() + " Can't write in output file");
            } catch (NoSuchAlgorithmException e) {
                System.err.println(e.getMessage() + " No such algorithm");
            } catch (InvalidPathException e) {
                System.err.println(e.getMessage() + " No such file/path");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage() + "Can't read in input file");
        } catch (InvalidPathException e) {
            System.err.println(e.getMessage() + " No such file/path");
        }
    }
}
