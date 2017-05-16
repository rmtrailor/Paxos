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

        if (this.membership.getNumDown() > this.membership.getQuorum()) {
            JSONObject response = new JSONObject();
            response.put("success", "false");
            response.put("err", "Number of nodes down is more than quorum needed");
            return response;
        }

        // For our use, we'll just select a paxos node at random to save us from any
        // complicated overhead.
        int nodeId = rand.nextInt(this.numNodes);
        NodeInfo nodeCopy = this.membership.getNode(nodeId);

        // Try to not get a down paxos node
        while (nodeCopy.getStatus().equals("DOWN")) {
            nodeId = rand.nextInt(this.numNodes);
            nodeCopy = this.membership.getNode(nodeId);
        }

        JSONObject info = new JSONObject();
        info.put("value", value);

        Request request = Communication.sendMessage(nodeId, nodeCopy.getPort(), Communication.GET_VALUE, info);

        if (request != null && request.getContent().get("err") != null && request.getContent().get("err").equals("No response")) {
            this.membership.getNode(nodeId).setStatus("DOWN");
            this.membership.updateNumDown();
        }

        return request != null ? request.getContent() : null;
    }
}
