package ru.zakharov.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMsg(String msg) {
        System.out.println(msg);
    }

    public static String readString() throws IOException {
        return reader.readLine();
    }

    public static int readInt() throws IOException {
        return Integer.parseInt(reader.readLine());
    }

}
