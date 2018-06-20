package ru.spbau.dkaznacheev.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Client {

    private ServerProtocols.ServerParametersMsg params;

    public Client(ServerProtocols.ServerParametersMsg msg) {
        this.params = msg;
    }

    private static class ClientRunnable implements Runnable {
        private int d, x;
        private List<Integer> listToSort;

        private List<Integer> clientTimes = new LinkedList<>();

        private ClientRunnable(int d, int x, List<Integer> listToSort) {
            this.d = d;
            this.x = x;
            this.listToSort = listToSort;
        }

        @Override
        public void run() {
            while (true) {
                try (Socket socket = new Socket("127.0.0.1", 8000);
                     InputStream is = socket.getInputStream();
                     OutputStream os = socket.getOutputStream()) {
                    for (int i = 0; i < x; i++) {
                        long clientStartTime = System.currentTimeMillis();

                        ServerProtocols.ArrayMsg.Builder builder = ServerProtocols.ArrayMsg.newBuilder();
                        builder.setLength(listToSort.size());
                        builder.addAllNumber(listToSort);

                        builder.build().writeDelimitedTo(os);

                        ServerProtocols.ArrayMsg result = ServerProtocols.ArrayMsg.parseDelimitedFrom(is);
                        clientTimes.add((int) (System.currentTimeMillis() - clientStartTime));
                        if (i != x - 1) {
                            Thread.sleep(d);
                        }
                    }
                    return;
                } catch (Exception e) {
                    System.err.println("cant connect");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        public int getClientTime() {
            return (int)Math.round(clientTimes.stream().mapToInt(e->e).average().orElse(0));
        }
    }

    private int start(int param)  {
        int m = params.getM();
        int n = params.getN();
        int d = params.getD();
        int x = params.getX();
        switch (params.getParameter()) {
            case "m": {
                m = param;
                break;
            }
            case "n": {
                n = param;
                break;
            }
            case "d": {
                d = param;
                break;
            }
        }

        List<Integer> listToSort;
        listToSort = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            listToSort.add(n - i + 1);
        }

        List<ClientRunnable> runnables = new LinkedList<>();
        List<Thread> threads = new LinkedList<>();

        for (int i = 0; i < m; i++) {
            ClientRunnable runnable = new ClientRunnable(d, x, listToSort);
            Thread thread = new Thread(runnable);
            runnables.add(runnable);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double averageClientTime = runnables.stream()
                .mapToInt(ClientRunnable::getClientTime)
                .average()
                .orElse(0);

        return (int)Math.round(averageClientTime);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080);
             Socket socket = serverSocket.accept();
             InputStream is = socket.getInputStream();
             OutputStream os = socket.getOutputStream()) {
            ServerProtocols.ServerParametersMsg msg = ServerProtocols.ServerParametersMsg.parseDelimitedFrom(is);

            Client client = new Client(msg);

            List<Integer> result = new LinkedList<>();

            for (int i = msg.getPMin(); i < msg.getPMax(); i += msg.getPStep()) {
                result.add(client.start(i));
            }

            ServerProtocols.ArrayMsg.Builder builder = ServerProtocols.ArrayMsg.newBuilder();
            builder.setLength(result.size());
            builder.addAllNumber(result);

            ServerProtocols.ArrayMsg res = builder.build();
            res.writeDelimitedTo(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
