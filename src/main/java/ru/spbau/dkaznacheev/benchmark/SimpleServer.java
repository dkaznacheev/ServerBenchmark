package ru.spbau.dkaznacheev.benchmark;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class SimpleServer extends Server {

    public SimpleServer(ServerSocket serverSocket, StatLogger logger, int maxClients) {
        super(serverSocket, logger, maxClients);
    }

    private class SimpleHandler implements Runnable {

        private final Socket socket;

        private SimpleHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

        }
    }

    @Override
    public void start() {
        List<Thread> handlers = new LinkedList<>();
        for (int i = 0; i < maxClients; i++) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread handler = new Thread(new SimpleHandler(clientSocket));
                handler.start();
                handlers.add(handler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Thread handler: handlers) {
            try {
                handler.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
