import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/* 
 * CLASSE CartaMagic
 *  Possui os valores, construtor, métodos getteres e setteres, além das conversões de objeto em um array de bytes e virse-versa
 * para leitura e preenchimento correto de valores em um arquivo sequencial
 *  Tem também o retorno de um objeto
 * 
*/
public class CartaMagic {
    private int id;
    private String nome;
    private LocalDate dataLancamento;
    private Map<String, Integer> habilidades;
    private double preco;

    public CartaMagic(int id, String nome, LocalDate dataLancamento, Map<String, Integer> habilidades, double preco) {
        this.id = id;
        this.nome = nome;
        this.dataLancamento = dataLancamento;
        this.habilidades = habilidades;
        this.preco = preco;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public Map<String, Integer> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(Map<String, Integer> habilidades) {
        this.habilidades = habilidades;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    /**
     * Converte o objeto em um array de bytes para armazenar no arquivo.
     */
    public byte[] toByteArray() throws IOException {

        // Fluxo de saída que armazena os dados em um array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeUTF(dataLancamento.toString());
        dos.writeInt(habilidades.size());

        for (Map.Entry<String, Integer> entry : habilidades.entrySet()) {
            dos.writeUTF(entry.getKey());
            dos.writeInt(entry.getValue());
        }

        dos.writeDouble(preco);

        return baos.toByteArray();
    }

    /**
     * Converte um array de bytes de volta para um objeto `CartaMagic`.
     */
    public static CartaMagic fromByteArray(byte[] dados) throws IOException {

        // Fluxo de entrada que armazena os dados em um array de bytes
        ByteArrayInputStream bis = new ByteArrayInputStream(dados);
        DataInputStream dis = new DataInputStream(bis);

        int id = dis.readInt();
        String nome = dis.readUTF();
        LocalDate dataLancamento = LocalDate.parse(dis.readUTF());

        int numHabilidades = dis.readInt();
        Map<String, Integer> habilidades = new HashMap<>();
        for (int i = 0; i < numHabilidades; i++) {
            habilidades.put(dis.readUTF(), dis.readInt());
        }

        double preco = dis.readDouble();

        // Construtor com base na conversao do array para objeto
        return new CartaMagic(id, nome, dataLancamento, habilidades, preco);
    }

    @Override
    public String toString() {
        return "CartaMagic {" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataLancamento=" + dataLancamento +
                ", habilidades=" + habilidades +
                ", preco=" + preco +
                '}';
    }
}
