import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.StatusCaminhao;
import zonas.MapaUrbano;
import zonas.ScoreZona;
import zonas.ZonaUrbana;

/**
 * Classe responsável por implementar algoritmos de distribuição inteligente
 * de caminhões pequenos entre as zonas urbanas.
 */
public class DistribuicaoCaminhoes implements Serializable {
    private static final long serialVersionUID = 1L;

    private MapaUrbano mapaUrbano;
    private Map<String, ScoreZona> scoresZonas;
    private Random random;

    /**
     * Construtor da classe DistribuicaoCaminhoes.
     * @param mapaUrbano Mapa urbano com as distâncias entre zonas.
     * @param zonas Lista de zonas urbanas.
     */
    public DistribuicaoCaminhoes(MapaUrbano mapaUrbano, Lista<ZonaUrbana> zonas) {
        this.mapaUrbano = mapaUrbano;
        this.random = new Random();
        this.scoresZonas = new HashMap<>();

        // Inicializa os scores para todas as zonas
        if (zonas != null) {
            for (int i = 0; i < zonas.tamanho(); i++) {
                ZonaUrbana zona = zonas.obter(i);
                scoresZonas.put(zona.getNome(), new ScoreZona(zona));
            }
        }
    }

    /**
     * Distribui caminhões ociosos para as zonas com base nos scores de prioridade.
     * @param caminhoes Lista de caminhões pequenos disponíveis para distribuição.
     * @param zonas Lista de zonas urbanas.
     * @return Número de caminhões distribuídos com sucesso.
     */
    public int distribuirCaminhoes(Lista<CaminhaoPequeno> caminhoes, Lista<ZonaUrbana> zonas) {
        if (caminhoes == null || caminhoes.estaVazia() || zonas == null || zonas.estaVazia()) {
            return 0;
        }

        // Atualiza contagem de caminhões ativos por zona
        atualizarContadorCaminhoesAtivos(caminhoes, zonas);

        // Lista para armazenar caminhões ociosos
        List<CaminhaoPequeno> caminhoesOciosos = new ArrayList<>();

        // Coleta todos os caminhões ociosos
        for (int i = 0; i < caminhoes.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoes.obter(i);
            if (caminhao.getStatus() == StatusCaminhao.OCIOSO) {
                caminhoesOciosos.add(caminhao);
            }
        }

        if (caminhoesOciosos.isEmpty()) {
            return 0;
        }

        // Calcula os scores de todas as zonas
        calcularScoresZonas();

        // Ordena as zonas por score (do maior para o menor)
        List<ScoreZona> zonasOrdenadas = new ArrayList<>(scoresZonas.values());
        zonasOrdenadas.sort(Comparator.comparingDouble(ScoreZona::getScore).reversed());

        int distribuidos = 0;

        // Distribui os caminhões com base na prioridade das zonas e distâncias
        for (CaminhaoPequeno caminhao : caminhoesOciosos) {
            // Obtém a zona de origem (se houver) para considerar a proximidade
            ZonaUrbana zonaOrigem = caminhao.getZonaDeOrigemParaRetorno();

            // Escolhe a zona de destino
            ZonaUrbana zonaDestino = null;

            if (zonaOrigem != null) {
                // Se tiver uma zona de origem, considera as distâncias
                zonaDestino = escolherZonaComBaseEmDistancia(zonaOrigem, zonasOrdenadas, zonas);
            } else {
                // Se não tiver origem, escolhe baseado apenas no score
                // com uma probabilidade proporcional ao score
                zonaDestino = escolherZonaComBaseProbabilistica(zonasOrdenadas, zonas);
            }

            if (zonaDestino != null) {
                caminhao.definirDestino(zonaDestino);
                caminhao.setStatus(StatusCaminhao.COLETANDO);

                // Atualiza os contadores
                String nomeZona = zonaDestino.getNome();
                if (scoresZonas.containsKey(nomeZona)) {
                    scoresZonas.get(nomeZona).incrementarCaminhoesAtivos();
                }

                distribuidos++;

                // Log para debug
                System.out.println("Caminhão " + caminhao.getPlaca() + " enviado para zona " +
                        zonaDestino.getNome() + " (Score: " +
                        scoresZonas.get(zonaDestino.getNome()).getScore() + ")");
            }
        }

        return distribuidos;
    }

