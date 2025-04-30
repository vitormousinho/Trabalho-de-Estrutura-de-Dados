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
        // Criar as 5 zonas especificadas no PDF [cite: 4, 19]
        System.out.println("Criando zonas...");
        listaZonas.adicionar(new ZonaUrbana("Sul"));
        listaZonas.adicionar(new ZonaUrbana("Norte"));
        listaZonas.adicionar(new ZonaUrbana("Centro"));
        listaZonas.adicionar(new ZonaUrbana("Leste"));
        listaZonas.adicionar(new ZonaUrbana("Sudeste"));
        // Aqui você precisará configurar os intervalos de geração de lixo de cada zona [cite: 6, 31]
        // Ex: zonaSul.setIntervaloGeracao(min, max); (teria que adicionar esse método em ZonaUrbana)
        System.out.println(" > " + listaZonas.tamanho() + " zonas criadas.");

        // --- 5. Criação das Estações de Transferência ---
        // Criar as 2 estações [cite: 8]
        System.out.println("Criando estações de transferência...");
        listaEstacoes.adicionar(new EstacaoPadrao("Estação A"));
        listaEstacoes.adicionar(new EstacaoPadrao("Estação B"));
        // Configurar tempos máximos de espera aqui, se aplicável [cite: 31]
        System.out.println(" > " + listaEstacoes.tamanho() + " estações criadas.");

        // --- 6. Preparar e Iniciar o Simulador ---
        System.out.println("Configurando o simulador...");
        Simulador simulador = new Simulador();

        // Passa as listas criadas para o simulador usar
        simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
        simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
        simulador.setListaZonas(listaZonas);
        simulador.setListaEstacoes(listaEstacoes);
        // Passar outros parâmetros de configuração para o simulador (tempos de viagem, etc.)

        System.out.println("\n=== Configuração Concluída. Iniciando a Simulação... ===");
        simulador.iniciar();

        // A partir daqui, a simulação roda no seu próprio ritmo até ser interrompida
        // ou até que você implemente uma condição de parada.

        // Você poderia adicionar um loop aqui para interagir com o simulador (pausar, continuar, etc.)
        // ou apenas deixá-lo rodar por um tempo.
    }
}