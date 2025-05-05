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
    protected int tempoRestanteViagem;
    protected ZonaUrbana zonaAtual; // Zona onde está coletando ou para onde está retornando
    protected EstacaoTransferencia estacaoDestino; // Estação para onde está viajando
    protected int viagensRealizadasHoje;
    protected int limiteViagensDiarias;

    private static final Random random = new Random(); // Gerador de números aleatórios para placa

    /**
     * Construtor para CaminhaoPequeno.
     * Inicializa a capacidade, carga, placa e status padrão.
     *
     * @param capacidade A capacidade máxima de carga do caminhão em kg (deve ser > 0).
     * @throws IllegalArgumentException se a capacidade não for positiva.
     */
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
        // Antes: this.limiteViagensDiarias = 10;
        this.limiteViagensDiarias = LIMITE_VIAGENS_DIARIAS_PADRAO; // Usa a constante
    }

    /**
     * Gera uma placa aleatória no formato LLLNNN.
     * @return Uma string representando a placa gerada.
     */
    private static String gerarPlacaAleatoria() {
        char l1 = (char) ('A' + random.nextInt(26));
        char l2 = (char) ('A' + random.nextInt(26));
        char l3 = (char) ('A' + random.nextInt(26));
        int n1 = random.nextInt(10);
        int n2 = random.nextInt(10);
        int n3 = random.nextInt(10);

        return String.format("%c%c%c%d%d%d", l1, l2, l3, n1, n2, n3);
    }

    /**
     * Método abstrato para coletar lixo. A implementação define como a coleta ocorre.
     * @param quantidade A quantidade de lixo a tentar coletar.
     * @return A quantidade de lixo efetivamente coletada.
     */
    public abstract int coletar(int quantidade);

    /**
     * Verifica se o caminhão atingiu ou excedeu sua capacidade máxima.
     * @return true se estiver cheio, false caso contrário.
     */
    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    /**
     * Esvazia a carga do caminhão (simula o descarregamento).
     * @return A quantidade de carga que foi descarregada.
     */
    public int descarregar() {
        int cargaDescarregada = cargaAtual;
        cargaAtual = 0;
        // Poderia mudar o status aqui se necessário, mas geralmente é feito pelo chamador (Estacao ou Simulador)
        return cargaDescarregada;
    }

    /**
     * Registra que uma viagem foi realizada e atualiza o status se o limite diário for atingido.
     * @return true se o caminhão ainda pode realizar mais viagens hoje, false se atingiu o limite.
     */
    public boolean registrarViagem() {
        viagensRealizadasHoje++;
        if (viagensRealizadasHoje >= limiteViagensDiarias) {
            status = StatusCaminhao.INATIVO_LIMITE_VIAGENS;
            System.out.println("Caminhão " + placa + " atingiu o limite de " + limiteViagensDiarias + " viagens.");
            return false;
        }
        return true;
    }

    /**
     * Reinicia o contador de viagens diárias (geralmente chamado no início de um novo dia simulado).
     * Se o caminhão estava inativo devido ao limite de viagens, ele volta ao estado OCIOSO.
     */
    public void reiniciarViagensDiarias() {
        viagensRealizadasHoje = 0;
        if (status == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
            status = StatusCaminhao.OCIOSO; // Volta a ficar disponível
            System.out.println("Caminhão " + placa + " reiniciou viagens diárias e está OCIOSO.");
        }
    }

    /**
     * Define o destino do caminhão como sendo uma Zona Urbana (para coleta ou retorno).
     * @param zona A ZonaUrbana de destino.
     */
    public void definirDestino(ZonaUrbana zona) {
        this.zonaAtual = zona;
        this.estacaoDestino = null; // Garante que não há estação destino
        // O status (COLETANDO, RETORNANDO_ZONA) deve ser definido pelo chamador
    }

    /**
     * Define o destino do caminhão como sendo uma Estação de Transferência.
     * @param estacao A EstacaoTransferencia de destino.
     */
    public void definirDestino(EstacaoTransferencia estacao) {
        this.estacaoDestino = estacao;
        this.zonaAtual = null; // Garante que não há zona atual (ele está em viagem para estação)
        this.status = StatusCaminhao.VIAJANDO_ESTACAO; // Define o status apropriado
    }

    /**
     * Define o tempo restante para a viagem atual.
     * @param tempoViagem O tempo em minutos.
     */
    public void definirTempoViagem(int tempoViagem) {
        this.tempoRestanteViagem = Math.max(0, tempoViagem); // Garante tempo não negativo
    }

    /**
     * Processa um passo de tempo da viagem, decrementando o tempo restante.
     * @return true se a viagem terminou (tempo <= 0), false caso contrário.
     */
    public boolean processarViagem() {
        if (tempoRestanteViagem > 0) {
            tempoRestanteViagem--;
        }
        return tempoRestanteViagem <= 0;
    }

    // --- Getters e Setters ---

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
        // Poderia adicionar validações de transição de status se necessário
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

    /**
     * Define um novo limite de viagens diárias para o caminhão.
     * @param limite O novo limite (deve ser > 0).
     * @throws IllegalArgumentException se o limite não for positivo.
     */
    public void setLimiteViagensDiarias(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("Limite de viagens deve ser positivo.");
        }
        this.limiteViagensDiarias = limite;
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
        }

        return String.format("CP[%s, Cap=%dkg, Carga=%dkg, St=%s%s, V=%d/%d]",
                placa, capacidade, cargaAtual, status, destinoStr, viagensRealizadasHoje, limiteViagensDiarias);
    }
}