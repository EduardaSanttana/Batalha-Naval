package jogo.model;

import jogo.view.BatalhaNaval;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClienteJogo {

    private String nome;
    private boolean suaVez;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;
    private String[][] tabuleiro;
    private List<Navio> navios;

    public ClienteJogo(String nome) {
        this.nome = nome;
        this.suaVez = false;
        inicializarTabuleiro();
        conectarServidor();
    }

    private void inicializarTabuleiro() {
        tabuleiro = new String[10][10];
        navios = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tabuleiro[i][j] = "";
            }
        }

        int[] tamanhos = {5, 4, 3, 3, 2}; // Exemplo: porta-aviões, etc.
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

    private void conectarServidor() {
        Socket socket = null;
        try {
            String ip = Config.getIp();
            int porta = Config.getPorta();
            if (ip == null || porta == -1) {
                throw new IllegalStateException("Configurações de IP ou porta não carregadas do config.xml. Verifique o arquivo.");
            }
            System.out.println("Conectando ao servidor em " + ip + ":" + porta);
            socket = new Socket(ip, porta);
            saida = new ObjectOutputStream(socket.getOutputStream());
            saida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());

            // Envia nome ao servidor
            saida.writeObject(nome);
            saida.flush();

            // Recebe boolean do servidor indicando se começa jogando
            suaVez = (Boolean) entrada.readObject();

            System.out.println("Conectado! Sua vez: " + suaVez);
            System.out.println("Criando interface gráfica para " + nome);

            // Cria a interface com os navios
            SwingUtilities.invokeLater(() -> {
                new BatalhaNaval(nome, suaVez, entrada, saida, tabuleiro);
            });

        } catch (Exception e) {
            e.printStackTrace();
            String mensagemErro = "Erro ao conectar no servidor: " + e.getMessage() + "\nCertifique-se de que o servidor está rodando em " + Config.getIp() + ":" + Config.getPorta();
            JOptionPane.showMessageDialog(null, mensagemErro);
        } finally {
            // Não fecha o socket aqui para manter a conexão ativa
        }
    }

    public static void main(String[] args) {
        String nome = JOptionPane.showInputDialog("Digite seu nome:");
        if (nome != null && !nome.trim().isEmpty()) {
            System.out.println("Iniciando ClienteJogo para " + nome);
            new ClienteJogo(nome.trim());
        } else {
            JOptionPane.showMessageDialog(null, "Nome inválido.");
        }
    }
}