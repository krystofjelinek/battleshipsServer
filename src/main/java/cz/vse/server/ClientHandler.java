package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private GameSession gameSession; // Reference to the current GameSession
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean isClosing = false;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession; // Set the GameSession when paired
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);


            log.info("Client {} connected to the server", this);
            server.addWaitingClient(this);

            // Continuously listen for messages from the client

            while (!socket.isClosed()) {
                String receivedMessage = in.readLine();
                if (receivedMessage == null) {
                    break;
                }
                if (gameSession != null) {
                    Message message = new Message(receivedMessage, gameSession, this);
                    message.process(receivedMessage);
                } else {
                    log.warn("Game session not yet started. Message: {} could not be processed", receivedMessage);
                }
            }
        } catch (IOException e) {
            log.warn("Client {} disconnected unexpectedly: {}", this, e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        log.info("Server sent message: {} to client: {}.", message, this);
    }


    public void closeConnection() {
        if (isClosing) {
            return; // Prevent recursive calls
        }
        isClosing = true;

        try {
            if (gameSession != null) {
                ClientHandler otherPlayer = gameSession.getOtherPlayer(this);
                if (otherPlayer != null && !otherPlayer.isClosing) {
                    //otherPlayer.sendMessage("QUIT");
                    otherPlayer.closeConnection();
                    otherPlayer.socket.close();// Properly disconnect the other player
                }
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            log.info("Connection to client {} was closed", this);
        } catch (IOException e) {
            log.error("Error while closing connection for client {}: {}", this, e.getMessage());
        }
    }
}