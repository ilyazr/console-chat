package ru.zakharov;


import java.io.*;
import java.net.Socket;

public class Connection implements Closeable {

    private Client client;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private boolean isClose = false;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMsg(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public Message receiveMsg() throws IOException, ClassNotFoundException {
        return (Message) in.readObject();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void close() throws IOException {
        setClose(true);
        socket.close();
    }
}
