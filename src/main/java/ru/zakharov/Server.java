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
    public static final List<Chat> allChats = new ArrayList<>();
    public static BotHelper botHelper;

    public static Client findUserByUsername(String name) {
        return allClients.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    private static class BotHelper {
        private final String botName;

        public BotHelper(String botName) {
            this.botName = botName;
            ConsoleHelper.writeMsg("Bot with name "+botName+" was created!");
        }

        public void startWorkWithBot(Client client) {
            greetingFromBot(client);
            OperationsOfBot operation = null;
            int lastElementOfOp = OperationsOfBot.values().length-1;
            while (true) {
                infoFromBot(client);
                botHelper.listOfOperation(client);
                Message respOfOperation = null;
                try {
                    respOfOperation = client.getConnection().receiveMsg();
                    if (respOfOperation.getData().equalsIgnoreCase("stop")) {
                        client.getConnection()
                                .sendMsg(new Message("Working with bot is over!", MessageType.INFO));
                        break;
                    }
                    if (Integer.parseInt(respOfOperation.getData()) > lastElementOfOp ||
                            Integer.parseInt(respOfOperation.getData()) < 0) {
                        client.getConnection()
                                .sendMsg(new Message(editForInfo("Ошибка! Такой операции не существует!"),
                                        MessageType.TEXT));
                    } else {
                        OperationsOfBot requested =
                                OperationsOfBot.values()[Integer.parseInt(respOfOperation.getData())];
                        execOperation(requested, client);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        public static String infoAboutBot() {
            return "On server there is the bot!" +
                    " If you need help type command \"bot\"!";
        }

        public void infoFromBot(Client client) {
            String str = "If you want to stop type command \"stop\".";
            Message msgFromBot = new Message(str, MessageType.INFO);
            try {
                client.getConnection().sendMsg(msgFromBot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void greetingFromBot(Client client) {
            String str = String.format("Hello %s! My name is %s " +
                    "and I'm here to help you!", client.getUsername(), botName);
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

        private String editMsgForPM(String msg, Client client) {
            return String.format("[PM FROM %s]: %s", client.getUsername(), msg);
        }

        private String editMsgForInvite(Chat chat, Client client) {
            return String.format("[INVITE TO PRIVATE CHAT \"%s\" FROM %s]",
                    chat.getChatName(), client.getUsername());
        }

        private String editMsgForAcceptedInvite(Chat chat, Client client) {
            return String.format("[%s ACCEPTED THE INVITATION TO %S CHAT]",
                    client.getUsername(), chat.getChatName());
        }

        private String editForInfo(String msg) {
            return String.format("[INFO]:\t%s\n", msg);
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

                case PRIVATE_MESSAGE:
                    sendPrivateMessage(client);
                    break;

                case CREATE_NEW_CHAT:
                    createNewPrivateChat(client);
                    break;

                case ALL_CHATS_ON_SERVER:
                    infoAboutAllChats(client);
                    break;

                case INVITE_USER_TO_THE_CHAT:
                    if (client.getChat() == null) {
                        break;
                    }
                    sendInvitesToTheChat(client);
                    break;

                case CHECK_ALL_INVITES:
                    checkInvitesToChats(client);
                    break;

            }

        }

        private void checkInvitesToChats(Client client) {
            try {
                if (client.getAmountOfInvites() == 0) {
                    String info = editForInfo("Количество приглашений: 0");
                    client.getConnection()
                            .sendMsg(new Message(info, MessageType.TEXT));
                } else {
                    String allInvites = client.listOfAllInvites();
                    client.getConnection()
                            .sendMsg(new Message(editForInfo("Чтобы принять инвайт, введите его номер. " +
                                    "Для того, чтобы просто выйти, введите \"exit\""),
                                    MessageType.TEXT));
                    client.getConnection()
                            .sendMsg(new Message(editForInfo(allInvites), MessageType.TEXT));
                    Message responseAboutInvite = client.getConnection().receiveMsg();
                    if (responseAboutInvite.getData().equalsIgnoreCase("exit")) {
                        return;
                    } else {
                        int numberOfInvite = Integer.parseInt(responseAboutInvite.getData());
                        Chat acceptedInvite = client.getInvitesToChats().get(numberOfInvite);
                        acceptOfInvite(client, acceptedInvite);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void acceptOfInvite(Client client, Chat chat) {
            try {
                chat.addUser(client);
                client.getInvitesToChats().remove(chat);

                Client chatOwner = chat.getOwner();
                String acceptedInvite = editMsgForAcceptedInvite(chat, client);
                chatOwner.getConnection().sendMsg(new Message(acceptedInvite, MessageType.TEXT));

                String acceptInfoToClient = editForInfo(String.format("Вы успешно вступили в \"%s\" чат!",
                        chat.getChatName()));
                client.getConnection().sendMsg(new Message(acceptInfoToClient, MessageType.TEXT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void addInviteToClient(Client client, Chat chat) {
            client.addChatInvite(chat);
        }

        private void infoAboutAllChats(Client client) {
            if (allChats.size() == 0) {
                try {
                    client.getConnection()
                            .sendMsg(new Message(editForInfo("На сервере нет приватных чатов!"), MessageType.INFO));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                StringBuilder infoMsg = new StringBuilder("Список всех чатов сервера:\n");
                for (int i = 0; i < allChats.size(); i++) {
                    Chat chat = allChats.get(i);
                    infoMsg.append(editForInfo(String.format("Название: %s", chat.getChatName())));
                    infoMsg.append(editForInfo(String.format("Владелец: %s", chat.getOwner().getUsername())));
                    infoMsg.append(editForInfo(String.format("Кол-во пользователей/вместимость: %d/%d",
                            chat.numberOfUsers(), chat.getMaxNumberOfClients())));
                    if (chat.numberOfUsers() > 0) {
                        infoMsg.append(editForInfo(chat.listOfUsers()));
                    }
                }
                try {
                    client.getConnection().sendMsg(new Message(infoMsg.toString(), MessageType.INFO));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void createNewPrivateChat(Client client) {
            try {
                String msg = editMsgForBot("Укажите название чата:");
                client.getConnection().sendMsg(new Message(msg, MessageType.TEXT));
                Message chatName = client.getConnection().receiveMsg();

                String msg2 = editMsgForBot("Укажите максимальное количество участников: ");
                client.getConnection().sendMsg(new Message(msg2, MessageType.TEXT));
                Message maxNumberOfUsersInTheChat = client.getConnection().receiveMsg();

                Chat chat = new Chat(chatName.getData(), Integer.parseInt(maxNumberOfUsersInTheChat.getData()));
                chat.addOwner(client);
                chat.addUser(client);
                allChats.add(chat);

                client.getConnection()
                        .sendMsg(new Message(editForInfo(
                                String.format("Чат с именем \"%s\" был успешно создан!", chat.getChatName())), MessageType.INFO));

                String invites = editMsgForBot("Хотите ли вы пригласить кого-то в чат? [y/n]");
                client.getConnection().sendMsg(new Message(invites, MessageType.TEXT));
                Message responseAboutInvites = client.getConnection().receiveMsg();
                if (responseAboutInvites.getData().equalsIgnoreCase("y")) {
                    sendInvitesToTheChat(client);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private int sendInvitesToTheChat(Client client) {
            try {
                Chat chat = client.getChat();
                boolean checkFreeSpace = chat.checkFreeSpaceInChat();
                if (checkFreeSpace) {
                    client.getConnection()
                            .sendMsg(new Message(editMsgForBot(String.format("Рассылаем приглашения в чат %s!",
                                    chat.getChatName())), MessageType.TEXT));

                    String msg = editMsgForBot("Введите имя пользователя, которого вы хотите пригласить:");
                    client.getConnection().sendMsg(new Message(msg, MessageType.TEXT));
                    Message invite = client.getConnection().receiveMsg();
                    String invitedName = invite.getData();
                    Client invitedClient = findUserByUsername(invitedName);

                    if (invitedClient != null) {
                        addInviteToClient(invitedClient, chat);
                        String inviteToChat = editMsgForInvite(chat, client);
                        invitedClient.getConnection().sendMsg(new Message(inviteToChat, MessageType.INFO));
                        client.getConnection()
                                .sendMsg(new Message(editForInfo(String.format("Приглашение было отправлено пользователю %s!",
                                        invitedClient.getUsername())), MessageType.TEXT));
                        return 1;

                    } else {
                        String err = editMsgForBot(String.format("Пользователя с именем %s не существует!", invitedName));
                        client.getConnection().sendMsg(new Message(err, MessageType.TEXT));
                        return -1;
                    }

                } else {
                    System.out.println(chat.getMaxNumberOfClients());
                    System.out.println(chat.numberOfUsers());
                    String msg = editMsgForBot("В чате нет свободного места!");
                    client.getConnection().sendMsg(new Message(msg, MessageType.TEXT));
                    return -1;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return -1;
        }

        private void sendPrivateMessage(Client client) {
            Message nameRequest = new Message(editMsgForBot("Введите имя адресата:"), MessageType.TEXT);
            try {
                client.getConnection().sendMsg(nameRequest);
                Message nameResponse = client.getConnection().receiveMsg();
                String receiverName = nameResponse.getData();
                Client receiver = findUserByUsername(receiverName);

                if (receiver == null) {
                    client.getConnection()
                            .sendMsg(new Message(editMsgForBot(String.format("Пользователя с именем %s не существует!",
                                    receiverName)),
                                    MessageType.TEXT));
                    client.getConnection()
                            .sendMsg(new Message(editMsgForBot("Вы хотите отправить сообщение? [y/n]"), MessageType.TEXT));
                    Message response = client.getConnection().receiveMsg();
                    if (response.getData().equalsIgnoreCase("y")) {
                        sendPrivateMessage(client);
                    }
                    else {
                        client.getConnection()
                                .sendMsg(new Message(editMsgForBot("Ошибка при отправке сообщения"),
                                MessageType.TEXT));
                    }
                }
                else {
                    client.getConnection()
                            .sendMsg(new Message(editMsgForBot("Введите текст сообщения:"), MessageType.TEXT));
                    Message msgText = client.getConnection().receiveMsg();
                    Message editedMsg = new Message(editMsgForPM(msgText.getData(), client), MessageType.TEXT);
                    receiver.getConnection().sendMsg(editedMsg);

                    //notification of sender
                    String resultText = String.format("Сообщение было отправлено! Получатель - %s",
                            receiver.getUsername());
                    Message result = new Message(editMsgForBot(resultText), MessageType.TEXT);
                    client.getConnection().sendMsg(result);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
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
