package paxos;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.*;

public class PaxosNode {

    private static Logger LOGGER = Logger.getLogger(PaxosNode.class.getName());

    private final int id;
    private final int port;
    private final int numThreads;
    private boolean isRunning;
    private WorkQueue threadPool;
    private Membership membership;

    public PaxosNode(int id) throws IOException {
        // Setup handler for logger
        FileHandler handler = new FileHandler("logs/node_" + id + ".log");
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);

        this.id = id;
        this.port = 8000 + id;
        this.numThreads = 10;
        this.membership = new Membership(this.id, this.port);
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

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
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

    private void performJob(Socket sock) {
        // TODO: Parse request
    }

    /**
     * Set the membership for this node. Automatically seals the membership so that
     * no further nodes can be added.
     * @param nodes The copy of nodes to add
     */
    public void setMembership(ArrayList<NodeInfo> nodes) {
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
