package cz.vse.server;

import lombok.Setter;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private GameSession gameSession; // Reference to the current GameSession

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

            out.println("Welcome to BattleShips! Waiting for an opponent...");
            server.addWaitingClient(this);

            // Continuously listen for messages from the client

            while ((in.readLine()) != null) {
                String receivedMessage = in.readLine();
                if (gameSession != null) {
                    // Process the received message with the current GameSession
                    Message message = new Message(receivedMessage, gameSession, this);
                    message.process(receivedMessage);
                } else {
                    sendMessage("Game session not yet started.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}