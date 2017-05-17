import client.Client;
import paxos.PaxosNode;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Starter is a class that starts either a Client node or a Paxos Node.
 */
public class Starter {
    public static void main(String[] args) {

        /*
         * Usage (Client): Starter client <number of paxos nodes>
         * Usage (Paxos Node): Starter paxos <id>
         */
        if (args[1].equals("client")) {
            Client client = new Client(Integer.parseInt(args[2]));

            try {
                client.startRequests();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else if (args[1].equals("paxos")) {
            try {
                PaxosNode node = new PaxosNode(Integer.parseInt(args[2]));
                node.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.err.println("Incorrect arguments given.\nUsage(Client): Starter client\nUsage (Paxos Node): Starter paxos <id>");
        }
    }
}
