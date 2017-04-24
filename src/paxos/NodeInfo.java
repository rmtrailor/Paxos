package paxos;

/**
 * NodeInfo is a class that represents the information for a Paxos node. Used
 * in the Membership class.
 */
public class NodeInfo {
    private int id;
    private int port;
    private String status;

    public NodeInfo(int id, int port, String status) {
        this.id = id;
        this.port = port;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "id=" + id +
                ", port=" + port +
                ", status='" + status + '\'' +
                '}';
    }
}
