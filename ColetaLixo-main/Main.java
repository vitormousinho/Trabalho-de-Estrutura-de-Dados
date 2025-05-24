import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoGrandePadrao;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;

import java.util.Scanner;
import java.util.InputMismatchException;
import javax.swing.SwingUtilities;

public class Main {

    // --- Constantes ---
    // Capacidades Caminhões Pequenos (kg)
    private static final int CAPACIDADE_2T = 2000;
    private static final int CAPACIDADE_4T = 4000;
    private static final int CAPACIDADE_8T = 8000;
    private static final int CAPACIDADE_10T = 10000;

    // Nomes das Zonas
    private static final String ZONA_SUL = "Sul";
    private static final String ZONA_NORTE = "Norte";
    private static final String ZONA_CENTRO = "Centro";
    private static final String ZONA_LESTE = "Leste";
    private static final String ZONA_SUDESTE = "Sudeste";

    // Nomes das Estações
    private static final String ESTACAO_A = "Estação A";
    private static final String ESTACAO_B = "Estação B";


    public static void main(String[] args) {

        System.out.println("============================================================");
        System.out.println("=== Simulador de Coleta de Lixo de Teresina (v2.1 GUI) ===");
        System.out.println("============================================================");

        Scanner scanner = new Scanner(System.in);
        Simulador simulador = null; // Será inicializado conforme a opção

        // --- Escolha da Interface e Início ---
        int opcao = 0;
        while (opcao < 1 || opcao > 3) {
            System.out.println("\nEscolha como iniciar a simulação:");
            System.out.println("  1 - Iniciar simulação direta no console (configuração CLI)");
            System.out.println("  2 - Usar interface de linha de comando (CLI) para controle (configuração CLI)");
            System.out.println("  3 - Usar interface gráfica Swing (GUI) (configuração na GUI)");
            System.out.print("Opção (1, 2 ou 3): ");
            try {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha

                if (opcao < 1 || opcao > 3) {
                    System.out.println("Opção inválida. Digite 1, 2 ou 3.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Digite um número (1, 2 ou 3).");
                scanner.nextLine(); // Consome a entrada inválida
                opcao = 0; // Reseta para continuar no loop
            }
        }

        if (opcao == 1 || opcao == 2) {
            // Configuração via CLI para as opções 1 e 2
            System.out.println("\n--- Configuração da Simulação via Linha de Comando ---");
            simulador = configurarSimuladorViaCLI(scanner);
            if (simulador == null) {
                System.err.println("Falha na configuração do simulador via CLI. Encerrando.");
                scanner.close();
                return;
            }
        }


        try {
            switch (opcao) {
                case 1:
                    System.out.println("\n=== Iniciando a Simulação Direta (Console)... ===");
                    simulador.iniciar();
                    System.out.println("(A simulação está rodando em background. O programa principal pode terminar se o timer for daemon.)");
                    // Não fechar o scanner aqui se a simulação for longa e não daemon
                    break;
                case 2:
                    System.out.println("\n=== Iniciando Interface de Linha de Comando (CLI)... ===");
                    InterfaceSimulador interfaceCLI = new InterfaceSimulador(simulador);
                    interfaceCLI.iniciar(); // A interface gerencia o loop de comandos
                    // Não fechar o scanner aqui, a interface CLI precisa dele
                    break;
                case 3:
                    System.out.println("\n=== Iniciando Interface Gráfica (GUI)... ===");
                    // A configuração será feita DENTRO da GUI.
                    // Passamos uma instância "crua" do simulador.
                    Simulador simuladorParaGUI = new Simulador();
                    SwingUtilities.invokeLater(() -> {
                        InterfaceSimuladorSwing gui = new InterfaceSimuladorSwing(simuladorParaGUI);
                        gui.iniciar();
                    });
                    System.out.println("(Interface gráfica iniciada em uma janela separada.)");
                    // O scanner pode ou não ser fechado aqui dependendo se outras partes do programa o usam.
                    // Se a GUI é a única opção restante, pode fechar: scanner.close();
                    break;
            }

        } catch (IllegalArgumentException e) {
            System.err.println("\n!!! ERRO FATAL ao configurar/iniciar simulador: " + e.getMessage() + " !!!");
            e.printStackTrace();
            // scanner.close(); // Fecha o scanner em caso de erro fatal se ele ainda estiver aberto
        } catch (Exception e) { // Captura outras exceções inesperadas
            System.err.println("\n!!! ERRO INESPERADO: " + e.getMessage() + " !!!");
            e.printStackTrace();
            // scanner.close();
        }

        if (opcao == 3) {

        }
    }

    /**
     * Configura o simulador usando a linha de comando.
     * Este método é chamado para as opções 1 e 2.
     */
    private static Simulador configurarSimuladorViaCLI(Scanner scanner) {
        Lista<CaminhaoPequeno> listaCaminhoesPequenos = new Lista<>();
        Lista<CaminhaoGrande> listaCaminhoesGrandes = new Lista<>();
        Lista<ZonaUrbana> listaZonas = new Lista<>();
        Lista<EstacaoTransferencia> listaEstacoes = new Lista<>();

        System.out.println("\n--- Configuração da Frota de Caminhões Pequenos ---");
        int qtd2T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_2T / 1000) + " Toneladas?");
        int qtd4T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_4T / 1000) + " Toneladas?");
        int qtd8T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_8T / 1000) + " Toneladas?");
        int qtd10T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_10T / 1000) + " Toneladas?");

        adicionarCaminhoes(listaCaminhoesPequenos, qtd2T, CAPACIDADE_2T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd4T, CAPACIDADE_4T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd8T, CAPACIDADE_8T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd10T, CAPACIDADE_10T);
        System.out.println(" > Total de " + listaCaminhoesPequenos.tamanho() + " caminhões pequenos configurados.");

        System.out.println("\n--- Configuração da Frota de Caminhões Grandes ---");
        int qtdCaminhoesGrandes = perguntarQuantidade(scanner, "Quantos caminhões grandes de " + (CaminhaoGrande.CAPACIDADE_MAXIMA_KG / 1000) + " Toneladas inicialmente?");
        System.out.println("\n--- Configuração de Tolerância dos Caminhões Grandes ---");
        System.out.println("Defina o tempo (em minutos) que um caminhão grande aguarda na estação antes de partir (se tiver carga).");
        int toleranciaCaminhoesGrandes = perguntarTempo(scanner, "Tempo de tolerância de espera dos caminhões grandes (minutos):");

        for (int i = 0; i < qtdCaminhoesGrandes; i++) {
            listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao(toleranciaCaminhoesGrandes));
        }
        System.out.println(" > " + listaCaminhoesGrandes.tamanho() + " caminhões grandes criados com tolerância de " + toleranciaCaminhoesGrandes + " min.");


        System.out.println("\n--- Configuração das Zonas ---");
        System.out.println("Defina os intervalos de geração diária de lixo (em kg).");
        listaZonas.adicionar(criarZonaComIntervalo(scanner, ZONA_SUL));
        listaZonas.adicionar(criarZonaComIntervalo(scanner, ZONA_NORTE));
        listaZonas.adicionar(criarZonaComIntervalo(scanner, ZONA_CENTRO));
        listaZonas.adicionar(criarZonaComIntervalo(scanner, ZONA_LESTE));
        listaZonas.adicionar(criarZonaComIntervalo(scanner, ZONA_SUDESTE));
        System.out.println("\n > " + listaZonas.tamanho() + " zonas configuradas.");

        System.out.println("\n--- Configuração das Estações ---");
        System.out.println("Defina o tempo máximo (em minutos) que caminhões pequenos podem esperar na fila antes de acionar um novo caminhão grande.");
        int tempoEsperaEstacaoA = perguntarTempo(scanner, "Tempo limite para " + ESTACAO_A + " (minutos):");
        int tempoEsperaEstacaoB = perguntarTempo(scanner, "Tempo limite para " + ESTACAO_B + " (minutos):");

        try {
            listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_A, tempoEsperaEstacaoA));
            listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_B, tempoEsperaEstacaoB));
            System.out.println(" > " + listaEstacoes.tamanho() + " estações criadas.");
        } catch (IllegalArgumentException e) {
            System.err.println("\n!!! ERRO ao criar estações: " + e.getMessage() + " !!!");
            return null;
        }

        Simulador sim = new Simulador();
        sim.setListaCaminhoesPequenos(listaCaminhoesPequenos);
        sim.setListaCaminhoesGrandes(listaCaminhoesGrandes);
        sim.setListaZonas(listaZonas);
        sim.setListaEstacoes(listaEstacoes);
        sim.setToleranciaCaminhoesGrandes(toleranciaCaminhoesGrandes); // Já definido ao criar os CGs, mas setar aqui garante a tolerância padrão para novos CGs.
        System.out.println(" > Simulador configurado com sucesso via CLI!");
        return sim;
    }


    // --- Métodos Auxiliares para Entrada do Usuário (CLI) ---

    private static int perguntarQuantidade(Scanner scanner, String mensagem) {
        int quantidade = -1;
        while (quantidade < 0) {
            System.out.print(mensagem + " ");
            try {
                quantidade = scanner.nextInt();
                scanner.nextLine();
                if (quantidade < 0) {
                    System.out.println("Erro: Por favor, insira um número igual ou maior que zero.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, insira um número inteiro.");
                scanner.nextLine();
                quantidade = -1;
            }
        }
        return quantidade;
    }

    private static int perguntarTempo(Scanner scanner, String mensagem) {
        int tempo = -1;
        while (tempo < 1) {
            System.out.print(mensagem + " ");
            try {
                tempo = scanner.nextInt();
                scanner.nextLine();
                if (tempo < 1) {
                    System.out.println("Erro: O valor deve ser pelo menos 1.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, insira um número inteiro.");
                scanner.nextLine();
                tempo = -1;
            }
        }
        return tempo;
    }

    private static ZonaUrbana criarZonaComIntervalo(Scanner scanner, String nomeZona) {
        System.out.println(" -> Configurando Zona " + nomeZona + ":");
        int min = -1, max = -1; // Initialize to invalid values
        while (true) {
            min = perguntarQuantidade(scanner, "   Geração Mínima (kg/dia) para " + nomeZona + ":");
            max = perguntarQuantidade(scanner, "   Geração Máxima (kg/dia) para " + nomeZona + ":");
            if (min <= max) {
                if (min == 0 && max == 0) { // Permitir 0 se ambos forem 0 (zona não gera lixo)
                    System.out.println("   Aviso: Zona " + nomeZona + " configurada para não gerar lixo (0-0 kg/dia).");
                    break;
                } else if (min == 0 && max > 0) { // Se min é 0 mas max é > 0, min deve ser pelo menos 1 para random.nextInt
                    System.out.println("Erro: Geração mínima deve ser maior que 0 se a máxima for maior que 0. Ou defina ambos como 0.");
                }
                else {
                    break; // Valores válidos
                }
            } else {
                System.out.println("Erro: O valor mínimo não pode ser maior que o máximo. Tente novamente.");
            }
        }

        ZonaUrbana zona = new ZonaUrbana(nomeZona);
        try {
            if (min == 0 && max == 0) {
                zona.setIntervaloGeracao(1, 1); // Para evitar erro no random, mas lixo gerado será 0
            }
            zona.setIntervaloGeracao(min, max);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao definir intervalo para " + nomeZona + " ("+min+"-"+max+"): " + e.getMessage() + ". Usando padrões da zona.");
            // ZonaUrbana já tem padrões se setIntervaloGeracao não for chamado ou falhar.
        }
        return zona;
    }

    private static void adicionarCaminhoes(Lista<CaminhaoPequeno> lista, int quantidade, int capacidade) {
        if (quantidade < 0) return;
        for (int i = 0; i < quantidade; i++) {
            try {
                lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
            } catch (IllegalArgumentException e) {
                System.err.println("Erro ao criar caminhão pequeno com capacidade " + capacidade + ": " + e.getMessage());
            }
        }
    }
}