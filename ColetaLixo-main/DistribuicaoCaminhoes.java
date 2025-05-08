import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.StatusCaminhao;
import zonas.MapaUrbano;
import zonas.ZonaUrbana;
import zonas.ScoreZona;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Classe responsável por implementar algoritmos de distribuição inteligente
 * de caminhões pequenos entre as zonas urbanas.
 * Versão melhorada para resolver problemas de distribuição desigual.
 */
public class DistribuicaoCaminhoes implements Serializable {
    private static final long serialVersionUID = 1L;

    private MapaUrbano mapaUrbano;
    private Map<String, ScoreZona> scoresZonas;
    private Random random;
    private boolean garantirDistribuicaoMinima;
    private int caminhoesPorZonaMinimo; // Mínimo de caminhões por zona
    private GaragemCentral garagemCentral; // Referência para a garagem central

    /**
     * Construtor da classe DistribuicaoCaminhoes.
     * @param mapaUrbano Mapa urbano com as distâncias entre zonas.
     * @param zonas Lista de zonas urbanas.
     */
    public DistribuicaoCaminhoes(MapaUrbano mapaUrbano, Lista<ZonaUrbana> zonas) {
        this.mapaUrbano = mapaUrbano;
        this.random = new Random();
        this.scoresZonas = new HashMap<>();
        this.garantirDistribuicaoMinima = true; // Por padrão, garantir distribuição mínima
        this.caminhoesPorZonaMinimo = 1; // Por padrão, pelo menos 1 caminhão por zona

        // Inicializa os scores para todas as zonas
        if (zonas != null) {
            for (int i = 0; i < zonas.tamanho(); i++) {
                ZonaUrbana zona = zonas.obter(i);
                scoresZonas.put(zona.getNome(), new ScoreZona(zona));
            }
        }
    }

    /**
     * Define a referência para a garagem central.
     * @param garagemCentral A garagem central a ser usada.
     */
    public void setGaragemCentral(GaragemCentral garagemCentral) {
        this.garagemCentral = garagemCentral;
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

        int distribuidos = 0;

        // Se devemos garantir distribuição mínima, primeiro atribuir caminhões para zonas desatendidas
        if (garantirDistribuicaoMinima) {
            distribuidos = garantirCoberturaMinima(caminhoesOciosos, zonas);
        }

        // Depois de garantir a cobertura mínima, distribui os restantes com base em scores
        if (!caminhoesOciosos.isEmpty()) {
            // Calcula os scores para balancear distribuição
            calcularScoresComReequilibrio();

            // Ordena as zonas por score (do maior para o menor)
            List<ScoreZona> zonasOrdenadas = new ArrayList<>(scoresZonas.values());
            zonasOrdenadas.sort(Comparator.comparingDouble(ScoreZona::getScore).reversed());

            // Aplica um fator de balanceamento para evitar concentração excessiva
            aplicarFatorBalanceamento(zonasOrdenadas);

            distribuidos += distribuirCaminhoesComBalanceamento(caminhoesOciosos, zonas, zonasOrdenadas);
        }

        return distribuidos;
    }

    /**
     * Garante uma cobertura mínima de caminhões para todas as zonas.
     * @param caminhoesOciosos Lista de caminhões ociosos disponíveis.
     * @param zonas Lista de zonas urbanas.
     * @return Número de caminhões distribuídos.
     */
    private int garantirCoberturaMinima(List<CaminhaoPequeno> caminhoesOciosos, Lista<ZonaUrbana> zonas) {
        int distribuidos = 0;
        boolean[] zonaCoberta = new boolean[zonas.tamanho()];

        // Identificar zonas que já têm cobertura mínima
        for (int i = 0; i < zonas.tamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            ScoreZona score = scoresZonas.get(zona.getNome());
            zonaCoberta[i] = (score.getCaminhoesAtivos() >= caminhoesPorZonaMinimo);
        }

        // Distribuir caminhões para zonas não cobertas
        for (int i = 0; i < zonas.tamanho() && !caminhoesOciosos.isEmpty(); i++) {
            if (!zonaCoberta[i]) {
                ZonaUrbana zona = zonas.obter(i);
                ScoreZona score = scoresZonas.get(zona.getNome());

                int caminhoesFaltantes = caminhoesPorZonaMinimo - score.getCaminhoesAtivos();
                for (int j = 0; j < caminhoesFaltantes && !caminhoesOciosos.isEmpty(); j++) {
                    CaminhaoPequeno caminhao = caminhoesOciosos.remove(0);
                    enviarCaminhaoParaZona(caminhao, zona);
                    distribuidos++;
                    score.incrementarCaminhoesAtivos();
                }

                // Atualiza se esta zona agora tem cobertura mínima
                zonaCoberta[i] = (score.getCaminhoesAtivos() >= caminhoesPorZonaMinimo);
            }
        }

        return distribuidos;
    }

