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
                System.out.println("10 - Comprimir Base de Dados");
                System.out.println("11 - Descomprimir Base de Dados");
                System.out.println("12 - Casamento de Padrões");
                System.out.println("13 - Criptografar habilidades com AES");
                System.out.println("14 - Criptografar habilidades com XOR");
                System.out.println("15 - Descriptografar habilidades com AES");
                System.out.println("16 - Descriptografar habilidades com XOR");
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
                                .print("Digite sua chave da API Kaggle (utilizar 4317f78fce2e746aae963a48a51e4907): ");
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
                            System.out.println("Carta encontrada: " + dao.descriptografarHabilidades(carta));
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
                            System.out.println(dao.descriptografarHabilidades(c));
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
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapAES(habilidadesAtual));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapXOR(habilidadesAtual, 'K'));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        List<CartaMagic> todasCartas = dao.listarTodos();
                        if (todasCartas.isEmpty()) {
                            System.out.println("Nenhuma carta registrada.");
                        } else {
                            for (CartaMagic cartaItem : todasCartas) {
                                System.out.println(dao.descriptografarHabilidades(cartaItem));
                            }
                        }
                        break;

                    case 8:
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapAES(habilidadesAtual));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapXOR(habilidadesAtual, 'K'));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        dao.getArvore().exibirArvore(); // Chama o método de exibição da árvore
                        break;

                    case 9:
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapAES(habilidadesAtual));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapXOR(habilidadesAtual, 'K'));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        dao.exibirEstadoHashing(); // Chama o método de exibição da tabela
                        break;

                    case 10:
                        System.out.print("Digite o caminho do arquivo a ser comprimido: ");
                        String caminhoOriginal = scanner.nextLine();

                        System.out.print("Digite a versão (X) da compressão: ");
                        String versao = scanner.nextLine();

                        // Chamada de método de compressão
                        try {
                            CompressaoUtils.realizarCompressao(caminhoOriginal, versao);
                        } catch (IOException e) {
                            System.out.println("Erro ao realizar a compressão: " + e.getMessage());
                        }
                        break;

                    case 11:
                        System.out.print("Digite a versão (X) que deseja descomprimir: ");
                        String versaoDescomp = scanner.nextLine();

                        System.out.print("Digite o nome do arquivo base: ");
                        String base = scanner.nextLine();

                        try {
                            CompressaoUtils.realizarDescompressao(base, versaoDescomp);
                        } catch (IOException e) {
                            System.out.println("Erro ao realizar a descompressão: " + e.getMessage());
                        }
                        break;
                    case 12:
                        System.out.print("Digite o padrão a ser buscado no nome das cartas: ");
                        String padrao = scanner.nextLine();

                        try {
                            List<CartaMagic> cartasEncontradas = dao.buscarPorPadraoNoNome(padrao);

                            if (cartasEncontradas.isEmpty()) {
                                System.out.println("Nenhuma carta encontrada com o padrão \"" + padrao + "\" no nome.");
                            } else {
                                System.out.println("Cartas encontradas:");
                                for (CartaMagic novacarta : cartasEncontradas) {
                                    System.out.println(novacarta);
                                    System.out.println("------------------------------");
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Erro ao buscar cartas por padrão: " + e.getMessage());
                        }
                        break;
                    case 13:
                        System.out.println("Criptografando todas as habilidades com AES...");
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.encryptMapAES(habilidadesAtual));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        System.out.println(
                                "Todas as habilidades foram criptografadas com AES. Aqui está a lista de cartas:");
                        // Exibe todas as cartas após criptografia
                        for (CartaMagic cartaCriptografada : dao.listarTodos()) {
                            System.out.println(cartaCriptografada);
                        }
                        break;

                    case 14:
                        System.out.println("Criptografando todas as habilidades com XOR...");
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.encryptMapXORInt(habilidadesAtual, 'K'));
                            dao.update(cartaAtual.getId(), cartaAtual); // cartaAtual já modificada

                        }
                        System.out.println(
                                "Todas as habilidades foram criptografadas com XOR. Aqui está a lista de cartas:");
                        // Exibe todas as cartas após criptografia
                        for (CartaMagic cartaCriptografada : dao.listarTodos()) {
                            System.out.println(cartaCriptografada);
                        }
                        break;

                    case 15:
                        System.out.println("Descriptografando todas as habilidades com AES...");
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapAES(habilidadesAtual));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        System.out.println(
                                "Todas as habilidades foram descriptografadas com AES.");
                        break;

                    case 16:
                        System.out.println("Descriptografando todas as habilidades com XOR...");
                        for (CartaMagic cartaAtual : dao.listarTodos()) {
                            Map<String, Integer> habilidadesAtual = cartaAtual.getHabilidades();
                            cartaAtual.setHabilidades(CryptoUtils.decryptMapXOR(habilidadesAtual, 'K'));
                            dao.update(cartaAtual.getId(), cartaAtual);
                        }
                        System.out.println(
                                "Todas as habilidades foram descriptografadas com XOR.");
                        break;
                    case 0:
                        running = false;
                        System.out.println("Saindo do programa...");
                        break;

                    default:
                        System.out.println("Opção inválida! Tente novamente. ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

}
