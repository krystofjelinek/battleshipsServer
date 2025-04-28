package cz.vse.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private final int port;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ConcurrentLinkedQueue<ClientHandler> waitingClients = new ConcurrentLinkedQueue<>();

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = 12345; // Default port
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }

        Server server = new Server(port);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        }
    }

    public synchronized void addWaitingClient(ClientHandler client) {
        waitingClients.add(client);
        if (waitingClients.size() >= 2) {
            ClientHandler player1 = waitingClients.poll();
            ClientHandler player2 = waitingClients.poll();

            // Create a new GameSession
            GameSession gameSession = new GameSession(player1, player2);

            // Assign the GameSession to both players
            player1.setGameSession(gameSession);
            player2.setGameSession(gameSession);

            // Start the game session
            gameSession.start();
        }
    }
}