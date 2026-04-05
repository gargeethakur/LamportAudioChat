package server;

import java.io.*;
import java.net.*;

public class LoadBalancer {
    private static String[] serverHosts = {"localhost", "localhost"};
    private static int[] serverPorts = {5001, 5002};
    private static int currentIndex = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket balancer = new ServerSocket(5000);
        System.out.println("Load Balancer running on port 5000...");

        while (true) {
            Socket clientSocket = balancer.accept();
            int index = getNextServer();
            System.out.println("Routing client to Server " + (index + 1));
            new Thread(() -> forwardToServer(clientSocket, index)).start();
        }
    }

    private static synchronized int getNextServer() {
        int index = currentIndex;
        currentIndex = (currentIndex + 1) % serverPorts.length;
        return index;
    }

    private static boolean isServerAlive(int index) {
        try (Socket test = new Socket(serverHosts[index], serverPorts[index])) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void forwardToServer(Socket clientSocket, int serverIndex) {
        int attempts = serverPorts.length;
        int index = serverIndex;

        while (attempts > 0) {
            if (isServerAlive(index)) {
                try (
                    Socket serverSocket = new Socket(serverHosts[index], serverPorts[index]);
                    InputStream clientIn = clientSocket.getInputStream();
                    OutputStream clientOut = clientSocket.getOutputStream();
                    InputStream serverIn = serverSocket.getInputStream();
                    OutputStream serverOut = serverSocket.getOutputStream()
                ) {
                    System.out.println("Connected to Server " + (index + 1) + " on port " + serverPorts[index]);

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
                    return;

                } catch (Exception e) {
                    System.out.println("Server " + (index + 1) + " failed mid-connection! Trying next...");
                }
            } else {
                System.out.println("Server " + (index + 1) + " is DOWN! Trying next...");
            }

            index = (index + 1) % serverPorts.length;
            attempts--;
        }

        System.out.println("All servers are DOWN! Could not handle client.");
        try { clientSocket.close(); } catch (IOException e) {}
    }
}
