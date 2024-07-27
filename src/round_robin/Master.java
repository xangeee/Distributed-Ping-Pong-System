package round_robin;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Master {
    private final int port;
    private final List<Socket> workerSockets = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Master node is running on port " + port);

            // Scheduled task to check and maintain active connections every 30 seconds
            scheduler.scheduleAtFixedRate(this::checkConnections, 10, 30, TimeUnit.SECONDS);

            // Continuously accept connections from worker nodes
            while (true) {
                Socket workerSocket = serverSocket.accept();
                synchronized (this.workerSockets) {
                    this.workerSockets.add(workerSocket);
                    System.out.println("Connected to worker: " + workerSocket.getInetAddress());
                }
                System.out.println("Total connected workers: " + this.workerSockets.size());

                // Listen for messages from this worker in a new thread
                new Thread(new WorkerHandler(workerSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the socket connection is still alive by attempting a minimal write.
     * @param socket The socket to check.
     * @return True if the socket is alive, false otherwise.
     */
    private boolean isSocketAlive(Socket socket) {
        try {
            // Check connection status
            socket.getOutputStream().write(0);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Periodically checks if each socket in the list is still connected.
     * If a socket is not alive, it is removed from the list.
     */
    private void checkConnections() {
        System.out.println("Checking connections...");
        Iterator<Socket> iterator = this.workerSockets.iterator();
        while (iterator.hasNext()) {
            Socket socket = iterator.next();
            if (!isSocketAlive(socket)) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iterator.remove();
                System.out.println("Disconnected worker removed from the list.");
            }
        }
    }

    /**
     * Initiates a round-robin message chain by sending a message to the first worker.
     */
    public void initiateRoundRobin() {
        System.out.println("Initiating round-robin message chain.");
        try {
            if (this.workerSockets.size() > 0) {
                Socket firstWorker = workerSockets.get(0);
                PrintWriter out = new PrintWriter(firstWorker.getOutputStream(), true);
                out.println("Chain:");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedules the round-robin message chain initiation at a fixed rate.
     * @param periodInSeconds The period between consecutive executions.
     */
    public void scheduleRoundRobin(int periodInSeconds) {
        scheduler.scheduleAtFixedRate(this::initiateRoundRobin, 0, periodInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Inner class to handle communication from each worker.
     */
    private class WorkerHandler implements Runnable {
        private final Socket workerSocket;

        /**
         * Constructor to initialize the WorkerHandler with a worker socket.
         * @param workerSocket The socket connected to the worker node.
         */
        public WorkerHandler(Socket workerSocket) {
            this.workerSocket = workerSocket;
        }

        /**
         * Listens for messages from the worker and processes them.
         */
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received from worker: " + message);
                    // Additional logic can be implemented here based on the message content
                }
            } catch (IOException e) {
                System.out.println("Error handling worker input");
                e.printStackTrace();
            }
        }
    }

    /**
     * Main method to initialize and start the Master node.
     * @param args Command-line arguments: port and round-robin period in seconds.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Master <port> <round-robin period in seconds>");
            return;
        }

        // Parse the command-line arguments
        int port = Integer.parseInt(args[0]);
        int periodInSeconds = Integer.parseInt(args[1]);

        // Create and start the Master node
        Master masterNode = new Master(port);
        masterNode.scheduleRoundRobin(periodInSeconds); // Schedule round-robin initiation
        masterNode.startServer(); // Start server to accept connections
    }
}
