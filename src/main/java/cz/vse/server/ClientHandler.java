package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

/**
 * This class handles communication with a connected client.
 */
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
                            //log.info("User '{}' logged in successfully", parts[1]);
                            server.addWaitingClient(this);

                        } else {
                            log.warn("Invalid USER command format: {}", receivedMessage);
                            sendMessage("FAILURE");
                        }
                    } else {
                        log.warn("Client not logged in. Message: {} could not be processed", receivedMessage);
                        sendMessage("FAILURE");
                    }
                    continue;
                }

                if (gameSession != null) {
                    Message message = new Message(receivedMessage, gameSession, this);
                    message.process(receivedMessage);
                } else {
                    log.warn("Game session not yet started. Message: {} could not be processed", receivedMessage);
                    sendMessage("FAILURE");
                }
            }
        } catch (IOException e) {
            log.warn("Client {} disconnected unexpectedly: {}", this.username, e.getMessage());
        } finally {
            try {
                if (gameSession != null) {
                    //gameSession.getOtherPlayerInSession(this).setLoggedIn(false); // Set loggedIn to false before closing connection
                    if (gameSession.getOtherPlayerInSession(this).isLoggedIn()){
                        closeConnection(loggedIn);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sends a message to the client.
     * @param message
     */
    public void sendMessage(String message) {
        out.println(message);
        if (isLoggedIn()) {
            log.info("Server sent message: {} to client: {}.", message, this.username);
        } else {
            log.info("Server sent message: {} to client: {}.", message, this);
        }
    }

    /**
     * Closes the connection to the client.
     * This method ensures that all resources are properly released and the connection is closed correctly.
     *
     * @param LoggedIn Indicates if the client was logged in before closing the connection
     * @throws IOException If an I/O error occurs while closing the connection
     */
    public void closeConnection(boolean LoggedIn) throws IOException {
        if (isClosing) {
            log.info("Connection for client {} is already closing", this.username);
            return; // Prevent recursive calls
        }
        isClosing = true;

        try {
            // Close input stream
            if (in != null) {
                try {
                    in.close();
                    log.debug("Closing InputStream for client {}", this.username);
                } catch (IOException e) {
                    log.error("Error closing input stream for client {}: {}", this.username, e.getMessage());
                }
            }

            // Close output stream
            if (out != null) {
                out.close();
                log.debug("Closing OutputStream for client {}", this.username);
            }

            // Close socket
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    log.debug("Closing socket for client {}", this.username);
                } catch (IOException e) {
                    log.error("Error closing socket for client {}: {}", this.username, e.getMessage());
                }
            }

            if (LoggedIn){
                // Notify server to remove client
                server.removeActiveUser(this);
                gameSession.getOtherPlayerInSession(this).sendMessage("WIN");
            }


            log.info("Connection to client {} was closed", this.username);
        } catch (Exception e) {
            log.error("Unexpected error closing connection for client {}: {}", this.username, e.getMessage());
        }
    }

    public Server getServer() {
        return server;
    }
}