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
 * do Kaggle, criação de objetos e toda sua manipualção em arquivo sequencial
 * 
*/

@SuppressWarnings({"ALL", "LanguageDetectionInspection"})
public class CartaDAO {
    private final String arquivo = "cartas.dat";

    // Construtor
    public CartaDAO() throws IOException {
        File file = new File(arquivo);
        if (!file.exists()) {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {

                // Inicializa o último ID no cabeçalho
                dos.writeInt(0);
            }
        }
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
    /*
     * #!/bin/bash
     * # Export your Kaggle username and API key
     * # export KAGGLE_USERNAME=<YOUR USERNAME>
     * # export KAGGLE_KEY=<YOUR KAGGLE KEY>
     * 
     * curl -L -u $KAGGLE_USERNAME:$KAGGLE_KEY\
     * -o ~/Downloads/cartasmagic.zip\
     * https://www.kaggle.com/api/v1/datasets/download/joaopedroreis10/cartasmagic
     * 
     */
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

            // Contador id
            if (flag == "semId") {
                carta.setId(++ultimoId);
            }

            raf.seek(0);
            raf.writeInt(ultimoId);

            // Manipulação para incremento no arquivo sequencial
            byte[] byteArray = carta.toByteArray();

            raf.seek(raf.length());
            raf.writeByte(0); // Lápide (0 = válido)
            raf.writeInt(byteArray.length);
            raf.write(byteArray);
        }
    }

    // Função para deletar objetos das cartas
    public boolean delete(int id) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {

            // Os primeiros 4 bytes do arquivo armazenam o último ID utilizado, logo o
            // ponteiro é movido para a posição onde começam os registros
            raf.seek(4);

            while (raf.getFilePointer() < raf.length()) {
                long posicao = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                byte[] byteArray = new byte[tamanho];
                raf.readFully(byteArray);

                // Manipulação correta de dados
                CartaMagic carta = CartaMagic.fromByteArray(byteArray);

                // Atualização de lápide
                if (lapide == 0 && carta.getId() == id) {
                    raf.seek(posicao);
                    raf.writeByte(1); // Marca como excluído
                    return true;
                }
            }
        }
        return false;
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
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            raf.seek(4);

            while (raf.getFilePointer() < raf.length()) {
                long posicao = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide == 0) {
                    byte[] dados = new byte[tamanhoRegistro];
                    raf.readFully(dados);
                    CartaMagic carta = CartaMagic.fromByteArray(dados);

                    if (carta.getId() == id) {
                        return carta;
                    }
                } else {

                    // Pula registros inválidos
                    raf.skipBytes(tamanhoRegistro);
                }
            }
        }
        return null; // Retorna null se não encontrar
    }

    // Função para ler cartas a partir de ID's informados de objetos das cartas
    public List<CartaMagic> readMultiple(List<Integer> ids) throws IOException {
        List<CartaMagic> cartas = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            raf.seek(4);

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide == 0) {
                    byte[] dados = new byte[tamanhoRegistro];
                    raf.readFully(dados);
                    CartaMagic carta = CartaMagic.fromByteArray(dados);

                    if (ids.contains(carta.getId())) {
                        cartas.add(carta);
                    }
                } else {

                    // Pula registros inválidos
                    raf.skipBytes(tamanhoRegistro);
                }
            }
        }
        return cartas;
    }

    // Função para atualizar objetos das cartas
    public boolean update(int id, CartaMagic novaCarta) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            raf.seek(4);

            while (raf.getFilePointer() < raf.length()) {
                long posicao = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide == 0) {
                    byte[] dados = new byte[tamanhoRegistro];
                    raf.readFully(dados);
                    CartaMagic carta = CartaMagic.fromByteArray(dados);

                    if (carta.getId() == id) {
                        raf.seek(posicao);
                        raf.writeByte(1);
                        novaCarta.setId(id);

                        // Adiciona novo registro ao final
                        create(novaCarta, "comId");
                        return true;
                    }
                } else {
                    raf.skipBytes(tamanhoRegistro);
                }
            }
        }
        return false;
    }

}
