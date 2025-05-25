import java.io.*;
import java.util.*;

public class Huffman {

    // Classe auxiliar para nós da árvore
    private static class Node implements Comparable<Node> {
        byte symbol;
        int frequency;
        Node left, right;

        Node(byte symbol, int frequency) {
            this.symbol = symbol;
            this.frequency = frequency;
        }

        Node(Node left, Node right) {
            this.symbol = 0;
            this.frequency = left.frequency + right.frequency;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return (left == null) && (right == null);
        }

        @Override
        public int compareTo(Node o) {
            return this.frequency - o.frequency;
        }
    }

    // Codificação
    public static byte[] compress(byte[] data) {
        Map<Byte, Integer> freqMap = new HashMap<>();
        for (byte b : data) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        Node root = buildTree(pq);
        Map<Byte, String> codeMap = new HashMap<>();
        buildCodeMap(root, "", codeMap);

        StringBuilder encodedBits = new StringBuilder();
        for (byte b : data) {
            encodedBits.append(codeMap.get(b));
        }

        // Convertendo bits em bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < encodedBits.length(); i += 8) {
            String byteStr = encodedBits.substring(i, Math.min(i + 8, encodedBits.length()));
            while (byteStr.length() < 8)
                byteStr += "0";
            baos.write((byte) Integer.parseInt(byteStr, 2));
        }

        // Serializa árvore + dados codificados
        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(finalOut);
            oos.writeObject(freqMap); // Serializa o mapa de frequência para reconstrução
            oos.writeInt(encodedBits.length()); // Tamanho dos bits reais
            oos.write(baos.toByteArray());
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalOut.toByteArray();
    }

    // Descodificação
    public static byte[] decompress(byte[] compressedData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            Map<Byte, Integer> freqMap = (Map<Byte, Integer>) ois.readObject();
            int bitsLength = ois.readInt();

            Node root = buildTreeFromFreq(freqMap);

            // Lê bytes comprimidos e transforma em string de bits
            ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
            int nextByte;
            while ((nextByte = ois.read()) != -1) {
                String bits = String.format("%8s", Integer.toBinaryString(nextByte & 0xFF)).replace(' ', '0');
                dataOut.write(bits.getBytes());
            }
            ois.close();

            byte[] bitsBytes = dataOut.toByteArray();
            StringBuilder allBits = new StringBuilder();
            for (byte b : bitsBytes) {
                allBits.append((char) b);
            }
            String bitString = allBits.substring(0, bitsLength);

            List<Byte> decoded = new ArrayList<>();
            Node current = root;
            for (char c : bitString.toCharArray()) {
                current = (c == '0') ? current.left : current.right;
                if (current.isLeaf()) {
                    decoded.add(current.symbol);
                    current = root;
                }
            }

            byte[] result = new byte[decoded.size()];
            for (int i = 0; i < decoded.size(); i++) {
                result[i] = decoded.get(i);
            }
            return result;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static Node buildTree(PriorityQueue<Node> pq) {
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            pq.add(new Node(left, right));
        }
        return pq.poll();
    }

    private static void buildCodeMap(Node node, String code, Map<Byte, String> codeMap) {
        if (node.isLeaf()) {
            codeMap.put(node.symbol, code);
            return;
        }
        buildCodeMap(node.left, code + "0", codeMap);
        buildCodeMap(node.right, code + "1", codeMap);
    }

    private static Node buildTreeFromFreq(Map<Byte, Integer> freqMap) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }
        return buildTree(pq);
    }
}
