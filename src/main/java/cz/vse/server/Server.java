package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Set;
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
    private final Set<String> activeUsernames = ConcurrentHashMap.newKeySet();
    private boolean running = true;


    public Server(int port) {
        this.port = port;
    }

    /**
     * Main method to start the server.
     * It accepts a port number as an argument; if not provided, it takes the port number from config.properties file.
     * @param args Command line arguments
     */
    public static void main(String[] args) throws IOException {
        int port = 0; // Default port
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                Server server = new Server(port);
                server.start();
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 9091.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (args.length == 0) {
            InputStream config = Server.class.getClassLoader().getResourceAsStream("config.properties");
            if (config == null) {
                throw new RuntimeException("Failed to load config.properties");
            }

            Properties properties = new Properties();
            properties.load(config);
            port = Integer.parseInt(properties.getProperty("server.port", "9091"));
            config.close();

            Server server = new Server(port);
            try {
                server.start();
            } catch (IOException e) {
                System.err.println("Error starting the server: " + e.getMessage());
            }
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

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            log.error("Error starting the server: {}", e.getMessage());
            throw e;
        } finally {
            log.info("Server has stopped listening for connections.");
            closeAllConnections();
            running = false;
        }
    }

    /**
     * Adds a client to the waiting list and starts a game session if there are two players.
     *
     * @param client The client to be added
     */
    public synchronized void addWaitingClient(ClientHandler client) throws IOException {
        String username = client.getUsername();
        if (activeUsernames.contains(username)) {
            log.warn("Username '{}' is already in use. Rejecting client connection.", username);
            client.sendMessage("QUIT");
            client.closeConnection();
            return;
        }

        activeUsernames.add(username);
        waitingClients.add(client);
        if (waitingClients.size() >= 2) {
            ClientHandler player1 = waitingClients.poll();
            ClientHandler player2 = waitingClients.poll();

            // Create a new GameSession
            GameSession gameSession = new GameSession(player1, player2);

            // Assign the GameSession to both players
            player1.setGameSession(gameSession);
            log.info("{} has been assigned to the game session", player1.getUsername());
            player2.setGameSession(gameSession);
            log.info("{} has been assigned to the game session", player2.getUsername());

            // Start the game session
            gameSession.start();
            log.info("Game session started between {} and {}", player1.getUsername(), player2.getUsername());
        }
    }

    public synchronized void removeActiveUser (ClientHandler client) {
        String username = client.getUsername();
        log.debug("Active users before: {}", activeUsernames);
        if (activeUsernames.remove(username)) {
            log.info("Removed active user: {}", username);
            log.debug("Active users after: {}", activeUsernames);
        } else {
            log.warn("Attempted to remove non-existent user: {}", username);
        }
    }

    /**
     * Closes all connections and shuts down the server.
     * This method is called when the server is shutting down.
     */
    public synchronized void closeAllConnections() throws IOException {
        for (ClientHandler client : waitingClients) {
            client.closeConnection();
        }
        waitingClients.clear();
        activeUsernames.clear();
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