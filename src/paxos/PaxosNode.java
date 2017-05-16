package paxos;


import connection.Request;
import connection.Response;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PaxosNode is a class that represents an instance of a node within Paxos.
 */
public class PaxosNode {

    private static Logger LOGGER = Logger.getLogger(PaxosNode.class.getName());

    private final int id;
    private final int port;
    private final int numNodes;
    private final int numThreads;
    private boolean isRunning;
    private WorkQueue threadPool;
    private Membership membership;
    private ReadWriteLock lock;
    private Log log;

    public PaxosNode(int id) throws IOException {
        // Setup handler for logger
        FileHandler handler = new FileHandler("logs/node_" + id + ".log");
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);

        this.id = id;
        this.port = 8000 + id;
        this.numNodes = 3; // Default membership to this number of nodes
        this.numThreads = 10;
        this.lock = new ReadWriteLock();
        this.log = new Log(this.id, this.numNodes);

        // TODO: Give basic membership based on number of nodes, then set initialized
        this.membership = new Membership(this.id, this.port);

        // Hack-y membership creation
        for (int i = 0; i < this.numNodes; i++)
            this.membership.createNode(i, 8000 + i, "UP");

        this.membership.setInitialized();
    }

    /**
     * Starts the Paxos node with its socket and threadpool, then sets the node to run.
     */
    public void start() {
        try {
            ServerSocket serve = new ServerSocket(this.port);
            threadPool = new WorkQueue(numThreads);
            isRunning = true;
            LOGGER.log(Level.FINE, "Server id: {0} started on port: {1}", new Object[] { this.id, this.port });
            run(serve);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Shuts down the Paxos node threadpool then exits.
     */
    public void shutdown() {
        this.threadPool.shutdown();
        this.threadPool.awaitTermination();
        this.isRunning = false;
        LOGGER.log(Level.FINE, "Sever id: {0} successfully shutdown.", this.id);
        System.exit(0);
    }

    /**
     * Run-loop for the Paxos node to accept requests on its socket.
     * @param serve The socket to accept requests on
     * @throws IOException
     */
    private void run(ServerSocket serve) throws IOException {
        while (isRunning) {
            Socket sock = serve.accept();
            Worker worker = new Worker(sock);
            threadPool.execute(worker);
        }
    }

    /**
     * Job that the node worker must complete. In this case, it must handle the given request.
     * We use a lock here to serialize the Paxos node. Therefore, only one job can be completed at any given time.
     * @param sock Node socket
     */
    private void performJob(Socket sock) {
        try {
            lock.lockWrite();
            getRequest(sock);
            lock.unlockWrite();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Read the request delivered through the socket.
     * @param sock Node socket
     * @throws IOException
     */
    private void getRequest(Socket sock) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
        String message = "";
        String line = in.readLine();

        parseRequest(sock, line);

        while (line != null && !line.trim().isEmpty()) {
            message += line + '\n';
            line = in.readLine();
        }
    }

    /**
     * Parses the request and sends it to the API to be handled
     * @param sock Node socket
     * @param request The received request
     * @throws IOException
     */
    private void parseRequest(Socket sock, String request) throws IOException {
        if (sock == null || request == null) return;

        final String REGEX = "(\\S*)\\s*(/api/([^\\?]*))\\?([^=]*=[^&]*)((&[^=]*=[^&]*)*)\\sHTTP/1.*";
        Pattern p = Pattern.compile(REGEX);
        Response response;
        JSONObject content;

        LOGGER.log(Level.INFO, "Request: {0}", request);

        Matcher m = p.matcher(request);
        if (m.find()) {

            content = API(m);
            System.out.println("Content: " + content.toString());

            if (content.get("success").toString().equals("true")) {
                response = new Response(200, content);
                response.sendResponse(sock);
            }
            // Failed success
            else {
                if (content.get("err").toString().equals("Method not found")) {
                    response = new Response(404, content);
                }
                else {
                    response = new Response(200, content);
                }
                response.sendResponse(sock);
            }
        }
        else {
            content = new JSONObject();
            content.put("success", "false");
            content.put("err", "Incorrectly formatted request.");
            response = new Response(400, content);
            response.sendResponse(sock);
        }
    }

    /**
     * Reads the parsed HTTP message and makes the corresponding API call if found.
     * @param m Parsed message
     * @return Results
     */
    private JSONObject API(Matcher m) throws MalformedURLException {
        JSONObject content = new JSONObject();

        String method = m.group(3).toLowerCase();

        switch (method) {
            case Communication.SEND_VALUE:
                int value = Integer.parseInt(m.group(4).split("=")[1]);
                int seqnum = this.log.generateNextSeqnum();
                LOGGER.log(Level.FINE, "Got request from client for consensus on value: {0}, attempting with seq num: {1}",
                        new Object[] {value, seqnum});

                content = proposalPhase(seqnum, value);
                break;
            case Communication.PROPOSE_SEQNUM:
                LOGGER.log(Level.FINE, "Got a proposal for seq num: {0}", m.group(4).split("=")[1]);

                content = this.log.promiseSeqnum(Integer.parseInt(m.group(4).split("=")[1]));
                LOGGER.log(Level.FINE, "Response: {0}", content.toString());
                break;
            case Communication.ACCEPT_VALUE:
                LOGGER.log(Level.FINE, "Got an accept request for seq num: {0} for value {1}",
                        new Object[] { m.group(4).split("=")[1], m.group(5).split("=")[1] });

                content = this.log.acceptValue(Integer.parseInt(m.group(4).split("=")[1]),
                        Integer.parseInt(m.group(5).split("=")[1]));

                LOGGER.log(Level.FINE, "Response: {0}", content.toString());
                break;
            case Communication.COMMIT:
                LOGGER.log(Level.FINE, "Got a commit request");

                content = this.log.commitValue(Integer.parseInt(m.group(4).split("=")[1]),
                        Integer.parseInt(m.group(5).split("=")[1]));
                break;
            default:
                // If we've reached here then 404 not found
                content.put("success", "false");
                content.put("err", "Method not found");
        }

        return content;
    }

    /**
     * Function to start proposal phase of paxos protocal to propose a seqnum to the other paxos nodes
     * @param seqnum Given sequence number
     * @param value Value to be agreed upon
     * @return  Results - whether or not the value was agreed upon
     * @throws MalformedURLException
     */
    private JSONObject proposalPhase(int seqnum, int value) throws MalformedURLException {
        List<NodeInfo> nodes = this.membership.getNodesCopy();
        JSONObject results = new JSONObject();
        int numAgrees = 0;

        JSONObject info = new JSONObject();
        info.put("seqnum", seqnum);

        List<JSONObject> prevAcceptedVals = new ArrayList<>();

        for (NodeInfo node : nodes) {
            if (node.getId() == this.id) continue;

            Request request = Communication.sendMessage(node.getId(), node.getPort(), Communication.PROPOSE_SEQNUM, info);

            if (request.getContent().get("success").equals("true") && request.getContent().get("reply").equals("agree"))
                numAgrees++;

            // This is done if we got a value sent back to the proposer
            if (request.getContent().get("seqnum") != null) {
                JSONObject newValue = new JSONObject();
                newValue.put("seqnum", request.getContent().get("seqnum"));
                newValue.put("value", request.getContent().get("value"));
                prevAcceptedVals.add(newValue);
            }

            // Check for quorum is in for-loop to optimize, since we only need the quorum of promises
            if (numAgrees >= this.membership.getQuorum()) {
                // If any values were sent back, pick the last value that was added (arbitrary decision)
                if (prevAcceptedVals.size() > 0) {
                    seqnum = Integer.parseInt(prevAcceptedVals.get(prevAcceptedVals.size() - 1).get("seqnum").toString());
                    value = Integer.parseInt(prevAcceptedVals.get(prevAcceptedVals.size() - 1).get("value").toString());
                }

                return acceptPhase(seqnum, value);
            }

        }

        // If we get here then we did not get an agreed upon value
        results.put("success", "false");
        results.put("err", "Rejected Seqnum {" + seqnum + "} Value {" + value + "}");
        return results;
    }

    /**
     * Function to start accept phase of paxos protocal to request acceptance of a seqnum with a corresponding value.
     * @param seqnum Given sequence number
     * @param value Given value
     * @return Results - whether or not a value was accepted
     * @throws MalformedURLException
     */
    private JSONObject acceptPhase(int seqnum, int value) throws MalformedURLException {
        List<NodeInfo> nodes = this.membership.getNodesCopy();
        JSONObject results = new JSONObject();
        int numAccepts = 0;

        JSONObject info = new JSONObject();
        info.put("seqnum", seqnum);
        info.put("value", value);

        for (NodeInfo node: nodes) {
            if (node.getId() == this.id) continue;

            Request request = Communication.sendMessage(node.getId(), node.getPort(), Communication.ACCEPT_VALUE, info);

            if (request.getContent().get("success").equals("true") && request.getContent().get("reply").equals("accepted"))
                numAccepts++;

            // Optimize again
            if (numAccepts >= this.membership.getQuorum()) {
                // Ask nodes to commit any values since we've done a successful accept run
                commitPhase(nodes, info);

                results.put("success", "true");
                results.put("msg", "Committed Seqnum {" + seqnum + "} Value {" + value + "}");
                return results;
            }
        }

        // If we get here then we did not get an accepted value
        results.put("success", "false");
        results.put("err", "Value was not accepted");
        return results;
    }

    /**
     * Sends a commit message to all nodes in membership to log the seqnum and value.
     * @param nodes Nodes in membership
     * @param info Info about seqnum and value
     * @throws MalformedURLException
     */
    private void commitPhase(List<NodeInfo> nodes, JSONObject info) throws MalformedURLException {

        for (NodeInfo node : nodes) {
            if (node.getId() == this.id) continue;

            Communication.sendMessage(node.getId(), node.getPort(), Communication.COMMIT, info);
        }
    }

    /**
     * Set the membership for this node. Automatically seals the membership so that
     * no further nodes can be added.
     * @param nodes The copy of nodes to add
     */
    public void setMembership(List<NodeInfo> nodes) {
        this.membership.setNodes(nodes);
        this.membership.setInitialized();
    }

    /**
     * Private worker class that performs jobs based on the request.
     */
    private class Worker implements Runnable {
        Socket sock;

        public Worker(Socket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            performJob(this.sock);
        }
    }
}
