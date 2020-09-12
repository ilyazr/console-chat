package ru.zakharov;

import ru.zakharov.util.ConsoleHelper;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private Connection connection;
    private String address;
    private int port;

    //Server give you access to chat!
    private boolean isPermit = false;

    //Client info
    private String username;
    private Chat chat;

    private List<Chat> invitesToChats = new ArrayList<>();

    public Client() {
    }

    public Client(Connection connection) {
        this.connection = connection;
    }

    public Client(Connection connection, String username) {
        this.connection = connection;
        this.username = username;
    }

    @Override
    public String toString() {
        return "Client{" +
                "username='" + username + '\'' +
                '}';
    }

    //Here we read all income messages!
    class MsgReader extends Thread {
        @Override
        public void run() {
            while (!connection.isClose()) {
                try {
                    Message incomeMsg = connection.receiveMsg();
                    if (incomeMsg.getMsgType() == MessageType.TEXT
                            || incomeMsg.getMsgType() == MessageType.INFO
                            || incomeMsg.getMsgType() == MessageType.REQUEST_OPERATION) {
                        ConsoleHelper.writeMsg(incomeMsg.getData());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
        client.runClient();
    }

    private void runClient() {
        new MsgReader().start();
        loopForWritingMessages();
    }

    private void loopForWritingMessages() {
        while (!connection.isClose()) {
            while (true) {
                try {
                    String msg = ConsoleHelper.readString();
                    Message message = new Message(msg, MessageType.TEXT);
                    connection.sendMsg(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startClient() {
        try {
            String address = requestOfAddress();
            int port = requestOfPort();
            Socket socket = new Socket(address, port);
            Connection connection = new Connection(socket);
            setConnection(connection);
            makeFriendWithServer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeFriendWithServer() {
        try {
            Message reqOfName = connection.receiveMsg();
            if (reqOfName.getMsgType()==MessageType.REQUEST_NAME) {
                ConsoleHelper.writeMsg(reqOfName.getData());
                String name = ConsoleHelper.readString();
                connection.sendMsg(new Message(name, MessageType.RESPONSE_NAME));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String requestOfAddress() throws IOException {
        ConsoleHelper.writeMsg("Type address:");
        return ConsoleHelper.readString();
    }

    public static int requestOfPort() throws IOException {
        ConsoleHelper.writeMsg("Type port:");
        return ConsoleHelper.readInt();
    }

    public void addNewChat(Chat chat) {
        this.chat = chat;
        chat.setOwner(this);
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public List<Chat> getInvitesToChats() {
        return invitesToChats;
    }

    public void addChatInvite(Chat chat) {
        invitesToChats.add(chat);
    }

    public String listOfAllInvites() {
        StringBuilder sb = new StringBuilder(String.format("Количество приглашений: %d\n", invitesToChats.size()));
        for (int i = 0; i < invitesToChats.size(); i++) {
            Chat invite = invitesToChats.get(i);
            sb.append(String.format("\t\t%d. Название: %s. Владелец: %s. Кол-во пользователей: %d\n",
                    i, invite.getChatName(), invite.getOwner().getUsername(), invite.numberOfUsers()));
        }
        return sb.toString();
    }

    public int getAmountOfInvites() {
        return invitesToChats.size();
    }

    public void setInvitesToChats(List<Chat> invitesToChats) {
        this.invitesToChats = invitesToChats;
    }

    public boolean isPermit() {
        return isPermit;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPermit(boolean permit) {
        isPermit = permit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
