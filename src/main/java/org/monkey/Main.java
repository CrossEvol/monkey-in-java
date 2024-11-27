package org.monkey;

import org.monkey.repl.Repl;

public class Main {
    public static void main(String[] args) {
        String userName = System.getProperty("user.name");
        System.out.printf("Hello %s! This is the Monkey programming language!\n", userName);
        System.out.println("Feel free to type in commands");
        new Repl().start();
    }
}