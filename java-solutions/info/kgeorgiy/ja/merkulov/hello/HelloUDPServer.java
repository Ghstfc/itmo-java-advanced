package info.kgeorgiy.ja.merkulov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {

    public static final int TIMEOUT = 50;
    private ExecutorService executors;
    private DatagramSocket socket;
    private static final String HELLO = "Hello, ";

    private void init(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            executors = Executors.newFixedThreadPool(threads);
        } catch (SocketException e) {
            throw new RuntimeException("Invalid port : " + e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        init(port, threads);
        for (int i = 0; i < threads; i++) {
            executors.submit(() -> {
                while (!socket.isClosed() && !Thread.interrupted()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(new byte[512], 512);
                        socket.receive(packet);
                        InetAddress address = packet.getAddress();

                        String received = new String(packet.getData(), 0, packet.getLength());
                        byte[] b = (HELLO + received).getBytes();
                        packet = new DatagramPacket(b, b.length, address, packet.getPort());
                        socket.send(packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        executors.shutdown();
        try {
            if (!executors.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS)) {
                executors.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executors.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            executors.shutdownNow();
            Thread.currentThread().interrupt();
        }
        socket.close();
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg == null) {
                throw new RuntimeException("Invalid parameter");
            }
            try {
                Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Parameter is not number");
            }
        }
        try (HelloUDPServer server = new HelloUDPServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
    }
}
