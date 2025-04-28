package cz.vse.server;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Acceptor {

    private static final Logger log = Logger.getLogger(Acceptor.class);
    private int port;
    private Message messageX;

    public Acceptor(String configFilePath) {
        this.port = readPortFromConfig(configFilePath);
    }

    private int readPortFromConfig(String configFilePath) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            properties.load(fis);
            return Integer.parseInt(properties.getProperty("server.port"));
        } catch (IOException | NumberFormatException e) {
            log.error("Failed to read port from config file. Using default port 12345.", e);
            return 8080;
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Handle the client in a separate thread
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
               // messageX = new Message(message);
                // Process the message (you can replace this with actual logic)
               // messageX.process(message);

                // Send a response back to the client
                writer.println("Message received: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) {
        // Placeholder for message processing logic
        System.out.println("Processing message: " + message);
    }
}