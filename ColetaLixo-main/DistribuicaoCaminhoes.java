import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.StatusCaminhao;
import zonas.MapaUrbano;
import zonas.ZonaUrbana;
import zonas.ScoreZona;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Comparator;

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
        Lista<CaminhaoPequeno> caminhoesOciosos = new Lista<>();

        // Coleta todos os caminhões ociosos
        for (int i = 0; i < caminhoes.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoes.obter(i);
            if (caminhao.getStatus() == StatusCaminhao.OCIOSO) {
                caminhoesOciosos.adicionar(caminhao);
            }
        }

        if (caminhoesOciosos.estaVazia()) {
            return 0;
        }

        int distribuidos = 0;

        // Se devemos garantir distribuição mínima, primeiro atribuir caminhões para zonas desatendidas
        if (garantirDistribuicaoMinima) {
            distribuidos = garantirCoberturaMinima(caminhoesOciosos, zonas);
        }

        // Depois de garantir a cobertura mínima, distribui os restantes com base em scores
        if (!caminhoesOciosos.estaVazia()) {
            // Calcula os scores para balancear distribuição
            calcularScoresComReequilibrio();

            // Ordena as zonas por score (do maior para o menor)
            Lista<ScoreZona> zonasOrdenadas = new Lista<>();
            for (ScoreZona score : scoresZonas.values()) {
                zonasOrdenadas.adicionar(score);
            }

            // Ordena manualmente a lista por score (do maior para o menor)
            ordenarZonasPorScore(zonasOrdenadas);

            // Aplica um fator de balanceamento para evitar concentração excessiva
            aplicarFatorBalanceamento(zonasOrdenadas);

            distribuidos += distribuirCaminhoesComBalanceamento(caminhoesOciosos, zonas, zonasOrdenadas);
        }

        // Mensagem log detalhada sobre distribuição
        int totalZonas = zonas.tamanho();
        int totalCobertas = 0;
        for (ScoreZona score : scoresZonas.values()) {
            if (score.getCaminhoesAtivos() > 0) {
                totalCobertas++;
            }
        }

        System.out.println("DISTRIBUIÇÃO: " + distribuidos + " caminhões distribuídos. "
                + totalCobertas + " de " + totalZonas + " zonas cobertas.");

        return distribuidos;
    }

    /**
     * Ordena manualmente uma lista de ScoreZona por score (do maior para o menor).
     * Implementação do bubble sort já que não podemos usar estruturas prontas.
     * @param lista A lista a ser ordenada.
     */
    private void ordenarZonasPorScore(Lista<ScoreZona> lista) {
        int n = lista.tamanho();
        boolean trocou;

        for (int i = 0; i < n - 1; i++) {
            trocou = false;
            for (int j = 0; j < n - i - 1; j++) {
                ScoreZona atual = lista.obter(j);
                ScoreZona proximo = lista.obter(j + 1);

                if (atual.getScore() < proximo.getScore()) {
                    // Troca os elementos usando uma lista temporária
                    Lista<ScoreZona> temp = new Lista<>();

                    // Salva todos os elementos antes de j
                    for (int k = 0; k < j; k++) {
                        temp.adicionar(lista.obter(k));
                    }

                    // Adiciona elementos trocados
                    temp.adicionar(proximo);
                    temp.adicionar(atual);

                    // Adiciona o resto
                    for (int k = j + 2; k < n; k++) {
                        temp.adicionar(lista.obter(k));
                    }

                    // Copia de volta para a lista original
                    lista = temp;
                    trocou = true;
                }
            }

            // Se nenhuma troca ocorreu nesta passagem, a lista já está ordenada
            if (!trocou) {
                break;
            }
        }
    }

    /**
     * Garante uma cobertura mínima de caminhões para todas as zonas.
     * @param caminhoesOciosos Lista de caminhões ociosos disponíveis.
     * @param zonas Lista de zonas urbanas.
     * @return Número de caminhões distribuídos.
     */
    private int garantirCoberturaMinima(Lista<CaminhaoPequeno> caminhoesOciosos, Lista<ZonaUrbana> zonas) {
        int distribuidos = 0;
        boolean[] zonaCoberta = new boolean[zonas.tamanho()];

        // Caso especial: número de caminhões menor que zonas
        int totalCaminhoes = caminhoesOciosos.tamanho();
        int totalZonas = zonas.tamanho();

        // Se não temos caminhões suficientes para cobrir todas as zonas
        if (totalCaminhoes < totalZonas) {
            System.out.println("ATENÇÃO: Temos apenas " + totalCaminhoes +
                    " caminhões para " + totalZonas + " zonas. Priorizando zonas com mais lixo.");

            // Criar uma cópia das zonas que podemos ordenar
            Lista<ZonaUrbana> zonasCopiadas = new Lista<>();
            for (int i = 0; i < zonas.tamanho(); i++) {
                zonasCopiadas.adicionar(zonas.obter(i));
            }

            // Ordenar as zonas pelo lixo acumulado (maior para menor)
            // Usando um algoritmo de ordenação simples (bubble sort)
            for (int i = 0; i < zonasCopiadas.tamanho() - 1; i++) {
                for (int j = 0; j < zonasCopiadas.tamanho() - i - 1; j++) {
                    ZonaUrbana zona1 = zonasCopiadas.obter(j);
                    ZonaUrbana zona2 = zonasCopiadas.obter(j + 1);

                    if (zona1.getLixoAcumulado() < zona2.getLixoAcumulado()) {
                        // Troca as zonas
                        Lista<ZonaUrbana> temp = new Lista<>();
                        for (int k = 0; k < j; k++) {
                            temp.adicionar(zonasCopiadas.obter(k));
                        }
                        temp.adicionar(zona2);
                        temp.adicionar(zona1);
                        for (int k = j + 2; k < zonasCopiadas.tamanho(); k++) {
                            temp.adicionar(zonasCopiadas.obter(k));
                        }
                        zonasCopiadas = temp;
                    }
                }
            }

            // Distribuir caminhões para as zonas com mais lixo primeiro
            for (int i = 0; i < totalCaminhoes && i < zonasCopiadas.tamanho(); i++) {
                ZonaUrbana zona = zonasCopiadas.obter(i);

                // Encontrar índice desta zona na lista original
                int indiceOriginal = -1;
                for (int j = 0; j < zonas.tamanho(); j++) {
                    if (zonas.obter(j).getNome().equals(zona.getNome())) {
                        indiceOriginal = j;
                        break;
                    }
                }

                if (indiceOriginal >= 0) {
                    // Distribuir um caminhão para esta zona prioritária
                    CaminhaoPequeno caminhao = caminhoesOciosos.obter(0);
                    caminhoesOciosos.remover(0);
                    enviarCaminhaoParaZona(caminhao, zona);
                    distribuidos++;
                    scoresZonas.get(zona.getNome()).incrementarCaminhoesAtivos();
                    zonaCoberta[indiceOriginal] = true;
                }
            }

            return distribuidos;
        }

        // Abordagem normal quando temos caminhões suficientes para pelo menos um por zona

        // Identificar zonas que já têm cobertura mínima
        for (int i = 0; i < zonas.tamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            ScoreZona score = scoresZonas.get(zona.getNome());
            zonaCoberta[i] = (score.getCaminhoesAtivos() >= caminhoesPorZonaMinimo);
        }

        // Distribuir caminhões para zonas não cobertas
        for (int i = 0; i < zonas.tamanho() && !caminhoesOciosos.estaVazia(); i++) {
            if (!zonaCoberta[i]) {
                ZonaUrbana zona = zonas.obter(i);
                ScoreZona score = scoresZonas.get(zona.getNome());

                int caminhoesFaltantes = caminhoesPorZonaMinimo - score.getCaminhoesAtivos();
                for (int j = 0; j < caminhoesFaltantes && !caminhoesOciosos.estaVazia(); j++) {
                    CaminhaoPequeno caminhao = caminhoesOciosos.obter(0);
                    caminhoesOciosos.remover(0);
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
    private int distribuirCaminhoesComBalanceamento(Lista<CaminhaoPequeno> caminhoesOciosos,
                                                    Lista<ZonaUrbana> todasZonas,
                                                    Lista<ScoreZona> zonasOrdenadas) {
        int distribuidos = 0;

        // Caso especial para poucos recursos
        boolean poucosRecursos = caminhoesOciosos.tamanho() < todasZonas.tamanho();
        if (poucosRecursos) {
            // Em situação de escassez, priorizar zonas com muito lixo
            for (int i = 0; i < zonasOrdenadas.tamanho(); i++) {
                ScoreZona scoreZona = zonasOrdenadas.obter(i);
                int lixoAcumulado = scoreZona.getZona().getLixoAcumulado();
                // Multiplicar o score por um fator baseado no lixo acumulado
                double fatorLixo = Math.log10(Math.max(10, lixoAcumulado)) * 2.0;
                scoreZona.setScoreFinal(scoreZona.getScore() * fatorLixo);
            }

            // Reordenar baseado no score ajustado para situação de escassez
            ordenarZonasPorScoreFinal(zonasOrdenadas);
        }

        // Cria uma cópia dos caminhões para trabalhar
        Lista<CaminhaoPequeno> caminhoes = new Lista<>();
        for (int i = 0; i < caminhoesOciosos.tamanho(); i++) {
            caminhoes.adicionar(caminhoesOciosos.obter(i));
        }

        // MODIFICAÇÃO: Primeira fase - distribuir caminhões iniciais com base nos scores
        // Limitamos a quantidade na primeira distribuição para permitir uma reserva para a segunda fase
        int caminhoesPrimeiraCota = Math.min(caminhoes.tamanho(), todasZonas.tamanho() * 3);
        int caminhoesPorZonaBasico = Math.max(1, caminhoesPrimeiraCota / todasZonas.tamanho());

        for (int z = 0; z < zonasOrdenadas.tamanho() && !caminhoes.estaVazia(); z++) {
            ScoreZona scoreZona = zonasOrdenadas.obter(z);
            ZonaUrbana zona = scoreZona.getZona();

            for (int i = 0; i < caminhoesPorZonaBasico && !caminhoes.estaVazia(); i++) {
                // Encontra o caminhão mais próximo ou o melhor candidato
                CaminhaoPequeno melhorCaminhao = encontrarCaminhaoMaisProximo(caminhoes, zona);

                if (melhorCaminhao != null) {
                    // Remove o caminhão das listas
                    for (int j = 0; j < caminhoes.tamanho(); j++) {
                        if (caminhoes.obter(j).getPlaca().equals(melhorCaminhao.getPlaca())) {
                            caminhoes.remover(j);
                            break;
                        }
                    }

                    for (int j = 0; j < caminhoesOciosos.tamanho(); j++) {
                        if (caminhoesOciosos.obter(j).getPlaca().equals(melhorCaminhao.getPlaca())) {
                            caminhoesOciosos.remover(j);
                            break;
                        }
                    }

                    enviarCaminhaoParaZona(melhorCaminhao, zona);
                    distribuidos++;
                    scoreZona.incrementarCaminhoesAtivos();
                }
            }
        }

        // MODIFICAÇÃO: Segunda fase - distribuir TODOS os caminhões restantes de forma circular
        // Isso garante que TODOS os caminhões sejam utilizados
        if (!caminhoes.estaVazia()) {
            int indiceZona = 0;

            System.out.println("INFO: Distribuindo " + caminhoes.tamanho() +
                    " caminhões restantes de forma circular entre as zonas.");

            while (!caminhoes.estaVazia()) {
                ZonaUrbana zona = todasZonas.obter(indiceZona);
                CaminhaoPequeno caminhao = caminhoes.obter(0);
                caminhoes.remover(0);

                for (int j = 0; j < caminhoesOciosos.tamanho(); j++) {
                    if (caminhoesOciosos.obter(j).getPlaca().equals(caminhao.getPlaca())) {
                        caminhoesOciosos.remover(j);
                        break;
                    }
                }

                enviarCaminhaoParaZona(caminhao, zona);
                distribuidos++;

                // Atualiza o score da zona para manter o registro consistente
                for (int i = 0; i < zonasOrdenadas.tamanho(); i++) {
                    if (zonasOrdenadas.obter(i).getZona().getNome().equals(zona.getNome())) {
                        zonasOrdenadas.obter(i).incrementarCaminhoesAtivos();
                        break;
                    }
                }

                // Avança para a próxima zona (distribuição circular)
                indiceZona = (indiceZona + 1) % todasZonas.tamanho();
            }
        }

        return distribuidos;
    }

    /**
     * Ordena manualmente uma lista de ScoreZona por scoreFinal (do maior para o menor).
     * @param lista A lista a ser ordenada.
     */
    private void ordenarZonasPorScoreFinal(Lista<ScoreZona> lista) {
        int n = lista.tamanho();

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                ScoreZona atual = lista.obter(j);
                ScoreZona proximo = lista.obter(j + 1);

                if (atual.getScoreFinal() < proximo.getScoreFinal()) {
                    // Troca os elementos usando uma lista temporária
                    Lista<ScoreZona> temp = new Lista<>();

                    // Salva todos os elementos antes de j
                    for (int k = 0; k < j; k++) {
                        temp.adicionar(lista.obter(k));
                    }

                    // Adiciona elementos trocados
                    temp.adicionar(proximo);
                    temp.adicionar(atual);

                    // Adiciona o resto
                    for (int k = j + 2; k < n; k++) {
                        temp.adicionar(lista.obter(k));
                    }

                    // Copia de volta para a lista original
                    for (int k = 0; k < temp.tamanho(); k++) {
                        if (k < lista.tamanho()) {
                            // Substituir o elemento existente
                            lista.remover(k);
                            lista.adicionar(temp.obter(k));
                        } else {
                            // Adicionar novo elemento
                            lista.adicionar(temp.obter(k));
                        }
                    }
                }
            }
        }
    }

    /**
     * Encontra o caminhão mais próximo ou melhor candidato para uma zona.
     * @param caminhoes Lista de caminhões disponíveis.
     * @param zona Zona de destino.
     * @return O caminhão mais adequado.
     */
    private CaminhaoPequeno encontrarCaminhaoMaisProximo(Lista<CaminhaoPequeno> caminhoes, ZonaUrbana zona) {
        if (caminhoes.estaVazia()) {
            return null;
        }

        CaminhaoPequeno melhorCaminhao = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (int i = 0; i < caminhoes.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoes.obter(i);
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
        return melhorCaminhao != null ? melhorCaminhao : caminhoes.obter(0);
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
    private void aplicarFatorBalanceamento(Lista<ScoreZona> zonasOrdenadas) {
        // Se tivermos pelo menos duas zonas, ajusta scores para não ter diferença excessiva
        if (zonasOrdenadas.tamanho() >= 2) {
            double scoreMaximo = zonasOrdenadas.obter(0).getScore();
            double scoreMinimo = zonasOrdenadas.obter(zonasOrdenadas.tamanho() - 1).getScore();

            // Se a diferença for muito grande, aproxima valores
            // Aumentamos o limiar para 8x (era 3x)
            if (scoreMaximo > 8 * scoreMinimo) {
                for (int i = 0; i < zonasOrdenadas.tamanho(); i++) {
                    ScoreZona score = zonasOrdenadas.obter(i);
                    // Modificação na fórmula: mudamos de 0.7/0.3 para 0.9/0.1
                    // para reduzir menos o score de zonas prioritárias
                    double novoScore = (score.getScore() * 0.9) + (scoreMinimo * 0.1);
                    score.setScoreFinal(novoScore);
                }

                // Reordena com scores ajustados
                ordenarZonasPorScoreFinal(zonasOrdenadas);
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
                // Reduzimos para 20% (era 30%)
                fatorModeracao = 1.0 - (0.2 * posicaoRelativa);
            }

            // Aplica fator de moderação e considera caminhões já alocados
            double scoreAjustado = scoreBruto * fatorModeracao;

            // Aplica penalização adicional para zonas que já têm muitos caminhões
            int caminhoesAtivos = score.getCaminhoesAtivos();
            // Reduzida a penalização por caminhões já alocados
            double penalizacaoCaminhoes = Math.pow(1.1, caminhoesAtivos) - 1.0; // Era 1.2

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
