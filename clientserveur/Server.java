package clientserveur;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

    private List<ClientInfo> clientList;

    public Server() {
        clientList = new ArrayList<>();
        startServer(8080); 
    }

    private void startServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientInfo clientInfo = new ClientInfo(clientSocket);
                clientList.add(clientInfo);

                Thread clientThread = new Thread(() -> {
                    try {
                        InputStream inputStream = clientSocket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            String message = new String(buffer, 0, bytesRead);
                            System.out.println("Received message: " + message);
                            broadcastMessage(message, clientInfo);
                        }
                    } catch (SocketException e) {
                        System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        removeClient(clientInfo);
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message, ClientInfo senderClient) {
        for (ClientInfo client : clientList) {
            if (client != senderClient) {
                try {
                    OutputStream outputStream = client.getSocket().getOutputStream();
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeClient(ClientInfo clientInfo) {
        clientList.remove(clientInfo);
        try {
            clientInfo.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startConversationMenu();
    }

    private void startConversationMenu() {
        Scanner scanner = new Scanner(System.in);
        String choice;
        boolean exit = false;

        while (!exit) {
            System.out.println("\nConversation Menu");
            System.out.println("1. Start a conversation with a client");
            System.out.println("2. Exit");

            System.out.print("Enter your choice: ");
            choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    startConversation();
                    break;
                case "2":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    private void startConversation() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the name of the client you want to talk to: ");
        String clientName = scanner.nextLine();

        System.out.print("Enter your message: ");
        String message = scanner.nextLine();

        // Find the client socket based on the name
        ClientInfo clientInfo = getClientInfoByName(clientName);

        if (clientInfo == null) {
            System.out.println("Client not found.");
        } else {
            try {
                OutputStream outputStream = clientInfo.getSocket().getOutputStream();
                outputStream.write(message.getBytes());
                outputStream.flush();
                System.out.println("Message sent to " + clientName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    private ClientInfo getClientInfoByName(String clientName) {
        for (ClientInfo clientInfo : clientList) {
            if (clientInfo.getName().equals(clientName)) {
                return clientInfo;
            }
        }
        return null;
    }

    private static class ClientInfo {

        private Socket socket;
        private String name;

        public ClientInfo(Socket socket) {
            this.socket = socket;
        }

        public Socket getSocket() {
            return socket;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
