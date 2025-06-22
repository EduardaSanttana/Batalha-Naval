package jogo.view;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BatalhaNaval extends JFrame {

    private String nome;
    private boolean suaVez;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;

    private JTextArea logArea;
    private JButton[][] botoesTabuleiro = new JButton[10][10];

    public BatalhaNaval(String nome, boolean suaVez, ObjectInputStream entrada, ObjectOutputStream saida) {
        this.nome = nome;
        this.suaVez = suaVez;
        this.entrada = entrada;
        this.saida = saida;

        setTitle("Batalha Naval - Jogador " + nome);
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        criarInterface();

        setLocationRelativeTo(null);
        setVisible(true);

        jogar();
    }

    private void criarInterface() {
        setLayout(new BorderLayout());

        logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);

        JPanel painelTabuleiro = new JPanel(new GridLayout(10, 10));

        for (int linha = 0; linha < 10; linha++) {
            for (int col = 0; col < 10; col++) {
                JButton btn = new JButton();
                btn.setBackground(new Color(70, 130, 180));
                btn.setMargin(new Insets(0,0,0,0));
                final int l = linha;
                final int c = col;
                btn.addActionListener(e -> jogarPosicao(l, c));
                botoesTabuleiro[linha][col] = btn;
                painelTabuleiro.add(btn);
            }
        }

        add(scroll, BorderLayout.SOUTH);
        add(painelTabuleiro, BorderLayout.CENTER);
    }

    private void jogarPosicao(int linha, int coluna) {
        if (!suaVez) {
            JOptionPane.showMessageDialog(this, "Ainda não é sua vez!");
            return;
        }

        String jogada = linha + "," + coluna;
        try {
            saida.writeObject(jogada);
            saida.flush();

            botoesTabuleiro[linha][coluna].setBackground(new Color(176, 224, 230));
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

                        String[] partes = mensagem.split(",");
                        if (partes.length == 2) {
                            try {
                                int linha = Integer.parseInt(partes[0]);
                                int coluna = Integer.parseInt(partes[1]);

                                botoesTabuleiro[linha][coluna].setBackground(new Color(255, 140, 0));
                                botoesTabuleiro[linha][coluna].setEnabled(false);
                            } catch (NumberFormatException e) {
                            }
                        }

                        suaVez = true;
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Conexão encerrada.\n");
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            botoesTabuleiro[i][j].setEnabled(false);
                        }
                    }
                });
            }
        });
        threadEscuta.start();
    }
}
