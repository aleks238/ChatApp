package lesson8.server;

import lesson8.constants.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class СlientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public СlientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(()->{
                try {
                    autentification();
                    readMessageAndBroadcast();
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    closeConnection();
                }
            }).start();
        }catch (IOException e){
            throw new RuntimeException("Проблема при создании обработчика");
        }
    }

    private void autentification () throws IOException {
        while (true){
            String str = in.readUTF();
            if(str.startsWith(Constants.AUTH_COMMAND)){
                String[]tokens = str.split("\\s+");
                String nick = server.getAuthService().getNickByLoginAndPassword(tokens[1],tokens[2]);
                if (nick != null){
                    name = nick;
                    sendMessageFromServer("Авторизация успешна: " + nick);
                    server.broadcastMessage(nick + " Вошел в чат");
                    server.subscribe(this);
                    return;
                }else {
                    sendMessageFromServer("Не верный: login/parol");
                }
            }
        }
    }

    public void sendMessageFromServer(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readMessageAndBroadcast() throws IOException {
        while (true){
            String messageFromClient = in.readUTF();
            if (messageFromClient.equals(Constants.END_COMMAND)){
                break;
            }
            System.out.println("Message from " + name + ": " + messageFromClient);
            if (messageFromClient.startsWith(Constants.PRIVITE_MESSAGE_COMMAND)){
                String[]tokens = messageFromClient.split("\\s+");
                server.privateMessage(messageFromClient,tokens[1]);
                this.sendMessageFromServer(this.name + ":" + messageFromClient);
            }else if (messageFromClient.startsWith(Constants.CLIENTS_LIST_COMMAND)){
                sendMessageFromServer(server.getActiveClientsCommand());
            }else {
                server.broadcastMessage(name + ": " + messageFromClient);
            }
        }
    }
    public String getName() {
    return name;
    }
    private void closeConnection(){
        server.unsubscribe(this);
        server.broadcastMessage(name + " escaped of chat");
        try {
            in.close();
        }catch (IOException e){
        }
        try {
            out.close();
        }catch (IOException e){
        }
        try {
            socket.close();
        }catch (IOException e){
        }
    }
}


