package paxos;

import java.io.IOException;
import java.util.logging.*;

/**
 * This class will set up the layer for implementing Paxos and will accept requests
 */
public class PaxosLayer {

    private static Logger LOGGER = Logger.getLogger(PaxosLayer.class.getName());

    private PaxosNode[] nodes;
    private final int numNodes;

    /**
     * Initializes the layer by setting up N nodes where N = numNodes
     * @param numNodes The number of paxos nodes to create
     */
    public PaxosLayer(int numNodes) throws IOException {
        // Setup handler for logger
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);

        // Setup the Paxos nodes
        this.numNodes = numNodes;
        this.nodes = new PaxosNode[this.numNodes];
        for (int i = 0; i < this.numNodes; i++) {
            nodes[i] = new PaxosNode(i);
        }
        LOGGER.log(Level.FINE, "Created {0} Paxos nodes.", this.numNodes);
    }

    /**
     * API for starting all the Paxos nodes in the Paxos layer
     */
    public void startNodes() {
        for (int i = 0; i < this.numNodes; i++) {
            this.nodes[i].start();
        }
    }

    /**
     * API for sending a request to the Paxos nodes.
     * @param id The id of the Paxos node the client wishes to send a request to.
     */
    public void sendRequest(int id) {
        if (id < 0 || id > numNodes) {
            // invalid id
        }

    }
}
