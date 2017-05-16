package paxos;

import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Log is a class that represents the stored key-value pairs of key => seqnum, and value => value
 */
public class Log {

    private Map<Integer, Integer> log;
    private int id;
    private int numNodes;
    private ReadWriteLock lock;
    private int promisedSeqnum;
    private int acceptedSeqnum;
    private int acceptedValue;

    public Log(int id, int numNodes) {
        this.log = new HashMap<>();
        this.id = id;
        this.numNodes = numNodes;
        this.lock = new ReadWriteLock();
        this.promisedSeqnum = -1;
        this.acceptedSeqnum = -1;
    }

    /**
     * Gets the highest sequence number in the log.
     * @return The highest number or -1 if the log is empty
     */
    private int getHighestSeqnumInLog() {
        Set<Integer> seqnums = this.log.keySet();
        if (seqnums.size() < 1) return -1;

        return Collections.max(seqnums);
    }

    /**
     * Generates a new seqnum that is higher than the highest sequence number this node has seen.
     * Uses "Paxos Made Live" paper's method of generating a new sequence number: s mod n = i
     * Where:
     *      s = the smallest sequence to generate
     *      n = the number of paxos nodes
     *      i = the id of this paxos node
     *
     * @return New sequence number
     */
    public int generateNextSeqnum() {
        lock.lockRead();
        int highestSoFar = this.getHighestSeqnumInLog();
        int newSeqnum = highestSoFar + 1; // try one more than what we've seen which sometimes saves time from generating

        while ((newSeqnum % this.numNodes) != this.id)
            newSeqnum++;

        lock.unlockRead();
        return newSeqnum;
    }

    /**
     * Generates a new seqnum using a previously rejected seqnum as a starting point rather than the highest known.
     * @param lastUsedSeqnum
     * @return
     */
    public int generateNextSeqnum(int lastUsedSeqnum) {
        lock.lockRead();
        int newSeqnum = lastUsedSeqnum + 1;

        while ((newSeqnum % this.numNodes) != this.id)
            newSeqnum++;

        lock.unlockRead();
        return newSeqnum;
    }

    /**
     * Function to run the promise aspect of the log
     * @param seqnum Proposed sequence number
     * @return Response which details either an accept or reject
     */
    public JSONObject promiseSeqnum(int seqnum) {
        lock.lockWrite();
        JSONObject response = new JSONObject();

        if (seqnum <= this.getHighestSeqnumInLog() || seqnum <= this.promisedSeqnum) {
            // We can ignore this value
            response.put("success", "true");
            response.put("reply", "rejected");
        }
        // True if this node has a previously accepted seqnum w/ value
        else if (this.acceptedSeqnum != -1) {
            System.out.println("ACCEPTED VALUE: " + this.acceptedValue);
            response.put("success", "true");
            response.put("reply", "agree");
            response.put("seqnum", this.acceptedSeqnum);
            response.put("value", this.acceptedValue);
        }
        else {
            // Promise this value
            this.promisedSeqnum = seqnum;
            response.put("success", "true");
            response.put("reply", "agree");
        }

        lock.unlockWrite();
        return response;
    }

    /**
     * Function to run the accept aspect of the log
     * @param seqnum Sequence number to accept
     * @param value  Value to accept
     * @return Response which details either an accept or reject
     */
    public JSONObject acceptValue(int seqnum, int value) {
        lock.lockWrite();
        JSONObject response = new JSONObject();

        if (seqnum <= this.getHighestSeqnumInLog() || seqnum < this.promisedSeqnum) {
            // Ignore request
            response.put("success", "true");
            response.put("reply", "rejected");
        }
        else {
            // Accept this value
            this.acceptedSeqnum = seqnum;
            this.acceptedValue = value;
            response.put("success", "true");
            response.put("reply", "accepted");
        }

        lock.unlockWrite();
        return response;
    }

    public JSONObject commitValue(int seqnum, int value) {
        lock.lockWrite();
        JSONObject response = new JSONObject();

        System.out.println("Committed: " + "Seqnum(" + seqnum + ") Value(" + value + ")");
        this.log.put(seqnum, value);

        // Reset values if we committed our promised value
        if (promisedSeqnum == seqnum) {
            this.promisedSeqnum = -1;
        }
        if (acceptedValue == value) {
            this.acceptedValue = -1;
            this.acceptedSeqnum = -1;
        }

        System.out.println("Log after committing:\n" + this.log.toString());

        lock.unlockWrite();

        response.put("success", "true");
        return response;
    }

    @Override
    public String toString() {
        return "Log{" +
                "log=" + log +
                '}';
    }
}
