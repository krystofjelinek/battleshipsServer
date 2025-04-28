package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
    private static Logger log = LoggerFactory.getLogger(Message.class);
    private String message;
    private GameSession gameSession;

    private static enum COMMAND {
        USER,
        PLACE,
        BOMB,
        MOVE,
    }

    public Message(String message, GameSession gameSession) {
        this.message = message;
        this.gameSession = gameSession;
    }

    public void process(String message) {
        log.debug("Received message: {}", message);
        log.info("Processing message...");
        String[] parts = message.split(" ");

        if (parts.length < 5) { // Ensure there are enough parts for the PLACE command
            log.warn("Invalid PLACE command: {}", message);
            return;
        }

        String firstPart = parts[0];
        Game game = gameSession.getGame(); // Access the current game

        if (firstPart.equals(COMMAND.PLACE.name())) {
            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                ShipShape shape;
                try {
                    shape = ShipShape.valueOf(parts[3]); // Validate enum value
                } catch (IllegalArgumentException e) {
                    log.error("Invalid ShipShape value: {}", parts[3], e);
                    return;
                }
                int size = Integer.parseInt(parts[4]);
                log.error("Parsed PLACE command: x={}, y={}, shape={}, size={}", x, y, shape, size);

                game.place(x, y, shape.getShape(), size);
            } catch (NumberFormatException e) {
                log.error("Error parsing PLACE command arguments: {}", message, e);
            } catch (IllegalArgumentException e) {
                log.error("Error processing PLACE command: {}", message, e);
            }
        } else if (firstPart.equals(COMMAND.BOMB.name())) {
            if (parts.length < 3) { // Ensure there are enough parts for the BOMB command
                log.warn("Invalid BOMB command: {}", message);
                return;
            }
            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                game.bomb(x, y);
            } catch (NumberFormatException e) {
                log.error("Error processing BOMB command: {}", message, e);
            }
        } else {
            log.warn("Unknown command: {}", firstPart);
        }
    }
}