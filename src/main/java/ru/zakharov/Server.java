package ru.zakharov;

import ru.zakharov.util.ConsoleHelper;
import ru.zakharov.util.OperationsOfBot;

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
    public static BotHelper botHelper;

    private static class BotHelper {
        private final String botName;

        public BotHelper(String botName) {
            this.botName = botName;
            ConsoleHelper.writeMsg("Bot with name "+botName+" was created!");
        }

        public void startWorkWithBot(Client client) {
            greetingFromBot(client);
            OperationsOfBot operation = null;
            while (true) {
                botHelper.listOfOperation(client);
                Message respOfOperation = null;
                try {
                    respOfOperation = client.getConnection().receiveMsg();
                    if (respOfOperation.getData().equalsIgnoreCase("stop")) {
                        client.getConnection()
                                .sendMsg(new Message("Working with bot is over!", MessageType.INFO));
                        break;
                    }
                    OperationsOfBot requested =
                            OperationsOfBot.values()[Integer.parseInt(respOfOperation.getData())];
                    execOperation(requested, client);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        public static String infoAboutBot() {
            return "On server there is the bot!" +
                    " If you need help type command \"bot\"!";
        }

        public void greetingFromBot(Client client) {
            String str = String.format("Hello %s! My name is %s " +
                    "and I'm here to help you!\n" +
                    "If you want to stop type command \"stop\".", client.getUsername(), botName);
            Message greeting = new Message(str, MessageType.INFO);
            try {
                client.getConnection().sendMsg(greeting);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Functionality of bot. List of operations for usage
        public void listOfOperation(Client client) {
            String borders = "========================================================================\n";
            StringBuilder sb = new StringBuilder(borders+"Choose the operation:\n");
            for (OperationsOfBot op : OperationsOfBot.values()) {
                String tmp = "";
                if (op.ordinal()==OperationsOfBot.values().length-1) {
                    tmp = String.format("\t#%d. %s.\n", op.ordinal(), op.getDescription());
                    sb.append(tmp);
                    sb.append(borders);
                }
                else {
                    tmp = String.format("\t#%d. %s.\n", op.ordinal(), op.getDescription());
                    sb.append(tmp);
                }
            }

            Message ops = new Message(sb.toString(), MessageType.REQUEST_OPERATION);
            requestOfOperation(client, ops);
        }

        private void requestOfOperation(Client client, Message ops) {
            try {
                client.getConnection().sendMsg(ops);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String editMsgForBot(String str) {
            return String.format("[BOT]: \t%s", str);
        }

        public void execOperation(OperationsOfBot operation, Client client) throws IOException {
            switch (operation) {
                case CURRENT_DATE:
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    String currentDate = String.format("Current date: %s.", sdf.format(calendar.getTime()));
                    Message msg = new Message(editMsgForBot(currentDate), MessageType.INFO);
                    client.getConnection().sendMsg(msg);
                    break;

                case COUNT_OF_USERS_ON_SERVER:
                    String tmp = String.format("Количество пользователей на сервере: %d",
                            allClients.size());
                    Message usersCount = new Message(editMsgForBot(tmp), MessageType.INFO);
                    client.getConnection().sendMsg(usersCount);
                    break;

                case LIST_OF_USERS:
                    StringBuilder sb = new StringBuilder("Список пользователей:\n");
                    for (int i = 0; i < allClients.size(); i++) {
                        if (i == allClients.size()-1) {
                            sb.append(String.format("\t%d. %s", i, allClients.get(i).getUsername()));
                        }
                        else {
                            sb.append(String.format("\t%d. %s\n", i, allClients.get(i).getUsername()));
                        }
                    }
                    Message listOfUsers = new Message(editMsgForBot(sb.toString()), MessageType.INFO);
                    client.getConnection().sendMsg(listOfUsers);
                    break;
            }

        }

    }

    //Each client has his own handler
    //in order to server can read
    //income messages
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
        String toClient = String.format("%s! %s", client.getUsername(), BotHelper.infoAboutBot());
        try {
            client.getConnection().sendMsg(new Message(toClient, MessageType.INFO));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Circling and reading income messages
        while (true) {
            try {
                Message message = client.getConnection().receiveMsg();
                if (message.getData().equalsIgnoreCase("bot")) {
                    botHelper.startWorkWithBot(client);
                }
                else if (message.getMsgType()==MessageType.TEXT || message.getMsgType()==MessageType.INFO) {
                    Message msgWithAuthor = addAuthorToMessage(message, client);
                    sendMessageToAllClients(msgWithAuthor);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendMessageToAllClients(Message message) {
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
            botHelper = new BotHelper("Tom");
            Client newClient = makeFriends(connection);
            notifyAllClientsAboutNewClient(newClient);
        }
    }

    private static void notifyAllClientsAboutNewClient(Client newClient) {
        allClients.forEach(client -> {
            if (!client.equals(newClient)) {
                Message infoMsg =
                        new Message(String.format("%s has been connected to the server!",
                                newClient.getUsername()),
                                MessageType.INFO);
                try {
                    client.getConnection().sendMsg(infoMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static Connection getContact(Socket socketClient) throws IOException {
        return new Connection(socketClient);
    }

    private static Client makeFriends(Connection conn) {
        //Request the name
        Client client = null;
        try {
            conn.sendMsg(new Message("What's your name?", MessageType.REQUEST_NAME));
            ConsoleHelper.writeMsg("Waiting for name...");
            Message message = conn.receiveMsg();
            if (message.getMsgType()==MessageType.RESPONSE_NAME) {
                conn.sendMsg(new Message(MessageType.NAME_ACCEPTED));
                client = new Client(conn, message.getData());
                allClients.add(client);
                System.out.println("Hello, "+message.getData());
                ConsoleHelper.writeMsg("Client has been added to the map!");
                new Handler(client).start();
            }
            else {
                System.out.println("Incorrect message type. Request the name again.");
                makeFriends(conn);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return client;
    }

}
