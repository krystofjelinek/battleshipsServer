package cz.vse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Connection {
    private static final List<Socket> connections = Collections.synchronizedList(new ArrayList<>());
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    public void addConnection(Socket socket) {
        synchronized (connections) {
            connections.add(socket);
        }
        log.debug("Connection added: {}", socket);
    }

    public void removeConnection(Socket socket) {
        synchronized (connections) {
            if (connections.contains(socket)) {
                try {
                    socket.close();
                    log.debug("Connection closed: {}", socket);
                } catch (Exception e) {
                    log.error("Error closing connection: {}", e.getMessage());
                }
            } else {
                log.warn("Connection not found: {}", socket);
            }
        }
    }

    public void closeAllConnections() throws IOException {
        for (Socket socket : connections) {
            synchronized (connections) {
                socket.close();
                System.out.println("Connection closed: " + socket);
            }
        }
        connections.clear();
    }

    public List<Socket> getConnections() {
        synchronized (connections) {
            return connections;
        }
    }
}