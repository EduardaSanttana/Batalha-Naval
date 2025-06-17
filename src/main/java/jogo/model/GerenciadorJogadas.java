package jogo.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GerenciadorJogadas implements Runnable {
    
    private ObjectInputStream entradaJogador1;
    private ObjectOutputStream saidaJogador2;

    public GerenciadorJogadas(ObjectInputStream entradaJogador1, ObjectOutputStream saidaJogador2) {
        this.entradaJogador1 = entradaJogador1;
        this.saidaJogador2 = saidaJogador2;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String mensagem = (String) entradaJogador1.readObject();
                System.out.println("Mensagem recebida de um jogador: " + mensagem);
                saidaJogador2.writeObject(mensagem);
                saidaJogador2.flush();
            } catch (Exception ex) {
                System.out.println("Erro na comunicação com um dos jogadores. Finalizando conexão.");
                ex.printStackTrace();
                break;
            }
        }
    }
}
