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
    public static final String SEND_VALUE = "sendvalue";

    // Messaging API
    public static final String GET_VALUE = "GET_VALUE";

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
        Request request;
        URL url;
        String params;

        switch(type) {
            case GET_VALUE:
                params = "?id=" + id + "&seqnum=" + info.get("seqnum") + "&value=" + info.get("value");
                url = new URL(DOMAIN + port + API + SEND_VALUE + params);
                request = new Request(url);
                break;
            default:
                request = null;
        }

        return request;
    }
}
