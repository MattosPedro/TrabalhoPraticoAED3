import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/* 
 * CLASSE Main
 *  É nela onde realizaremos todas as funções de usuário. Toda a lógica de casos para criar o arquivo (e derivações como arvore e tabela hash) e 
 * realizar todas operações de CRUD são chamadas via tela no terminal a partir dessa classe
 * 
*/
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            CartaDAO dao = new CartaDAO();
            boolean running = true;

            // Menu de opções
            while (running) {
                System.out.println("\n===== MENU =====");
                System.out.println("1 - Importar CSV");
                System.out.println("2 - Criar uma nova carta");
                System.out.println("3 - Buscar carta por ID");
                System.out.println("4 - Buscar várias cartas");
                System.out.println("5 - Atualizar carta");
                System.out.println("6 - Excluir carta");
                System.out.println("7 - Exibir todos os registros");
                System.out.println("8 - Exibir a árvore B+");
                System.out.println("9 - Exibir o estado do Hashing Estendido");
                System.out.println("0 - Sair");
                System.out.print("Escolha uma opção: ");

                int opcao = scanner.nextInt();

                // Limpa o buffer do teclado
                scanner.nextLine();

                switch (opcao) {
                    case 1:
                        System.out.print("Digite seu nome de usuário Kaggle (utilizar joaopedroreis10): ");
                        String kaggleUsername = scanner.nextLine();
                        System.out
                                .print("Digite sua chave da API Kaggle (utilizar 9c3127acd68ac7514f5eea16517578e0): ");
                        String kaggleKey = scanner.nextLine();

                        // Caminho para o arquivo ZIP e onde descompactar
                        String destinoZip = "cartasmagic.zip";
                        String destinoDescompactado = "destino_descompactado";

                        try {
                            dao.baixarArquivoViaAPI(kaggleUsername, kaggleKey, destinoZip);
                            System.out.println("Arquivo baixado com sucesso!");

                            String caminhoDescompactado = dao.descompactarArquivo(destinoZip, destinoDescompactado);
                            System.out.println("Arquivo descompactado com sucesso! Diretório: " + caminhoDescompactado);

                            String caminhoCSV = caminhoDescompactado + "/cartasInicial.csv";
                            dao.importarCSVDeZip(caminhoCSV);
                            System.out.println("CSV importado com sucesso!");

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("Ocorreu um erro durante o processo de download ou descompactação.");
                        }
                        break;

                    case 2:
                        System.out.println("Criando uma nova carta...");
                        System.out.print("Nome da carta: ");
                        String nome = scanner.nextLine();

                        System.out.print("Data de lançamento (YYYY-MM-DD): ");
                        LocalDate dataLancamento = LocalDate.parse(scanner.nextLine());

                        Map<String, Integer> habilidades = new HashMap<>();
                        while (true) {
                            System.out.print("Adicionar habilidade (nome:pontuação) ou 'fim' para encerrar: ");
                            String input = scanner.nextLine();
                            if (input.equalsIgnoreCase("fim"))
                                break;
                            try {
                                String[] partes = input.split(":");
                                if (partes.length != 2) {
                                    System.out.println("Formato inválido! Use nomeDaHabilidade:pontuação.");
                                    continue;
                                }
                                String nomeHabilidade = partes[0];
                                int pontuacao = Integer.parseInt(partes[1]);
                                habilidades.put(nomeHabilidade, pontuacao);
                            } catch (NumberFormatException e) {
                                System.out.println(
                                        "Erro ao converter a pontuação. Certifique-se de usar um número inteiro.");
                            }
                        }

                        System.out.print("Preço da carta: ");
                        double preco = scanner.nextDouble();

                        // Construtor
                        CartaMagic novaCarta = new CartaMagic(0, nome, dataLancamento, habilidades, preco);
                        dao.create(novaCarta, "semId");

                        System.out.println("Carta criada com sucesso!");
                        break;

                    case 3:
                        System.out.print("Digite o ID da carta a ser buscada: ");
                        int id = scanner.nextInt();
                        CartaMagic carta = dao.read(id);
                        if (carta != null) {
                            System.out.println("Carta encontrada: " + carta);
                        } else {
                            System.out.println("Carta não encontrada!");
                        }
                        break;

                    case 4:
                        System.out.print("Digite os IDs das cartas (separados por espaço): ");
                        String[] idsStr = scanner.nextLine().split(" ");
                        List<Integer> ids = new ArrayList<>();
                        for (String idStr : idsStr) {
                            ids.add(Integer.parseInt(idStr));
                        }
                        List<CartaMagic> cartas = dao.readMultiple(ids);
                        for (CartaMagic c : cartas) {
                            System.out.println(c);
                        }
                        break;

                    case 5:
                        System.out.print("Digite o ID da carta a ser atualizada: ");
                        int idUpdate = scanner.nextInt();

                        // Limpa o buffer
                        scanner.nextLine();

                        System.out.print("Novo nome da carta: ");
                        String novoNome = scanner.nextLine();

                        System.out.print("Nova data de lançamento (YYYY-MM-DD): ");
                        LocalDate novaData = LocalDate.parse(scanner.nextLine());

                        Map<String, Integer> novasHabilidades = new HashMap<>();
                        while (true) {
                            System.out.print("Adicionar habilidade (nome:pontuação) ou 'fim' para encerrar: ");
                            String input = scanner.nextLine();
                            if (input.equalsIgnoreCase("fim"))
                                break;
                            try {
                                String[] partes = input.split(":");
                                if (partes.length != 2) {
                                    System.out.println("Formato inválido! Use nomeDaHabilidade:pontuação.");
                                    continue;
                                }
                                String nomeHabilidade = partes[0];
                                int pontuacao = Integer.parseInt(partes[1]);
                                novasHabilidades.put(nomeHabilidade, pontuacao);
                            } catch (NumberFormatException e) {
                                System.out.println(
                                        "Erro ao converter a pontuação. Certifique-se de usar um número inteiro.");
                            }
                        }

                        System.out.print("Novo preço da carta: ");
                        double novoPreco = scanner.nextDouble();

                        CartaMagic cartaAtualizada = new CartaMagic(idUpdate, novoNome, novaData, novasHabilidades,
                                novoPreco);
                        if (dao.update(idUpdate, cartaAtualizada)) {
                            System.out.println("Carta atualizada com sucesso!");
                        } else {
                            System.out.println("Erro: Carta não encontrada!");
                        }
                        break;

                    case 6:
                        System.out.print("Digite o ID da carta a ser excluída: ");
                        int idDelete = scanner.nextInt();
                        if (dao.delete(idDelete)) {
                            System.out.println("Carta excluída com sucesso!");
                        } else {
                            System.out.println("Erro: Carta não encontrada!");
                        }
                        break;

                    case 7:
                        System.out.println("\n=== Exibindo Todos os Registros ===");
                        List<CartaMagic> todasCartas = dao.listarTodos();
                        if (todasCartas.isEmpty()) {
                            System.out.println("Nenhuma carta registrada.");
                        } else {
                            for (CartaMagic cartaItem : todasCartas) {
                                System.out.println(cartaItem);
                            }
                        }
                        break;

                    case 8:
                        dao.getArvore().exibirArvore(); // Chama o método de exibição da árvore
                        break;

                    case 9:
                        dao.exibirEstadoHashing(); // Chama o método de exibição da tabela
                        break;
                    case 0:
                        running = false;
                        System.out.println("Saindo do programa...");
                        break;

                    default:
                        System.out.println("Opção inválida! Tente novamente.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

}
