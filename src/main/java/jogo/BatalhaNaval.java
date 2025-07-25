package jogo;

import jogo.Som;
import jogo.EscreverXML;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BatalhaNaval extends JFrame {

    private static final String MSG_FIM = "FIM";
    private static final String MSG_REINICIAR = "REINICIAR";
    private static final String MSG_DESISTENCIA = "DESISTENCIA";

    private volatile boolean jogoEncerrado = false;

    private String nome;
    private String nomeAdversario;
    private boolean suaVez;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;

    private JTextArea logArea;
    private JButton[][] botoesTabuleiro = new JButton[10][10];

    private int[][] tabuleiroJogador = new int[10][10];
    private int naviosRestantes = 5;

    private JButton btnIniciar;
    private JButton btnReiniciar;
    private JButton btnDesistir;

    private String ultimaJogada = "";

    public BatalhaNaval(String nome, boolean suaVez, ObjectInputStream entrada, ObjectOutputStream saida, String nomeAdversario) {

        this.nomeAdversario = nomeAdversario;
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

        iniciarThreadEscuta();
    }

    private void criarInterface() {
        setLayout(new BorderLayout());

        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnIniciar = new JButton("Iniciar Jogo");
        btnIniciar.setEnabled(false);
        btnIniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarJogo();
                btnIniciar.setEnabled(false);
                btnReiniciar.setEnabled(true);
                btnDesistir.setEnabled(true);
                logArea.append("Jogo iniciado! Sua vez? " + suaVez + "\n");
            }
        });
        painelControles.add(btnIniciar);

        btnReiniciar = new JButton("Reiniciar Jogo");
        btnReiniciar.setEnabled(false);
        btnReiniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(BatalhaNaval.this,
                        "Tem certeza que deseja reiniciar o jogo?", "Reiniciar Jogo", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    suaVez = false;
                    enviarReiniciarAoOponente();
                    resetarEstadoJogo(false);
                    btnIniciar.setEnabled(false);
                }
            }
        });
        painelControles.add(btnReiniciar);

        btnDesistir = new JButton("Desistir");
        btnDesistir.setEnabled(false);
        btnDesistir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(BatalhaNaval.this,
                        "Tem certeza que deseja desistir do jogo? Você perderá!", "Desistir do Jogo",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    logArea.append("Você desistiu do jogo.\n");
                    JOptionPane.showMessageDialog(BatalhaNaval.this, "Você desistiu e perdeu!");
                    enviarDesistenciaAoOponente();
                    jogoEncerrado = true;
                    disableBoard();
                    btnReiniciar.setEnabled(false);
                    btnDesistir.setEnabled(false);
                    btnIniciar.setEnabled(false);
                }
            }
        });
        painelControles.add(btnDesistir);

        add(painelControles, BorderLayout.NORTH);

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

                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (tabuleiroJogador[l][c] == 0 && naviosRestantes > 0) {
                            tabuleiroJogador[l][c] = 1;
                            btn.setBackground(Color.GRAY);

                            Som.tocarColocar();

                            naviosRestantes--;
                            logArea.append("Navio posicionado em: " + l + "," + c + "\n");
                            if (naviosRestantes == 0) {
                                logArea.append("Todos os navios posicionados!\n");
                                btnIniciar.setEnabled(true);
                            }
                        }
                    }
                });
            }
        }
    }

    private void iniciarJogo() {
        jogoEncerrado = false;

        EscreverXML.iniciarPartida(nome, nomeAdversario);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JButton btn = botoesTabuleiro[i][j];
                for (ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
                final int l = i, c = j;
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        jogarPosicao(l, c);
                    }
                });
            }
        }
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

            char letra = (char) ('A' + linha);
            String coordenada = letra + "-" + (coluna + 1);
            ultimaJogada = coordenada;
            EscreverXML.registrarJogada(1, coordenada, "enviada");

            suaVez = false;
        } catch (Exception ex) {
            logArea.append("Erro ao enviar jogada: " + ex.getMessage() + "\n");
        }
    }

    private void iniciarThreadEscuta() {
        Thread threadEscuta = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String mensagem = (String) entrada.readObject();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (MSG_FIM.equals(mensagem)) {
                                    if (!jogoEncerrado) {
                                        jogoEncerrado = true;
                                        EscreverXML.registrarResultado(nome, ultimaJogada);
                                        logArea.append("Oponente foi derrotado. Você venceu!\n");
                                        JOptionPane.showMessageDialog(BatalhaNaval.this, "Você venceu!");
                                        disableBoard();
                                        btnReiniciar.setEnabled(true);
                                        btnDesistir.setEnabled(true);
                                        btnIniciar.setEnabled(false);
                                    }
                                } else if (MSG_REINICIAR.equals(mensagem)) {
                                    suaVez = true;
                                    resetarEstadoJogo(true);
                                    logArea.append("O jogo foi reiniciado pelo adversário. Sua vez de jogar!\n");
                                    btnIniciar.setEnabled(false);
                                    btnReiniciar.setEnabled(true);
                                    btnDesistir.setEnabled(true);
                                } else if (MSG_DESISTENCIA.equals(mensagem)) {
                                    jogoEncerrado = true;
                                    logArea.append("Oponente desistiu. Você venceu por desistência!\n");
                                    JOptionPane.showMessageDialog(BatalhaNaval.this,
                                            "O oponente desistiu. Você venceu por desistência!");
                                    disableBoard();
                                    btnReiniciar.setEnabled(true);
                                    btnDesistir.setEnabled(true);
                                    btnIniciar.setEnabled(false);
                                } else {
                                    tratarJogadaRecebida(mensagem);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            logArea.append("Conexão encerrada.\n");
                            disableBoard();
                            btnReiniciar.setEnabled(false);
                            btnDesistir.setEnabled(false);
                            btnIniciar.setEnabled(false);
                        }
                    });
                }
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

                char letra = (char) ('A' + linha);
                String coordenada = letra + "-" + (coluna + 1);

                JButton btn = botoesTabuleiro[linha][coluna];
                if (tabuleiroJogador[linha][coluna] == 1) {
                    tabuleiroJogador[linha][coluna] = -1;
                    btn.setBackground(Color.RED);
                    logArea.append("Seu navio foi atingido em " + linha + "," + coluna + "\n");
                    Som.tocarAcerto();
                    EscreverXML.registrarJogada(2, coordenada, "acerto");

                    if (verificaDerrota()) {
                        logArea.append("Todos os navios destruídos. Você perdeu!\n");
                        EscreverXML.registrarResultado(nomeAdversario, coordenada);
                        JOptionPane.showMessageDialog(this, "Você perdeu!");
                        enviarFimAoOponente();
                        jogoEncerrado = true;
                        disableBoard();
                        btnReiniciar.setEnabled(true);
                        btnDesistir.setEnabled(true);
                        btnIniciar.setEnabled(false);
                        return;
                    }

                } else {
                    btn.setBackground(Color.WHITE);
                    logArea.append("Oponente errou em " + linha + "," + coluna + "\n");
                    Som.tocarErro();
                    EscreverXML.registrarJogada(2, coordenada, "erro");
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

    private void enviarReiniciarAoOponente() {
        try {
            saida.writeObject(MSG_REINICIAR);
            saida.flush();
        } catch (Exception ex) {
            logArea.append("Erro ao enviar reinício: " + ex.getMessage() + "\n");
        }
    }

    private void enviarDesistenciaAoOponente() {
        try {
            saida.writeObject(MSG_DESISTENCIA);
            saida.flush();
        } catch (Exception ex) {
            logArea.append("Erro ao enviar desistência: " + ex.getMessage() + "\n");
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

    private void resetarEstadoJogo(boolean suaVezInicial) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tabuleiroJogador[i][j] = 0;
                botoesTabuleiro[i][j].setBackground(new Color(70, 130, 180));
                botoesTabuleiro[i][j].setEnabled(true);
            }
        }
        naviosRestantes = 5;
        jogoEncerrado = false;
        suaVez = suaVezInicial;
        logArea.setText("");
        logArea.append("Jogo reiniciado! Posicione seus navios novamente.\n");
        posicionarNavios();
    }
}
