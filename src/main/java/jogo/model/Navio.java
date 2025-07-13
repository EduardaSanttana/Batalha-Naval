package jogo.model;

import java.util.ArrayList;
import java.util.List;

public class Navio {
    private int tamanho;
    private int linha;
    private int coluna;
    private boolean horizontal;
    private int acertos;

    public Navio(int tamanho, int linha, int coluna, boolean horizontal) {
        this.tamanho = tamanho;
        this.linha = linha;
        this.coluna = coluna;
        this.horizontal = horizontal;
        this.acertos = 0;
    }

    public int getTamanho() {
        return tamanho;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void registrarAcerto() {
        acertos++;
    }

    public boolean foiAfundado() {
        return acertos >= tamanho;
    }

    //Verificar se acertaram o navio
    public boolean contemCoordenada(int l, int c) {
        for (int i = 0; i < tamanho; i++) {
            int atualLinha = horizontal ? linha : linha + i;
            int atualColuna = horizontal ? coluna + i : coluna;
            if (atualLinha == l && atualColuna == c) {
                return true;
            }
        }
        return false;
    }

    //Retorna as cordenadas do navio
    public List<int[]> getCoordenadas() {
        List<int[]> coords = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            int l = horizontal ? linha : linha + i;
            int c = horizontal ? coluna + i : coluna;
            coords.add(new int[]{l, c});
        }
        return coords;
    }
}
