package broadcasting;

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
        try (Socket socket = new Socket(masterAddress, masterPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to master at " + masterAddress + ":" + masterPort);

            // Listening for messages from the master
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message from master: " + message);
                // Respond to the master after receiving the message
                out.println("Pong: Received your message: " + message);
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
            System.out.println("Usage: java Worker <masterAddress> <masterPort>");
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
