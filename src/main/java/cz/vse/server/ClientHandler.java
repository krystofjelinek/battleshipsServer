package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private PrintWriter out;
    private BufferedReader in;
    private GameSession gameSession; // Reference to the current GameSession
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean isClosing = false;
    private boolean loggedIn = false;
    private String username;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession; // Set the GameSession when paired
    }

    void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return  username;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            log.info("Client {} connected to the server", this);

            while (!socket.isClosed()) {
                String receivedMessage = in.readLine();
                if (receivedMessage == null) {
                    break;
                }
                if (!isLoggedIn()) {
                    // Handle login or user-related commands
                    if (receivedMessage.startsWith("USER")) {
                        String[] parts = receivedMessage.split(" ", 2);
                        if (parts.length == 2) {
                            setUsername(parts[1]);
                            setLoggedIn(true);
                            log.info("User '{}' logged in successfully", parts[1]);
                            //sendMessage("Welcome, " + parts[1] + "!");
                            server.addWaitingClient(this);

                        } else {
                            log.warn("Invalid USER command format: {}", receivedMessage);
                            sendMessage("ERROR: Invalid USER command format.");
                        }
                    } else {
                        log.warn("Client not logged in. Message: {} could not be processed", receivedMessage);
                        sendMessage("ERROR: You must log in first.");
                    }
                    continue;
                }

                if (gameSession != null) {
                    Message message = new Message(receivedMessage, gameSession, this);
                    message.process(receivedMessage);
                } else {
                    log.warn("Game session not yet started. Message: {} could not be processed", receivedMessage);
                    sendMessage("ERROR: Game session not started yet.");
                }
            }
        } catch (IOException e) {
            log.warn("Client {} disconnected unexpectedly: {}", this.username, e.getMessage());
        } finally {
            try {
                closeConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        if (isLoggedIn()) {
            log.info("Server sent message: {} to client: {}.", message, this.username);
        } else {
            log.info("Server sent message: {} to client: {}.", message, this);
        }
    }


    public void closeConnection() throws IOException {
        if (isClosing) {
            log.info("Connection for client {} is already closing", this.username);
            return; // Prevent recursive calls
        }
        isClosing = true;

        log.info("Client {} OUT closed", this.username);
        if (isClosing) {
            return; // Prevent recursive calls
        }
        isClosing = true;
        try {
            // Close input stream
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Error closing input stream for client {}: {}", this.username, e.getMessage());
                }
            }

            // Close output stream
            if (out != null) {
                out.close();
            }

            // Close socket
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Error closing socket for client {}: {}", this.username, e.getMessage());
                }
            }

            log.info("Connection to client {} was closed", this);
            server.removeActiveUser(this);
        } catch (Exception e) {
            log.error("Unexe closing connection for client {}: {}", this.username, e.getMessage());
        }
    }

    public Server getServer() {
        return server;
    }
}