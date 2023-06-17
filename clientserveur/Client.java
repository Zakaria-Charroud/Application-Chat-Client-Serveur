package clientserveur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends JFrame {

    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private JButton sendButton;

    private Socket socket;
    private OutputStream outputStream;
    private String clientName;

    public Client() {
        setTitle("Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        clientName = JOptionPane.showInputDialog(this, "Enter your name:");

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageTextField = new JTextField();
        bottomPanel.add(messageTextField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        connectToServer("localhost", 8080); 
    }

    private void connectToServer(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);
            outputStream = socket.getOutputStream();

            Thread receivingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            String message = new String(buffer, 0, bytesRead);
                            displayMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            receivingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageTextField.getText();
        try {
            outputStream.write((clientName + ": " + message).getBytes());
            outputStream.flush();
            displayMessage("Me: " + message);
            messageTextField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(String message) {
        chatTextArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}
