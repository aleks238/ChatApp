package lesson8.client;

import lesson8.constants.Constants;

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
    private JTextArea textAreaOnlineUsers;
    JScrollPane scrollPane;
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
        socket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String messageFromServer = dataInputStream.readUTF();
                        if (messageFromServer.equals("/end")) {
                            break;
                        }
                        if (messageFromServer.startsWith("Online: ")) {
                            String nickname;
                            String[] tokensMessage = messageFromServer.split("\\s+");
                            nickname = tokensMessage[1];
                            if (checkDoubleClientOnline(nickname)){
                                textAreaOnlineUsers.append(nickname);
                                textAreaOnlineUsers.append("\n");
                            }
                        } else if (messageFromServer.startsWith("Remove: ")){
                           String nickname;
                           String[] tokensMessageRemove = messageFromServer.split("\\s+");
                           nickname = tokensMessageRemove[1];
                           textAreaOnlineUsers.setText(textAreaOnlineUsers.getText().replace(nickname,""));
                        } else {
                            textArea.append(messageFromServer);
                            textArea.append("\n");
                        }
                    }
                    textArea.append("Connection closed");
                    textField.setEnabled(false);
                    closeConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private boolean checkDoubleClientOnline(String nickname) {
        String[] tokensTextArea = textAreaOnlineUsers.getText().split("\\s+");
        for (String token : tokensTextArea) {
            if (token.equals(nickname)) {
                return false;
            }
        }
        return true;
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
        setTitle("Chat application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN,15));
        textArea.setForeground(Color.white);
        textArea.setBackground(new Color(38, 39, 35));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);


        JPanel onlinePanel = new JPanel(new BorderLayout());
        onlinePanel.setPreferredSize(new Dimension(145,400));
        add(onlinePanel,BorderLayout.EAST);

        textAreaOnlineUsers = new JTextArea();
        textAreaOnlineUsers.setEditable(false);
        textAreaOnlineUsers.setLineWrap(true);
        textAreaOnlineUsers.append("Онлайн: ");
        textAreaOnlineUsers.append("\n");
        textAreaOnlineUsers.setFont(new Font("Consolas", Font.BOLD,14));
        textAreaOnlineUsers.setForeground(Color.white);
        textAreaOnlineUsers.setBackground(new Color(38, 39, 35));
        onlinePanel.add(textAreaOnlineUsers, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400,60));
        JButton sendButton = new JButton("Отправить");
        sendButton.setFont(new Font("Consolas", Font.BOLD,14));
        sendButton.setPreferredSize(new Dimension(160,40));
        panel.add(sendButton, BorderLayout.EAST);

        textField = new JTextField();
        textField.setFont(new Font("Consolas", Font.PLAIN,15));
        textField.setForeground(Color.white);
        textField.setBackground(new Color(38, 39, 35));
        textField.setCaretColor(Color.white);
        panel.add(textField, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        JPanel loginPasswordPanel = new JPanel();
        add(loginPasswordPanel, BorderLayout.NORTH);
        loginPasswordPanel.setLayout(new GridLayout());

        JTextField loginTextField = new JTextField();
        loginTextField.setFont(new Font("Consolas", Font.PLAIN,15));
        loginTextField.setForeground(Color.white);
        loginTextField.setBackground(new Color(38, 39, 35));
        loginTextField.setCaretColor(Color.white);
        loginPasswordPanel.add(loginTextField);

        JTextField passwordTextField = new JTextField();
        passwordTextField.setFont(new Font("Consolas", Font.PLAIN,15));
        passwordTextField.setForeground(Color.white);
        passwordTextField.setBackground(new Color(38, 39, 35));
        passwordTextField.setCaretColor(Color.white);
        loginPasswordPanel.add(passwordTextField);

        JButton authButton = new JButton("Войти в чат");
        loginPasswordPanel.add(authButton);
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

        sendButton.addActionListener(new ActionListener() {
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
