package ru.zakharov;

import ru.zakharov.util.ConsoleHelper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Server {

    private static final int PORT = 8080;
    public static final List<Client> allClients = new ArrayList<>();

    private static class Handler extends Thread {
        private Client client;
        private static int number = 0;

        public Handler(Client client) {
            this.client = client;
            setName("Handler-"+number++);
        }

        @Override
        public void run() {
            ConsoleHelper.writeMsg("Hello from handler!");
            serverMessagesLoop(this.client);
        }
    }

    private static Message addAuthorToMessage(Message message, Client client) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar currentTime = Calendar.getInstance();
        String author = client.getUsername();
        String editedMsg = String.format("[%s] %s: %s",
                formatter.format(currentTime.getTime()), author, message.getData());
        return new Message(editedMsg, MessageType.TEXT);
    }

    private static void serverMessagesLoop(Client client) {
        ConsoleHelper.writeMsg("Hello from serverMessagesLoop()");
        //Circling and reading income messages
        while (true) {
            try {
                Message message = client.getConnection().receiveMsg();
                if (message.getMsgType()==MessageType.TEXT || message.getMsgType()==MessageType.INFO) {
                    ConsoleHelper.writeMsg(message.getData());
                    ConsoleHelper.writeMsg(message.getMsgType().toString());
                    ConsoleHelper.writeMsg(client.getUsername());
                    Message msgWithAuthor = addAuthorToMessage(message, client);
                    sendMessageToAllClients(msgWithAuthor);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendMessageToAllClients(Message message) {
        ConsoleHelper.writeMsg("sendMessageToAllConnections()");
        allClients.forEach(s-> {
            try {
                s.getConnection().sendMsg(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        try {
            runServer();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске сервера!");
        }
    }

    private static void runServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server has been started!");
        while (true) {
            Socket socketClient = serverSocket.accept();
            ConsoleHelper.writeMsg("We have a guest!");
            Connection connection = getContact(socketClient);
            ConsoleHelper.writeMsg("Connection is here!");
            makeFriends(connection);
        }
    }

    private static Connection getContact(Socket socketClient) throws IOException {
        return new Connection(socketClient);
    }

    private static void makeFriends(Connection conn) {
        //Request the name
        try {
            conn.sendMsg(new Message("What's your name?", MessageType.REQUEST_NAME));
            ConsoleHelper.writeMsg("Waiting for name...");
            Message message = conn.receiveMsg();
            if (message.getMsgType()==MessageType.RESPONSE_NAME) {
                conn.sendMsg(new Message(MessageType.NAME_ACCEPTED));
                Client client = new Client(conn, message.getData());
                allClients.add(client);
                System.out.println("Hello, "+message.getData());
                ConsoleHelper.writeMsg("Connection has been added to the map!");
                new Handler(client).start();
            }
            else {
                System.out.println("Incorrect message type. Request the name again.");
                makeFriends(conn);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
