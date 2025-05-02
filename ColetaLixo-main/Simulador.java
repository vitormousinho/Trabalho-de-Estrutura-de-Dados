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
    // --- Adicionar Setters para os parâmetros de tempo se forem configuráveis ---

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

    // --- Método para carregar o estado (CORRIGIDO) ---
    public static Simulador carregar(String caminho) throws IOException, ClassNotFoundException {
        Simulador sim = null; // 1. Declara a variável ANTES do try
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            sim = (Simulador) ois.readObject(); // 2. Atribui o valor DENTRO do try
            // Configurações pós-carregamento
            sim.timer = null; // Timer não é salvo, precisa ser recriado
            sim.pausado = true; // Carrega pausado
            System.out.println("Simulação carregada de " + caminho + ". Está pausada.");
        }
        // 3. Retorna o valor DEPOIS do try
        // Se ocorreu uma exceção no try, ela será lançada para fora do método
        // antes de chegar aqui, então o compilador sabe que se chegar aqui, 'sim' foi inicializado.
        if (sim == null) {
            // Segurança adicional: se algo muito estranho acontecer e sim continuar null sem exceção
            throw new IOException("Falha ao carregar o objeto Simulador, objeto nulo após deserialização.");
        }
        return sim; // <<<< O return agora está fora e garantido (se não houver exceção)
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


    // --- LÓGICA PRINCIPAL DA SIMULAÇÃO ---
    private void atualizarSimulacao() {
        int hora = tempoSimulado / 60;
        int minuto = tempoSimulado % 60;
        System.out.printf("Tempo simulado: %02d:%02d %s%n", hora, minuto, isHorarioDePico(tempoSimulado) ? "(PICO)" : "");

        // Implementar a lógica principal aqui...


        // Exemplo de impressão a cada 60 minutos simulados
        if (tempoSimulado % 60 == 0 && tempoSimulado != 0) { // Evita imprimir no tempo 0
            System.out.println(" > Status: " + listaCaminhoesPequenos.tamanho() + " CP, "
                    + listaCaminhoesGrandes.tamanho() + " CG, "
                    + listaZonas.tamanho() + " Z, "
                    + listaEstacoes.tamanho() + " E.");
            if (!listaCaminhoesPequenos.estaVazia()) {
                System.out.println(" > Primeiro CP: " + listaCaminhoesPequenos.obter(0));
            }
        }
    }
}