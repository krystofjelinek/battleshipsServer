package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The Message class processes incoming messages from clients and handles game commands.
 * It validates the commands, interacts with the GameSession, and sends responses back to clients.
 */
public class Message {
    private static final Logger log = LoggerFactory.getLogger(Message.class);
    private String message;
    private GameSession gameSession;
    private ClientHandler sender;

    /**
     * Enum representing the different commands that can be processed.
     */
    private static enum COMMAND {
        USER,
        PLACE,
        BOMB,
        MOVE,
        PING,
        QUIT
    }

    /**
     * Constructor for the Message class.
     *
     * @param message      The message received from the client.
     * @param gameSession  The current game session.
     * @param sender       The client handler that sent the message.
     */
    public Message(String message, GameSession gameSession, ClientHandler sender) {
        this.message = message;
        this.gameSession = gameSession;
        this.sender = sender;
    }

    /**
     * Processes the incoming message and executes the corresponding command.
     *
     * @param message The message to be processed.
     */
    public void process(String message) throws IOException {
        log.info("Received message: {}", message);
        String[] parts = message.split(" ");

        if (!gameSession.isPlayerTurn(sender)) {
            log.warn("Command could not be processed: {}, it is not {}`s turn", message, sender.getUsername());
            return;
        }

        if (parts.length < 1) {
            log.warn("Invalid command: {}", message);
            return;
        }

        String firstPart = parts[0];
        Game game = gameSession.getGame();

        if (firstPart.equalsIgnoreCase(COMMAND.QUIT.name())) {
            handleQuitCommand();
        }

        if (firstPart.equalsIgnoreCase(COMMAND.PING.name())) {
            sender.sendMessage("PONG");
            log.info("Responded to PING with PONG");
        } else {
            log.warn("Unknown command: {}", firstPart);
        }

        if (firstPart.equalsIgnoreCase(COMMAND.BOMB.name())) {
            if (gameSession.isPlacementPhase()) {
                log.warn("Command could not be processed: {}, it is placement phase", message);
                return;
            }
            if (gameSession.getCurrentPlayer() != gameSession.getOtherPlayer()) {
                if (parts.length != 3) {
                    log.warn("Invalid BOMB command: {}", message);
                    return;
                }
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String result = game.bomb(x, y);
                    gameSession.getCurrentPlayer().sendMessage(result);
                    gameSession.getOtherPlayer().sendMessage(result);
                    gameSession.switchTurn();
                } catch (NumberFormatException e) {
                    log.error("Error processing BOMB command: {}", message, e);
                }
            } else {
                log.warn("Command could not be processed: {}, it is not {}`s turn", message, sender.getUsername());
            }
        } else if (firstPart.equalsIgnoreCase(COMMAND.PLACE.name())) {
            if (gameSession.isPlacementPhase()) {
                if (parts.length != 5) {
                    log.warn("Invalid PLACE command: {}", message);
                    return;
                }
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    try {
                        int[][] result = ShipShape.valueOf(parts[3]).getShape();
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ship shape: {}", parts[3]);
                        sender.sendMessage("FAILURE");
                        return;
                    }
                    int[][] shape = ShipShape.valueOf(parts[3]).getShape();
                    int r = Integer.parseInt(parts[4]);

                    if (!gameSession.canPlaceShip(sender, ShipShape.valueOf(parts[3]))) {
                        sender.sendMessage("FAILURE");
                        log.warn("Player {} tried to place more ships of type {} than allowed", sender.toString(), parts[3]);
                        return;
                    }
                    String result = game.place(x, y, shape, r, sender);

                    if (result.equals("SUCCESS")) {
                        gameSession.incrementShipCount(sender, ShipShape.valueOf(parts[3]));
                        sender.sendMessage(result);
                        gameSession.incrementShipsPlaced(sender);
                        gameSession.switchTurn();
                    } else {
                        sender.sendMessage(result);
                    }
                } catch (NumberFormatException e) {
                    log.error("Error processing PLACE command: {}", message, e);
                    sender.sendMessage("FAILURE");
                }
            } else {
                sender.sendMessage("FAILURE");
                log.warn("Command could not be processed: {}, it is not placement phase", message);
            }
        }
    }

    /**
     * Handles the QUIT command, either from the client or the server.
     */
    private void handleQuitCommand() throws IOException {
        if (sender != null) { // Client sent QUIT
            log.info("Client {} is disconnecting.", sender.getUsername());
            sender.sendMessage("QUIT: Connection closed by client.");
            sender.closeConnection();
        } else { // Server sent QUIT
            log.info("Server is shutting down. Notifying all clients.");
            gameSession.notifyAllClients("QUIT");
            gameSession.closeAllConnections();
        }
    }
}