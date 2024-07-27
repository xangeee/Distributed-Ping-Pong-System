package one_to_one;

import java.io.*;
import java.net.Socket;

public class Worker {
    private String masterAddress;
    private int masterPort;

    /**
     * Constructor to initialize the Worker class with the specified master address and port.
     * @param masterAddress Address of the master node.
     * @param masterPort Port number of the master node.
     */
    public Worker(String masterAddress, int masterPort) {
        this.masterAddress = masterAddress;
        this.masterPort = masterPort;
    }

    /**
     * Starts the Worker node, connects to the master node, and handles communication.
     */
    public void start() {
        try {
            Socket socket = new Socket(masterAddress, masterPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to master at " + masterAddress + ":" + masterPort);

            // Listening for messages from the master
            String message;
            while ((message = in.readLine()) != null) {
                if ("ping".equals(message)) {
                    System.out.println("Received ping from master");
                    // Respond with a pong message
                    out.println("pong");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to initialize and start the Worker node.
     * @param args Command-line arguments: master address and port number.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Worker <master_address> <master_port>");
            return;
        }

        // Parse the command-line arguments
        String masterAddress = args[0];
        int masterPort = Integer.parseInt(args[1]);

        // Create and start the Worker node
        Worker workerNode = new Worker(masterAddress, masterPort);
        workerNode.start();
    }
}