    /**
     * Atualiza o contador de caminhões ativos para cada zona.
     * @param caminhoes Lista de todos os caminhões.
     * @param zonas Lista de todas as zonas.
     */
    private void atualizarContadorCaminhoesAtivos(Lista<CaminhaoPequeno> caminhoes, Lista<ZonaUrbana> zonas) {
        // Reinicia todos os contadores
        for (ScoreZona score : scoresZonas.values()) {
            score.setCaminhoesAtivos(0);
        }

        // Conta caminhões ativos por zona
        for (int i = 0; i < caminhoes.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoes.obter(i);
            ZonaUrbana zonaAtual = caminhao.getZonaAtual();

            if (zonaAtual != null && caminhao.getStatus() == StatusCaminhao.COLETANDO) {
                String nomeZona = zonaAtual.getNome();
                if (scoresZonas.containsKey(nomeZona)) {
                    scoresZonas.get(nomeZona).incrementarCaminhoesAtivos();
                }
            }
        }
    }

    /**
     * Escolhe uma zona com base na distância da zona de origem, priorizando zonas com maior score.
     * @param zonaOrigem Zona de origem do caminhão.
     * @param zonasOrdenadas Lista de scores de zonas ordenadas por prioridade.
     * @param todasZonas Lista de todas as zonas (para referência).
     * @return A zona escolhida.
     */
    private ZonaUrbana escolherZonaComBaseEmDistancia(ZonaUrbana zonaOrigem, List<ScoreZona> zonasOrdenadas, Lista<ZonaUrbana> todasZonas) {
        // Se não houver zonas, retorna null
        if (zonasOrdenadas.isEmpty()) {
            return null;
        }

        // Se houver apenas uma zona, retorna ela
        if (zonasOrdenadas.size() == 1) {
            return encontrarZonaPorNome(zonasOrdenadas.get(0).getZona().getNome(), todasZonas);
        }

        // Pega as 3 zonas com maior score (ou menos, se não houver 3)
        int topN = Math.min(3, zonasOrdenadas.size());
        List<String> zonasTopN = new ArrayList<>();

        for (int i = 0; i < topN; i++) {
            zonasTopN.add(zonasOrdenadas.get(i).getZona().getNome());
        }

        // Encontra a zona mais próxima dentre as top N
        String zonaMaisProxima = mapaUrbano.encontrarZonaMaisProxima(zonaOrigem.getNome(), zonasTopN);

        if (zonaMaisProxima != null) {
            return encontrarZonaPorNome(zonaMaisProxima, todasZonas);
        } else {
            // Fallback para a zona com maior score
            return encontrarZonaPorNome(zonasOrdenadas.get(0).getZona().getNome(), todasZonas);
        }
    }