    /**
     * Distribui os caminhões restantes com base nos scores balanceados.
     * @param caminhoesOciosos Lista de caminhões ociosos disponíveis.
     * @param todasZonas Lista de todas as zonas.
     * @param zonasOrdenadas Lista de zonas ordenadas por score.
     * @return Número de caminhões distribuídos.
     */
    private int distribuirCaminhoesComBalanceamento(List<CaminhaoPequeno> caminhoesOciosos,
                                                    Lista<ZonaUrbana> todasZonas,
                                                    List<ScoreZona> zonasOrdenadas) {
        int distribuidos = 0;

        // Cria uma cópia dos caminhões para trabalhar
        List<CaminhaoPequeno> caminhoes = new ArrayList<>(caminhoesOciosos);

        // Limite proporcional de caminhões por zona para evitar concentração excessiva
        int maxProporcional = Math.max(2, caminhoes.size() / todasZonas.tamanho());

        // Primeira iteração: distribuir caminhões com base na proximidade e score
        for (ScoreZona scoreZona : zonasOrdenadas) {
            // Pula zonas que já têm caminhões suficientes
            if (scoreZona.getCaminhoesAtivos() >= maxProporcional) {
                continue;
            }

            // Quantos caminhões podemos ainda enviar para esta zona
            int disponivelParaZona = maxProporcional - scoreZona.getCaminhoesAtivos();
            ZonaUrbana zona = scoreZona.getZona();

            for (int i = 0; i < disponivelParaZona && !caminhoes.isEmpty(); i++) {
                // Encontra o caminhão mais próximo ou o melhor candidato
                CaminhaoPequeno melhorCaminhao = encontrarCaminhaoMaisProximo(caminhoes, zona);

                if (melhorCaminhao != null) {
                    caminhoes.remove(melhorCaminhao);
                    caminhoesOciosos.remove(melhorCaminhao);

                    enviarCaminhaoParaZona(melhorCaminhao, zona);
                    distribuidos++;
                    scoreZona.incrementarCaminhoesAtivos();
                }
            }
        }

        // Segunda iteração: distribuir caminhões restantes proporcionalmente
        if (!caminhoes.isEmpty()) {
            // Recalcula os scores para esta iteração
            calcularScoresComReequilibrio();

            double totalScores = 0;
            for (ScoreZona score : zonasOrdenadas) {
                totalScores += Math.max(0.1, score.getScore());
            }

            for (ScoreZona scoreZona : zonasOrdenadas) {
                double proporcao = Math.max(0.1, scoreZona.getScore()) / totalScores;
                int caminhoesProporcional = (int)Math.ceil(proporcao * caminhoes.size());
                ZonaUrbana zona = scoreZona.getZona();

                for (int i = 0; i < caminhoesProporcional && !caminhoes.isEmpty(); i++) {
                    CaminhaoPequeno caminhao = caminhoes.remove(0);
                    caminhoesOciosos.remove(caminhao);

                    enviarCaminhaoParaZona(caminhao, zona);
                    distribuidos++;
                    scoreZona.incrementarCaminhoesAtivos();
                }
            }
        }

        return distribuidos;
    }

    /**
     * Encontra o caminhão mais próximo ou melhor candidato para uma zona.
     * @param caminhoes Lista de caminhões disponíveis.
     * @param zona Zona de destino.
     * @return O caminhão mais adequado.
     */
    private CaminhaoPequeno encontrarCaminhaoMaisProximo(List<CaminhaoPequeno> caminhoes, ZonaUrbana zona) {
        if (caminhoes.isEmpty()) {
            return null;
        }

        CaminhaoPequeno melhorCaminhao = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (CaminhaoPequeno caminhao : caminhoes) {
            ZonaUrbana zonaOrigem = caminhao.getZonaDeOrigemParaRetorno();

            // Se o caminhão tem uma zona de origem, calcula a distância
            if (zonaOrigem != null && mapaUrbano != null) {
                int distancia = mapaUrbano.getDistancia(zonaOrigem.getNome(), zona.getNome());

                if (distancia >= 0 && distancia < menorDistancia) {
                    menorDistancia = distancia;
                    melhorCaminhao = caminhao;
                }
            }
        }

        // Se não encontrou nenhum caminhão com zona de origem, escolhe o primeiro
        return melhorCaminhao != null ? melhorCaminhao : caminhoes.get(0);
    }

    /**
     * Envia um caminhão para uma zona específica.
     * @param caminhao O caminhão a ser enviado.
     * @param zona A zona de destino.
     */
    private void enviarCaminhaoParaZona(CaminhaoPequeno caminhao, ZonaUrbana zona) {
        caminhao.definirDestino(zona);
        caminhao.setStatus(StatusCaminhao.COLETANDO);

        // Registra na garagem central, se fornecida
        if (garagemCentral != null) {
            garagemCentral.registrarCaminhaoEmZona(caminhao, zona);
        }

        System.out.println("Caminhão " + caminhao.getPlaca() + " enviado para zona " +
                zona.getNome() + " (Score: " + scoresZonas.get(zona.getNome()).getScore() + ")");
    }

