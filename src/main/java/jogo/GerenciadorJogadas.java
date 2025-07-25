package jogo;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import jogo.EscreverXML;

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
                saidaJogador2.writeObject(mensagem);
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}
