package jogo;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class EscreverXML {

    private static final String CAMINHO_ARQUIVO = "partida.xml";
    private static Document doc;
    private static Element raiz;
    private static Element jogadas;

    public static void iniciarPartida(String jogador1, String jogador2) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();

            raiz = doc.createElement("partida");
            doc.appendChild(raiz);

            Element j1 = doc.createElement("jogador1");
            j1.setAttribute("nome", jogador1);
            raiz.appendChild(j1);

            Element j2 = doc.createElement("jogador2");
            j2.setAttribute("nome", jogador2);
            raiz.appendChild(j2);

            jogadas = doc.createElement("jogadas");
            raiz.appendChild(jogadas);

            salvarXML();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registrarJogada(int jogador, String coordenada, String resultado) {
        try {
            if (doc == null || raiz == null || jogadas == null) {
                return;
            }

            Element jogada = doc.createElement("jogada");
            jogada.setAttribute("jogador", String.valueOf(jogador));
            jogada.setAttribute("coordenada", coordenada);
            jogada.setAttribute("resultado", resultado);
            jogadas.appendChild(jogada);

            salvarXML();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registrarResultado(String vencedor, String ultimaJogada) {
        try {
            if (doc == null || raiz == null) {
                return;
            }

            Element resultado = doc.createElement("resultado");
            resultado.setAttribute("vencedor", vencedor);
            resultado.setAttribute("ultimaJogada", ultimaJogada);
            raiz.appendChild(resultado);

            salvarXML();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void salvarXML() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(CAMINHO_ARQUIVO));
        transformer.transform(source, result);
    }
}
