package jogo;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BatalhaNaval extends JFrame {

    private static final String MSG_FIM = "FIM";
    private volatile boolean jogoEncerrado = false;

    private String nome;
    private boolean suaVez;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;

    private JTextArea logArea;
    private JButton[][] botoesTabuleiro = new JButton[10][10];

    private int[][] tabuleiroJogador = new int[10][10];
    private int naviosRestantes = 5;

    public BatalhaNaval(String nome, boolean suaVez, ObjectInputStream entrada, ObjectOutputStream saida) {
        this.nome = nome;
        this.suaVez = suaVez;
        this.entrada = entrada;
        this.saida = saida;

        setTitle("Batalha Naval - Jogador " + nome);
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        criarInterface();
        posicionarNavios();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void criarInterface() {
        setLayout(new BorderLayout());
        logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);

        JPanel painelTabuleiro = new JPanel(new GridLayout(10, 10));
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JButton btn = new JButton();
                btn.setBackground(new Color(70, 130, 180));
                btn.setMargin(new Insets(0, 0, 0, 0));
                botoesTabuleiro[i][j] = btn;
                painelTabuleiro.add(btn);
            }
        }

        add(scroll, BorderLayout.SOUTH);
        add(painelTabuleiro, BorderLayout.CENTER);
    }

    private void posicionarNavios() {
        logArea.append("Posicione " + naviosRestantes + " navios clicando no tabuleiro.\n");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JButton btn = botoesTabuleiro[i][j];
                final int l = i, c = j;

                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
                btn.addActionListener(e -> {
                    if (tabuleiroJogador[l][c] == 0 && naviosRestantes > 0) {
                        tabuleiroJogador[l][c] = 1;
                        btn.setBackground(Color.GRAY);
                        naviosRestantes--;
                        logArea.append("Navio posicionado em: " + l + "," + c + "\n");
                        if (naviosRestantes == 0) {
                            logArea.append("Todos os navios posicionados!\n");
                            iniciarJogo();
                        }
                    }
                });
            }
        }
    }

    private void iniciarJogo() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JButton btn = botoesTabuleiro[i][j];
                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
                final int l = i, c = j;
                btn.addActionListener(e -> jogarPosicao(l, c));
            }
        }
        jogar();
        logArea.append("Começando o jogo! Sua vez? " + suaVez + "\n");
    }

    private void jogarPosicao(int linha, int coluna) {
        if (jogoEncerrado) {
            return;
        }

        if (!suaVez) {
            JOptionPane.showMessageDialog(this, "Ainda não é sua vez!");
            return;
        }
        String jogada = linha + "," + coluna;
        try {
            saida.writeObject(jogada);
            saida.flush();
            botoesTabuleiro[linha][coluna].setEnabled(false);
            suaVez = false;
        } catch (Exception ex) {
            logArea.append("Erro ao enviar jogada: " + ex.getMessage() + "\n");
        }
    }

    private void jogar() {
        Thread threadEscuta = new Thread(() -> {
            try {
                while (true) {
                    String mensagem = (String) entrada.readObject();
                    SwingUtilities.invokeLater(() -> {
                        if (MSG_FIM.equals(mensagem)) {
                            if (!jogoEncerrado) {
                                jogoEncerrado = true;
                                logArea.append("Oponente foi derrotado. Você venceu!\n");
                                JOptionPane.showMessageDialog(BatalhaNaval.this, "Você venceu!");
                                disableBoard();
                            }
                        } else {
                            tratarJogadaRecebida(mensagem);
                        }
                    });
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Conexão encerrada.\n");
                    disableBoard();
                });
            }
        });
        threadEscuta.start();
    }

    private void tratarJogadaRecebida(String mensagem) {
        String[] partes = mensagem.split(",");
        if (partes.length == 2) {
            try {
                int linha = Integer.parseInt(partes[0]);
                int coluna = Integer.parseInt(partes[1]);

                JButton btn = botoesTabuleiro[linha][coluna];
                if (tabuleiroJogador[linha][coluna] == 1) {
                    tabuleiroJogador[linha][coluna] = -1;
                    btn.setBackground(Color.RED);
                    logArea.append("Seu navio foi atingido em " + linha + "," + coluna + "\n");
                    if (verificaDerrota()) {
                        logArea.append("Todos os navios destruídos. Você perdeu!\n");
                        JOptionPane.showMessageDialog(this, "Você perdeu!");
                        enviarFimAoOponente();
                        jogoEncerrado = true;
                        disableBoard();
                        return;
                    }

                } else {
                    btn.setBackground(Color.WHITE);
                    logArea.append("Oponente errou em " + linha + "," + coluna + "\n");
                }

                suaVez = true;
            } catch (NumberFormatException e) {
                logArea.append("Mensagem inválida recebida: " + mensagem + "\n");
            }
        }
    }

    private void enviarFimAoOponente() {
        try {
            saida.writeObject(MSG_FIM);
            saida.flush();
        } catch (Exception ex) {
            logArea.append("Erro ao avisar oponente: " + ex.getMessage() + "\n");
        }
    }

    private boolean verificaDerrota() {
        for (int[] linha : tabuleiroJogador) {
            for (int cell : linha) {
                if (cell == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void disableBoard() {
        for (JButton[] linha : botoesTabuleiro) {
            for (JButton btn : linha) {
                btn.setEnabled(false);
            }
        }
    }
}
