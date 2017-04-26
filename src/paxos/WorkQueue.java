package paxos;

import java.util.LinkedList;

/**
 * WorkQueue.java
 *
 * Original code from IBM: http://www.ibm.com/developerworks/library/j-jtp0730/
 * This Queue was used previously in Software Development CS 212 and is being reused for this server.
 * Some modifications are made for this server.
 *
 */
public class WorkQueue {
    private int numThreads;
    private Worker[] threads;
    private LinkedList queue;
    private volatile boolean running = true;

    /**
     * Constructor for the WorkQueue object.
     * @param numThreads The number of threads in to be in the work queue
     */
    public WorkQueue (int numThreads) {
        this.numThreads = numThreads;
        this.queue = new LinkedList();
        this.threads = new Worker[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Worker();
            threads[i].start();
        }
    }

    /**
     * Adds work to queue and notifies the queue that work has been added.
     * @param r Runnable object
     */
    public void execute (Runnable r) {
        if (running) {
            synchronized(queue) {
                queue.addLast(r);
                queue.notify();
            }
        }
    }

    /**
     * Sets the running to false and notifies the queue that the WorkQueue has been shutdown.
     */
    public void shutdown() {
        running = false;
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    /**
     * Joins each thread in the thread pool.
     */
    public void awaitTermination() {
        for (Worker thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Worker extends Thread {
        public void run () {
            Runnable r;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty() && running) {
                        try {
                            queue.wait();
                        }
                        catch (InterruptedException ignored) {
                        }
                    }
                    if (queue.isEmpty() && !running) {
                        break;
                    }
                    r = (Runnable) queue.removeFirst();
                }
                try {
                    r.run();
                } catch (RuntimeException e) {
                    e.printStackTrace();;
                }
            }
        }
    }
}