    /**
     * Aplica um fator de balanceamento para evitar concentração excessiva em zonas.
     * @param zonasOrdenadas Lista ordenada de zonas por score.
     */
    private void aplicarFatorBalanceamento(List<ScoreZona> zonasOrdenadas) {
        // Se tivermos pelo menos duas zonas, ajusta scores para não ter diferença excessiva
        if (zonasOrdenadas.size() >= 2) {
            double scoreMaximo = zonasOrdenadas.get(0).getScore();
            double scoreMinimo = zonasOrdenadas.get(zonasOrdenadas.size() - 1).getScore();

            // Se a diferença for muito grande, aproxima valores
            if (scoreMaximo > 3 * scoreMinimo) {
                for (ScoreZona score : zonasOrdenadas) {
                    // Aplica fator de balanceamento
                    double novoScore = (score.getScore() * 0.7) + (scoreMinimo * 0.3);
                    score.setScoreFinal(novoScore);
                }

                // Reordena com scores ajustados
                zonasOrdenadas.sort(Comparator.comparingDouble(ScoreZona::getScoreFinal).reversed());
            }
        }
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
     * Calcula os scores das zonas com fator de reequilíbrio para evitar priorização excessiva.
     */
    private void calcularScoresComReequilibrio() {
        // Primeiro calcula scores básicos
        for (ScoreZona score : scoresZonas.values()) {
            score.calcularScore();
        }

        // Encontra score máximo e mínimo
        double scoreMaximo = Double.MIN_VALUE;
        double scoreMinimo = Double.MAX_VALUE;

        for (ScoreZona score : scoresZonas.values()) {
            scoreMaximo = Math.max(scoreMaximo, score.getScore());
            scoreMinimo = Math.min(scoreMinimo, score.getScore());
        }

        // Aplica fator de reequilíbrio para moderar extremos
        for (ScoreZona score : scoresZonas.values()) {
            double scoreBruto = score.getScore();

            // Quanto mais alta a prioridade, mais é reduzida (suavização logarítmica)
            double fatorModeracao = 1.0;
            if (scoreMaximo > scoreMinimo) {
                double posicaoRelativa = (scoreBruto - scoreMinimo) / (scoreMaximo - scoreMinimo);
                fatorModeracao = 1.0 - (0.3 * posicaoRelativa);  // Reduz até 30% com base na posição
            }

            // Aplica fator de moderação e considera caminhões já alocados
            double scoreAjustado = scoreBruto * fatorModeracao;

            // Aplica penalização adicional para zonas que já têm muitos caminhões
            int caminhoesAtivos = score.getCaminhoesAtivos();
            double penalizacaoCaminhoes = Math.pow(1.2, caminhoesAtivos) - 1.0;

            // O score final considera penalização por caminhões já alocados
            double scoreFinal = Math.max(0.1, scoreAjustado - penalizacaoCaminhoes);
            score.setScoreFinal(scoreFinal);
        }

        // Log para debug - mostra os scores calculados
        System.out.println("--- Scores reequilibrados para distribuição ---");
        for (ScoreZona score : scoresZonas.values()) {
            System.out.printf("Zona %-10s: Lixo=%5d kg, CPs=%2d, Tempo=%4d min, Score=%8.2f, Score Final=%8.2f%n",
                    score.getZona().getNome(), score.getZona().getLixoAcumulado(),
                    score.getCaminhoesAtivos(), score.getTempoDesdeUltimaColeta(),
                    score.getScore(), score.getScoreFinal());
        }
        System.out.println("----------------------------------------");
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
     * Obtém o score atual de uma zona.
     * @param nomeZona Nome da zona.
     * @return O score atual, ou 0 se a zona não for encontrada.
     */
    public double getScoreZona(String nomeZona) {
        if (scoresZonas.containsKey(nomeZona)) {
            return scoresZonas.get(nomeZona).getScoreFinal();
        }
        return 0;
    }

    /**
     * Define se a distribuição deve garantir uma cobertura mínima a todas as zonas.
     * @param garantir Verdadeiro para garantir a cobertura mínima.
     */
    public void setGarantirDistribuicaoMinima(boolean garantir) {
        this.garantirDistribuicaoMinima = garantir;
    }

    /**
     * Define o número mínimo de caminhões por zona para a política de cobertura mínima.
     * @param minimo Número mínimo de caminhões por zona (pelo menos 1).
     */
    public void setCaminhoesPorZonaMinimo(int minimo) {
        this.caminhoesPorZonaMinimo = Math.max(1, minimo);
    }

    /**
     * Retorna o MapaUrbano associado a esta distribuição.
     * @return O mapa urbano.
     */
    public MapaUrbano getMapaUrbano() {
        return mapaUrbano;
    }
}