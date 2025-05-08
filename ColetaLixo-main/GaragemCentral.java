import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.StatusCaminhao;
import zonas.MapaUrbano;
import zonas.ZonaUrbana;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa a garagem central de onde partem os caminhões pequenos
 * e para onde retornam ao final do dia ou quando não estão em serviço.
 * Permite uma distribuição mais equitativa dos caminhões entre as zonas.
 */
public class GaragemCentral implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private Lista<CaminhaoPequeno> caminhoesEstacionados;
    private MapaUrbano mapaUrbano;
    private Map<String, Lista<CaminhaoPequeno>> caminhoesPorZona; // Rastreia caminhões por zona
    private DistribuicaoCaminhoes distribuidor;
    private int limiteViagensDiariasPadrao;

    /**
     * Construtor da GaragemCentral.
     * @param nome Nome da garagem.
     * @param mapaUrbano Mapa urbano com as distâncias entre zonas.
     */
    public GaragemCentral(String nome, MapaUrbano mapaUrbano) {
        this.nome = nome;
        this.caminhoesEstacionados = new Lista<>();
        this.mapaUrbano = mapaUrbano;
        this.caminhoesPorZona = new HashMap<>();
        this.limiteViagensDiariasPadrao = 10; // Valor padrão, agora configurável
    }

    /**
     * Configura o distribuidor de caminhões.
     * @param distribuidor O distribuidor a ser usado.
     */
    public void setDistribuidor(DistribuicaoCaminhoes distribuidor) {
        this.distribuidor = distribuidor;
        if (this.distribuidor != null) {
            this.distribuidor.setGaragemCentral(this);
        }
    }

    /**
     * Adiciona um caminhão à garagem.
     * @param caminhao Caminhão a ser adicionado.
     */
    public void adicionarCaminhao(CaminhaoPequeno caminhao) {
        if (caminhao != null) {
            caminhao.setStatus(StatusCaminhao.OCIOSO);
            caminhoesEstacionados.adicionar(caminhao);
            System.out.println("Garagem " + nome + ": CP " + caminhao.getPlaca() + " estacionado. Total: " + caminhoesEstacionados.tamanho());
        }
    }

    /**
     * Registra um caminhão como ativo em uma zona específica.
     * Usado para rastrear onde cada caminhão está operando.
     * @param caminhao Caminhão a ser registrado.
     * @param zona Zona onde o caminhão está operando.
     */
    public void registrarCaminhaoEmZona(CaminhaoPequeno caminhao, ZonaUrbana zona) {
        if (caminhao != null && zona != null) {
            String nomeZona = zona.getNome();

            // Inicializa a lista de caminhões para a zona se não existir
            if (!caminhoesPorZona.containsKey(nomeZona)) {
                caminhoesPorZona.put(nomeZona, new Lista<>());
            }

            // Adiciona o caminhão à lista da zona
            caminhoesPorZona.get(nomeZona).adicionar(caminhao);

            System.out.println("Garagem " + nome + ": CP " + caminhao.getPlaca() +
                    " registrado na zona " + nomeZona);
        }
    }

    /**
     * Remove um caminhão da zona e o adiciona de volta à garagem.
     * @param caminhao Caminhão a retornar.
     * @param zona Zona de onde o caminhão está retornando.
     */
    public void retornarCaminhaoDeZona(CaminhaoPequeno caminhao, ZonaUrbana zona) {
        if (caminhao != null && zona != null) {
            String nomeZona = zona.getNome();

            // Remove o caminhão da zona, se estiver registrado
            if (caminhoesPorZona.containsKey(nomeZona)) {
                Lista<CaminhaoPequeno> caminhoesDaZona = caminhoesPorZona.get(nomeZona);

                for (int i = 0; i < caminhoesDaZona.tamanho(); i++) {
                    if (caminhoesDaZona.obter(i).getPlaca().equals(caminhao.getPlaca())) {
                        caminhoesDaZona.remover(i);
                        break;
                    }
                }
            }

            // Adiciona o caminhão à garagem
            caminhao.setStatus(StatusCaminhao.OCIOSO);
            caminhoesEstacionados.adicionar(caminhao);

            System.out.println("Garagem " + nome + ": CP " + caminhao.getPlaca() +
                    " retornou da zona " + nomeZona + ". Total estacionados: " +
                    caminhoesEstacionados.tamanho());
        }
    }

    /**
     * Distribui os caminhões para as zonas, garantindo que todas recebam pelo menos um se possível.
     * @param zonas Lista de zonas urbanas.
     * @param distribuidor Objeto DistribuicaoCaminhoes para cálculo de scores e priorização.
     * @return Número de caminhões distribuídos.
     */
    public int distribuirCaminhoesParaZonas(Lista<ZonaUrbana> zonas, DistribuicaoCaminhoes distribuidor) {
        if (caminhoesEstacionados.estaVazia() || zonas == null || zonas.estaVazia()) {
            return 0;
        }

        int distribuidos = 0;

        // Usa o distribuidor fornecido como parâmetro
        if (distribuidor != null) {
            // Configura temporariamente o distribuidor para usar esta garagem
            distribuidor.setGaragemCentral(this);
            distribuidos = distribuidor.distribuirCaminhoes(caminhoesEstacionados, zonas);

            // Limpa a lista de caminhões estacionados apenas dos que foram distribuídos
            Lista<CaminhaoPequeno> caminhoesNaoDistribuidos = new Lista<>();
            for (int i = 0; i < caminhoesEstacionados.tamanho(); i++) {
                CaminhaoPequeno cp = caminhoesEstacionados.obter(i);
                if (cp.getStatus() == StatusCaminhao.OCIOSO) {
                    caminhoesNaoDistribuidos.adicionar(cp);
                }
            }
            caminhoesEstacionados = caminhoesNaoDistribuidos;
        }
        // Se não foi fornecido distribuidor, usa o distribuidor da própria garagem
        else if (this.distribuidor != null) {
            distribuidos = this.distribuidor.distribuirCaminhoes(caminhoesEstacionados, zonas);

            // Limpa a lista de caminhões estacionados apenas dos que foram distribuídos
            Lista<CaminhaoPequeno> caminhoesNaoDistribuidos = new Lista<>();
            for (int i = 0; i < caminhoesEstacionados.tamanho(); i++) {
                CaminhaoPequeno cp = caminhoesEstacionados.obter(i);
                if (cp.getStatus() == StatusCaminhao.OCIOSO) {
                    caminhoesNaoDistribuidos.adicionar(cp);
                }
            }
            caminhoesEstacionados = caminhoesNaoDistribuidos;
        }
        // Fallback para distribuição básica se não há distribuidor
        else {
            distribuidos = distribuirCaminhoesBasico(zonas);
        }

        return distribuidos;
    }

    /**
     * Versão sobrecarregada que usa o distribuidor interno.
     * @param zonas Lista de zonas urbanas.
     * @return Número de caminhões distribuídos.
     */
    public int distribuirCaminhoesParaZonas(Lista<ZonaUrbana> zonas) {
        return distribuirCaminhoesParaZonas(zonas, null);
    }

    /**
     * Método de fallback para distribuição básica garantindo pelo menos um caminhão por zona.
     * @param zonas Lista de zonas urbanas.
     * @return Número de caminhões distribuídos.
     */
    private int distribuirCaminhoesBasico(Lista<ZonaUrbana> zonas) {
        int distribuidos = 0;

        // Primeira fase: garantir que cada zona tenha pelo menos um caminhão
        boolean[] zonaAtendida = new boolean[zonas.tamanho()];
        for (int i = 0; i < zonas.tamanho(); i++) {
            zonaAtendida[i] = false;
        }

        // Verificar quais zonas já têm caminhões
        for (int i = 0; i < zonas.tamanho(); i++) {
            ZonaUrbana zona = zonas.obter(i);
            if (zona == null) continue;

            String nomeZona = zona.getNome();
            if (caminhoesPorZona.containsKey(nomeZona) &&
                    !caminhoesPorZona.get(nomeZona).estaVazia()) {
                zonaAtendida[i] = true;
            }
        }

        // Distribuir um caminhão para cada zona que ainda não tem
        for (int i = 0; i < zonas.tamanho(); i++) {
            if (!zonaAtendida[i] && !caminhoesEstacionados.estaVazia()) {
                ZonaUrbana zona = zonas.obter(i);
                CaminhaoPequeno caminhao = caminhoesEstacionados.remover(0);
                caminhao.definirDestino(zona);
                caminhao.setStatus(StatusCaminhao.COLETANDO);
                registrarCaminhaoEmZona(caminhao, zona);
                distribuidos++;
                zonaAtendida[i] = true;
                System.out.println("Garagem " + nome + ": CP " + caminhao.getPlaca() +
                        " enviado para garantir atendimento da zona " + zona.getNome());
            }
        }

        // Segunda fase: distribuir os caminhões restantes com base em necessidade de lixo acumulado
        if (!caminhoesEstacionados.estaVazia()) {
            // Cria um array para armazenar as zonas ordenadas por lixo acumulado
            ZonaUrbana[] zonasOrdenadas = new ZonaUrbana[zonas.tamanho()];
            for (int i = 0; i < zonas.tamanho(); i++) {
                zonasOrdenadas[i] = zonas.obter(i);
            }

            // Ordena as zonas por lixo acumulado (da maior para a menor quantidade)
            for (int i = 0; i < zonasOrdenadas.length - 1; i++) {
                for (int j = i + 1; j < zonasOrdenadas.length; j++) {
                    if (zonasOrdenadas[i].getLixoAcumulado() < zonasOrdenadas[j].getLixoAcumulado()) {
                        ZonaUrbana temp = zonasOrdenadas[i];
                        zonasOrdenadas[i] = zonasOrdenadas[j];
                        zonasOrdenadas[j] = temp;
                    }
                }
            }

            // Distribui caminhões para as zonas com mais lixo acumulado
            int indiceZona = 0;
            while (!caminhoesEstacionados.estaVazia() && indiceZona < zonasOrdenadas.length) {
                ZonaUrbana zona = zonasOrdenadas[indiceZona];
                CaminhaoPequeno caminhao = caminhoesEstacionados.remover(0);
                caminhao.definirDestino(zona);
                caminhao.setStatus(StatusCaminhao.COLETANDO);
                registrarCaminhaoEmZona(caminhao, zona);
                distribuidos++;

                System.out.println("Garagem " + nome + ": CP " + caminhao.getPlaca() +
                        " enviado para zona " + zona.getNome() +
                        " com " + zona.getLixoAcumulado() + "kg de lixo acumulado");

                // Avança para a próxima zona (distribuição circular)
                indiceZona = (indiceZona + 1) % zonasOrdenadas.length;
            }
        }

        return distribuidos;
    }

    /**
     * Define um novo limite de viagens diárias para todos os caminhões na garagem.
     * @param limite Novo limite de viagens diárias.
     */
    public void setLimiteViagensDiarias(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("Limite de viagens diárias deve ser positivo");
        }

        this.limiteViagensDiariasPadrao = limite;

        // Atualiza todos os caminhões estacionados
        for (int i = 0; i < caminhoesEstacionados.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesEstacionados.obter(i);
            caminhao.setLimiteViagensDiarias(limite);
        }

        // Atualiza caminhões distribuídos em zonas
        for (String nomeZona : caminhoesPorZona.keySet()) {
            Lista<CaminhaoPequeno> caminhoesDaZona = caminhoesPorZona.get(nomeZona);
            for (int i = 0; i < caminhoesDaZona.tamanho(); i++) {
                caminhoesDaZona.obter(i).setLimiteViagensDiarias(limite);
            }
        }

        System.out.println("Garagem " + nome + ": Limite de viagens diárias atualizado para " + limite);
    }

    /**
     * Retorna um caminhão para a garagem ao final do dia ou quando ocioso.
     * @param caminhao Caminhão a retornar.
     * @param tempoViagem Tempo de viagem até a garagem.
     */
    public void retornarCaminhao(CaminhaoPequeno caminhao, int tempoViagem) {
        // Lógica para retornar o caminhão à garagem
        // Esta implementação seria mais completa com integração direta ao simulador
        System.out.println("Garagem " + nome + ": CP " + caminhao.getPlaca() +
                " retornando em " + tempoViagem + " minutos.");
    }

    /**
     * Obtém a quantidade de caminhões na garagem.
     * @return Número de caminhões estacionados.
     */
    public int getCaminhoesEstacionados() {
        return caminhoesEstacionados.tamanho();
    }

    /**
     * Retorna o nome da garagem.
     * @return Nome da garagem.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o mapa urbano associado à garagem.
     * @return O mapa urbano.
     */
    public MapaUrbano getMapaUrbano() {
        return mapaUrbano;
    }

    /**
     * Retorna o limite de viagens diárias padrão.
     * @return Limite de viagens diárias.
     */
    public int getLimiteViagensDiariasPadrao() {
        return limiteViagensDiariasPadrao;
    }

    /**
     * Aplica o limite de viagens diárias a um caminhão específico.
     * @param caminhao Caminhão a receber o limite.
     */
    public void aplicarLimiteViagensDiarias(CaminhaoPequeno caminhao) {
        if (caminhao != null) {
            caminhao.setLimiteViagensDiarias(limiteViagensDiariasPadrao);
        }
    }

    /**
     * Verifica se um caminhão atingiu seu limite de viagens e deve retornar à garagem.
     * @param caminhao Caminhão a verificar.
     * @return true se o caminhão atingiu o limite, false caso contrário.
     */
    public boolean verificarLimiteViagensAtingido(CaminhaoPequeno caminhao) {
        if (caminhao == null) return false;
        return caminhao.getViagensRealizadasHoje() >= caminhao.getLimiteViagensDiarias();
    }

    /**
     * Reinicia o contador de viagens diárias para todos os caminhões.
     * Chamado no início de um novo dia de simulação.
     */
    public void reiniciarViagensDiarias() {
        // Reinicia caminhões estacionados
        for (int i = 0; i < caminhoesEstacionados.tamanho(); i++) {
            caminhoesEstacionados.obter(i).reiniciarViagensDiarias();
        }

        // Reinicia caminhões nas zonas
        for (String nomeZona : caminhoesPorZona.keySet()) {
            Lista<CaminhaoPequeno> caminhoesDaZona = caminhoesPorZona.get(nomeZona);
            for (int i = 0; i < caminhoesDaZona.tamanho(); i++) {
                caminhoesDaZona.obter(i).reiniciarViagensDiarias();
            }
        }

        System.out.println("Garagem " + nome + ": Viagens diárias reiniciadas para todos os caminhões.");
    }

    /**
     * Retorna o distribuidor de caminhões associado à garagem.
     * @return O distribuidor de caminhões.
     */
    public DistribuicaoCaminhoes getDistribuidor() {
        return distribuidor;
    }
}