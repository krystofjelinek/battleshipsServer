package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
    private static Logger log = LoggerFactory.getLogger(Message.class);
    private String message;
    private GameSession gameSession;
    private ClientHandler sender;

    private static enum COMMAND {
        USER,
        PLACE,
        BOMB,
        MOVE,
    }

    public Message(String message, GameSession gameSession, ClientHandler sender) {
        this.message = message;
        this.gameSession = gameSession;
        this.sender = sender;
    }


    public void process(String message) {
        log.info("Received message: {}", message);
        String[] parts = message.split(" ");

        if (!gameSession.isPlayerTurn(sender)) {
            sender.sendMessage("Not your turn!");
            return;
        }

        if (parts.length < 1) {
            log.warn("Invalid command: {}", message);
            return;
        }

        String firstPart = parts[0];
        Game game = gameSession.getGame();

        if (firstPart.equals(COMMAND.BOMB.name())) {
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

                    // Switch turn after a valid BOMB command
                    gameSession.switchTurn();
                } catch (NumberFormatException e) {
                    log.error("Error processing BOMB command: {}", message, e);
                }
            } else {
                gameSession.getOtherPlayer().sendMessage("Not your turn!");
            }
        } else {
            log.warn("Unknown command: {}", firstPart);
        }
    }
}