package cz.vse;

import cz.vse.server.Acceptor;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Acceptor acceptor = new Acceptor("/src/main/resources/config.properties");
        acceptor.start();
    }
}