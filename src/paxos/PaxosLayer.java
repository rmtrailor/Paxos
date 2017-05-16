package paxos;

import connection.Request;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.logging.*;

/**
 * Paxos Layer is the API layer used by the Client to communicate with Paxos nodes.
 */
public class PaxosLayer {

    private static Logger LOGGER = Logger.getLogger(PaxosLayer.class.getName());

    private final int numNodes;
    private Membership membership;
    private Random rand;

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

        this.numNodes = numNodes;
        this.membership = new Membership();

        // Hack-y membership creation
        for (int i = 0; i < this.numNodes; i++)
            this.membership.createNode(i, 8000 + i, "UP");

        this.membership.setInitialized();
        this.rand = new Random();
    }


    /**
     * API for sending a request to the Paxos nodes.
     * @param value The value the client application is requesting consensus on
     */
    public JSONObject sendRequest(int value) throws MalformedURLException {
        // For our use, we'll just select a paxos node at random to save us from any
        // complicated overhead.
        int nodeId = rand.nextInt(3);
        NodeInfo nodeCopy = this.membership.getNode(nodeId);

        JSONObject info = new JSONObject();
        info.put("value", value);

        // Make the request and save the response
        Request request = Communication.sendMessage(nodeId, nodeCopy.getPort(), Communication.GET_VALUE, info);

        return request.getContent();
    }
}
