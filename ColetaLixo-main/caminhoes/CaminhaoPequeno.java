package caminhoes;

import java.util.Random;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;

public abstract class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    protected String placa;

    // Novos atributos para controle de estado
    protected StatusCaminhao status;
    protected int tempoRestanteViagem;
    protected ZonaUrbana zonaAtual;
    protected EstacaoTransferencia estacaoDestino;
    protected int viagensRealizadasHoje;
    protected int limiteViagensDiarias;

    private static final Random random = new Random();

    protected CaminhaoPequeno(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser positiva.");
        }
        this.capacidade = capacidade;
        this.cargaAtual = 0;
        this.placa = gerarPlacaAleatoria();

        // Inicializa os novos atributos
        this.status = StatusCaminhao.OCIOSO;
        this.tempoRestanteViagem = 0;
        this.zonaAtual = null;
        this.estacaoDestino = null;
        this.viagensRealizadasHoje = 0;
        this.limiteViagensDiarias = 10; // Valor padrão, pode ser configurado
    }

    private static String gerarPlacaAleatoria() {
        char l1 = (char) ('A' + random.nextInt(26));
        char l2 = (char) ('A' + random.nextInt(26));
        char l3 = (char) ('A' + random.nextInt(26));
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

    // Novos métodos para gestão do estado

    /**
     * Registra uma viagem realizada e verifica se o limite diário foi atingido
     * @return true se ainda pode realizar viagens, false se atingiu o limite
     */
    public boolean registrarViagem() {
        viagensRealizadasHoje++;
        if (viagensRealizadasHoje >= limiteViagensDiarias) {
            status = StatusCaminhao.INATIVO_LIMITE_VIAGENS;
            return false;
        }
        return true;
    }

    /**
     * Reinicia o contador de viagens diárias
     */
    public void reiniciarViagensDiarias() {
        viagensRealizadasHoje = 0;
        if (status == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
            status = StatusCaminhao.OCIOSO;
        }
    }

    /**
     * Define o destino do caminhão (zona ou estação)
     */
    public void definirDestino(ZonaUrbana zona) {
        this.zonaAtual = zona;
        this.estacaoDestino = null;
        this.status = StatusCaminhao.VIAJANDO_ESTACAO;
    }

    public void definirDestino(EstacaoTransferencia estacao) {
        this.estacaoDestino = estacao;
        this.status = StatusCaminhao.VIAJANDO_ESTACAO;
    }

    /**
     * Define o tempo de viagem restante
     */
    public void definirTempoViagem(int tempoViagem) {
        this.tempoRestanteViagem = tempoViagem;
    }

    /**
     * Processa o tempo de viagem, decrementando o contador
     * @return true se a viagem terminou, false se ainda está em andamento
     */
    public boolean processarViagem() {
        if (tempoRestanteViagem > 0) {
            tempoRestanteViagem--;
        }
        return tempoRestanteViagem <= 0;
    }

    // Getters e Setters
    public int getCargaAtual() {
        return cargaAtual;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getPlaca() {
        return placa;
    }

    public StatusCaminhao getStatus() {
        return status;
    }

    public void setStatus(StatusCaminhao status) {
        this.status = status;
    }

    public ZonaUrbana getZonaAtual() {
        return zonaAtual;
    }

    public EstacaoTransferencia getEstacaoDestino() {
        return estacaoDestino;
    }

    public int getViagensRealizadasHoje() {
        return viagensRealizadasHoje;
    }

    public int getLimiteViagensDiarias() {
        return limiteViagensDiarias;
    }

    public void setLimiteViagensDiarias(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("Limite de viagens deve ser positivo.");
        }
        this.limiteViagensDiarias = limite;
    }

    @Override
    public String toString() {
        return String.format("CaminhaoPequeno[Placa=%s, Cap=%dkg, Carga=%dkg, Status=%s]",
                placa, capacidade, cargaAtual, status);
    }
}