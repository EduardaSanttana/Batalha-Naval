package jogo;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Som {

    private static void tocar(String caminho) {
        try {
            URL url = Som.class.getResource(caminho);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tocarAcerto() {
        tocar("/som/acerto.wav");
    }

    public static void tocarErro() {
        tocar("/som/erro.wav");
    }

    public static void tocarColocar() {
        tocar("/som/colocar.wav"); 
    }
}
