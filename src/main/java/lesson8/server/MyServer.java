package lesson8.server;

import lesson8.constants.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private AuthService authService;
    private List<СlientHandler> clients;
    public AuthService getAuthService() {
        return authService;
    }

    public MyServer() {
        try (ServerSocket server = new ServerSocket(Constants.SERVER_PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер ожидает подключения клиентов...");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new СlientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера: ");
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }
    public synchronized void broadcastMessage(String message) {
        for (СlientHandler client : clients) {
            client.sendMessageFromServer(message);
        }
    }
    public synchronized void privateMessage(String message, String nick) {
        for (СlientHandler client : clients) {
            if (client.getName().equals(nick))
                client.sendMessageFromServer(message);
        }
    }
    public synchronized void subscribe(СlientHandler client) {
        clients.add(client);
        for (СlientHandler i : clients) {
            for (СlientHandler j : clients) {
                i.sendMessageFromServer("Online: " + j.getName());
            }
        }
    }
    public synchronized void unsubscribe(СlientHandler client) {
        clients.remove(client);
        for (СlientHandler i : clients){
            i.sendMessageFromServer("Remove: " + client.getName());
        }
    }
    public synchronized String getActiveClientsCommand() {
        StringBuilder sb = new StringBuilder(Constants.CLIENTS_LIST_COMMAND).append(" ");
        for (СlientHandler client : clients) {
            sb.append("\n").append(client.getName()).append("\n");
        }
        return sb.toString();
    }
}
