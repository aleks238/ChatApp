package lesson7.server;

import lesson7.constants.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private AuthService authService;
    private List <ClientHandler> clients;
    public AuthService getAuthService() {
        return authService;
    }
    public MyServer(){
        try (ServerSocket server = new ServerSocket(Constants.SERVER_PORT)){
            authService= new BaseAuthService();
            authService.start();
            clients= new ArrayList<>();

            while (true){
                System.out.println("Сервер ожидает подключения клиента...");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this,socket);
            }
        }catch (IOException e){
            System.out.println("Ошибка в работе сервера: ");
            e.printStackTrace();
        }finally {
            if (authService != null){
                authService.stop();
            }
        }
    }
    public synchronized void broadcastMessage (String message){
        for (ClientHandler client : clients){
            client.sendMessageFromServer(message);
        }
    }
    public synchronized void privateMessageToClient (String message,String nick){
        for (ClientHandler client : clients){
            if (client.getName().equals(nick))
            client.sendMessageFromServer(message);
        }
    }
    public synchronized void subscribe(ClientHandler client){
        clients.add(client);
    }
    public synchronized void unsubscribe(ClientHandler client){
        clients.remove(client);
    }
}
