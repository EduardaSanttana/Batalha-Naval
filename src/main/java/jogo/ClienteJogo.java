package jogo;

import jogo.BatalhaNaval;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteJogo {

    public static void main(String[] args) throws Exception {
        String nome = JOptionPane.showInputDialog(null, "Digite seu nome:", "Entrada", JOptionPane.PLAIN_MESSAGE);
        if (nome == null || nome.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nome inválido. Encerrando.");
            System.exit(0);
        }

        Socket socket = new Socket(Config.getIp(), Config.getPorta());
        ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
        saida.flush();
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

        saida.writeObject(nome);
        saida.flush();

        String mensagem = (String) entrada.readObject();
        boolean suaVez = Boolean.parseBoolean(mensagem);

        String nomeAdversario = (String) entrada.readObject();

        System.out.println("Jogador: " + nome);
        System.out.println("Sua vez de jogar? " + suaVez);
        System.out.println("Nome do adversário: " + nomeAdversario);

        SwingUtilities.invokeLater(() -> {
            BatalhaNaval jogo = new BatalhaNaval(nome, suaVez, entrada, saida, nomeAdversario);
        });
    }
}
