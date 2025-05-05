// Importa todas as classes que vamos usar na configuração
import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoGrandePadrao;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;

// --- Imports para a configuração interativa ---
import java.util.Scanner;
import java.util.InputMismatchException;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== Configurando o Simulador de Coleta de Lixo de Teresina ===");

        // --- Usa o Scanner para interação ---
        Scanner scanner = new Scanner(System.in);

        // --- 1. Criação das Listas para guardar os elementos ---
        Lista<CaminhaoPequeno> listaCaminhoesPequenos = new Lista<>();
        Lista<CaminhaoGrande> listaCaminhoesGrandes = new Lista<>();
        Lista<ZonaUrbana> listaZonas = new Lista<>();
        Lista<EstacaoTransferencia> listaEstacoes = new Lista<>();

        // --- 2. Criação Interativa dos Caminhões Pequenos ---
        System.out.println("\n--- Configuração da Frota de Caminhões Pequenos ---");
        int qtd2T = perguntarQuantidade(scanner, "Quantos caminhões de 2 Toneladas?");
        int qtd4T = perguntarQuantidade(scanner, "Quantos caminhões de 4 Toneladas?");
        int qtd8T = perguntarQuantidade(scanner, "Quantos caminhões de 8 Toneladas?");
        int qtd10T = perguntarQuantidade(scanner, "Quantos caminhões de 10 Toneladas?");

        // Adiciona os caminhões à lista com base nas respostas
        adicionarCaminhoes(listaCaminhoesPequenos, qtd2T, 2000);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd4T, 4000);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd8T, 8000);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd10T, 10000);

        System.out.println(" > Total de " + listaCaminhoesPequenos.tamanho() + " caminhões pequenos configurados.");
        if (!listaCaminhoesPequenos.estaVazia()) {
            System.out.println(" > Frota: " + listaCaminhoesPequenos); // Mostra a frota criada
        }

        // --- 3. Criação dos Caminhões Grandes Iniciais ---
        System.out.println("\n--- Configuração da Frota de Caminhões Grandes ---");
        int qtdCaminhoesGrandes = perguntarQuantidade(scanner, "Quantos caminhões grandes de 20 Toneladas inicialmente?");
        for (int i = 0; i < qtdCaminhoesGrandes; i++) {
            listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao()); // 20 Ton
        }
        System.out.println(" > " + listaCaminhoesGrandes.tamanho() + " caminhões grandes criados.");

        // --- 4. Criação e Configuração das Zonas ---
        System.out.println("\n--- Configuração das Zonas ---");

        // Criar as 5 zonas com referências para poder configurá-las
        ZonaUrbana zonaSul = new ZonaUrbana("Sul");
        ZonaUrbana zonaNorte = new ZonaUrbana("Norte");
        ZonaUrbana zonaCentro = new ZonaUrbana("Centro");
        ZonaUrbana zonaLeste = new ZonaUrbana("Leste");
        ZonaUrbana zonaSudeste = new ZonaUrbana("Sudeste");

        // Configurar os intervalos de geração para cada zona (em kg)
        try {
            zonaSul.setIntervaloGeracao(250, 650);      // Zona Sul: área residencial média
            zonaNorte.setIntervaloGeracao(350, 750);    // Zona Norte: área residencial densa
            zonaCentro.setIntervaloGeracao(450, 950);   // Zona Centro: área comercial (mais lixo)
            zonaLeste.setIntervaloGeracao(300, 700);    // Zona Leste: mista residencial/comercial
            zonaSudeste.setIntervaloGeracao(200, 500);  // Zona Sudeste: área residencial menos densa
        } catch (Exception e) {
            System.out.println("\n!!! ERRO: " + e.getMessage() + " !!!");
            System.out.println("!!!      Verifique a implementação da classe ZonaUrbana      !!!");
        }

        // Adicionar as zonas à lista
        listaZonas.adicionar(zonaSul);
        listaZonas.adicionar(zonaNorte);
        listaZonas.adicionar(zonaCentro);
        listaZonas.adicionar(zonaLeste);
        listaZonas.adicionar(zonaSudeste);

        System.out.println(" > " + listaZonas.tamanho() + " zonas criadas.");
        // Imprime a configuração de geração
        try {
            System.out.println(" > Intervalos de geração configurados (kg por dia - valor teórico):");
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                System.out.println("   - " + zona.getNome() + ": " +
                        zona.getGeracaoMinima() + " a " +
                        zona.getGeracaoMaxima() + " kg");
            }
        } catch (Exception e) {
            System.out.println("   (Não foi possível exibir os intervalos - erro: " + e.getMessage() + ")");
        }

        // --- 5. Criação das Estações de Transferência com configuração de tempos de espera ---
        System.out.println("\n--- Configuração das Estações ---");

        // Perguntar ao usuário os tempos máximos de espera para cada estação
        int tempoEsperaEstacaoA = perguntarTempo(scanner, "Tempo limite antes de adicionar um novo caminhão grande na Estação A (minutos):");
        int tempoEsperaEstacaoB = perguntarTempo(scanner, "Tempo limite antes de adicionar um novo caminhão grande na Estação B (minutos):");

        // Criar estações com os tempos configurados
        try {
            listaEstacoes.adicionar(new EstacaoPadrao("Estação A", tempoEsperaEstacaoA));
            listaEstacoes.adicionar(new EstacaoPadrao("Estação B", tempoEsperaEstacaoB));

            System.out.println(" > " + listaEstacoes.tamanho() + " estações criadas.");
            System.out.println(" > Tempos de espera configurados:");
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                EstacaoTransferencia estacao = listaEstacoes.obter(i);
                System.out.println("   - " + estacao.getNome() + ": " + estacao.getTempoMaximoEspera() + " minutos");
            }
        } catch (Exception e) {
            System.out.println("\n!!! ERRO ao criar estações: " + e.getMessage() + " !!!");
        }

        // --- 6. Configurar tempo de espera dos caminhões grandes ---
        System.out.println("\n--- Configuração de Tolerância dos Caminhões Grandes ---");
        int toleranciaCaminhoesGrandes = perguntarTempo(scanner, "Tempo de tolerância de espera (em minutos) para caminhões grandes:");

        // --- 7. Preparar o Simulador ---
        System.out.println("\n--- Configurando o Simulador ---");
        Simulador simulador = new Simulador();

        // Variável para controlar a opção escolhida - declarada FORA dos blocos try/catch
        int opcao = 0;

        // Passa as listas criadas para o simulador usar
        try {
            simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
            simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
            simulador.setListaZonas(listaZonas);
            simulador.setListaEstacoes(listaEstacoes);

            // Configurar o tempo de tolerância dos caminhões grandes
            simulador.setToleranciaCaminhoesGrandes(toleranciaCaminhoesGrandes);

            System.out.println("\n=== Configuração Concluída ===");

            // --- 8. Perguntar se deseja iniciar imediatamente ou usar a interface ---
            System.out.println("\nEscolha o tipo de interface:");
            System.out.println("1 - Iniciar simulação diretamente (sem interface)");
            System.out.println("2 - Usar interface de linha de comando");
            System.out.println("3 - Usar interface gráfica Swing (recomendado)");

            // Inicializa a variável opcao fora do loop
            while (opcao < 1 || opcao > 3) {
                System.out.print("Opção (1, 2 ou 3): ");
                try {
                    opcao = scanner.nextInt();
                    scanner.nextLine(); // Consome a quebra de linha

                    if (opcao < 1 || opcao > 3) {
                        System.out.println("Opção inválida. Digite 1, 2 ou 3.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Entrada inválida. Digite 1, 2 ou 3.");
                    scanner.nextLine(); // Consome a entrada inválida
                }
            }

            if (opcao == 1) {
                // Iniciar imediatamente sem interface
                System.out.println("\n=== Iniciando a Simulação... ===");
                simulador.iniciar();
                System.out.println("(Thread principal 'main' terminando. Simulação continua em background.)");
                scanner.close(); // Fechamos o scanner apenas aqui
            }
            else if (opcao == 2) {
                // Iniciar a interface de linha de comando
                System.out.println("\n=== Iniciando Interface de Linha de Comando... ===");
                InterfaceSimulador interfaceSimulador = new InterfaceSimulador(simulador);
                interfaceSimulador.iniciar();
                System.out.println("Interface de controle iniciada. Use o comando 'ajuda' para ver as opções.");
                // Não fechamos o scanner aqui para a interface de linha de comando usar
            }
            else { // opcao == 3
                // Iniciar a interface gráfica Swing
                System.out.println("\n=== Iniciando Interface Gráfica... ===");
                scanner.close(); // Podemos fechar o scanner aqui, pois não será mais usado

                // Iniciar a interface Swing em uma thread separada (thread de eventos)
                SwingUtilities.invokeLater(() -> {
                    InterfaceSimuladorSwing gui = new InterfaceSimuladorSwing(simulador);
                    gui.iniciar();
                });

                System.out.println("Interface gráfica iniciada.");
            }

        } catch (Exception e) {
            System.out.println("\n!!! ERRO ao configurar simulador: " + e.getMessage() + " !!!");
            e.printStackTrace();
            scanner.close(); // Fechamos o scanner em caso de erro
        }
    }

    // --- Método Auxiliar para perguntar quantidade ao usuário ---
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
                scanner.next(); // Consome a entrada inválida
                quantidade = -1;
            }
        }
        return quantidade;
    }

    // --- Método Auxiliar para perguntar tempo de espera ---
    private static int perguntarTempo(Scanner scanner, String mensagem) {
        int tempo = -1;
        while (tempo < 1) { // Exige pelo menos 1 minuto
            System.out.print(mensagem + " ");
            try {
                tempo = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha
                if (tempo < 1) {
                    System.out.println("Erro: O tempo deve ser pelo menos 1 minuto.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, insira um número inteiro.");
                scanner.next(); // Consome a entrada inválida
                tempo = -1;
            }
        }
        return tempo;
    }

    // --- Método Auxiliar para adicionar caminhões à lista ---
    private static void adicionarCaminhoes(Lista<CaminhaoPequeno> lista, int quantidade, int capacidade) {
        for (int i = 0; i < quantidade; i++) {
            lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
        }
    }
}