package jogo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BatalhaNaval extends JFrame {

    private static String mensagemFinal = "FIM";
    private static String mensagemReiniciar = "REINICIAR";
    private static String mensagemDesistencia = "DESISTENCIA";

    private volatile boolean jogoEncerrado = false;

    private String nome;
    private String nomeAdversario;
    private boolean suaVez;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;

    private JTextArea logArea;
    private JLabel labelMensagem;
    private JButton[][] botoesTabuleiro = new JButton[10][10];

    private int[][] tabuleiroJogador = new int[10][10];
    private int naviosRestantes = 5;

    private JButton btnIniciar;
    private JButton btnReiniciar;
    private JButton btnDesistir;

    private String ultimaJogada = "";

    private static final int tamanhoIcone = 48;

    private final ImageIcon iconeNavio = redimensionarIcone("/imagens/navio.png");
    private final ImageIcon iconeExplosao = redimensionarIcone("/imagens/explosao.png");
    private final ImageIcon iconeErro = redimensionarIcone("/imagens/marcador.png");
    private final ImageIcon iconeMarcador = redimensionarIcone("/imagens/marcador.png");

    private ImageIcon redimensionarIcone(String caminho) {
        Image img = new ImageIcon(getClass().getResource(caminho)).getImage();
        return new ImageIcon(img.getScaledInstance(tamanhoIcone, tamanhoIcone, Image.SCALE_SMOOTH));
    }

    public BatalhaNaval(String nome, boolean suaVez, ObjectInputStream entrada, ObjectOutputStream saida,
            String nomeAdversario) {
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

        labelMensagem = new JLabel("Posicione seus navios clicando no tabuleiro.");
        labelMensagem.setHorizontalAlignment(SwingConstants.CENTER);
        labelMensagem.setFont(new Font("Arial", Font.BOLD, 16));
        labelMensagem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelMensagem, BorderLayout.SOUTH);

        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnIniciar = new JButton("Iniciar Jogo");
        btnIniciar.setEnabled(false);
        btnIniciar.addActionListener(e -> {
            iniciarJogo();
            btnIniciar.setEnabled(false);
            btnReiniciar.setEnabled(true);
            btnDesistir.setEnabled(true);
            // logArea.append("Jogo iniciado! Sua vez? " + suaVez + "\n");
        });
        painelControles.add(btnIniciar);

        btnReiniciar = new JButton("Reiniciar Jogo");
        btnReiniciar.setEnabled(false);
        btnReiniciar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(BatalhaNaval.this,
                    "Tem certeza que deseja reiniciar o jogo?", "Reiniciar Jogo", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                suaVez = false;
                enviarReiniciarAoOponente();
                resetarEstadoJogo(false);
                btnIniciar.setEnabled(false);
            }
        });
        painelControles.add(btnReiniciar);

        btnDesistir = new JButton("Desistir");
        btnDesistir.setEnabled(false);
        btnDesistir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(BatalhaNaval.this,
                    "Tem certeza que deseja desistir do jogo?", "Desistir do Jogo", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // logArea.append("Você desistiu do jogo.\n");
                JOptionPane.showMessageDialog(BatalhaNaval.this, "Você desistiu e perdeu!");
                dispose();
                enviarDesistenciaAoOponente();
                jogoEncerrado = true;
                disableBoard();
                btnReiniciar.setEnabled(false);
                btnDesistir.setEnabled(false);
                btnIniciar.setEnabled(false);
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
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setBackground(new Color(70, 130, 180));
                botoesTabuleiro[i][j] = btn;
                painelTabuleiro.add(btn);
            }
        }

        add(scroll, BorderLayout.SOUTH);
        add(painelTabuleiro, BorderLayout.CENTER);
    }

    private void posicionarNavios() {
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
                        btn.setIcon(iconeNavio);

                        // Som.tocarColocar();
                        naviosRestantes--;
                        // logArea.append("Navio posicionado em: " + l + "," + c + "\n");
                        if (naviosRestantes == 0) {
                            logArea.append("Todos os navios posicionados!\n");
                            btnIniciar.setEnabled(true);
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
                btn.addActionListener(e -> jogarPosicao(l, c));
            }
        }
    }

    private void jogarPosicao(int linha, int coluna) {
        if (jogoEncerrado || !suaVez) {
            JOptionPane.showMessageDialog(this, "Ainda não é sua vez!");
            return;
        }
        String jogada = linha + "," + coluna;
        try {
            saida.writeObject(jogada);
            saida.flush();

            JButton botao = botoesTabuleiro[linha][coluna];
            botao.setEnabled(false);
            botao.setIcon(iconeMarcador);

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
        Thread threadEscuta = new Thread(() -> {
            try {
                while (true) {
                    String mensagem = (String) entrada.readObject();
                    SwingUtilities.invokeLater(() -> {
                        switch (mensagem) {
                            case "FIM":
                                if (!jogoEncerrado) {
                                    jogoEncerrado = true;
                                    EscreverXML.registrarResultado(nome, ultimaJogada);
                                    JOptionPane.showMessageDialog(BatalhaNaval.this, "Você venceu!");
                                    dispose();
                                    disableBoard();
                                    btnReiniciar.setEnabled(true);
                                    btnDesistir.setEnabled(true);
                                    btnIniciar.setEnabled(false);
                                }
                                break;
                            case "REINICIAR":
                                suaVez = true;
                                resetarEstadoJogo(true);
                                // logArea.append("O jogo foi reiniciado pelo adversário. Sua vez de jogar!\n");
                                btnIniciar.setEnabled(false);
                                btnReiniciar.setEnabled(true);
                                btnDesistir.setEnabled(true);
                                break;
                            case "DESISTENCIA":
                                jogoEncerrado = true;
                                JOptionPane.showMessageDialog(BatalhaNaval.this, "O oponente desistiu. Você venceu!");
                                dispose();
                                disableBoard();
                                btnReiniciar.setEnabled(true);
                                btnDesistir.setEnabled(true);
                                btnIniciar.setEnabled(false);
                                break;
                            case "ACERTO":
                                JOptionPane.showMessageDialog(BatalhaNaval.this, "Você acertou um navio do oponente!");
                                break;
                            default:
                                tratarJogadaRecebida(mensagem);
                        }
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    // logArea.append("Conexão encerrada.\n");
                    disableBoard();
                    btnReiniciar.setEnabled(false);
                    btnDesistir.setEnabled(false);
                    btnIniciar.setEnabled(false);
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

                char letra = (char) ('A' + linha);
                String coordenada = letra + "-" + (coluna + 1);

                JButton btn = botoesTabuleiro[linha][coluna];
                if (tabuleiroJogador[linha][coluna] == 1) {
                    tabuleiroJogador[linha][coluna] = -1;
                    btn.setIcon(iconeExplosao);
                    try {
                        saida.writeObject("ACERTO");
                        saida.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    Som.tocarAcerto();
                    EscreverXML.registrarJogada(2, coordenada, "acerto");

                    if (verificaDerrota()) {
                        logArea.append("Todos os navios destruídos. Você perdeu!\n");
                        EscreverXML.registrarResultado(nomeAdversario, coordenada);
                        JOptionPane.showMessageDialog(this, "Você perdeu!");
                        dispose();
                        enviarFimAoOponente();
                        jogoEncerrado = true;
                        disableBoard();
                        btnReiniciar.setEnabled(true);
                        btnDesistir.setEnabled(true);
                        btnIniciar.setEnabled(false);
                        return;
                    }

                } else {
                    btn.setIcon(iconeErro);
                    // logArea.append("Oponente errou em " + linha + "," + coluna + "\n");
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
            saida.writeObject(mensagemFinal);
            saida.flush();
        } catch (Exception ex) {
            logArea.append("Erro ao avisar oponente: " + ex.getMessage() + "\n");
        }
    }

    private void enviarReiniciarAoOponente() {
        try {
            saida.writeObject(mensagemReiniciar);
            saida.flush();
        } catch (Exception ex) {
            logArea.append("Erro ao enviar reinício: " + ex.getMessage() + "\n");
        }
    }

    private void enviarDesistenciaAoOponente() {
        try {
            saida.writeObject(mensagemDesistencia);
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
