package caminhoes;

import java.util.Random;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;

/**
 * Classe abstrata que representa um caminhão de coleta de pequeno porte.
 * Possui capacidade variável, placa, status e controle de viagens.
 */
public abstract class CaminhaoPequeno {

    // Constante para o limite de viagens diárias padrão
    public static final int LIMITE_VIAGENS_DIARIAS_PADRAO = 10;

    // Atributos protegidos
    protected int capacidade;
    protected int cargaAtual;
    protected String placa;
    protected StatusCaminhao status;
    protected int tempoRestanteViagem; // Acesso via getter agora
    protected ZonaUrbana zonaAtual;
    protected EstacaoTransferencia estacaoDestino;
    protected int viagensRealizadasHoje;
    protected int limiteViagensDiarias;
    protected ZonaUrbana zonaDeOrigemParaRetorno;

    // Atributo para calcular tempo de espera na fila
    private int tempoChegadaNaFila;

    private static final Random random = new Random();

    protected CaminhaoPequeno(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser positiva.");
        }
        this.capacidade = capacidade;
        this.cargaAtual = 0;
        this.placa = gerarPlacaAleatoria();
        this.status = StatusCaminhao.OCIOSO;
        this.tempoRestanteViagem = 0;
        this.zonaAtual = null;
        this.estacaoDestino = null;
        this.zonaDeOrigemParaRetorno = null;
        this.viagensRealizadasHoje = 0;
        this.limiteViagensDiarias = LIMITE_VIAGENS_DIARIAS_PADRAO;
        this.tempoChegadaNaFila = -1;
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

    public abstract int coletar(int quantidade);

    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    public int descarregar() {
        int cargaDescarregada = cargaAtual;
        cargaAtual = 0;
        return cargaDescarregada;
    }

    public boolean registrarViagem() {
        viagensRealizadasHoje++;
        if (viagensRealizadasHoje >= limiteViagensDiarias) {
            status = StatusCaminhao.INATIVO_LIMITE_VIAGENS;
            System.out.println("Caminhão " + placa + " atingiu o limite de " + limiteViagensDiarias + " viagens e está INATIVO.");
            return false;
        }
        return true;
    }

    public void reiniciarViagensDiarias() {
        boolean estavaInativoPorLimite = (status == StatusCaminhao.INATIVO_LIMITE_VIAGENS);
        this.viagensRealizadasHoje = 0;
        if (estavaInativoPorLimite) {
            this.status = StatusCaminhao.OCIOSO;
            System.out.println("Caminhão " + placa + " teve viagens diárias reiniciadas e está OCIOSO.");
        }
        // O status não é alterado se não estava INATIVO_LIMITE_VIAGENS,
        // pois ele pode estar NA_FILA, VIAJANDO, etc., e deve continuar nesse estado
        // até que a lógica da simulação o mova para OCIOSO ou COLETANDO.
    }

    public void definirDestino(ZonaUrbana zona) {
        this.zonaAtual = zona;
        this.zonaDeOrigemParaRetorno = zona;
        this.estacaoDestino = null;
    }

    public void definirDestino(EstacaoTransferencia estacao) {
        if (this.zonaAtual != null) {
            this.zonaDeOrigemParaRetorno = this.zonaAtual;
        }
        this.estacaoDestino = estacao;
        this.zonaAtual = null; // CORRETO: zonaAtual fica null aqui
        this.status = StatusCaminhao.VIAJANDO_ESTACAO;
    }

    public void definirTempoViagem(int tempoViagem) {
        this.tempoRestanteViagem = Math.max(0, tempoViagem);
    }

    public boolean processarViagem() {
        if (tempoRestanteViagem > 0) {
            tempoRestanteViagem--;
        }
        return tempoRestanteViagem <= 0;
    }

    // Getters e Setters
    public int getCargaAtual() { return cargaAtual; }
    public int getCapacidade() { return capacidade; }
    public String getPlaca() { return placa; }
    public StatusCaminhao getStatus() { return status; }
    public void setStatus(StatusCaminhao status) { this.status = status; }
    public ZonaUrbana getZonaAtual() { return zonaAtual; }
    public void setEstacaoDestino(EstacaoTransferencia estacao) { this.estacaoDestino = estacao; }
    public EstacaoTransferencia getEstacaoDestino() { return estacaoDestino; }
    public ZonaUrbana getZonaDeOrigemParaRetorno() { return zonaDeOrigemParaRetorno; }
    public int getViagensRealizadasHoje() { return viagensRealizadasHoje; }
    public int getLimiteViagensDiarias() { return limiteViagensDiarias; }
    public void setLimiteViagensDiarias(int limite) {
        if (limite <= 0) { throw new IllegalArgumentException("Limite de viagens deve ser positivo."); }
        this.limiteViagensDiarias = limite;
    }

    // Métodos para tempo na fila
    public void setTempoChegadaNaFila(int tempoSimulado) { this.tempoChegadaNaFila = tempoSimulado; }
    public int getTempoChegadaNaFila() { return this.tempoChegadaNaFila; }

    // NOVO GETTER para tempoRestanteViagem
    public int getTempoRestanteViagem() {
        return this.tempoRestanteViagem;
    }

    @Override
    public String toString() {
        String destinoStr = "";
        if (status == StatusCaminhao.VIAJANDO_ESTACAO && estacaoDestino != null) {
            destinoStr = " -> " + estacaoDestino.getNome();
        } else if ((status == StatusCaminhao.COLETANDO || status == StatusCaminhao.RETORNANDO_ZONA) && zonaAtual != null) {
            destinoStr = " @ " + zonaAtual.getNome();
        } else if (status == StatusCaminhao.NA_FILA && estacaoDestino != null){
            destinoStr = " [Fila " + estacaoDestino.getNome() + "]";
        } else if (status == StatusCaminhao.OCIOSO && zonaDeOrigemParaRetorno != null) {
            destinoStr = " (Última zona: " + zonaDeOrigemParaRetorno.getNome() + ")";
        }
        return String.format("CP[%s, Cap=%dkg, Carga=%dkg, St=%s%s, V=%d/%d]",
                placa, capacidade, cargaAtual, status, destinoStr, viagensRealizadasHoje, limiteViagensDiarias);
    }
}