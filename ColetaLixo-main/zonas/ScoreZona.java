package zonas;

import java.io.Serializable;

/**
 * Classe que calcula e armazena scores de prioridade para as zonas urbanas.
 * Os scores são usados para determinar quais zonas precisam de mais atenção
 * na distribuição de caminhões de coleta.
 */
public class ScoreZona implements Serializable {
    private static final long serialVersionUID = 1L;

    // Pesos para os diferentes fatores no cálculo do score
    public static final double PESO_LIXO_ACUMULADO = 1.0;
    public static final double PESO_TEMPO_SEM_COLETA = 0.5;
    public static final double PESO_CAMINHOES_ATIVOS = -0.8;
    public static final double PESO_TAXA_GERACAO = 0.3;

    private ZonaUrbana zona;
    private int caminhoesPequenosAtivos;
    private int tempoDesdeUltimaColeta;
    private double score;

    /**
     * Construtor da classe ScoreZona.
     * @param zona Zona urbana associada a este score.
     */
    public ScoreZona(ZonaUrbana zona) {
        this.zona = zona;
        this.caminhoesPequenosAtivos = 0;
        this.tempoDesdeUltimaColeta = 0;
        calcularScore();
    }

    /**
     * Calcula o score de prioridade da zona com base nos fatores definidos.
     */
    public void calcularScore() {
        double scoreLixoAcumulado = zona.getLixoAcumulado() * PESO_LIXO_ACUMULADO;
        double scoreTempoSemColeta = tempoDesdeUltimaColeta * PESO_TEMPO_SEM_COLETA;
        double scoreCaminhoesAtivos = caminhoesPequenosAtivos * PESO_CAMINHOES_ATIVOS;
        double scoreTaxaGeracao = ((zona.getGeracaoMaxima() + zona.getGeracaoMinima()) / 2.0) * PESO_TAXA_GERACAO;

        this.score = scoreLixoAcumulado + scoreTempoSemColeta + scoreCaminhoesAtivos + scoreTaxaGeracao;
    }

    /**
     * Registra uma coleta realizada na zona, resetando o contador de tempo.
     */
    public void registrarColeta() {
        this.tempoDesdeUltimaColeta = 0;
        calcularScore();
    }

    /**
     * Incrementa o contador de tempo desde a última coleta.
     * @param minutos Minutos a incrementar.
     */
    public void incrementarTempo(int minutos) {
        this.tempoDesdeUltimaColeta += minutos;
        calcularScore();
    }

    /**
     * Incrementa o contador de caminhões ativos na zona.
     */
    public void incrementarCaminhoesAtivos() {
        this.caminhoesPequenosAtivos++;
        calcularScore();
    }

    /**
     * Decrementa o contador de caminhões ativos na zona.
     */
    public void decrementarCaminhoesAtivos() {
        if (this.caminhoesPequenosAtivos > 0) {
            this.caminhoesPequenosAtivos--;
            calcularScore();
        }
    }

    /**
     * Define diretamente o número de caminhões ativos na zona.
     * @param quantidade Nova quantidade de caminhões ativos.
     */
    public void setCaminhoesAtivos(int quantidade) {
        this.caminhoesPequenosAtivos = Math.max(0, quantidade);
        calcularScore();
    }

    /**
     * Retorna o score atual da zona.
     * @return O score de prioridade calculado.
     */
    public double getScore() {
        return score;
    }

    /**
     * Retorna a zona associada a este score.
     * @return A zona urbana.
     */
    public ZonaUrbana getZona() {
        return zona;
    }

    /**
     * Retorna o número de caminhões pequenos atualmente ativos na zona.
     * @return Quantidade de caminhões ativos.
     */
    public int getCaminhoesAtivos() {
        return caminhoesPequenosAtivos;
    }

    /**
     * Retorna o tempo (em minutos) desde a última coleta na zona.
     * @return Tempo desde a última coleta.
     */
    public int getTempoDesdeUltimaColeta() {
        return tempoDesdeUltimaColeta;
    }

    @Override
    public String toString() {
        return String.format("ScoreZona[%s, Lixo=%d, CPs=%d, Tempo=%d, Score=%.2f]",
                zona.getNome(), zona.getLixoAcumulado(), caminhoesPequenosAtivos,
                tempoDesdeUltimaColeta, score);
    }
}