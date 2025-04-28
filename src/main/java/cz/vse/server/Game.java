package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Game {
    private String name;
    private String player1;
    private List<List<Integer>> listPlayerOne = new ArrayList<>();
    private List<List<Integer>> listPlayerTwo = new ArrayList<>();
    private String player2;
    private String status;
    private static final Logger log = LoggerFactory.getLogger(Game.class);

    public Game(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
    }



    public void initializeGame() {
        for (int i = 0; i < 10; i++) {
            listPlayerOne.add(new ArrayList<>(Arrays.asList(1,1,1,1,1,1,1,1,1,1)));
            listPlayerTwo.add(new ArrayList<>(Arrays.asList(1,1,1,1,1,1,1,1,1,1)));
        }

        // Example coordinates for player1
        // Initialize maps for both players
        // Initialize game logic here
        //System.out.println(listPlayerTwo);
    }

    public static void main(String[] args) {
        Game game = new Game("Player1", "Player2");
        game.initializeGame();
        //System.out.println(game.bomb(3, 3));
        System.out.println(game.place(0, 0, ShipShape.L_SHAPE.getShape(), 1));
       // System.out.println(game.place(5,5,ShipShape.T_SHAPE.getShape(), 2));// Place L-shaped ship
       // System.out.println(game.place(5, 5, tShape));
        System.out.println(game.listPlayerTwo);// Place T-shaped ship
        game.bomb(1,1);
        game.bomb(1,2);
        game.bomb(1,3);
        game.bomb(2,1);
        System.out.println(game.listPlayerTwo);
        game.checkForWin();
    }

    public String bomb(int x, int y) {

        if ((x < 0 || x > 10) || (y < 0 || y > 10)) {
            log.error("Invalid coordinates for bomb placement: {}, {}", x, y);
            return "Invalid coordinates for bomb placement"; //TODO upravit aby vracelo chybu na FE
        } else {
            int a = listPlayerTwo.get(x-1).get(y-1);
            if (a == 0){
                listPlayerTwo.get(x-1).set(y-1, -1);
                System.out.println("HIT");
                return "HIT";
            } else  {
                listPlayerTwo.get(x-1).set(y-1, 2);
                System.out.println("MISS");
                return "MISS";
            }
        }
    }

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

    public String place(int x, int y, int[][] shape, int rotation) {
        // Rotate the shape based on the rotation parameter
        int[][] rotatedShape = rotateShape(shape, rotation);

        // Validate coordinates
        for (int i = 0; i < rotatedShape.length; i++) {
            for (int j = 0; j < rotatedShape[i].length; j++) {
                if (rotatedShape[i][j] == 1) { // Part of the ship
                    int newX = x + i;
                    int newY = y + j;

                    // Check boundaries
                    if (newX < 0 || newX >= 10 || newY < 0 || newY >= 10) {
                        log.error("Invalid placement: Ship part out of bounds at {}, {}", newX, newY);
                        return "Invalid placement: Out of bounds";
                    }

                    // Check for overlaps
                    if (listPlayerTwo.get(newX).get(newY) != 1) {
                        log.error("Invalid placement: Overlap detected at {}, {}", newX, newY);
                        return "Invalid placement: Overlap detected";
                    }
                }
            }
        }

        // Place the ship
        for (int i = 0; i < rotatedShape.length; i++) {
            for (int j = 0; j < rotatedShape[i].length; j++) {
                if (rotatedShape[i][j] == 1) { // Part of the ship
                    listPlayerTwo.get(x + i).set(y + j, 0);
                }
            }
        }

        log.info("Ship placed successfully at {}, {} with rotation {}", x, y, rotation);
        return "Placement successful";
    }

    public String move(int x, int y, int[][] shape, int rotation) {
        // Rotate the shape based on the rotation parameter
        int[][] rotatedShape = rotateShape(shape, rotation);

        // Validate coordinates
        for (int i = 0; i < rotatedShape.length; i++) {
            for (int j = 0; j < rotatedShape[i].length; j++) {
                if (rotatedShape[i][j] == 1) { // Part of the ship
                    int newX = x + i;
                    int newY = y + j;

                    // Check boundaries
                    if (newX < 0 || newX >= 10 || newY < 0 || newY >= 10) {
                        log.error("Invalid placement: Ship part out of bounds at {}, {}", newX, newY);
                        return "Invalid placement: Out of bounds";
                    }

                    // Check for overlaps
                    if (listPlayerTwo.get(newX).get(newY) != 1) {
                        log.error("Invalid placement: Overlap detected at {}, {}", newX, newY);
                        return "Invalid placement: Overlap detected";
                    }
                }
            }
        }

        // Place the ship
        for (int i = 0; i < rotatedShape.length; i++) {
            for (int j = 0; j < rotatedShape[i].length; j++) {
                if (rotatedShape[i][j] == 1) { // Part of the ship
                    listPlayerTwo.get(x + i).set(y + j, 0);
                }
            }
        }

        log.info("Ship moved successfully at {}, {} with rotation {}", x, y, rotation);
        return "Placement successful";
    }

    public String checkForWin() {
        // Check if all ships of the opponent are sunk
        boolean allSunk = true;
        for (List<Integer> row : listPlayerTwo) {
            for (Integer cell : row) {
                if (cell == 1) { // Ship part not sunk
                    allSunk = false;
                    break;
                }
            }
        }

        if (allSunk) {
            return "All ships sunk! You win!";
        } else {
            return "Ships still afloat.";
        }
    }
}
