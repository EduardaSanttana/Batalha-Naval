package jogo.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GerenciadorJogadas implements Runnable {

    private ObjectInputStream entradaJogador1;
    private ObjectOutputStream saidaJogador2;

    // === NOVO: estrutura do jogo ===
    private String[][] tabuleiro;
    private List<Navio> navios;

    public GerenciadorJogadas(ObjectInputStream entradaJogador1, ObjectOutputStream saidaJogador2) {
        this.entradaJogador1 = entradaJogador1;
        this.saidaJogador2 = saidaJogador2;

        // Inicializa o tabuleiro e posiciona navios
        tabuleiro = new String[10][10];
        navios = new ArrayList<>();

        // Preenche o tabuleiro com vazio
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tabuleiro[i][j] = "";
            }
        }

        posicionarNaviosAutomaticamente(); // ⬅️ novo método
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

    // === NOVO: posicionamento automático dos navios ===
    private void posicionarNaviosAutomaticamente() {
        int[] tamanhos = {5, 4, 3, 3, 2}; // Exemplo: porta-aviões, navio-tanque, etc.
        Random rand = new Random();

        for (int tamanho : tamanhos) {
            boolean posicionado = false;

            while (!posicionado) {
                int linha = rand.nextInt(10);
                int coluna = rand.nextInt(10);
                boolean horizontal = rand.nextBoolean();

                if (podeColocarNavio(linha, coluna, tamanho, horizontal)) {
                    colocarNavio(linha, coluna, tamanho, horizontal);
                    posicionado = true;
                }
            }
        }
    }

    private boolean podeColocarNavio(int linha, int coluna, int tamanho, boolean horizontal) {
        if (horizontal) {
            if (coluna + tamanho > 10) return false;
            for (int i = 0; i < tamanho; i++) {
                if (!tabuleiro[linha][coluna + i].equals("")) return false;
            }
        } else {
            if (linha + tamanho > 10) return false;
            for (int i = 0; i < tamanho; i++) {
                if (!tabuleiro[linha + i][coluna].equals("")) return false;
            }
        }
        return true;
    }

    private void colocarNavio(int linha, int coluna, int tamanho, boolean horizontal) {
        Navio navio = new Navio(tamanho, linha, coluna, horizontal);
        navios.add(navio);

        for (int i = 0; i < tamanho; i++) {
            int l = horizontal ? linha : linha + i;
            int c = horizontal ? coluna + i : coluna;
            tabuleiro[l][c] = "N";
        }
    }

    public String[][] getTabuleiro() {
        return tabuleiro;
    }

    public List<Navio> getNavios() {
        return navios;
    }
}
