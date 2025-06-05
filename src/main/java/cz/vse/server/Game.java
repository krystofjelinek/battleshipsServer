package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class represents the game logic for a two-player game.
 */
public class Game {
    private final List<List<Integer>> listPlayerOne = new ArrayList<>();
    private final List<List<Integer>> listPlayerTwo = new ArrayList<>();
    private final GameSession gameSession;
    private static final Logger log = LoggerFactory.getLogger(Game.class);

    public Game(GameSession gs) {
        this.gameSession = gs;
    }

    /**
     * Initializes the game board for both players.
     * Each player has a 10x10 grid represented as a list of lists.
     * Each cell is initialized to 1, indicating that it is empty.
     */
    public void initializeGame() {
        for (int i = 0; i < 10; i++) {
            listPlayerOne.add(new ArrayList<>(Arrays.asList(1,1,1,1,1,1,1,1,1,1)));
            listPlayerTwo.add(new ArrayList<>(Arrays.asList(1,1,1,1,1,1,1,1,1,1)));
        }
    }

    /**
     * This method is called when a player bombs another player's ship.
     * It checks if the coordinates are valid and if the bomb hits or misses.
     * If the bomb hits, it updates the grid and checks for a win condition.
     * @param x x coordinate
     * @param y y coordinate
     * @return a string indicating the result of the bombing (HIT or MISS)
     */
    public String bomb(int x, int y) {
        if ((x < 0 || x > 9) || (y < 0 || y > 9)) {
            log.error("Invalid coordinates for bomb placement: {}, {}", x, y);
            return "Invalid coordinates for bomb placement";
        } else {
            if (gameSession.isPlayer1Turn())
                if (listPlayerTwo.get(x).get(y) == 0) {
                    listPlayerTwo.get(x).set(y, -1);
                    checkForWin();
                    return "HIT" + " " + x + " " + y;
                } else {
                    listPlayerTwo.get(x).set(y, 2);
                    log.info(listPlayerTwo.toString());
                    return "MISS" + " " + x + " " + y;
                }
            else {
                if (listPlayerOne.get(x).get(y) == 0) {
                    listPlayerOne.get(x).set(y, -1);
                    checkForWin();
                    return "HIT" + " " + x + " " + y;
                } else {
                    listPlayerOne.get(x).set(y, 2);
                    log.info(listPlayerOne.toString());
                    return "MISS" + " " + x + " " + y;
                }
            }
        }
    }

    /**
     * Rotates the shape of the ship based on the rotation parameter.
     * The rotation can be 0 (no rotation), 1 (90 degrees clockwise), 2 (180 degrees), 3 (270 degrees clockwise).
     * @param shape the shape of the ship represented as a 2D array
     * @param rotation the rotation parameter
     * @return the rotated shape of the ship
     */
    private int[][] rotateShape(int[][] shape, int rotation) {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotatedShape;

        switch (rotation) {
            case 1: // 90 degrees clockwise
                rotatedShape = new int[cols][rows];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        rotatedShape[j][rows - 1 - i] = shape[i][j];
                    }
                }
                break;
            case 2: // 180 degrees
                rotatedShape = new int[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        rotatedShape[rows - 1 - i][cols - 1 - j] = shape[i][j];
                    }
                }
                break;
            case 3: // 270 degrees clockwise
                rotatedShape = new int[cols][rows];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        rotatedShape[cols - 1 - j][i] = shape[i][j];
                    }
                }
                break;
            default: // 0 degrees (no rotation)
                rotatedShape = shape;
        }

        return rotatedShape;
    }

    /**
     * Checks if the ship can be placed at the specified coordinates.
     * It checks if the coordinates are within bounds and if the adjacent cells are occupied.
     * @param x x coordinate
     * @param y y coordinate
     * @param playerMap the grid of the player
     * @return true if the ship can be placed, false otherwise
     * @note This part of code is AI generated
     */
    private boolean isAdjacentCellOccupied(List<List<Integer>> playerMap, int x, int y) {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < dx.length; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (newX >= 0 && newX < 10 && newY >= 0 && newY < 10) {
                if (playerMap.get(newX).get(newY) == 0) { // Adjacent cell occupied
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Places the ship on the grid for the specified player.
     * It checks if the coordinates are valid and if the ship can be placed without overlaps.
     * @param x x coordinate
     * @param y y coordinate
     * @param shape the shape of the ship represented as a 2D array
     * @param rotation the rotation parameter
     * @param sender the player who is placing the ship
     * @return a string indicating the result of the placement (success or failure)
     */
    public String place(int x, int y, int[][] shape, int rotation, ClientHandler sender) {
        // Rotate the shape based on the rotation parameter
        int[][] rotatedShape = rotateShape(shape, rotation);
        x = x - 1; // Adjust for 0-based indexing
        y = y - 1; // Adjust for 0-based indexing

        List<List<Integer>> playerMap = sender == gameSession.getPlayer1() ? listPlayerOne : listPlayerTwo;

        // Validate coordinates
        for (int i = 0; i < rotatedShape.length; i++) {
            for (int j = 0; j < rotatedShape[i].length; j++) {
                if (rotatedShape[i][j] == 1) { // Part of the ship
                    int newX = x + i;
                    int newY = y + j;

                    // Check boundaries
                    if (newX < 0 || newX >= 10 || newY < 0 || newY >= 10) {
                        log.warn("Invalid placement: Ship part out of bounds at {}, {}", newX, newY);
                        return "FAILURE";
                    }

                    // Check for overlaps
                    if (playerMap.get(newX).get(newY) != 1) {
                        log.warn("Invalid placement: Overlap detected at {}, {}", newX, newY);
                        return "FAILURE";
                    }

                    // Check for adjacency
                    if (isAdjacentCellOccupied(playerMap, newX, newY)) {
                        log.warn("Invalid placement: Adjacent ship detected at {}, {}", newX, newY);
                        return "FAILURE";
                    }
                }
            }
        }

        // Place the ship
        for (int i = 0; i < rotatedShape.length; i++) { // Iterate through the rows of the rotated shape
            for (int j = 0; j < rotatedShape[i].length; j++) { // Iterate through the columns of the rotated shape
                if (rotatedShape[i][j] == 1) { // Part of the ship
                    playerMap.get(x + i).set(y + j, 0); // Mark the cell as occupied by the ship
                }
            }
        }

        log.info("Ship placed successfully at {}, {} with rotation {}", x, y, rotation);
        return "SUCCESS";
    }

    /**
     * Checks if all ships of the opponent are sunk.
     * If so, it sends a win message to the current player and a loss message to the opponent.
     * This method is called after each bombing action.
     * It checks the grid of both players to determine if any ship parts are left.
     * If all ship parts are sunk, it sends a win message to the current player and a loss message to the opponent.
     */
    public void checkForWin() {
        // Check if all ships of the opponent are sunk
        boolean player2AllSunk = true;
        for (List<Integer> row : listPlayerTwo) {
            for (Integer cell : row) {
                if (cell == 1) { // Ship part not sunk
                    player2AllSunk = false;
                    break;
                }
            }
        }

        boolean player1AllSunk = true;
        for (List<Integer> row : listPlayerOne) {
            for (Integer cell : row) {
                if (cell == 1) { // Ship part not sunk
                    player1AllSunk = false;
                    break;
                }
            }
        }

        if (player2AllSunk) {
            gameSession.getPlayer1().sendMessage("WIN");
            gameSession.getPlayer2().sendMessage("LOST");
        } else if (player1AllSunk) {
            gameSession.getPlayer1().sendMessage("LOST");
            gameSession.getPlayer2().sendMessage("WIN");
        }
    }
}
