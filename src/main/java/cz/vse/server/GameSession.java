package cz.vse.server;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a game session between two players.
 * Handles the game state, player turns, and ship placement.
 */
@Slf4j
public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Game game;
    private boolean player1Turn = true;
    private boolean placementPhase = true;
    private int player1ShipsPlaced = 0;
    private int player2ShipsPlaced = 0;
    private final Map<ClientHandler, Map<ShipShape, Integer>> shipPlacementCount = new HashMap<>();


    /**
     * Constructor for GameSession.
     * Initializes the game and sets up ship placement counts for both players.
     *
     * @param player1 The first player
     * @param player2 The second player
     */
    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game(this);
        this.game.initializeGame();

        shipPlacementCount.put(player1, new HashMap<>());
        shipPlacementCount.put(player2, new HashMap<>());
        for (ShipShape shape : ShipShape.values()) {
            shipPlacementCount.get(player1).put(shape, 0);
            shipPlacementCount.get(player2).put(shape, 0);
        }
    }

    /**
     * Checks if a player can place a ship of a certain shape.
     *
     * @param player The player trying to place the ship
     * @param shape  The shape of the ship
     * @return true if the player can place the ship, false otherwise
     */
    public synchronized boolean canPlaceShip(ClientHandler player, ShipShape shape) {
        int count = shipPlacementCount.get(player).getOrDefault(shape, 0);
        if (shape == ShipShape.SIX_SHAPE) {
            return count < 1; // SIX_SHAPE can only be placed once
        }
        return count < 2; // Each ship type can only be placed twice
    }

    /**
     * Increments the count of ships placed by a player for a specific shape.
     *
     * @param player The player who placed the ship
     * @param shape  The shape of the ship
     */
    public synchronized void incrementShipCount(ClientHandler player, ShipShape shape) {
        int count = shipPlacementCount.get(player).getOrDefault(shape, 0);
        shipPlacementCount.get(player).put(shape, count + 1);
    }

    /**
     * Switches the turn between players.
     * If it's the placement phase, the turn is not switched.
     * Otherwise, it alternates the turn between player1 and player2.
     */
    public synchronized void switchTurn() {
        if (placementPhase) {
            log.error("Cannot switch turn during placement phase");
        } else {
            if (this.isPlayer1Turn()) {
                player1Turn = false;
                player2.sendMessage("TURN");
                game.checkForWin();
            } else {
                player1Turn = true;
                player1.sendMessage("TURN");
                game.checkForWin();
            }
        }
    }

    /**
     * Increments the number of ships placed by a player.
     * If both players have placed 8 ships, the placement phase ends.
     * @param player The player who placed the ship
     */
    public synchronized void incrementShipsPlaced(ClientHandler player) {
        if (player == player1) {
            player1ShipsPlaced++;
        } else if (player == player2) {
            player2ShipsPlaced++;
        }

        if (player1ShipsPlaced >= 7 && player2ShipsPlaced >= 7) {
            placementPhase = false;
        }
    }

    /**
     * Checks if it's the player's turn.
     * If it's the placement phase, it returns true for both players.
     *
     * @param player The player to check
     * @return true if it's the player's turn, false otherwise
     */
    public synchronized boolean isPlayerTurn(ClientHandler player) {
        if (placementPhase) {
            return true;
        }
        if (this.isPlayer1Turn() && (player == player1)) {
            return true;
        } else return !this.isPlayer1Turn() && (player == player2);
    }

    public synchronized boolean isPlacementPhase() {
        return placementPhase;
    }

    public synchronized boolean isPlayer1Turn() {
        return player1Turn;
    }

    public ClientHandler getCurrentPlayer() {
        return player1Turn ? player1 : player2;
    }

    public ClientHandler getOtherPlayer() {
        return player1Turn ? player2 : player1;
    }

    /**
     * Starts the game session by notifying both players that they are ready.
     * This method is called after both players have placed their ships.
     */
    public void start() {
        notifyAllClients("READY");
    }

    public Game getGame() {
        return this.game;
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return  player2;
    }

    public void notifyAllClients(String s) {
        player1.sendMessage(s);
        player2.sendMessage(s);
    }

    public void closeAllConnections() {
    }
}

