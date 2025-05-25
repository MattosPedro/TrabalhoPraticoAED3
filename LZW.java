import java.io.*;
import java.util.*;

public class LZW {

    // Compressão
    public static byte[] compress(byte[] input) {
        Map<String, Integer> dictionary = new HashMap<>();
        int dictSize = 256;

        // Inicializa dicionário com todos os bytes
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        String w = "";
        List<Integer> result = new ArrayList<>();

        for (byte b : input) {
            String wc = w + (char) (b & 0xFF);
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                dictionary.put(wc, dictSize++);
                w = "" + (char) (b & 0xFF);
            }
        }

        if (!w.equals("")) {
            result.add(dictionary.get(w));
        }

        // Escreve saída como bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            for (int code : result) {
                // 16 bits por código
                dos.writeShort(code);
            }
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    // Descompressão
    public static byte[] decompress(byte[] compressed) {
        Map<Integer, String> dictionary = new HashMap<>();
        int dictSize = 256;

        // Inicializa dicionário com todos os bytes
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        List<Integer> compressedCodes = new ArrayList<>();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(compressed));

        try {
            while (dis.available() > 0) {
                compressedCodes.add((int) dis.readShort() & 0xFFFF);
            }
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String w = "" + (char) (int) compressedCodes.remove(0);
        StringBuilder result = new StringBuilder(w);

        for (int k : compressedCodes) {
            String entry;
            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Código inválido: " + k);
            }

            result.append(entry);

            dictionary.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }

        // Converte para array de bytes
        byte[] output = new byte[result.length()];
        for (int i = 0; i < result.length(); i++) {
            output[i] = (byte) result.charAt(i);
        }

        return output;
    }
}
