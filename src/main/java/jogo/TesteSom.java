package jogo;

public class TesteSom {
    public static void main(String[] args) {
        Som.tocarAcerto();
        try {
            Thread.sleep(3000); // espera 3 segundos para o som tocar
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
