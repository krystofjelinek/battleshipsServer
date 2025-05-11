package cz.vse.server;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Game game;
    private boolean player1Turn = true;
    private boolean placementPhase = true;
    private int player1ShipsPlaced = 0;
    private int player2ShipsPlaced = 0;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game(this);
        this.game.initializeGame();
    }

    public synchronized void switchTurn() {
        if (placementPhase) {
            return; // Don't switch turns during placement phase
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

    public synchronized void incrementShipsPlaced(ClientHandler player) {
        if (player == player1) {
            player1ShipsPlaced++;
        } else if (player == player2) {
            player2ShipsPlaced++;
        }

        if (player1ShipsPlaced >= 6 && player2ShipsPlaced >= 6) {
            placementPhase = false;
            start();
        }
    }

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

    public void start() {
        player1.sendMessage("Game started! You are Player 1.");
        player2.sendMessage("Game started! You are Player 2.");

        //player1.sendMessage("Your turn!");
        //player2.sendMessage("Waiting for Player 1...");


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
}

