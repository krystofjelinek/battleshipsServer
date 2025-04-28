package cz.vse.server;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Game game;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game(player1.toString(), player2.toString());
    }

    public Game getGame() {
        return game;
    }

    public void start() {
        player1.sendMessage("Game started! You are Player 1.");
        player2.sendMessage("Game started! You are Player 2.");
        // Implement turn-taking logic here
    }
}