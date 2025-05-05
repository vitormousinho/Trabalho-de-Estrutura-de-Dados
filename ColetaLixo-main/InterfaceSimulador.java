import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe que fornece uma interface de linha de comando para interação com o simulador.
 * Permite controlar a simulação e obter estatísticas em tempo real.
 */
public class InterfaceSimulador {
    private Scanner scanner;
    private Simulador simulador;
    private boolean executando = true;
    private ExecutorService executor;

    public InterfaceSimulador(Simulador simulador) {
        this.simulador = simulador;
        this.scanner = new Scanner(System.in);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Inicia a interface de linha de comando em uma thread separada
     */
    public void iniciar() {
        System.out.println("\n=== Interface de Controle do Simulador ===");
        System.out.println("Digite 'ajuda' para ver os comandos disponíveis.");

        // Executa o loop de comando em uma thread separada
        executor.submit(() -> {
            while (executando) {
                System.out.print("\n> ");
                String comando = scanner.nextLine().trim().toLowerCase();
                processarComando(comando);
            }
        });
    }

    /**
     * Processa os comandos digitados pelo usuário
     */
    private void processarComando(String comando) {
        try {
            String[] partes = comando.split("\\s+");
            String acao = partes[0];

            switch (acao) {
                case "ajuda":
                    exibirAjuda();
                    break;

                case "status":
                    exibirStatus();
                    break;

                case "iniciar":
                    simulador.iniciar();
                    break;

                case "pausar":
                    simulador.pausar();
                    break;

                case "continuar":
                    simulador.continuarSimulacao();
                    break;

                case "encerrar":
                    simulador.encerrar();
                    break;

                case "estatisticas":
                case "stats":
                    exibirEstatisticas();
                    break;

                case "zonas":
                    exibirInfoZonas();
                    break;

                case "estacoes":
                    exibirInfoEstacoes();
                    break;

                case "caminhoes":
                    if (partes.length > 1) {
                        if (partes[1].equals("pequenos")) {
                            exibirInfoCaminhoesPequenos();
                        } else if (partes[1].equals("grandes")) {
                            exibirInfoCaminhoesGrandes();
                        } else {
                            System.out.println("Tipo de caminhão desconhecido. Use 'pequenos' ou 'grandes'.");
                        }
                    } else {
                        exibirInfoTodosCaminhoes();
                    }
                    break;

                case "adicionar":
                    if (partes.length > 1 && partes[1].equals("grande")) {
                        simulador.adicionarCaminhaoGrande();
                        System.out.println("Caminhão grande adicionado.");
                    } else {
                        System.out.println("Comando inválido. Use 'adicionar grande'.");
                    }
                    break;

                case "salvar":
                    if (partes.length > 1) {
                        salvarSimulacao(partes[1]);
                    } else {
                        salvarSimulacao("simulacao.dat");
                    }
                    break;

                case "carregar":
                    if (partes.length > 1) {
                        carregarSimulacao(partes[1]);
                    } else {
                        carregarSimulacao("simulacao.dat");
                    }
                    break;

                case "relatorio":
                    gerarRelatorio();
                    break;

                case "sair":
                    executando = false;
                    System.out.println("Encerrando interface...");
                    executor.shutdown();
                    break;

                default:
                    System.out.println("Comando desconhecido. Digite 'ajuda' para ver os comandos disponíveis.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar comando: " + e.getMessage());
        }
    }

    /**
     * Exibe a lista de comandos disponíveis
     */
    private void exibirAjuda() {
        System.out.println("\n=== Comandos Disponíveis ===");
        System.out.println("ajuda - Exibe esta lista de comandos");
        System.out.println("status - Exibe o status atual da simulação");
        System.out.println("iniciar - Inicia a simulação");
        System.out.println("pausar - Pausa a simulação");
        System.out.println("continuar - Continua a simulação pausada");
        System.out.println("encerrar - Encerra a simulação");
        System.out.println("estatisticas - Exibe estatísticas da simulação");
        System.out.println("zonas - Exibe informações sobre as zonas");
        System.out.println("estacoes - Exibe informações sobre as estações");
        System.out.println("caminhoes [pequenos|grandes] - Exibe informações sobre os caminhões");
        System.out.println("adicionar grande - Adiciona um novo caminhão grande");
        System.out.println("salvar [arquivo] - Salva o estado da simulação");
        System.out.println("carregar [arquivo] - Carrega um estado salvo da simulação");
        System.out.println("relatorio - Gera um relatório completo da simulação");
        System.out.println("sair - Encerra a interface (a simulação continua em background)");
    }

    /**
     * Exibe o status atual da simulação
     */
    private void exibirStatus() {
        int tempoSimulado = simulador.getTempoSimulado();
        int hora = tempoSimulado / 60;
        int minuto = tempoSimulado % 60;

        System.out.println("\n=== Status da Simulação ===");
        System.out.printf("Tempo simulado: %02d:%02d\n", hora, minuto);
        System.out.println("Caminhões grandes: " + simulador.getTotalCaminhoesGrandes() +
                " (em uso: " + simulador.getCaminhoesGrandesEmUso() + ")");
    }

    /**
     * Exibe estatísticas da simulação
     */
    private void exibirEstatisticas() {
        System.out.println("\n=== Estatísticas da Simulação ===");
        System.out.println(simulador.getEstatisticas().gerarRelatorio());
    }

    /**
     * Exibe informações sobre as zonas
     */
    private void exibirInfoZonas() {
        // Esta implementação seria mais completa com acesso aos dados das zonas
        System.out.println("\n=== Informações das Zonas ===");
        System.out.println("Para implementar com acesso direto às zonas do simulador.");
    }

    /**
     * Exibe informações sobre as estações
     */
    private void exibirInfoEstacoes() {
        // Esta implementação seria mais completa com acesso aos dados das estações
        System.out.println("\n=== Informações das Estações ===");
        System.out.println("Para implementar com acesso direto às estações do simulador.");
    }

    /**
     * Exibe informações sobre os caminhões pequenos
     */
    private void exibirInfoCaminhoesPequenos() {
        // Esta implementação seria mais completa com acesso aos dados dos caminhões
        System.out.println("\n=== Informações dos Caminhões Pequenos ===");
        System.out.println("Para implementar com acesso direto aos caminhões do simulador.");
    }

    /**
     * Exibe informações sobre os caminhões grandes
     */
    private void exibirInfoCaminhoesGrandes() {
        // Esta implementação seria mais completa com acesso aos dados dos caminhões
        System.out.println("\n=== Informações dos Caminhões Grandes ===");
        System.out.println("Total: " + simulador.getTotalCaminhoesGrandes());
        System.out.println("Em uso: " + simulador.getCaminhoesGrandesEmUso());
        System.out.println("Tolerância de espera: " + simulador.getToleranciaCaminhoesGrandes() + " minutos");
    }

    /**
     * Exibe informações sobre todos os caminhões
     */
    private void exibirInfoTodosCaminhoes() {
        exibirInfoCaminhoesPequenos();
        exibirInfoCaminhoesGrandes();
    }

    /**
     * Salva o estado atual da simulação
     */
    private void salvarSimulacao(String arquivo) {
        try {
            simulador.gravar(arquivo);
            System.out.println("Simulação salva em: " + arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao salvar simulação: " + e.getMessage());
        }
    }

    /**
     * Carrega um estado salvo da simulação
     */
    private void carregarSimulacao(String arquivo) {
        try {
            this.simulador = Simulador.carregar(arquivo);
            System.out.println("Simulação carregada de: " + arquivo);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro ao carregar simulação: " + e.getMessage());
        }
    }

    /**
     * Gera um relatório completo da simulação
     */
    private void gerarRelatorio() {
        try {
            String arquivo = "relatorio_" + System.currentTimeMillis() + ".txt";
            simulador.getEstatisticas().salvarRelatorio(arquivo);
            System.out.println("Relatório gerado e salvo em: " + arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }
}