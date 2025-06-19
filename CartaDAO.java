import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.nio.file.*;
import java.util.zip.*;
import java.net.URL;

/* 
 * CLASSE CartaDAO
 *  Classe responsável por realizar o CRUD no arquivo, além de realizar a chamada e leitura do arquivo.csv no banco de dados
 * do Kaggle, criação de objetos e toda a sua manipualação em arquivo sequencial
 *  Também realiza operações na árvore B+ e na tabela hash, realizando as interações com crud entre classes
 * 
*/

public class CartaDAO {
    private final String arquivo = "cartas.dat";
    private BPlusTree<Integer, Long> indice;
    private BPlusTree<Integer, CartaMagic> arvore;
    private long ultimaPosicaoRegistro;
    private HashingEstendido indiceHash;

    // Construtor
    public CartaDAO() throws IOException {
        File file = new File(arquivo);
        if (!file.exists()) {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {

                // Inicializa o último ID no cabeçalho
                dos.writeInt(0);
            }
        }
        this.indice = new BPlusTree<>(3);
        carregarIndice();
        this.indiceHash = new HashingEstendido(3, 10);
        indiceHash.carregarIndice();
        this.ultimaPosicaoRegistro = 0;
    }

    // Função para obter arvore B+
    public BPlusTree<Integer, Long> getArvore() {
        return indice;
    }

    // Função para carregar o indice na tabela HASH
    private void carregarIndice() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            raf.seek(4); // Pula o cabeçalho

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tam = raf.readInt();
                byte[] dados = new byte[tam];
                raf.readFully(dados);

