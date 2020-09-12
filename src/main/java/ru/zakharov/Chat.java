package ru.zakharov;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String chatName;
    private int maxNumberOfClients;
    private List<Client> clientsOfChat;
    private Client owner;

    public Chat(String chatName, int maxNumberOfClients) {
        this.chatName = chatName;
        this.maxNumberOfClients = maxNumberOfClients;
        createClientsOfChat(maxNumberOfClients);
    }

    public boolean checkFreeSpaceInChat() {
        return (clientsOfChat.size()+1) < maxNumberOfClients;
    }

    public int numberOfUsers() {
        return clientsOfChat.size();
    }

    private void createClientsOfChat(int maxNumberOfClients) {
        clientsOfChat = new ArrayList<>(maxNumberOfClients);
    }

    public String listOfUsers() {
        StringBuilder allUsers = new StringBuilder(String.format("All users of \"%s\" chat:\n", chatName));
        for (int i = 0; i < clientsOfChat.size(); i++) {
            Client client = clientsOfChat.get(i);
            allUsers.append(String.format("\t\t\t\t\t#%d. %s\n", i, client.getUsername()));
        }
        return allUsers.toString();
    }

    public void addUser(Client client) {
        clientsOfChat.add(client);
    }

    public Client getOwner() {
        return owner;
    }

    public void addOwner(Client client) {
        client.addNewChat(this);
    }

    public void setOwner(Client owner) {
        this.owner = owner;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public List<Client> getClientsOfChat() {
        return clientsOfChat;
    }

    public void setClientsOfChat(List<Client> clientsOfChat) {
        this.clientsOfChat = clientsOfChat;
    }

    public int getMaxNumberOfClients() {
        return maxNumberOfClients;
    }

    public void setMaxNumberOfClients(int maxNumberOfClients) {
        this.maxNumberOfClients = maxNumberOfClients;
    }
}
