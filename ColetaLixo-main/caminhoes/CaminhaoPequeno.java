package caminhoes;

import java.util.Random; // Para gerar a placa aleatória

public abstract class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    protected String placa; // Novo atributo para a placa

    // --- Novos atributos para estado e viagem (a serem usados na lógica do Simulador) ---
    // protected StatusCaminhao status; // Ex: OCIOSO, COLETANDO, VIAJANDO_ESTACAO, NA_FILA, etc. (Enum a ser criado)
    // protected int tempoRestanteViagem;
    // protected Zona zonaAtual; // Onde o caminhão está ou para onde vai
    // protected EstacaoTransferencia estacaoDestino;
    // protected int viagensRealizadasHoje;
    // ------------------------------------------------------------------------------------

    private static final Random random = new Random(); // Gerador de números aleatórios (static para ser compartilhado)

    // Construtor agora também gera uma placa
    protected CaminhaoPequeno(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser positiva.");
        }
        this.capacidade = capacidade;
        this.cargaAtual = 0;
        this.placa = gerarPlacaAleatoria(); // Gera e atribui a placa
        // Inicializar outros atributos aqui (status = StatusCaminhao.OCIOSO, tempoRestanteViagem = 0, etc.)
    }

    // Método auxiliar estático para gerar uma placa aleatória simples (LLLNNN)
    private static String gerarPlacaAleatoria() {
        // Gera 3 letras maiúsculas aleatórias (A-Z)
        char l1 = (char) ('A' + random.nextInt(26));
        char l2 = (char) ('A' + random.nextInt(26));
        char l3 = (char) ('A' + random.nextInt(26));
        // Gera 3 números aleatórios (0-9)
        int n1 = random.nextInt(10);
        int n2 = random.nextInt(10);
        int n3 = random.nextInt(10);

        return String.format("%c%c%c%d%d%d", l1, l2, l3, n1, n2, n3);
    }

    // Método de coleta AINDA abstrato aqui
    public abstract boolean coletar(int quantidade);

    // Métodos concretos comuns
    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    public int descarregar() {
        int carga = cargaAtual;
        cargaAtual = 0;
        return carga;
    }

    public int getCargaAtual() {
        return cargaAtual;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getPlaca() {
        return placa;
    }

    // Adicione outros métodos get/set/lógica comuns se precisar

    @Override
    public String toString() {
        // Atualizado para incluir a placa
        return String.format("CaminhaoPequeno[Placa=%s, Cap=%dkg, Carga=%dkg]",
                placa, capacidade, cargaAtual);
    }
}