    /**
     * Escolhe uma zona com base em probabilidade proporcional ao score.
     * Zonas com scores mais altos têm maior chance de serem escolhidas.
     * @param zonasOrdenadas Lista de scores de zonas ordenadas por prioridade.
     * @param todasZonas Lista de todas as zonas (para referência).
     * @return A zona escolhida.
     */
    private ZonaUrbana escolherZonaComBaseProbabilistica(List<ScoreZona> zonasOrdenadas, Lista<ZonaUrbana> todasZonas) {
        if (zonasOrdenadas.isEmpty()) {
            return null;
        }

        // Normaliza os scores para evitar valores negativos
        double minScore = Double.MAX_VALUE;
        for (ScoreZona sz : zonasOrdenadas) {
            if (sz.getScore() < minScore) {
                minScore = sz.getScore();
            }
        }

        // Soma total dos scores normalizados
        double somaScores = 0;
        for (ScoreZona sz : zonasOrdenadas) {
            // Adiciona uma constante para todos os scores serem positivos
            double scoreNormalizado = sz.getScore() - minScore + 1.0;
            somaScores += scoreNormalizado;
        }

        if (somaScores <= 0) {
            // Escolha aleatória se algo der errado
            int idx = random.nextInt(zonasOrdenadas.size());
            return encontrarZonaPorNome(zonasOrdenadas.get(idx).getZona().getNome(), todasZonas);
        }

        // Gera um número aleatório entre 0 e a soma total
        double r = random.nextDouble() * somaScores;
        double countScore = 0;

        // Caminha pela lista até encontrar a zona correspondente
        for (ScoreZona sz : zonasOrdenadas) {
            double scoreNormalizado = sz.getScore() - minScore + 1.0;
            countScore += scoreNormalizado;

            if (countScore >= r) {
                return encontrarZonaPorNome(sz.getZona().getNome(), todasZonas);
            }
        }

        // Fallback para a primeira zona
        return encontrarZonaPorNome(zonasOrdenadas.get(0).getZona().getNome(), todasZonas);
    }

    /**
     * Encontra uma zona pelo nome na lista de todas as zonas.
     * @param nomeZona Nome da zona a ser encontrada.
     * @param zonas Lista de todas as zonas.
     * @return A zona encontrada, ou null se não existir.
     */
    private ZonaUrbana encontrarZonaPorNome(String nomeZona, Lista<ZonaUrbana> zonas) {
        for (int i = 0; i < zonas.tamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            if (zona.getNome().equals(nomeZona)) {
                return zona;
            }
        }
        return null;
    }

    /**
     * Calcula os scores de todas as zonas.
     * Este método deve ser chamado antes de distribuir os caminhões.
     */
    private void calcularScoresZonas() {
        for (ScoreZona score : scoresZonas.values()) {
            score.calcularScore();
        }

        // Log para debug - mostra os scores calculados
        System.out.println("--- Scores calculados para distribuição ---");
        for (ScoreZona score : scoresZonas.values()) {
            System.out.printf("Zona %-10s: Lixo=%5d kg, CPs=%2d, Tempo=%4d min, Score=%8.2f%n",
                    score.getZona().getNome(), score.getZona().getLixoAcumulado(),
                    score.getCaminhoesAtivos(), score.getTempoDesdeUltimaColeta(),
                    score.getScore());
        }
        System.out.println("----------------------------------------");
    }

    /**
     * Incrementa o tempo desde a última coleta para todas as zonas.
     * Este método deve ser chamado a cada intervalo de tempo simulado.
     * @param minutosPassados Minutos que passaram desde a última atualização.
     */
    public void incrementarTempoSemColeta(int minutosPassados) {
        for (ScoreZona score : scoresZonas.values()) {
            score.incrementarTempo(minutosPassados);
        }
    }

    /**
     * Registra uma coleta realizada em uma zona, resetando o contador de tempo.
     * @param nomeZona Nome da zona onde a coleta foi realizada.
     */
    public void registrarColetaEmZona(String nomeZona) {
        if (scoresZonas.containsKey(nomeZona)) {
            scoresZonas.get(nomeZona).registrarColeta();
            System.out.println("Registrada coleta na zona " + nomeZona + ", tempo desde última coleta resetado.");
        }
    }

    /**
     * Obtém o score atual de uma zona.
     * @param nomeZona Nome da zona.
     * @return O score atual, ou 0 se a zona não for encontrada.
     */
    public double getScoreZona(String nomeZona) {
        if (scoresZonas.containsKey(nomeZona)) {
            return scoresZonas.get(nomeZona).getScore();
        }
        return 0;
    }

    /**
     * Recalcula todos os scores de zonas quando necessário.
     */
    public void recalcularTodosScores() {
        calcularScoresZonas();
    }

    /**
     * Retorna o MapaUrbano associado a esta distribuição.
     * @return O mapa urbano.
     */
    public MapaUrbano getMapaUrbano() {
        return mapaUrbano;
    }
}