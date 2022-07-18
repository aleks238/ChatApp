package lesson7.server;

import lesson7.constants.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    public String getName() {
    return name;
    }

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try {
                    autentification();
                    getMessageAndBroadcast();
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
                    sendMessageFromServer(Constants.AUTH_CORRECT_COMMAND + " " + nick);
                    server.broadcastMessage(nick + " Вошел в чат");
                    server.subscribe(this);
                    return;
                }else {
                    sendMessageFromServer("неверный login/password");
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
    private void getMessageAndBroadcast() throws IOException {
        while (true){
            String messageFromClient = in.readUTF();
            if (messageFromClient.equals(Constants.END_COMMAND)){
                break;
            }
            System.out.println("Message from " + name + ": " + messageFromClient);

            if (messageFromClient.startsWith(Constants.PRIVITE_MESSAGE_COMMAND)){
                String[]tokens = messageFromClient.split("\\s+");
                server.privateMessageToClient(messageFromClient,tokens[1]);
                this.sendMessageFromServer(this.name + ":" + messageFromClient);

            }else {
                server.broadcastMessage(name + ": " + messageFromClient);
            }
        }
    }
    private void closeConnection(){
        server.unsubscribe(this);
        server.broadcastMessage(name + " вышел из чата");
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


