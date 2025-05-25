import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CompressaoUtils {

    public static void realizarCompressao(String caminhoArquivo, String versao) throws IOException {
        File arquivoOriginal = new File(caminhoArquivo);
        byte[] dadosOriginais = Files.readAllBytes(arquivoOriginal.toPath());

        // Huffman
        long startHuffman = System.currentTimeMillis();
        byte[] comprimidoHuffman = Huffman.compress(dadosOriginais);
        long endHuffman = System.currentTimeMillis();
        long tempoHuffman = endHuffman - startHuffman;

        String nomeHuffman = caminhoArquivo + "HuffmanCompressao" + versao;
        Files.write(Paths.get(nomeHuffman), comprimidoHuffman);

        // LZW
        long startLZW = System.currentTimeMillis();
        byte[] comprimidoLZW = LZW.compress(dadosOriginais);
        long endLZW = System.currentTimeMillis();
        long tempoLZW = endLZW - startLZW;

        String nomeLZW = caminhoArquivo + "LZWCompressao" + versao;
        Files.write(Paths.get(nomeLZW), comprimidoLZW);

        // Tamanhos
        double tamanhoOriginal = dadosOriginais.length;
        double tamanhoHuffman = comprimidoHuffman.length;
        double tamanhoLZW = comprimidoLZW.length;

        double ganhoHuffman = (1 - (tamanhoHuffman / tamanhoOriginal)) * 100;
        double ganhoLZW = (1 - (tamanhoLZW / tamanhoOriginal)) * 100;

        // Exibição
        System.out.printf("Huffman: %.2f%% de ganho, tempo: %d ms\n", ganhoHuffman, tempoHuffman);
        System.out.printf("LZW: %.2f%% de ganho, tempo: %d ms\n", ganhoLZW, tempoLZW);

        if (ganhoHuffman > ganhoLZW) {
            System.out.println("Huffman foi melhor para esta compressão.");
        } else {
            System.out.println("LZW foi melhor para esta compressão.");
        }
    }

    public static void realizarDescompressao(String caminhoBase, String versao) throws IOException {
        String caminhoHuffman = caminhoBase + "HuffmanCompressao" + versao;
        String caminhoLZW = caminhoBase + "LZWCompressao" + versao;

        // Huffman
        byte[] comprimidoHuffman = Files.readAllBytes(Paths.get(caminhoHuffman));
        long startHuffman = System.currentTimeMillis();
        byte[] descomprimidoHuffman = Huffman.decompress(comprimidoHuffman);
        long endHuffman = System.currentTimeMillis();
        long tempoHuffman = endHuffman - startHuffman;

        // LZW
        byte[] comprimidoLZW = Files.readAllBytes(Paths.get(caminhoLZW));
        long startLZW = System.currentTimeMillis();
        byte[] descomprimidoLZW = LZW.decompress(comprimidoLZW);
        long endLZW = System.currentTimeMillis();
        long tempoLZW = endLZW - startLZW;

        // Substituindo o arquivo base com Huffman ou LZW (escolha)
        Files.write(Paths.get(caminhoBase + "_descomprimidoHuffman"), descomprimidoHuffman);
        Files.write(Paths.get(caminhoBase + "_descomprimidoLZW"), descomprimidoLZW);

        System.out.printf("Descompressão Huffman: %d ms\n", tempoHuffman);
        System.out.printf("Descompressão LZW: %d ms\n", tempoLZW);

        if (tempoHuffman < tempoLZW) {
            System.out.println("Huffman foi mais rápido na descompressão.");
        } else {
            System.out.println("LZW foi mais rápido na descompressão.");
        }
    }
}
