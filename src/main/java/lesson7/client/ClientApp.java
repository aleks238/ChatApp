package lesson7.client;

import lesson7.constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientApp extends JFrame {

    private JTextField textField;
    private JTextArea textArea;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    ClientApp() {
        try {
            openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        prepareGUI();
    }
    private void openConnection() throws IOException {
        socket = new Socket(Constants.SERVER_ADDRESS,Constants.SERVER_PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        String messageFromServer = dataInputStream.readUTF();
                        if(messageFromServer.equals("/end")){
                            break;
                        }
                        textArea.append(messageFromServer);
                        textArea.append("\n");
                    }
                    textArea.append("Connection closed");
                    textField.setEnabled(false);
                    closeConnection();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void closeConnection(){
        try {
            dataOutputStream.close();
        }catch (Exception e){
     }
        try {
            dataInputStream.close();

        }catch (Exception e){
        }
        try {
            socket.close();

        }catch (Exception e){
        }
    }
    private void sendMessageToServer(){
        if (textField.getText().trim().isEmpty()){
            return;
        }
        try {
            dataOutputStream.writeUTF(textField.getText());
            textField.setText("");
            textField.grabFocus();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void prepareGUI() {
        setBounds(200,200,500,500);
        setTitle("Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400,50));
        JButton button = new JButton("Send");
        panel.add(button, BorderLayout.EAST);
        textField = new JTextField();
        panel.add(textField, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);


        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout());
        JTextField loginTextField = new JTextField();
        loginPanel.add(loginTextField);
        JTextField passwordTextField = new JTextField();
        loginPanel.add(passwordTextField);
        JButton authButton = new JButton("Войти в чат");
        loginPanel.add(authButton);
        add(loginPanel, BorderLayout.NORTH);


        authButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dataOutputStream.writeUTF(Constants.AUTH_COMMAND + " " + loginTextField.getText() + " " +  passwordTextField.getText());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToServer();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToServer();
            }
        });
        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientApp();
            }
        });
    }
}
