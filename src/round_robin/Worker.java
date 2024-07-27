package round_robin;

import java.io.*;
import java.net.*;

public class Worker {
    // Class member variables
    private final String masterAddress;
    private final int masterPort;
    private final String previousNodeAddress;
    private final String nextNodeAddress;
    private final int startPort;
    private final int myIndex;
    private final int totalWorkers;

    /**
     * Constructor to initialize the Worker class with provided parameters.
     * @param masterAddress address of the master node.
     * @param masterPort port of the master node.
     * @param previousNodeAddress address of the previous node.
     * @param nextNodeAddress address of the next node.
     * @param startPort starting port number for the worker nodes.
     * @param myIndex index of this worker node.
     * @param totalWorkers total number of worker nodes.
     */
    public Worker(String masterAddress, int masterPort, String previousNodeAddress, String nextNodeAddress, int startPort, int myIndex, int totalWorkers) {
        this.masterAddress = masterAddress;
        this.masterPort = masterPort;
        this.previousNodeAddress = previousNodeAddress;
        this.nextNodeAddress = nextNodeAddress;
        this.startPort = startPort;
        this.myIndex = myIndex;
        this.totalWorkers = totalWorkers;
    }

    /**
     * Method to start the Worker node.
     * @throws IOException if an I/O error occurs.
     */
    public void start() throws IOException {
        // Start the server thread to accept connections
        new Thread(this::startServer).start();

        // Connect to the previous node and start communication
        connectToNode();
    }

    /**
     * Method to start the server and listen for incoming connections.
     */
    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(startPort + myIndex)) {
            System.out.println("Worker " + myIndex + " listening on port " + (startPort + myIndex));

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message = in.readLine();
                    System.out.println("Received message: " + message);

                    // Process and forward the message
                    String newMessage = message + ", Worker" + myIndex;

                    if (myIndex == totalWorkers - 1) {
                        // If the current node is the last one, send the message to the master
                        sendToMaster(newMessage);
                    } else {
                        // Otherwise, continue with the message passing
                        sendToNextWorker(newMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to connect to the previous node and start communication.
     * @throws IOException if an I/O error occurs.
     */
    private void connectToNode() throws IOException {
        int previousNodePort = this.startPort + this.myIndex - 1;
        Socket socket;
        try {
            if (this.myIndex == 0) {
                // If this is the first node, connect to the master node
                socket = new Socket(masterAddress, masterPort);
                System.out.println("Connected to the node at " + masterAddress + ":" + masterPort);
            } else {
                // Otherwise, connect to the previous worker node
                socket = new Socket(previousNodeAddress, previousNodePort);
                System.out.println("Connected to the node at " + previousNodeAddress + ":" + previousNodePort);
            }
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message);
                String newMessage = message + ", Worker" + myIndex;
                sendToNextWorker(newMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to send a message to the next worker node.
     * @param message the message to send.
     */
    private void sendToNextWorker(String message) {
        try (Socket nextWorkerSocket = new Socket(nextNodeAddress, getNextWorkerPort())) {
            PrintWriter out = new PrintWriter(nextWorkerSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to the next worker: " + e.getMessage());
        }
    }

    /**
     * Method to send a message to the master node.
     * @param message the message to send.
     */
    private void sendToMaster(String message) {
        try (Socket masterSocket = new Socket(masterAddress, masterPort)) {
            PrintWriter out = new PrintWriter(masterSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to the master: " + e.getMessage());
        }
    }

    /**
     * Method to get the port number of the next worker node.
     * @return the port number of the next worker node.
     */
    private int getNextWorkerPort() {
        return startPort + (myIndex + 1) % totalWorkers;
    }

    /**
     * Main method to initialize and start the Worker node.
     * @param args command-line arguments.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.out.println("Usage: java Worker <master_address> <master_port> <previous_node_address> " +
                    "<next_node_address> <start_port> <index> <total_workers>");
            return;
        }

        // Parse the command-line arguments
        String masterAddress = args[0];
        int masterPort = Integer.parseInt(args[1]);
        String previousNodeAddress = args[2];
        String nextNodeAddress = args[3];
        int startPort = Integer.parseInt(args[4]);
        int index = Integer.parseInt(args[5]);
        int totalWorkers = Integer.parseInt(args[6]);

        // Create and start the Worker node
        Worker workerNode = new Worker(masterAddress, masterPort, previousNodeAddress, nextNodeAddress, startPort, index, totalWorkers);
        workerNode.start();
    }
}
