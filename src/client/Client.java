package client;

import org.json.simple.JSONObject;
import paxos.PaxosLayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class acts as a client that will send requests to a Paxos Layer
 */
public class Client {

    private static Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final int numNodes;
    private PaxosLayer layer;

    public Client(int numNodes) {
        // Setup handler for logger
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);

        // Create paxos layer
        this.numNodes = numNodes;
        this.layer = null;

        try {
            this.layer = new PaxosLayer(this.numNodes);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Start the request sequence.
     * @throws MalformedURLException
     */
    public void startRequests() throws MalformedURLException {
        JSONObject req1 = this.layer.sendRequest(0, 1, 1);
        System.out.println(req1.toString());
    }
}
