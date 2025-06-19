import java.io.*;
import java.util.*;

public class HashingEstendido {
    private final String arquivoIndices = "indices.dat"; // Arquivo de índices
    private final int capacidadeBuckets; // Quantidade de registros por bucket
    private final int numBuckets; // Quantidade de buckets na tabela hash
    private final List<List<Registro>> tabelaHash; // Tabela de Hashing Estendido

    // Construtor
    public HashingEstendido(int capacidadeBuckets, int numBuckets) throws IOException {
        this.capacidadeBuckets = capacidadeBuckets;
        this.numBuckets = numBuckets;
        this.tabelaHash = new ArrayList<>(numBuckets);

        for (int i = 0; i < numBuckets; i++) {
            tabelaHash.add(new ArrayList<>());
        }

        // Verifica se o arquivo de índices existe, se não, cria um novo.
        File file = new File(arquivoIndices);
        if (!file.exists()) {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
                // Inicializa os buckets e o arquivo de índices
                for (int i = 0; i < numBuckets; i++) {
                    dos.writeInt(0); // Inicializa cada bucket com 0 (sem registros)
                }
            }
        } else {
            carregarIndice(); // Se o arquivo existir, carrega os índices do arquivo
        }
    }

    // Função hash (exemplo de divisão simples)
    private int hash(int id) {
        return id % numBuckets;
    }

    // Função para inserir um novo registro no índice
    public void adicionarRegistro(int id, long pos) throws IOException {
        int index = hash(id); // Determina o índice do bucket
        List<Registro> bucket = tabelaHash.get(index);

        if (bucket.size() < capacidadeBuckets) {
            bucket.add(new Registro(id, pos));
            salvarIndice();
        } else {
            // System.out.println("Bucket cheio! Implementar expansão...");
        }
    }

    // Função para buscar um registro no índice
    public Long buscar(int id) throws IOException {
        int index = hash(id);
        List<Registro> bucket = tabelaHash.get(index);

        for (Registro r : bucket) {
            if (r.getId() == id) {
                return r.getPos();
            }
        }
        return null;
    }

    public boolean excluir(int id) throws IOException {
        int index = hash(id);
        List<Registro> bucket = tabelaHash.get(index);

        boolean removido = bucket.removeIf(r -> r.getId() == id);

        if (removido) {
            salvarIndice();
        }

        return removido;
    }

    // Salva o estado da tabela hash no arquivo de índices
    private void salvarIndice() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivoIndices))) {
            for (List<Registro> bucket : tabelaHash) {
                dos.writeInt(bucket.size());
                for (Registro r : bucket) {
                    dos.writeInt(r.getId());
                    dos.writeLong(r.getPos());
                }
            }
        }
    }

    // Carrega a tabela de índices do arquivo
    public void carregarIndice() throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivoIndices))) {
            for (int i = 0; i < numBuckets; i++) {
                int tamanhoBucket = dis.readInt();
                List<Registro> bucket = tabelaHash.get(i);

                for (int j = 0; j < tamanhoBucket; j++) {
                    int id = dis.readInt();
                    long pos = dis.readLong();
                    bucket.add(new Registro(id, pos));
                }
            }
        }
    }

    // Classe interna para representar um registro no índice
    public static class Registro {
        private final int id;
        private final long pos;

        public Registro(int id, long pos) {
            this.id = id;
            this.pos = pos;
        }

        public int getId() {
            return id;
        }

        public long getPos() {
            return pos;
        }
    }

    // Método para obter a tabela de hash (útil para exibir os dados)
    public List<List<Registro>> getTabelaHash() {
        return tabelaHash;
    }

    // Método para obter o número de buckets
    public int getNumBuckets() {
        return numBuckets;
    }

    public List<Registro> getRegistrosDoBucket(int bucketIndex) {
        if (bucketIndex >= 0 && bucketIndex < numBuckets) {
            return tabelaHash.get(bucketIndex);
        }
        return Collections.emptyList(); // Retorna uma lista vazia se o índice for inválido
    }
}
