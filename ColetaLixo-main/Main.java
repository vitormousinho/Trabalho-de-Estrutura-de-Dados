// Importa todas as classes que vamos usar na configuração
import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoGrandePadrao;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== Configurando o Simulador de Coleta de Lixo de Teresina ===");

        // --- 1. Criação das Listas para guardar os elementos ---
        Lista<CaminhaoPequeno> listaCaminhoesPequenos = new Lista<>();
        Lista<CaminhaoGrande> listaCaminhoesGrandes = new Lista<>();
        Lista<ZonaUrbana> listaZonas = new Lista<>();
        Lista<EstacaoTransferencia> listaEstacoes = new Lista<>();

        // --- 2. Criação dos Caminhões Pequenos Iniciais ---
        System.out.println("Criando caminhões pequenos...");
        listaCaminhoesPequenos.adicionar(new CaminhaoPequenoPadrao(2000)); // 2 Ton
        listaCaminhoesPequenos.adicionar(new CaminhaoPequenoPadrao(2000)); // 2 Ton
        listaCaminhoesPequenos.adicionar(new CaminhaoPequenoPadrao(4000)); // 4 Ton
        listaCaminhoesPequenos.adicionar(new CaminhaoPequenoPadrao(8000)); // 8 Ton
        listaCaminhoesPequenos.adicionar(new CaminhaoPequenoPadrao(10000)); // 10 Ton
        System.out.println(" > " + listaCaminhoesPequenos.tamanho() + " caminhões pequenos criados.");
        System.out.println(" > Lista: " + listaCaminhoesPequenos); // Mostra os caminhões criados

        // --- 3. Criação dos Caminhões Grandes Iniciais ---
        // Quantos caminhões grandes começam? Depende da estratégia. Começar com 1 por estação?
        System.out.println("Criando caminhões grandes...");
        listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao()); // 20 Ton
        listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao()); // 20 Ton
        System.out.println(" > " + listaCaminhoesGrandes.tamanho() + " caminhões grandes criados.");

        // --- 4. Criação das Zonas ---
        System.out.println("Criando zonas...");

        // Criar as 5 zonas com referências para poder configurá-las
        ZonaUrbana zonaSul = new ZonaUrbana("Sul");
        ZonaUrbana zonaNorte = new ZonaUrbana("Norte");
        ZonaUrbana zonaCentro = new ZonaUrbana("Centro");
        ZonaUrbana zonaLeste = new ZonaUrbana("Leste");
        ZonaUrbana zonaSudeste = new ZonaUrbana("Sudeste");

        // Configurar os intervalos de geração para cada zona (em kg)
        // Valores diferentes para cada zona com base em suas características
        zonaSul.setIntervaloGeracao(250, 650);      // Zona Sul: área residencial média
        zonaNorte.setIntervaloGeracao(350, 750);    // Zona Norte: área residencial densa
        zonaCentro.setIntervaloGeracao(450, 950);   // Zona Centro: área comercial (mais lixo)
        zonaLeste.setIntervaloGeracao(300, 700);    // Zona Leste: mista residencial/comercial
        zonaSudeste.setIntervaloGeracao(200, 500);  // Zona Sudeste: área residencial menos densa

        // Adicionar as zonas à lista
        listaZonas.adicionar(zonaSul);
        listaZonas.adicionar(zonaNorte);
        listaZonas.adicionar(zonaCentro);
        listaZonas.adicionar(zonaLeste);
        listaZonas.adicionar(zonaSudeste);

        System.out.println(" > " + listaZonas.tamanho() + " zonas criadas com intervalos de geração configurados:");
        for (int i = 0; i < listaZonas.tamanho(); i++) {
            ZonaUrbana zona = listaZonas.obter(i);
            System.out.println("   - " + zona.getNome() + ": " +
                    zona.getGeracaoMinima() + " a " +
                    zona.getGeracaoMaxima() + " kg por dia");
        }

        // --- 5. Criação das Estações de Transferência ---
        System.out.println("Criando estações de transferência...");
        listaEstacoes.adicionar(new EstacaoPadrao("Estação A"));
        listaEstacoes.adicionar(new EstacaoPadrao("Estação B"));
        System.out.println(" > " + listaEstacoes.tamanho() + " estações criadas.");

        // --- 6. Preparar e Iniciar o Simulador ---
        System.out.println("Configurando o simulador...");
        Simulador simulador = new Simulador();

        // Passa as listas criadas para o simulador usar
        simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
        simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
        simulador.setListaZonas(listaZonas);
        simulador.setListaEstacoes(listaEstacoes);

        System.out.println("\n=== Configuração Concluída. Iniciando a Simulação... ===");
        simulador.iniciar();
    }
}