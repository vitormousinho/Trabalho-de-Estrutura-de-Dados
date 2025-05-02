import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

// Importe as classes necessárias
import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;
import caminhoes.CaminhaoGrandePadrao;



public class Simulador implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Timer timer;
    private int tempoSimulado = 0; // Em minutos simulados
    private boolean pausado = false;
    private transient Random random = new Random();

    // --- Atributos para guardar os elementos ---
    private Lista<CaminhaoPequeno> listaCaminhoesPequenos;
    private Lista<CaminhaoGrande> listaCaminhoesGrandes;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;

    // Novo atributo para tolerância de espera padrão dos caminhões grandes
    private int toleranciaCaminhoesGrandes = 30; // valor padrão em minutos

    // --- PARÂMETROS DE CONFIGURAÇÃO (Horário de Pico e Tempos de Viagem) ---
    private int picoManhaInicio = 7 * 60; // 420
    private int picoManhaFim = 9 * 60 - 1; // 539
    private int picoTardeInicio = 17 * 60; // 1020
    private int picoTardeFim = 19 * 60 - 1; // 1139

    private int minViagemForaPico = 15;
    private int maxViagemForaPico = 45;
    private int minViagemPico = 30;
    private int maxViagemPico = 90;

    // Construtor
    public Simulador() {
        this.listaCaminhoesPequenos = new Lista<>();
        this.listaCaminhoesGrandes = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
    }

    // --- Métodos Setters para as listas ---
    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) { this.listaCaminhoesPequenos = lista; }
    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) { this.listaCaminhoesGrandes = lista; }
    public void setListaZonas(Lista<ZonaUrbana> lista) { this.listaZonas = lista; }
    public void setListaEstacoes(Lista<EstacaoTransferencia> lista) { this.listaEstacoes = lista; }

    // Novo setter para tolerância de caminhões grandes
    public void setToleranciaCaminhoesGrandes(int tolerancia) {
        if (tolerancia < 1) {
            throw new IllegalArgumentException("A tolerância deve ser pelo menos 1 minuto.");
        }
        this.toleranciaCaminhoesGrandes = tolerancia;

        // Atualiza a tolerância em todos os caminhões grandes existentes
        for (int i = 0; i < listaCaminhoesGrandes.tamanho(); i++) {
            CaminhaoGrande caminhao = listaCaminhoesGrandes.obter(i);
            caminhao.setToleranciaEspera(tolerancia);
        }
    }

    // Getter para tolerância de caminhões grandes
    public int getToleranciaCaminhoesGrandes() {
        return toleranciaCaminhoesGrandes;
    }

    // --- MÉTODOS DE CONTROLE ---
    public void iniciar() {
        if (timer != null) {
            System.out.println("Simulação já estava iniciada. Para reiniciar, encerre primeiro.");
            return;
        }
        System.out.println("Simulação iniciada...");
        pausado = false;
        tempoSimulado = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!pausado) {
                    tempoSimulado = (tempoSimulado + 1) % (24 * 60); // Avança 1 minuto e faz o ciclo diário
                    atualizarSimulacao();
                }
            }
        }, 0, 50); // Atualiza a cada 50ms reais
    }

    public void pausar() {
        System.out.println("Simulação pausada.");
        pausado = true;
    }

    public void continuarSimulacao() {
        if(timer == null) {
            System.out.println("Simulação não iniciada. Use iniciar().");
            return;
        }
        System.out.println("Simulação retomada.");
        pausado = false;
    }

    public void encerrar() {
        System.out.println("Simulação encerrada.");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // --- Método para salvar o estado ---
    public void gravar(String caminho) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(this);
            System.out.println("Simulação salva em " + caminho);
        }
    }

    // --- Método para carregar o estado ---
    public static Simulador carregar(String caminho) throws IOException, ClassNotFoundException {
        Simulador sim = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            sim = (Simulador) ois.readObject();
            sim.timer = null;
            sim.pausado = true;
            System.out.println("Simulação carregada de " + caminho + ". Está pausada.");
        }
        if (sim == null) {
            throw new IOException("Falha ao carregar o objeto Simulador, objeto nulo após deserialização.");
        }
        return sim;
    }

    // --- MÉTODO AUXILIAR para verificar horário de pico ---
    private boolean isHorarioDePico(int tempoAtualMinutos) {
        boolean manha = (tempoAtualMinutos >= picoManhaInicio && tempoAtualMinutos <= picoManhaFim);
        boolean tarde = (tempoAtualMinutos >= picoTardeInicio && tempoAtualMinutos <= picoTardeFim);
        return manha || tarde;
    }

    // --- MÉTODO AUXILIAR para calcular tempo de viagem ---
    private int calcularTempoViagemPequeno() {
        int min, max;
        if (isHorarioDePico(this.tempoSimulado)) {
            min = this.minViagemPico;
            max = this.maxViagemPico;
        } else {
            min = this.minViagemForaPico;
            max = this.maxViagemForaPico;
        }
        if (min >= max) return min;
        return random.nextInt(max - min + 1) + min;
    }

    // --- MÉTODO AUXILIAR para adicionar um novo caminhão grande ---
    public void adicionarCaminhaoGrande() {
        CaminhaoGrande novoCaminhao = new CaminhaoGrandePadrao(this.toleranciaCaminhoesGrandes);
        listaCaminhoesGrandes.adicionar(novoCaminhao);
        System.out.println("NOVO CAMINHÃO GRANDE ADICIONADO! Total agora: " + listaCaminhoesGrandes.tamanho());
    }

    // --- LÓGICA PRINCIPAL DA SIMULAÇÃO ---
    private void atualizarSimulacao() {
        int hora = tempoSimulado / 60;
        int minuto = tempoSimulado % 60;

        // Log menos verboso - apenas a cada minuto completo
        if (minuto % 1 == 0) {
            System.out.printf("Tempo simulado: %02d:%02d %s%n", hora, minuto, isHorarioDePico(tempoSimulado) ? "(PICO)" : "");
        }

        // 1. Gerenciar geração de lixo (a cada hora simulada)
        if (minuto == 0) {
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                zona.gerarLixo();
            }
        }

        // 2. Processar caminhões pequenos (lógica de coleta e viagens)
        // (Esta parte seria implementada com base nas regras de movimento dos caminhões pequenos)
        // ...

        // 3. Processar estações de transferência
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            // Precisa fazer cast para acessar os métodos específicos da implementação
            if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);

                // Processar a fila de caminhões pequenos
                estacao.processarFila();

                // Gerenciar tempo de espera do caminhão grande atual
                estacao.gerenciarTempoEsperaCaminhaoGrande();

                // Verificar se o tempo de espera foi excedido e se precisa de um novo caminhão grande
                if (estacao.tempoEsperaExcedido()) {
                    System.out.println("ALERTA! Tempo de espera excedido na " + estacao.getNome());

                    // Adicionar novo caminhão grande se necessário
                    adicionarCaminhaoGrande();

                    // Tentar encontrar um caminhão grande disponível e atribuir à estação
                    atribuirCaminhaoGrandeParaEstacao(estacao);
                }

                // Verificar se a estação precisa de um caminhão grande (não tem nenhum atualmente)
                if (estacao.precisaCaminhaoGrande()) {
                    System.out.println("Estação " + estacao.getNome() + " precisa de um caminhão grande.");
                    atribuirCaminhaoGrandeParaEstacao(estacao);
                }
            }
        }

        // 4. Relatório a cada hora simulada (a cada 60 minutos)
        if (tempoSimulado % 60 == 0 && tempoSimulado != 0) { // Evita imprimir no tempo 0
            System.out.println("\n=== RELATÓRIO DE STATUS: HORA " + hora + " ===");
            System.out.println(" > Frota: " + listaCaminhoesPequenos.tamanho() + " caminhões pequenos, "
                    + listaCaminhoesGrandes.tamanho() + " caminhões grandes");
            System.out.println(" > Infraestrutura: " + listaZonas.tamanho() + " zonas, "
                    + listaEstacoes.tamanho() + " estações");

            // Relatório de lixo por zona
            System.out.println(" > Lixo acumulado por zona:");
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                System.out.println("   - " + zona.getNome() + ": " + zona.getLixoAcumulado() + "kg");
            }

            // Relatório de filas nas estações
            System.out.println(" > Status das estações:");
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                    EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);
                    System.out.println("   - " + estacao.getNome() + ": "
                            + estacao.getCaminhoesNaFila() + " caminhões na fila");
                }
            }

            System.out.println("============================================\n");
        }
    }

    // Método auxiliar para atribuir um caminhão grande disponível a uma estação
    private void atribuirCaminhaoGrandeParaEstacao(EstacaoPadrao estacao) {
        // Procura por um caminhão grande "disponível" (lógica simplificada)
        // Em uma implementação completa, seria necessário rastrear o estado de cada caminhão

        // Por enquanto, apenas verificamos se há caminhões disponíveis e, se não, criamos um novo
        if (listaCaminhoesGrandes.tamanho() == 0) {
            adicionarCaminhaoGrande();
        }

        // Atribui o primeiro caminhão da lista (supostamente disponível)
        // Em uma implementação completa, haveria uma lista separada de caminhões disponíveis
        if (listaCaminhoesGrandes.tamanho() > 0) {
            CaminhaoGrande caminhao = listaCaminhoesGrandes.obter(0);
            estacao.atribuirCaminhaoGrande(caminhao);

            // Remove da lista principal (já que agora está em uso na estação)
            // Em uma implementação completa, moveria para uma lista de caminhões em uso
            listaCaminhoesGrandes.remover(0);
        }
    }
}