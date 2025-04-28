package cz.vse.server;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Game game;
    private boolean player1Turn = true;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game(player1.toString(), player2.toString());
        this.game.initializeGame();
    }

    public synchronized void switchTurn() {
        if (this.isPlayer1Turn()) {
            player1Turn = false;
            player2.sendMessage("TURN");
        } else {
            player1Turn = true;
            player1.sendMessage("TURN");
        }
    }

    public synchronized boolean isPlayerTurn(ClientHandler player) {
        if (this.isPlayer1Turn() && (player == player1)) {
            return true;
        } else return !this.isPlayer1Turn() && (player == player2);
    }

    public boolean isPlayer1Turn() {
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

        player1.sendMessage("Your turn!");
        player2.sendMessage("Waiting for Player 1...");


    }

    public Game getGame() {
        return this.game;
    }
}

