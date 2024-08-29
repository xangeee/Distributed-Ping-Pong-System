package one_to_one;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Master {
    private int port;
    private List<Socket> workerSockets = new ArrayList<>();

    /**
     * Constructor to initialize the Master class with the specified port.
     * @param port Port number on which the Master node listens for connections.
     */
    public Master(int port) {
        this.port = port;
    }

    /**
     * Starts the Master server to accept connections from worker nodes.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Master node is running on port " + port);

            // Accept connections from worker nodes
            while (true) {
                Socket workerSocket = serverSocket.accept();
                workerSockets.add(workerSocket);
                System.out.println("Connected to worker: " + workerSocket.getInetAddress());
                handleWorker(workerSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles communication with a connected worker node.
     * @param workerSocket The socket connected to the worker node.
     */
    private void handleWorker(Socket workerSocket) {
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(workerSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));

                // Send a ping message to the worker
                out.println("ping");

                // Read the response from the worker
                String response = in.readLine();
                if ("pong".equals(response)) {
                    System.out.println("Received pong from " + workerSocket.getInetAddress());
                } else {
                    System.out.println("Unexpected response from " + workerSocket.getInetAddress());
                }
            } catch (IOException e) {
                System.out.println("Error handling worker " + workerSocket.getInetAddress());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Main method to initialize and start the Master node.
     * @param args Command-line arguments: port number.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Master <port>");
            return;
        }

        // Parse the command-line arguments
        int port = Integer.parseInt(args[0]);

        // Create and start the Master node
        Master masterNode = new Master(port);
        masterNode.start();
    }
}
