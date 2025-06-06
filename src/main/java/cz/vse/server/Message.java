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
    private final String message;
    private final GameSession gameSession;
    private final ClientHandler sender;

    /**
     * Enum representing the different commands that can be processed.
     */
    private enum COMMAND {
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

        String[] parts = message.split(" ");

        if (message.startsWith(COMMAND.QUIT.name())) {
            handleQuitCommand();
        } else if (message.startsWith(COMMAND.PING.name())) {
            handlePingCommand();
        }

        if (!gameSession.isPlayerTurn(sender)) {
            log.warn("Command could not be processed: {}, it is not {}`s turn", message, sender.getUsername());
            return;
        }

        Game game = gameSession.getGame();

        log.info("Received message: {}", message);

        if (message.startsWith(COMMAND.PLACE.name())) {
            handlePlaceCommand(game, parts);
        } else if (message.startsWith(COMMAND.BOMB.name())) {
            handleBombCommand(game, parts);
        }

        try {
            COMMAND.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            log.error("Invalid command received: {}", parts[0]);
            sender.sendMessage("FAILURE");
            return;
        }
    }

    /**
     * Handles the PLACE command.
     * Validates the command and places a ship in the game if the command is valid.
     *
     * @param game  The current game instance.
     * @param parts The parts of the command split by spaces.
     */
    private void handlePlaceCommand(Game game, String[] parts) {
        if (gameSession.isPlacementPhase()) {
            if (parts.length != 5) {
                log.warn("Invalid PLACE command: {}", message);
                sender.sendMessage("FAILURE");
                return;
            }
            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int[][] shape = ShipShape.valueOf(parts[3]).getShape();
                int r = Integer.parseInt(parts[4]);

                if (!gameSession.canPlaceShip(sender, ShipShape.valueOf(parts[3]))) {
                    sender.sendMessage("FAILURE");
                    log.warn("Player {} tried to place more ships of type {} than allowed", sender.getUsername(), parts[3]);
                    return;
                }
                String result = game.place(x, y, shape, r, sender);
                if (result.equals("SUCCESS")) {
                    gameSession.incrementShipCount(sender, ShipShape.valueOf(parts[3]));
                    gameSession.incrementShipsPlaced(sender);
                    sender.sendMessage(result);
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

    /**
     * Handles the PING command.
     * Responds with a PONG message to the client.
     */
    private void handlePingCommand() {
        sender.sendMessage("PONG");
        log.info("Responded to PING with PONG");
    }


    /**
     * Handles the BOMB command.
     * Validates the command and executes the bombing action in the game.
     *
     * @param game  The current game instance.
     * @param parts The parts of the command split by spaces.
     */
    private void handleBombCommand(Game game, String[] parts) {
        if (gameSession.isPlacementPhase()) {
            log.warn("Command could not be processed: {}, it is placement phase", message);
            sender.sendMessage("FAILURE");
            return;
        }
        if (gameSession.getCurrentPlayer() != gameSession.getOtherPlayer()) {
            if (parts.length != 3) {
                log.warn("Invalid BOMB command: {}", message);
                sender.sendMessage("FAILURE");
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
                sender.sendMessage("FAILURE");
            }
        }
    }

    /**
     * Handles the QUIT command.
     */
    private void handleQuitCommand() throws IOException {
        if (sender != null) {
            log.info("Client {} is disconnecting.", sender.getUsername());
            sender.sendMessage("QUIT");
            gameSession.getOtherPlayerInSession(sender).sendMessage("WIN");
            sender.closeConnection(true);
        }
    }
}