                if (lapide == 0) {
                    CartaMagic carta = CartaMagic.fromByteArray(dados);
                    indice.insert(carta.getId(), pos);
                }
            }
        }
    }

    // Função para obter tabela
    public HashingEstendido getIndice() {
        return indiceHash;
    }

    // Função para codificar em Base64 (para a autenticação)
    private String encodeToBase64(String value) {
        return java.util.Base64.getEncoder().encodeToString(value.getBytes());
    }

    // Método para descompactar o arquivo ZIP
    public String descompactarArquivo(String arquivoZip, String destino) throws IOException {
        File destinoDir = new File(destino);
        if (!destinoDir.exists()) {

            // Cria o diretório se não existir
            destinoDir.mkdirs();
        }

        // Cria um fluxo de entrada para ler o arquivo ZIP
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(arquivoZip))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outputFile = new File(destinoDir, entry.getName());

                // Cria o diretório da entrada, se necessário
                outputFile.getParentFile().mkdirs();

                // Abre um fluxo de saída para gravar o conteúdo do arquivo extraído
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                }
            }
        }

        // Retorna o caminho absoluto do diretório descompactado
        return destinoDir.getAbsolutePath();
    }

    // Função para importar os dados após o descompactamento
    public void importarCSVDeZip(String caminhoCSV) throws IOException {
        importarCSV(caminhoCSV);
    }

    // Função para baixar o arquivo via API - exemplo de chamada
    public void baixarArquivoViaAPI(String username, String key, String destinoZip)
            throws IOException, InterruptedException {
        String urlString = "https://www.kaggle.com/api/v1/datasets/download/joaopedroreis10/cartasmagic";
        URL url = new URL(urlString);

        // Conectando com a URL do Kaggle
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Definindo cabeçalhos de autenticação
        String auth = username + ":" + key;
        String encodedAuth = encodeToBase64(auth);
        connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

        // Conectando e recebendo o arquivo
        try (InputStream in = connection.getInputStream();
                // Abre um fluxo de saída (FileOutputStream) para salvar os dados no arquivo
                // especificado
                FileOutputStream out = new FileOutputStream(destinoZip)) {
            // Limite para ler dados aos poucos
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("Arquivo baixado com sucesso para " + destinoZip);
        } catch (IOException e) {
            System.err.println("Erro ao baixar o arquivo: " + e.getMessage());
            throw e;
        } finally {

            // Interrompe a API
            connection.disconnect();
        }
    }

    // Função para importar um arquivo CSV e criar objetos das cartas
    public void importarCSV(String caminhoCSV) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(caminhoCSV), StandardCharsets.UTF_8))) {
            String linha = br.readLine(); // Ignora cabeçalho
            if (linha == null)
                return;

            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");

                String nome = campos[0];
                LocalDate dataLancamento = LocalDate.parse(campos[1]);
                double preco = Double.parseDouble(campos[3]);

                Map<String, Integer> habilidades = new HashMap<>();
                String[] habilidadesArray = campos[2].replace("\"", "").split(";");
                for (String h : habilidadesArray) {
                    String[] partes = h.split(":");
                    habilidades.put(partes[0], Integer.parseInt(partes[1]));
                }

                CartaMagic carta = new CartaMagic(0, nome, dataLancamento, habilidades, preco);
                create(carta, "semId");
            }
        }
    }

    // Função para criar objetos das cartas
    public void create(CartaMagic carta, String flag) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            raf.seek(0);
            int ultimoId = raf.readInt();
            raf.seek(ultimaPosicaoRegistro);
            if (flag.equals("semId")) {
                carta.setId(++ultimoId);
                raf.seek(0);
                raf.writeInt(ultimoId);
            }

            byte[] byteArray = carta.toByteArray();

            raf.seek(raf.length());
            long pos = raf.getFilePointer(); // Posição antes de escrever
            raf.writeByte(0); // Lápide
            raf.writeInt(byteArray.length);
            raf.write(byteArray);

            // Atualiza índice
            indice.insert(carta.getId(), pos);
            ultimaPosicaoRegistro = raf.getFilePointer();
            indiceHash.adicionarRegistro(carta.getId(), ultimaPosicaoRegistro);
        }
    }

    public long getPosicaoDoUltimoRegistroCriado() {
        return ultimaPosicaoRegistro;
    }

    // Função para deletar objetos das cartas
    public boolean delete(int id) throws IOException {
        Long pos = indice.search(id);
        if (pos == null)
            return false;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            raf.seek(pos);
            raf.writeByte(1); // Lápide
            indice.delete(id); // arvore
            indiceHash.excluir(id); // tabela hash
            return true;
        }
    }

    // Função para listar todos os objetos das cartas
    public List<CartaMagic> listarTodos() throws IOException {
        List<CartaMagic> cartas = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            raf.seek(4);

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                byte[] byteArray = new byte[tamanho];
                raf.readFully(byteArray);

                if (lapide == 0) {
                    cartas.add(CartaMagic.fromByteArray(byteArray));
                }
            }
        }
        return cartas;
    }

    // Função para ler carta a partir de ID informado de um dos objetos das cartas
    public CartaMagic read(int id) throws IOException {
        Long pos = indice.search(id);
        if (pos == null)
            return null;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            raf.seek(pos);
            byte lapide = raf.readByte();
            int tam = raf.readInt();
            byte[] dados = new byte[tam];
            raf.readFully(dados);

            if (lapide == 0) {
                return CartaMagic.fromByteArray(dados);
            }
        }
        return null;
    }

    // Função para ler cartas a partir de ID's informados de objetos das cartas
    public List<CartaMagic> readMultiple(List<Integer> ids) throws IOException {
        List<CartaMagic> cartas = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            for (int id : ids) {
                Long pos = indice.search(id);
                if (pos != null) {
                    raf.seek(pos);
                    byte lapide = raf.readByte();
                    int tam = raf.readInt();
                    byte[] dados = new byte[tam];
                    raf.readFully(dados);

                    if (lapide == 0) {
                        cartas.add(CartaMagic.fromByteArray(dados));
                    }
                }
            }
        }
        return cartas;
    }

    // Função para atualizar alguma carta no arquivo
    public boolean update(int id, CartaMagic novaCarta) throws IOException {
        Long pos = indice.search(id);
        if (pos == null)
            return false;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            raf.seek(pos);
            raf.writeByte(1); // Marca como excluído
            indice.delete(id);

            novaCarta.setId(id);
            create(novaCarta, "comId");
            return true;
        }
    }

    // Método de exibição da tabela hash
    public void exibirEstadoHashing() throws IOException {
        System.out.println("\n===== Estado do Hashing Estendido =====");

        for (int i = 0; i < indiceHash.getNumBuckets(); i++) {
            System.out.print("Bucket " + i + ": ");
            List<HashingEstendido.Registro> bucket = indiceHash.getRegistrosDoBucket(i);

            if (bucket.isEmpty()) {
                System.out.println("Vazio");
            } else {
                for (HashingEstendido.Registro registro : bucket) {
                    System.out.print("ID: " + registro.getId() + ", Posição: " + registro.getPos() + " | ");
                }
                System.out.println();
            }
        }
    }

    /**
     * Busca todas as cartas cujo nome contenha o padrão informado.
     * Utiliza o algoritmo Knuth-Morris-Pratt (KMP) para casamento eficiente de
     * padrões.
     *
     * Justificativa: o KMP é um algoritmo clássico de busca de padrão em texto com
     * complexidade linear O(n + m), onde n é o tamanho do texto e m o tamanho do
     * padrão.
     * Ele é mais eficiente do que a busca ingênua em cenários com muitas
     * comparações.
     */

    public List<CartaMagic> buscarPorPadraoNoNome(String padrao) throws IOException {
        List<CartaMagic> resultados = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            raf.seek(4); // Pula o cabeçalho

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tam = raf.readInt();

                byte[] dados = new byte[tam];
                raf.readFully(dados);

                if (lapide == 0) {
                    CartaMagic carta = CartaMagic.fromByteArray(dados);
                    if (kmp(carta.getNome().toLowerCase(), padrao.toLowerCase())) {
                        resultados.add(carta);
                    }
                }
            }
        }

        return resultados;
    }

    /**
     * Algoritmo Knuth-Morris-Pratt (KMP) para busca de padrão em string.
     */
    private boolean kmp(String texto, String padrao) {
        int[] lps = construirLPS(padrao);
        int i = 0; // índice para texto
        int j = 0; // índice para padrão

        while (i < texto.length()) {
            if (texto.charAt(i) == padrao.charAt(j)) {
                i++;
                j++;
                if (j == padrao.length()) {
                    return true; // padrão encontrado
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }

        return false;
    }

    /**
     * Constrói o vetor LPS (longest proper prefix which is also suffix)
     * usado pelo algoritmo KMP.
     */
    private int[] construirLPS(String padrao) {
        int[] lps = new int[padrao.length()];
        int len = 0;
        int i = 1;

        while (i < padrao.length()) {
            if (padrao.charAt(i) == padrao.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    public CartaMagic descriptografarHabilidades(CartaMagic carta) {
        if (carta == null)
            return null;
        Map<String, Integer> habilidades = carta.getHabilidades();

        // Tenta descriptografar com AES
        Map<String, Integer> habilidadesAES = CryptoUtils.decryptMapAES(habilidades);
        if (!habilidadesAES.equals(habilidades)) {
            return new CartaMagic(
                carta.getId(),
                carta.getNome(),
                carta.getDataLancamento(),
                habilidadesAES,
                carta.getPreco()
            );
        }

        // Tenta descriptografar com XOR
        Map<String, Integer> habilidadesXOR = CryptoUtils.decryptMapXOR(habilidades, 'K');
        if (!habilidadesXOR.equals(habilidades)) {
            return new CartaMagic(
                carta.getId(),
                carta.getNome(),
                carta.getDataLancamento(),
                habilidadesXOR,
                carta.getPreco()
            );
        }

        // Se não conseguir, retorna a original
        return carta;
    }

    // Função auxiliar para verificar se todas as chaves são legíveis (apenas letras e números)
    private boolean todasChavesSaoLegiveis(Map<String, Integer> map) {
        for (String key : map.keySet()) {
            if (!key.matches("[\\p{L}\\p{N} _-]+")) { // letras, números, espaço, underline, hífen
                return false;
            }
        }
        return true;
    }

}