package jogo.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorJogo {
    public static void main(String[] args) throws Exception {

        ServerSocket servidor = new ServerSocket(Config.getPorta(), 2, InetAddress.getByName(Config.getIp()));
        System.out.println("Servidor Batalha Naval Inicializado (" + servidor + ").\n");

        System.out.println("Esperando por conexão (Jogador X)...");
        Socket jogadorX = servidor.accept();
        System.out.println("Jogador X conectado: " + jogadorX.toString() + ":" + jogadorX.getPort() + "\n");

        ObjectOutputStream saidaJogadorX = new ObjectOutputStream(jogadorX.getOutputStream());
        saidaJogadorX.flush();
        saidaJogadorX.writeObject("X;true"); 

        ObjectInputStream entradaJogadorX = new ObjectInputStream(jogadorX.getInputStream());

        System.out.println("Esperando por conexão (Jogador Y)...");
        Socket jogadorY = servidor.accept();
        System.out.println("Jogador Y conectado: " + jogadorY.toString() + ":" + jogadorY.getPort() + "\n");

        ObjectOutputStream saidaJogadorY = new ObjectOutputStream(jogadorY.getOutputStream());
        saidaJogadorY.flush();
        saidaJogadorY.writeObject("Y;false");

        ObjectInputStream entradaJogadorY = new ObjectInputStream(jogadorY.getInputStream());

        Thread thread1 = new Thread(new GerenciadorJogadas(entradaJogadorX, saidaJogadorY));
        Thread thread2 = new Thread(new GerenciadorJogadas(entradaJogadorY, saidaJogadorX));

        thread1.start();
        thread2.start();
    }
}
