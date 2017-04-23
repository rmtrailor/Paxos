package client;

import paxos.PaxosLayer;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class acts as a client that will send requests to a Paxos Layer
 */
public class Client {

    private static Logger LOGGER = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) {
        // Setup handler for logger
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);

        // Create paxos layer, the number of nodes is specified by the second argument
        PaxosLayer layer = null;
        try {
            layer = new PaxosLayer(Integer.parseInt(args[1]));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
        layer.startNodes();
    }
}
