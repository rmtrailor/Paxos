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
    private ReadWriteLock lock;
    private int promisedSeqnum;
    private int acceptedSeqnum;
    private int acceptedValue;

    public Log() {
        this.log = new HashMap<>();
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
     * Function to run the promise aspect of the log
     * @param seqnum Proposed sequence number
     * @return Response which details either an accept or reject
     */
    public JSONObject promiseSeqnum(int seqnum) {
        lock.lockWrite();
        JSONObject response = new JSONObject();

        // TODO: send back accepted value at some point

        if (seqnum < this.getHighestSeqnumInLog() || seqnum < promisedSeqnum) {
            // We can ignore this value
            response.put("success", "true");
            response.put("reply", "rejected");
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

        if (seqnum < this.getHighestSeqnumInLog() || seqnum < promisedSeqnum) {
            // Ignore request
            response.put("success", "true");
            response.put("reply", "rejected");
        }
        else {
            // Accept this value
            this.log.put(seqnum, value);
            response.put("success", "true");
            response.put("reply", "accepted");
        }

        lock.unlockWrite();
        return response;
    }
}
