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
    public static final double PESO_LIXO_ACUMULADO = 2.0;
    public static final double PESO_TEMPO_SEM_COLETA = 1.5;
    public static final double PESO_CAMINHOES_ATIVOS = -2.0;
    public static final double PESO_TAXA_GERACAO = 0.3;

    private ZonaUrbana zona;
    private int caminhoesPequenosAtivos;
    private int tempoDesdeUltimaColeta;
    private double score;
    private double scoreFinal; // Novo atributo para o score após aplicar balanceamento

    /**
     * Construtor da classe ScoreZona.
     * @param zona Zona urbana associada a este score.
     */
    public ScoreZona(ZonaUrbana zona) {
        this.zona = zona;
        this.caminhoesPequenosAtivos = 0;
        this.tempoDesdeUltimaColeta = 0;
        calcularScore();
        this.scoreFinal = this.score; // Inicializa o scoreFinal igual ao score bruto
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
        this.scoreFinal = this.score; // Atualiza o scoreFinal (pode ser modificado posteriormente por algoritmos de balanceamento)
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
     * Define o score final após a aplicação de fatores de balanceamento.
     * Usado para evitar priorização excessiva de zonas com scores muito altos.
     * @param scoreFinal O novo valor de score final balanceado.
     */
    public void setScoreFinal(double scoreFinal) {
        this.scoreFinal = scoreFinal;
    }

    /**
     * Retorna o score bruto calculado sem ajustes de balanceamento.
     * @return O score de prioridade original calculado.
     */
    public double getScore() {
        return score;
    }

    /**
     * Retorna o score final após aplicar fatores de balanceamento.
     * Usado na distribuição para evitar concentração excessiva de caminhões.
     * @return O score final balanceado.
     */
    public double getScoreFinal() {
        return scoreFinal;
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
        return String.format("ScoreZona[%s, Lixo=%d, CPs=%d, Tempo=%d, Score=%.2f, ScoreFinal=%.2f]",
                zona.getNome(), zona.getLixoAcumulado(), caminhoesPequenosAtivos,
                tempoDesdeUltimaColeta, score, scoreFinal);
    }
}