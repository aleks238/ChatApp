package lesson8.server;

import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    private List<Entry> entries;
    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("login1", "password1","nick1"));
        entries.add(new Entry("login2", "password2","nick2"));
        entries.add(new Entry("login3", "password3","nick3"));
    }
    @Override
    public void start() {
        System.out.println("Сервис аутентификации включен");
    }
    @Override
    public void stop() {
        System.out.println("Сервис аутентификации отключен");
    }
    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (Entry entry : entries){
            if (entry.login.equals(login) && entry.password.equals(password)){
                return entry.nick;
            }
        }
        return null;
    }
    private class Entry{
        private String login;
        private String password;
        private String nick;

        public Entry(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }
}
