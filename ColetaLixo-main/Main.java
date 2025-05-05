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
        System.out.println("=== Simulador de Coleta de Lixo de Teresina (v2.0) ===");
        System.out.println("============================================================");

        Scanner scanner = new Scanner(System.in);

        // --- 1. Criação das Listas ---
        Lista<CaminhaoPequeno> listaCaminhoesPequenos = new Lista<>();
        Lista<CaminhaoGrande> listaCaminhoesGrandes = new Lista<>();
        Lista<ZonaUrbana> listaZonas = new Lista<>();
        Lista<EstacaoTransferencia> listaEstacoes = new Lista<>();

        // --- 2. Criação Interativa dos Caminhões Pequenos ---
        System.out.println("\n--- Configuração da Frota de Caminhões Pequenos ---");
        int qtd2T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_2T/1000) + " Toneladas?");
        int qtd4T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_4T/1000) + " Toneladas?");
        int qtd8T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_8T/1000) + " Toneladas?");
        int qtd10T = perguntarQuantidade(scanner, "Quantos caminhões de " + (CAPACIDADE_10T/1000) + " Toneladas?");

        adicionarCaminhoes(listaCaminhoesPequenos, qtd2T, CAPACIDADE_2T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd4T, CAPACIDADE_4T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd8T, CAPACIDADE_8T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd10T, CAPACIDADE_10T);

        System.out.println(" > Total de " + listaCaminhoesPequenos.tamanho() + " caminhões pequenos configurados.");
        // Opcional: Mostrar a frota criada (pode ser longo)
        // if (!listaCaminhoesPequenos.estaVazia()) {
        //     System.out.println(" > Frota Pequena: " + listaCaminhoesPequenos);
        // }

        // --- 3. Criação dos Caminhões Grandes Iniciais ---
        System.out.println("\n--- Configuração da Frota de Caminhões Grandes ---");
        int qtdCaminhoesGrandes = perguntarQuantidade(scanner, "Quantos caminhões grandes de " + (CaminhaoGrande.CAPACIDADE_MAXIMA_KG / 1000) + " Toneladas inicialmente?");
        // Tolerância será perguntada depois, criamos com a padrão por enquanto
        for (int i = 0; i < qtdCaminhoesGrandes; i++) {
            listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao());
        }
        System.out.println(" > " + listaCaminhoesGrandes.tamanho() + " caminhões grandes criados.");

        // --- 4. Criação e Configuração das Zonas ---
        System.out.println("\n--- Configuração das Zonas ---");
        System.out.println("Defina os intervalos de geração diária de lixo (em kg).");

        // Criar zonas
        ZonaUrbana zonaSul = criarZonaComIntervalo(scanner, ZONA_SUL);
        ZonaUrbana zonaNorte = criarZonaComIntervalo(scanner, ZONA_NORTE);
        ZonaUrbana zonaCentro = criarZonaComIntervalo(scanner, ZONA_CENTRO);
        ZonaUrbana zonaLeste = criarZonaComIntervalo(scanner, ZONA_LESTE);
        ZonaUrbana zonaSudeste = criarZonaComIntervalo(scanner, ZONA_SUDESTE);

        // Adicionar à lista
        listaZonas.adicionar(zonaSul);
        listaZonas.adicionar(zonaNorte);
        listaZonas.adicionar(zonaCentro);
        listaZonas.adicionar(zonaLeste);
        listaZonas.adicionar(zonaSudeste);

        System.out.println("\n > " + listaZonas.tamanho() + " zonas configuradas:");
        for (int i = 0; i < listaZonas.tamanho(); i++) {
            ZonaUrbana z = listaZonas.obter(i);
            System.out.printf("   - %-10s: %d a %d kg/dia%n", z.getNome(), z.getGeracaoMinima(), z.getGeracaoMaxima());
        }

        // --- 5. Criação das Estações de Transferência ---
        System.out.println("\n--- Configuração das Estações ---");
        System.out.println("Defina o tempo máximo (em minutos) que caminhões pequenos podem esperar na fila antes de acionar um novo caminhão grande.");

        int tempoEsperaEstacaoA = perguntarTempo(scanner, "Tempo limite para " + ESTACAO_A + " (minutos):");
        int tempoEsperaEstacaoB = perguntarTempo(scanner, "Tempo limite para " + ESTACAO_B + " (minutos):");

        try {
            listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_A, tempoEsperaEstacaoA));
            listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_B, tempoEsperaEstacaoB));

            System.out.println(" > " + listaEstacoes.tamanho() + " estações criadas:");
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                EstacaoTransferencia est = listaEstacoes.obter(i);
                System.out.printf("   - %-10s: Limite Espera Pequenos = %d minutos%n", est.getNome(), est.getTempoMaximoEspera());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("\n!!! ERRO ao criar estações: " + e.getMessage() + " !!! Encerrando.");
            scanner.close();
            return;
        }

        // --- 6. Configurar tempo de tolerância dos caminhões grandes ---
        System.out.println("\n--- Configuração de Tolerância dos Caminhões Grandes ---");
        System.out.println("Defina o tempo (em minutos) que um caminhão grande aguarda na estação antes de partir (se tiver carga).");
        int toleranciaCaminhoesGrandes = perguntarTempo(scanner, "Tempo de tolerância de espera dos caminhões grandes (minutos):");

        // --- 7. Preparar o Simulador ---
        System.out.println("\n--- Configurando o Simulador ---");
        Simulador simulador = new Simulador();

        try {
            // Passa as listas e parâmetros para o simulador
            simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
            simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
            simulador.setListaZonas(listaZonas);
            simulador.setListaEstacoes(listaEstacoes);
            simulador.setToleranciaCaminhoesGrandes(toleranciaCaminhoesGrandes); // Define a tolerância para os caminhões

            System.out.println(" > Simulador configurado com sucesso!");
            System.out.println("\n============================================================");
            System.out.println("=== Configuração Concluída ===");
            System.out.println("============================================================");


            // --- 8. Escolha da Interface e Início ---
            int opcao = 0;
            while (opcao < 1 || opcao > 3) {
                System.out.println("\nEscolha como iniciar a simulação:");
                System.out.println("  1 - Iniciar simulação diretamente (sem interface interativa)");
                System.out.println("  2 - Usar interface de linha de comando (CLI)");
                System.out.println("  3 - Usar interface gráfica Swing (GUI - recomendado)");
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

            switch (opcao) {
                case 1:
                    System.out.println("\n=== Iniciando a Simulação Direta... ===");
                    simulador.iniciar();
                    System.out.println("(A simulação está rodando em background. O programa principal pode terminar.)");
                    // Não fechar o scanner aqui pode manter o programa vivo se a simulação usar threads não-daemon
                    // Se a simulação usa thread daemon (como no código atual), o scanner pode ser fechado.
                    // scanner.close(); // Descomente se apropriado
                    break;
                case 2:
                    System.out.println("\n=== Iniciando Interface de Linha de Comando (CLI)... ===");
                    InterfaceSimulador interfaceCLI = new InterfaceSimulador(simulador);
                    interfaceCLI.iniciar(); // A interface gerencia o loop de comandos
                    // Não fechar o scanner aqui, a interface precisa dele
                    break;
                case 3:
                    System.out.println("\n=== Iniciando Interface Gráfica (GUI)... ===");
                    scanner.close(); // Fecha o scanner, não é mais necessário
                    // Inicia a interface Swing na Event Dispatch Thread (EDT)
                    SwingUtilities.invokeLater(() -> {
                        InterfaceSimuladorSwing gui = new InterfaceSimuladorSwing(simulador);
                        gui.iniciar(); // Torna a janela visível
                    });
                    System.out.println("(Interface gráfica iniciada em uma janela separada.)");
                    break;
            }

        } catch (IllegalArgumentException e) {
            System.err.println("\n!!! ERRO FATAL ao configurar simulador: " + e.getMessage() + " !!!");
            e.printStackTrace();
            scanner.close(); // Fecha o scanner em caso de erro fatal
        } catch (Exception e) { // Captura outras exceções inesperadas
            System.err.println("\n!!! ERRO INESPERADO durante a configuração: " + e.getMessage() + " !!!");
            e.printStackTrace();
            scanner.close();
        }
    }

    // --- Métodos Auxiliares para Entrada do Usuário ---

    /** Pergunta ao usuário por uma quantidade (inteiro >= 0). */
    private static int perguntarQuantidade(Scanner scanner, String mensagem) {
        int quantidade = -1;
        while (quantidade < 0) {
            System.out.print(mensagem + " ");
            try {
                quantidade = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha
                if (quantidade < 0) {
                    System.out.println("Erro: Por favor, insira um número igual ou maior que zero.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, insira um número inteiro.");
                scanner.nextLine(); // Consome a entrada inválida
                quantidade = -1; // Força continuar no loop
            }
        }
        return quantidade;
    }

    /** Pergunta ao usuário por um tempo/intervalo (inteiro >= 1). */
    private static int perguntarTempo(Scanner scanner, String mensagem) {
        int tempo = -1;
        while (tempo < 1) { // Exige pelo menos 1
            System.out.print(mensagem + " ");
            try {
                tempo = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha
                if (tempo < 1) {
                    System.out.println("Erro: O valor deve ser pelo menos 1.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, insira um número inteiro.");
                scanner.nextLine(); // Consome a entrada inválida
                tempo = -1; // Força continuar no loop
            }
        }
        return tempo;
    }

    /** Cria uma ZonaUrbana pedindo o intervalo de geração ao usuário. */
    private static ZonaUrbana criarZonaComIntervalo(Scanner scanner, String nomeZona) {
        System.out.println(" -> Configurando Zona " + nomeZona + ":");
        int min = -1;
        int max = -1;
        while (min < 0 || max < 0 || min > max) {
            min = perguntarQuantidade(scanner, "   Geração Mínima (kg/dia):");
            max = perguntarQuantidade(scanner, "   Geração Máxima (kg/dia):");
            if (min > max) {
                System.out.println("Erro: O valor mínimo não pode ser maior que o máximo. Tente novamente.");
                min = -1; // Força repetir
            }
        }
        ZonaUrbana zona = new ZonaUrbana(nomeZona);
        try {
            zona.setIntervaloGeracao(min, max);
        } catch (IllegalArgumentException e) {
            // Embora perguntarQuantidade já valide >= 0, a validação em setIntervaloGeracao é uma segurança extra.
            System.err.println("Erro inesperado ao definir intervalo para " + nomeZona + ": " + e.getMessage());
            // Cria com valores padrão se a definição falhar (raro)
            zona = new ZonaUrbana(nomeZona);
        }
        return zona;
    }

    /** Adiciona uma quantidade específica de caminhões pequenos com uma dada capacidade à lista. */
    private static void adicionarCaminhoes(Lista<CaminhaoPequeno> lista, int quantidade, int capacidade) {
        if (quantidade < 0) return; // Segurança extra
        for (int i = 0; i < quantidade; i++) {
            try {
                lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
            } catch (IllegalArgumentException e) {
                // Deve acontecer apenas se capacidade <= 0, o que não deve ocorrer com as constantes
                System.err.println("Erro ao criar caminhão pequeno com capacidade " + capacidade + ": " + e.getMessage());
            }
        }
    }
}