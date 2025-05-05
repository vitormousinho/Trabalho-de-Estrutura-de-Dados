package caminhoes;

/**
 * Classe abstrata que representa um caminhão de grande porte (20 toneladas),
 * utilizado nas estações de transferência para levar o lixo ao aterro.
 * Possui uma tolerância de espera configurável.
 */
public abstract class CaminhaoGrande {
    // Constante para a capacidade máxima em KG
    public static final int CAPACIDADE_MAXIMA_KG = 20000;

    // Atributos
    protected int capacidadeMaxima = CAPACIDADE_MAXIMA_KG; // Usa a constante
    protected int cargaAtual;
    protected int toleranciaEspera; // Tempo em minutos que o caminhão vai esperar

    /**
     * Construtor para CaminhaoGrande.
     * @param toleranciaEspera O tempo máximo em minutos que o caminhão aguardará
     * na estação antes de partir (se tiver carga). Deve ser >= 1.
     * @throws IllegalArgumentException se toleranciaEspera for menor que 1.
     */
    public CaminhaoGrande(int toleranciaEspera) {
        if (toleranciaEspera < 1) {
            throw new IllegalArgumentException("A tolerância de espera deve ser pelo menos 1 minuto.");
        }
        this.cargaAtual = 0;
        this.toleranciaEspera = toleranciaEspera;
    }

    /**
     * Adiciona lixo ao caminhão.
     * A carga não excederá a capacidade máxima.
     * @param quantidade A quantidade de lixo a ser carregada.
     */
    public void carregar(int quantidade) {
        if (quantidade < 0) return; // Ignora quantidades negativas
        cargaAtual += quantidade;
        if (cargaAtual > capacidadeMaxima) {
            cargaAtual = capacidadeMaxima;
        }
    }

    /**
     * Verifica se o caminhão atingiu sua capacidade máxima.
     * @return true se a carga atual for maior ou igual à capacidade máxima, false caso contrário.
     */
    public boolean prontoParaPartir() {
        return cargaAtual >= capacidadeMaxima;
    }

    /**
     * Simula a partida do caminhão para o aterro e descarrega o lixo.
     * Zera a carga atual.
     * (Na implementação atual, apenas imprime uma mensagem).
     */
    public void descarregar() {
        System.out.println("Caminhão grande partiu para o aterro com " + cargaAtual + "kg.");
        // Aqui poderia ser adicionada lógica para registrar estatísticas de transporte
        cargaAtual = 0;
    }

    // --- Getters e Setters ---

    /**
     * Obtém a tolerância de espera configurada para este caminhão.
     * @return O tempo de tolerância em minutos.
     */
    public int getToleranciaEspera() {
        return toleranciaEspera;
    }

    /**
     * Define a tolerância de espera para este caminhão.
     * @param toleranciaEspera O novo tempo de tolerância em minutos (deve ser >= 1).
     * @throws IllegalArgumentException se toleranciaEspera for menor que 1.
     */
    public void setToleranciaEspera(int toleranciaEspera) {
        if (toleranciaEspera < 1) {
            throw new IllegalArgumentException("A tolerância de espera deve ser pelo menos 1 minuto.");
        }
        this.toleranciaEspera = toleranciaEspera;
    }

    /**
     * Obtém a carga atual do caminhão.
     * @return A carga atual em kg.
     */
    public int getCargaAtual() {
        return cargaAtual;
    }

    /**
     * Obtém a capacidade máxima do caminhão.
     * @return A capacidade máxima em kg.
     */
    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    @Override
    public String toString() {
        return String.format("CaminhaoGrande[Cap=%dkg, Carga=%dkg, Tolerancia=%dmin]",
                capacidadeMaxima, cargaAtual, toleranciaEspera);
    }
}