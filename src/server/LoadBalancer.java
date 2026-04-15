package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private static String[] serverHosts = {"localhost", "localhost"};
    private static int[] serverPorts = {5001, 5002};
    
    // Tracks how many active connections each server has
    private static AtomicInteger[] serverLoad = {
        new AtomicInteger(0),
        new AtomicInteger(0)
    };

    public static void main(String[] args) throws IOException {
        ServerSocket balancer = new ServerSocket(5000);
        System.out.println("Load Balancer running on port 5000...");
        System.out.println("Strategy: Least Load (routes to least busy server)");

        while (true) {
            Socket clientSocket = balancer.accept();
            int index = getLeastLoadedServer();
            if (index == -1) {
                System.out.println("All servers are DOWN!");
                clientSocket.close();
                continue;
            }
            System.out.println("Routing to Server " + (index + 1) 
                + " [Load: S1=" + serverLoad[0].get() 
                + ", S2=" + serverLoad[1].get() + "]");
            new Thread(() -> forwardToServer(clientSocket, index)).start();
        }
    }

    // Returns index of server with least load
    // If both have equal load, picks Server 1
    // If a server is DOWN, skips it
    private static int getLeastLoadedServer() {
        int bestIndex = -1;
        int lowestLoad = Integer.MAX_VALUE;

        for (int i = 0; i < serverPorts.length; i++) {
            if (isServerAlive(i)) {
                int load = serverLoad[i].get();
                if (load < lowestLoad) {
                    lowestLoad = load;
                    bestIndex = i;
                }
            }
        }
        return bestIndex;
    }

    private static boolean isServerAlive(int index) {
        try (Socket test = new Socket(serverHosts[index], serverPorts[index])) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void forwardToServer(Socket clientSocket, int serverIndex) {
        // Increment load when connection starts
        serverLoad[serverIndex].incrementAndGet();
        System.out.println("Server " + (serverIndex + 1) + " load increased to " + serverLoad[serverIndex].get());

        try (
            Socket serverSocket = new Socket(serverHosts[serverIndex], serverPorts[serverIndex]);
            InputStream clientIn = clientSocket.getInputStream();
            OutputStream clientOut = clientSocket.getOutputStream();
            InputStream serverIn = serverSocket.getInputStream();
            OutputStream serverOut = serverSocket.getOutputStream()
        ) {
            System.out.println("Connected to Server " + (serverIndex + 1) + " on port " + serverPorts[serverIndex]);

            Thread t1 = new Thread(() -> {
                try { clientIn.transferTo(serverOut); }
                catch (IOException e) {}
            });

            Thread t2 = new Thread(() -> {
                try { serverIn.transferTo(clientOut); }
                catch (IOException e) {}
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

        } catch (Exception e) {
            System.out.println("Server " + (serverIndex + 1) + " failed: " + e.getMessage());
        } finally {
            // Decrement load when connection ends
            serverLoad[serverIndex].decrementAndGet();
            System.out.println("Server " + (serverIndex + 1) + " load decreased to " + serverLoad[serverIndex].get());
        }
    }
}
