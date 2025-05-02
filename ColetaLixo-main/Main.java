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
        // (Substituímos a criação fixa pela interativa)
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
        // (Mantido como no código do seu amigo)
        System.out.println("\n--- Configuração da Frota de Caminhões Grandes ---");
        System.out.println("(Iniciando com 2 caminhões grandes de 20T)"); // Exemplo
        listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao()); // 20 Ton
        listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao()); // 20 Ton
        System.out.println(" > " + listaCaminhoesGrandes.tamanho() + " caminhões grandes criados.");

        // --- 4. Criação e Configuração das Zonas ---
        // (Mantido como no código do seu amigo, assumindo que ZonaUrbana foi atualizada)
        System.out.println("\n--- Configuração das Zonas ---");

        // Criar as 5 zonas com referências para poder configurá-las
        ZonaUrbana zonaSul = new ZonaUrbana("Sul");
        ZonaUrbana zonaNorte = new ZonaUrbana("Norte");
        ZonaUrbana zonaCentro = new ZonaUrbana("Centro");
        ZonaUrbana zonaLeste = new ZonaUrbana("Leste");
        ZonaUrbana zonaSudeste = new ZonaUrbana("Sudeste");

        // Configurar os intervalos de geração para cada zona (em kg) - REQUER MUDANÇAS em ZonaUrbana.java
        // Verifique se os métodos setIntervaloGeracao, getGeracaoMinima, getGeracaoMaxima existem em ZonaUrbana!
        try {
            zonaSul.setIntervaloGeracao(250, 650);      // Zona Sul: área residencial média
            zonaNorte.setIntervaloGeracao(350, 750);    // Zona Norte: área residencial densa
            zonaCentro.setIntervaloGeracao(450, 950);   // Zona Centro: área comercial (mais lixo)
            zonaLeste.setIntervaloGeracao(300, 700);    // Zona Leste: mista residencial/comercial
            zonaSudeste.setIntervaloGeracao(200, 500);  // Zona Sudeste: área residencial menos densa
        } catch (NoSuchMethodError e) {
            System.out.println("\n!!! ERRO: Parece que a classe ZonaUrbana não foi atualizada !!!");
            System.out.println("!!!      Adicione os métodos setIntervaloGeracao, getGeracaoMinima, getGeracaoMaxima em ZonaUrbana.java      !!!");
            // O programa pode continuar, mas a geração de lixo não usará os intervalos corretos.
        }


        // Adicionar as zonas à lista
        listaZonas.adicionar(zonaSul);
        listaZonas.adicionar(zonaNorte);
        listaZonas.adicionar(zonaCentro);
        listaZonas.adicionar(zonaLeste);
        listaZonas.adicionar(zonaSudeste);

        System.out.println(" > " + listaZonas.tamanho() + " zonas criadas.");
        // Imprime a configuração de geração (se os getters existirem)
        System.out.println(" > Intervalos de geração configurados (kg por dia - valor teórico):");
        try {
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                // Tenta chamar os getters. Se não existirem, um erro ocorrerá (ou não imprimirá nada útil).
                System.out.println("   - " + zona.getNome() + ": " +
                        zona.getGeracaoMinima() + " a " + // Requer getGeracaoMinima()
                        zona.getGeracaoMaxima() + " kg"); // Requer getGeracaoMaxima()
            }
        } catch (NoSuchMethodError e) {
            System.out.println("   (Não foi possível exibir os intervalos - atualize ZonaUrbana.java com os getters)");
        }


        // --- 5. Criação das Estações de Transferência ---
        // (Mantido como no código do seu amigo)
        System.out.println("\n--- Configuração das Estações ---");
        listaEstacoes.adicionar(new EstacaoPadrao("Estação A"));
        listaEstacoes.adicionar(new EstacaoPadrao("Estação B"));
        System.out.println(" > " + listaEstacoes.tamanho() + " estações criadas.");

        // --- 6. Preparar e Iniciar o Simulador ---
        System.out.println("\n--- Configurando o Simulador ---");
        Simulador simulador = new Simulador();

        // Passa as listas criadas para o simulador usar
        simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
        simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
        simulador.setListaZonas(listaZonas);
        simulador.setListaEstacoes(listaEstacoes);
        // Passar outros parâmetros para o simulador aqui (tempos de viagem, etc.)

        System.out.println("\n=== Configuração Concluída. Iniciando a Simulação... ===");
        simulador.iniciar();

        // Fecha o Scanner, pois não será mais usado nesta parte do código.
        scanner.close();

        System.out.println("(Thread principal 'main' terminando. Simulação continua em background.)");
    }


    // --- Método Auxiliar para perguntar quantidade ao usuário ---
    // (Mantido da versão anterior)
    private static int perguntarQuantidade(Scanner scanner, String mensagem) {
        int quantidade = -1;
        while (quantidade < 0) {
            System.out.print(mensagem + " ");
            try {
                quantidade = scanner.nextInt();
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

    // --- Método Auxiliar para adicionar caminhões à lista ---
    // (Mantido da versão anterior)
    private static void adicionarCaminhoes(Lista<CaminhaoPequeno> lista, int quantidade, int capacidade) {
        for (int i = 0; i < quantidade; i++) {
            lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
        }
    }
}