package broadcasting;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master {
    private int port;
    private List<Socket> workerSockets;
    private ExecutorService executorService;

    /**
     * Constructor to initialize the Master class with the specified port.
     * @param port Port number on which the Master node listens for connections.
     */
    public Master(int port) {
        this.port = port;
        this.workerSockets = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Starts the Master server to accept connections from worker nodes and handle broadcasting.
     */
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Master node is running on port " + port);

            // Thread to handle periodic tasks like broadcasting messages
            executorService.submit(() -> {
                try {
                    while (true) {
                        Thread.sleep(10000); // For example, broadcasting a message every 10 seconds
                        broadcastMessage("broadcast: ping");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Accept connections from worker nodes
            while (true) {
                Socket workerSocket = serverSocket.accept();
                synchronized (workerSockets) {
                    workerSockets.add(workerSocket);
                }
                System.out.println("Connected to worker: " + workerSocket.getInetAddress());

                // Handle responses from worker in a separate thread
                executorService.submit(() -> handleResponse(workerSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles responses from the worker node.
     * @param workerSocket The socket connected to the worker node.
     */
    private void handleResponse(Socket workerSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println("Received response from " + workerSocket.getInetAddress() + ": " + response);
            }
        } catch (IOException e) {
            System.out.println("Error reading response from " + workerSocket.getInetAddress());
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected worker nodes.
     * @param message The message to broadcast.
     */
    public void broadcastMessage(String message) {
        synchronized (workerSockets) {
            System.out.println("Broadcasting message to all workers.");
            for (Socket socket : workerSockets) {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    System.out.println("Failed to send message to " + socket.getInetAddress());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Main method to initialize and start the Master node.
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Master <port> ");
            return;
        }

        // Parse the command-line arguments
        int port = Integer.parseInt(args[0]);

        Master masterNode = new Master(port); // Create a Master node listening on port 8000
        masterNode.startServer(); // Start the server to accept connections
    }
}
