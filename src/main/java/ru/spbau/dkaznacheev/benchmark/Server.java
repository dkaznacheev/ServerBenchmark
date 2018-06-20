package ru.spbau.dkaznacheev.benchmark;

import java.net.ServerSocket;

public abstract class Server {
    protected final ServerSocket serverSocket;
    protected final int maxClients;
    protected final int x;

    public Server(ServerSocket serverSocket, int maxClients, int x) {
        this.serverSocket = serverSocket;
        this.maxClients = maxClients;
        this.x = x;
    }

    public abstract TestResult start();
}
