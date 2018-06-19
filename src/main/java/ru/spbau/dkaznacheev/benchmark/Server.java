package ru.spbau.dkaznacheev.benchmark;

import java.net.ServerSocket;

public abstract class Server {
    protected final ServerSocket serverSocket;
    protected final StatLogger logger;
    protected final int maxClients;

    public Server(ServerSocket serverSocket, StatLogger logger, int maxClients) {
        this.serverSocket = serverSocket;
        this.logger = logger;
        this.maxClients = maxClients;
    }

    public abstract void start();
}
