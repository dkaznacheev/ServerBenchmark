package ru.spbau.dkaznacheev.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class BlockingServer extends Server {

    public BlockingServer(ServerSocket serverSocket, int maxClients, int x) {
        super(serverSocket, maxClients, x);
    }


    private static final int NUMBER_OF_THREADS = 4;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static class sortQuery implements Callable<List<Integer>> {

        private List<Integer> listToSort;

        private sortQuery(List<Integer> listToSort) {
            this.listToSort = listToSort;
        }

        @Override
        public List<Integer> call() throws Exception {
            return Util.sort(listToSort);
        }
    }

    private class SimpleHandler implements Runnable {

        private final Socket socket;
        private SimpleHandler(Socket socket) {
            this.socket = socket;
        }

        private List<Integer> handleTimes = new LinkedList<>();
        private List<Integer> calcTimes = new LinkedList<>();


        public int getHandleTime() {
            return (int)Math.round(handleTimes.stream().mapToInt(e->e).average().orElse(0));
        }

        public int getCalcTime() {
            return (int)Math.round(calcTimes.stream().mapToInt(e->e).average().orElse(0));
        }

        @Override
        public void run() {
            try (InputStream is = socket.getInputStream();
                 OutputStream os = socket.getOutputStream()) {
                for (int i = 0; i < x; i++) {
                    long handleStartTime = System.currentTimeMillis();
                    ServerProtocols.ArrayMsg msg = ServerProtocols.ArrayMsg.parseDelimitedFrom(is);
                    long calcStartTime = System.currentTimeMillis();

                    Future<List<Integer>> future = threadPool.submit(new sortQuery(msg.getNumberList()));
                    List<Integer> result = future.get();

                    calcTimes.add((int) (System.currentTimeMillis() - calcStartTime));

                    ServerProtocols.ArrayMsg.Builder builder = ServerProtocols.ArrayMsg.newBuilder();
                    builder.setLength(result.size());
                    builder.addAllNumber(result);

                    builder.build().writeDelimitedTo(os);
                    handleTimes.add((int) (System.currentTimeMillis() - handleStartTime));
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public TestResult start() {
        List<SimpleHandler> handlers = new LinkedList<>();
        List<Thread> threads = new LinkedList<>();
        for (int i = 0; i < maxClients; i++) {
            try {
                Socket clientSocket = serverSocket.accept();
                SimpleHandler handler = new SimpleHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
                threads.add(thread);
                handlers.add(handler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Thread handler: threads) {
            try {
                handler.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int averageCalcTime = (int)Math.round(handlers.stream().mapToInt(SimpleHandler::getCalcTime).average().orElse(0));
        int averageHandleTime = (int)Math.round(handlers.stream().mapToInt(SimpleHandler::getHandleTime).average().orElse(0));

        threadPool.shutdown();
        return new TestResult(averageCalcTime, averageHandleTime);
    }
}
