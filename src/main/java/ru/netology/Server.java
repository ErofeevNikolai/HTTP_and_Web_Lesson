package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;

    public static void main(String[] args) {
        new Server(9999).startServer();
    }

    public Server(int port) {
        this.port = port;
    }

    public void startServer() {
        System.out.println("Server start");
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(new Run(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



