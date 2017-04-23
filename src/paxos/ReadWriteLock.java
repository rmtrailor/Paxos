package paxos;

/**
 * ReadWriteLock.java
 *
 * Read and Write locks used to synchronize the storage of messages.
 */
public class ReadWriteLock {
    private int readers;
    private int writers;

    public ReadWriteLock() {
        readers = 0;
        writers = 0;
    }

    /**
     * Locks on reading and increments readers.
     */
    public synchronized void lockRead() {
        while (writers > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
        readers++;
    }

    /**
     * Unlocks on reading.
     */
    public synchronized void unlockRead() {
        assert readers > 0;

        readers--;
        this.notifyAll();
    }

    /**
     * Locks on writing and increments writers.
     */
    public synchronized void lockWrite() {
        while (writers > 0 || readers > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
        writers++;
    }

    /**
     * Unlocks on writing.
     */
    public synchronized void unlockWrite() {
        assert writers > 0;

        writers--;
        this.notifyAll();

    }
}
