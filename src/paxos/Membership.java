package paxos;

import java.util.ArrayList;

/**
 * Data structure for Paxos nodes to keep track of basic membership
 */
public class Membership {

    private NodeInfo myInfo;
    private ArrayList<NodeInfo> nodes;
    private ReadWriteLock lock;
    private boolean initialized;

    public Membership(int id, int port) {
        this.myInfo = new NodeInfo(id, port, "UP");
        this.nodes = new ArrayList<>();
        this.lock = new ReadWriteLock();
        this.initialized = false;
    }

    public Membership() {
        this.nodes = new ArrayList<>();
        this.lock = new ReadWriteLock();
        this.initialized = false;
    }

    /**
     * Sets the membership to initialized which means that no new nodes can
     * be added or modified (besides setting status). This is to ensure that
     * the membership stays consistent across the Paxos layer, which follows
     * the convention for Paxos.
     */
    public void setInitialized() {
        lock.lockWrite();
        this.initialized = true;
        lock.unlockWrite();
    }

    /**
     * Creates a node of information for the membership
     * @param id Id of the Paxos node
     * @param port Port of the Paxos node
     * @param status Status of the paxos node
     */
    public void createNode(int id, int port, String status) {
        lock.lockWrite();
        if (initialized) {
            lock.unlockWrite();
            return;
        }
        nodes.add(new NodeInfo(id, port, status));
        lock.unlockWrite();
    }

    /**
     * Adds a group of nodes
     * @param nodes Copy of a group of nodes
     */
    public void setNodes(ArrayList<NodeInfo> nodes) {
        lock.lockWrite();
        if (initialized) {
            lock.unlockWrite();
            return;
        }
        for (NodeInfo node : nodes) {
            if (!nodes.contains(node))
                nodes.add(node);
        }
        lock.unlockWrite();
    }

    /**
     * Creates a deep copy of the nodes in the membership
     * @return Deep copy of the nodes in the membership
     */
    public ArrayList<NodeInfo> getNodesCopy() {
        lock.lockRead();
        ArrayList<NodeInfo> nodesCopy = new ArrayList<>();
        for (NodeInfo node: this.nodes) {
            nodesCopy.add(new NodeInfo(node.getId(), node.getPort(), node.getStatus()));
        }
        lock.unlockRead();
        return nodesCopy;
    }

    public int getId() {
        return this.myInfo.getId();
    }

    public int getPort() {
        return this.myInfo.getPort();
    }

    public String getStatus() {
        return this.myInfo.getStatus();
    }

    public boolean isAlive() {
        return this.myInfo.getStatus().equals("UP");
    }

    public void setAlive() {
        this.myInfo.setStatus("UP");
    }

    public void setDown() {
        this.myInfo.setStatus("DOWN");
    }
}