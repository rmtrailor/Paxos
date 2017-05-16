package paxos;

import connection.Request;
import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Communication is a class that represents communications across the application.
 */
public class Communication {

    private static final String DOMAIN = "http://localhost:";

    // Node API
    public static final String API = "/api/";
    public static final String SEND_VALUE = "value.send";
    public static final String PROPOSE_SEQNUM = "value.propose";
    public static final String ACCEPT_VALUE = "value.accept";
    public static final String COMMIT = "value.commit";

    // Messaging API
    public static final String GET_VALUE = "GET_VALUE";

    // The maximum number of times a request will attempt to get a response
    private static final int MAX_ATTEMPTS = 2;

    /**
     * API for sending messages to Paxos nodes can be used by the Paxos Layer or a Paxos node
     * @param id Recipient Id
     * @param port Recipient port
     * @param type Message type
     * @param info Info needed for the params
     * @return Resulting request
     * @throws MalformedURLException
     */
    public static Request sendMessage(int id, int port, String type, JSONObject info) throws MalformedURLException {
        Request request = null;
        URL url;
        String params;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            switch (type) {
                // Used by the client to send a message for paxos to start consensus on a proposed value with sequence num
                case GET_VALUE:
                    params = "?value=" + info.get("value");
                    url = new URL(DOMAIN + port + API + SEND_VALUE + params);
                    request = new Request(url);
                    break;
                // Used by a Proposer Paxos Node to propose a new seqnum
                case PROPOSE_SEQNUM:
                    params = "?seqnum=" + info.get("seqnum");
                    url = new URL(DOMAIN + port + API + PROPOSE_SEQNUM + params);
                    request = new Request(url);
                    break;
                // Used by a Proposer Paxos Node to send out an accept message on a value
                case ACCEPT_VALUE:
                    params = "?seqnum=" + info.get("seqnum") + "&value=" + info.get("value");
                    url = new URL(DOMAIN + port + API + ACCEPT_VALUE + params);
                    request = new Request(url);
                    break;
                // Used by a Proposer to send a commit message to other nodes
                case COMMIT:
                    params = "?seqnum=" + info.get("seqnum") + "&value=" + info.get("value");
                    url = new URL(DOMAIN + port + API + COMMIT + params);
                    request = new Request(url);
                    break;
                default:
                    request = null;
            }

            if (request == null) continue;

            // If we weren't able to get a response, attempt again
            if (request.getContent().get("success").equals("false"))
                if (request.getContent().get("err").equals("No response"))
                    continue;

            // If we've reached this point, then we've gotten a response
            break;
        }

        return request;
    }
}
