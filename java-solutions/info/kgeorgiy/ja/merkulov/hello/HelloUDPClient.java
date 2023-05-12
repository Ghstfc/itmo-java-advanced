package info.kgeorgiy.ja.merkulov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class HelloUDPClient implements HelloClient {

    public static final int SIZE = 512;
    private static final int TIMEOUT = 100;


    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {

        ExecutorService executors = Executors.newFixedThreadPool(threads);

        Phaser phaser = new Phaser(threads + 1);
        for (int i = 1; i <= threads; i++) {
            int finalI = i;
            executors.submit(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress address = InetAddress.getByName(host);
                    socket.setSoTimeout(TIMEOUT);
                    for (int j = 1; j <= requests; j++) {
                        String toSen = prefix + finalI + "_" + j;
                        while (true) {
                            try {
                                DatagramPacket packet = new DatagramPacket(toSen.getBytes(), toSen.getBytes().length, address, port);
                                socket.send(packet);
                                byte[] data = new byte[SIZE];
                                packet = new DatagramPacket(data, SIZE);
                                socket.receive(packet);
                                String qoute = new String(packet.getData(), 0, packet.getLength());
                                if (qoute.contains(toSen)) {
                                    System.out.println(qoute);
                                    break;
                                }
                            } catch (IOException e) {
                                System.err.println("Can't receive packet : " + e.getMessage());
                            }
                        }
                    }

                } catch (UnknownHostException e) {
                    System.err.println("Unknown host: " + e.getMessage());
                } catch (SocketException e) {
                    System.err.println("Socket exception: " + e.getMessage());
                } finally {
                    phaser.arrive();
                }
            });
        }
        phaser.arriveAndAwaitAdvance();
        executors.shutdownNow();
    }

    private static void checker(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Parameter is not number");
        }
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg == null) {
                throw new RuntimeException("Invalid parameter");
            }
        }
        checker(args[1]);
        checker(args[3]);
        checker(args[4]);
        HelloUDPClient client = new HelloUDPClient();
        client.run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }
}
