package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * This class represents the server that listens for incoming client connections.
 * It manages the game sessions and handles client interactions.
 */
public class Server {
    private final int port;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ConcurrentLinkedQueue<ClientHandler> waitingClients = new ConcurrentLinkedQueue<>();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Server(int port) {
        this.port = port;
    }

    /**
     * Main method to start the server.
     * It accepts a port number as an argument; if not provided, it defaults to 1234.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        int port = 1234; // Default port
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
            //TODO logging
            e.printStackTrace();
        }
    }

    /**
     * Starts the server and listens for incoming client connections.
     * When a client connects, it creates a new ClientHandler to manage the connection.
     *
     * @throws IOException If an I/O error occurs when opening the socket
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Server is listening for connections on port: {}", port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        }
    }

    /**
     * Adds a client to the waiting list and starts a game session if there are two players.
     *
     * @param client The client to be added
     */
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

    /**
     * Closes all connections and shuts down the server.
     * This method is called when the server is shutting down.
     */
    public synchronized void closeAllConnections() {
        for (ClientHandler client : waitingClients) {
            client.closeConnection();
        }
        waitingClients.clear();

        // Shut down the thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }

        log.warn("All connections closed and server shut down.");
    }

    public synchronized void closeConnection() {
        for (ClientHandler client : waitingClients) {
            client.closeConnection();
        }
        waitingClients.clear();

        // Shut down the thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }

        log.warn("All connections closed and server shut down.");
    }
}