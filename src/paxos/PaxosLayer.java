package paxos;

import connection.Request;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.*;

/**
 * Paxos Layer is the API layer used by the Client to communicate with Paxos nodes.
 */
public class PaxosLayer {

    private static Logger LOGGER = Logger.getLogger(PaxosLayer.class.getName());
    
    private final int numNodes;
    private Membership membership;

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
    }


    /**
     * API for sending a request to the Paxos nodes.
     * @param id The id of the Paxos node the client wishes to send a request to.
     */
    public JSONObject sendRequest(int id, int seqnum, int value) throws MalformedURLException {
        JSONObject results = new JSONObject();

        if (id < 0 || id > numNodes) {
            // invalid id
            results.put("success", "false");
            results.put("err", "Invalid id");
            return results;
        }

        NodeInfo nodeCopy = this.membership.getNode(id);

        if (nodeCopy == null) {
            results.put("success", "false");
            results.put("err", "Node not found");
            return results;
        }

        JSONObject info = new JSONObject();
        info.put("seqnum", seqnum);
        info.put("value", value);

        // Make the request and save the response
        Request request = Communication.sendMessage(id, nodeCopy.getPort(), Communication.GET_VALUE, info);

        return request.getContent();
    }
}
