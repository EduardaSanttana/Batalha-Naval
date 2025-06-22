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

        System.out.println("Esperando por conexão (Jogador 1)...");
        Socket jogador1 = servidor.accept();
        ObjectOutputStream saidaJogador1 = new ObjectOutputStream(jogador1.getOutputStream());
        saidaJogador1.flush();
        
        ObjectInputStream entradaJogador1 = new ObjectInputStream(jogador1.getInputStream());
        String nomeJogador1 = (String) entradaJogador1.readObject();
        
        System.out.println("Jogador 1 conectado: " + nomeJogador1 + " (" + jogador1.getInetAddress() + ":" + jogador1.getPort() + ")");
        saidaJogador1.writeObject("true"); 

        System.out.println("Esperando por conexão (Jogador 2)...");
        Socket jogador2 = servidor.accept();
        ObjectOutputStream saidaJogador2 = new ObjectOutputStream(jogador2.getOutputStream());
        saidaJogador2.flush();
        
        ObjectInputStream entradaJogador2 = new ObjectInputStream(jogador2.getInputStream());
        String nomeJogador2 = (String) entradaJogador2.readObject();
        System.out.println("Jogador 2 conectado: " + nomeJogador2 + " (" + jogador2.getInetAddress() + ":" + jogador2.getPort() + ")");
        saidaJogador2.writeObject("false");

        System.out.println("Jogo iniciado entre " + nomeJogador1 + " e " + nomeJogador2 + ".");

        Thread thread1 = new Thread(new GerenciadorJogadas(entradaJogador1, saidaJogador2));
        Thread thread2 = new Thread(new GerenciadorJogadas(entradaJogador2, saidaJogador1));

        thread1.start();
        thread2.start();
    }
}
