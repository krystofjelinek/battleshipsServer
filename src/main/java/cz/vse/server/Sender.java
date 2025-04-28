package cz.vse.server;

public class Sender {
    private String message;
    private String sender;
    private String receiver;

    public Sender(String message, String sender, String receiver) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
    }

    public void sendMessage() {
        // Log the message being sent
        System.out.println("Sending message to client");
        // Send the message to the receiver
        // This is a placeholder for the actual sending logic
        System.out.println("Sending message: " + message + " from " + sender + " to " + receiver);
    }
}
