package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import Estruturas.Fila; // Importa a Fila customizada

/**
 * Implementação padrão de uma EstacaoTransferencia.
 * Utiliza uma Fila para gerenciar a chegada de CaminhoesPequenos e
 * interage com um CaminhaoGrande por vez para o transbordo do lixo.
 */
public class EstacaoPadrao extends EstacaoTransferencia {

    private Fila<CaminhaoPequeno> filaCaminhoesPequenos; // Fila para caminhões pequenos esperando
    private CaminhaoGrande caminhaoGrandeAtual; // Caminhão grande atualmente na estação (ou null)
    private int tempoEsperaCaminhaoGrandeAtual; // Contador de tempo que o caminhão grande está esperando
    private int tempoPrimeiroCaminhaoNaFila; // Contador de tempo que o PRIMEIRO caminhão pequeno está esperando (para alerta)


    /**
     * Construtor da EstacaoPadrao.
     * @param nome Nome da estação.
     * @param tempoMaximoEsperaPequenos Tempo máximo (em minutos) que um caminhão pequeno pode esperar
     * na fila antes de acionar a adição de um novo caminhão grande (lógica de alerta).
     * @throws IllegalArgumentException se tempoMaximoEsperaPequenos for menor que 1.
     */
    public EstacaoPadrao(String nome, int tempoMaximoEsperaPequenos) {
        super(nome, tempoMaximoEsperaPequenos); // Chama construtor da classe abstrata
        this.filaCaminhoesPequenos = new Fila<>();
        this.caminhaoGrandeAtual = null;
        this.tempoEsperaCaminhaoGrandeAtual = 0;
        this.tempoPrimeiroCaminhaoNaFila = 0;
    }

    /**
     * Construtor alternativo com tempo máximo de espera padrão (30 minutos) para alerta.
     * @param nome Nome da estação.
     */
    public EstacaoPadrao(String nome) {
        this(nome, 30); // Chama o outro construtor com valor padrão
    }

    /**
     * Recebe um caminhão pequeno que chegou à estação, registra seu tempo de chegada na fila
     * e o adiciona à fila de espera.
     * @param caminhao O CaminhaoPequeno que chegou.
     * @param tempoSimuladoAtual O tempo atual da simulação para registrar a chegada.
     */
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao, int tempoSimuladoAtual) {
        if (caminhao != null) {
            caminhao.setTempoChegadaNaFila(tempoSimuladoAtual); // Define o tempo de chegada
            filaCaminhoesPequenos.adicionar(caminhao);
            // System.out.printf("EST %s: CP %s entrou na fila (chegada em %d). Total: %d%n",
            //                    nome, caminhao.getPlaca(), tempoSimuladoAtual, filaCaminhoesPequenos.tamanho());
            if (filaCaminhoesPequenos.tamanho() == 1) { // Se ele é o novo primeiro da fila
                tempoPrimeiroCaminhaoNaFila = 0; // Reseta contador de tempo de alerta para este caminhão
            }
        } else {
            System.err.println("AVISO EST " + nome + ": Tentativa de adicionar caminhão nulo à fila.");
        }
    }

    /**
     * Sobrecarga para manter compatibilidade ou para casos onde o tempo não é crucial.
     * É RECOMENDADO usar a versão que recebe `tempoSimuladoAtual`.
     * @param caminhao O CaminhaoPequeno que chegou.
     */
    @Override
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        System.err.println("AVISO EST " + nome + ": Método receberCaminhaoPequeno(caminhao) SEM tempoSimuladoAtual foi chamado." +
                " O tempo de espera na fila não será calculado com precisão para o CP " + (caminhao != null ? caminhao.getPlaca() : "NULO") + ".");
        if (caminhao != null) {
            caminhao.setTempoChegadaNaFila(-1); // Indica que o tempo de chegada não foi precisamente registrado
            filaCaminhoesPequenos.adicionar(caminhao);
            if (filaCaminhoesPequenos.tamanho() == 1) {
                tempoPrimeiroCaminhaoNaFila = 0;
            }
        }
    }


    /**
     * Processa a fila de caminhões pequenos, tentando descarregar o primeiro
     * caminhão da fila no caminhão grande atual.
     *
     * @param tempoSimuladoAtual O tempo atual da simulação, usado para calcular o tempo de espera.
     * @return Um objeto ResultadoProcessamentoFila contendo o caminhão processado e seu tempo de espera,
     * ou um resultado indicando que nenhum caminhão foi processado.
     */
    public ResultadoProcessamentoFila processarFila(int tempoSimuladoAtual) {
        if (filaCaminhoesPequenos.estaVazia()) {
            tempoPrimeiroCaminhaoNaFila = 0; // Reseta o tempo de alerta se não há ninguém esperando
            return new ResultadoProcessamentoFila(null, 0); // Nenhum caminhão processado
        }

        // Incrementa o contador de tempo de alerta para o PRIMEIRO caminhão da fila
        tempoPrimeiroCaminhaoNaFila++;

        if (caminhaoGrandeAtual == null) {
            // System.out.println("EST " + nome + ": Fila com " + filaCaminhoesPequenos.tamanho() + ", mas aguardando caminhão grande.");
            return new ResultadoProcessamentoFila(null, 0); // Nenhum caminhão processado, precisa de CG
        }

        CaminhaoPequeno caminhaoPequeno = filaCaminhoesPequenos.primeiroElemento(); // Apenas olha (peek)

        if (caminhaoGrandeAtual.getCargaAtual() + caminhaoPequeno.getCargaAtual() <= caminhaoGrandeAtual.getCapacidadeMaxima()) {
            caminhaoPequeno = filaCaminhoesPequenos.remover(); // AGORA remove da fila

            int cargaDescarregada = caminhaoPequeno.descarregar();
            caminhaoGrandeAtual.carregar(cargaDescarregada);

            long tempoDeEspera = 0;
            if (caminhaoPequeno.getTempoChegadaNaFila() != -1) { // Verifica se o tempo de chegada foi registrado
                tempoDeEspera = tempoSimuladoAtual - caminhaoPequeno.getTempoChegadaNaFila();
                if (tempoDeEspera < 0) { // Segurança, não deve acontecer em simulação linear
                    // System.err.println("AVISO EST " + nome + ": Tempo de espera calculado negativo para CP " + caminhaoPequeno.getPlaca() + ". Resetado para 0.");
                    tempoDeEspera = 0;
                }
            } else {
                System.err.println("AVISO EST " + nome + ": CP " + caminhaoPequeno.getPlaca() +
                        " processado, mas seu tempo de chegada na fila não foi registrado. Tempo de espera considerado 0.");
            }

            System.out.printf("EST %s: CP %s (esperou %d min) descarregou %dkg. CG Carga: %d/%d kg. Fila restante: %d.%n",
                    nome, caminhaoPequeno.getPlaca(), tempoDeEspera, cargaDescarregada,
                    caminhaoGrandeAtual.getCargaAtual(), caminhaoGrandeAtual.getCapacidadeMaxima(),
                    filaCaminhoesPequenos.tamanho());

            tempoPrimeiroCaminhaoNaFila = 0; // Reseta tempo de alerta, pois o primeiro saiu (ou a fila ficou vazia)

            if (caminhaoGrandeAtual.prontoParaPartir()) {
                System.out.println("EST " + nome + ": Caminhão grande ("+caminhaoGrandeAtual+") ficou cheio após descarregar CP " + caminhaoPequeno.getPlaca() + ".");
                // A partida será tratada no método gerenciarTempoEsperaCaminhaoGrande
            }
            return new ResultadoProcessamentoFila(caminhaoPequeno, tempoDeEspera);
        } else {
            // Caminhão grande não tem espaço suficiente para a carga completa do pequeno.
            System.out.println("EST " + nome + ": Caminhão grande ("+caminhaoGrandeAtual+") sem espaço para carga de CP " + caminhaoPequeno.getPlaca() +
                    " (" + caminhaoPequeno.getCargaAtual() + "kg). CG precisa partir.");
            // A partida será tratada no método gerenciarTempoEsperaCaminhaoGrande
            return new ResultadoProcessamentoFila(null, 0);
        }
    }

    /**
     * Gerencia o tempo de espera do caminhão grande atual.
     * Se o tempo exceder a tolerância e o caminhão tiver carga, ele parte para o aterro.
     * Também parte se estiver cheio ou se for necessário para liberar espaço para a fila.
     *
     * @return O CaminhaoGrande que partiu para o aterro, ou null se nenhum partiu.
     */
    public CaminhaoGrande gerenciarTempoEsperaCaminhaoGrande() {
        if (caminhaoGrandeAtual == null) {
            tempoEsperaCaminhaoGrandeAtual = 0;
            return null;
        }

        tempoEsperaCaminhaoGrandeAtual++;
        boolean devePartir = false;
        String motivoPartida = "";

        // Condição 1: Caminhão grande está cheio
        if (caminhaoGrandeAtual.prontoParaPartir()) {
            devePartir = true;
            motivoPartida = "atingiu a capacidade máxima";
        }
        // Condição 2: Tempo de espera excedeu a tolerância E tem alguma carga
        else if (tempoEsperaCaminhaoGrandeAtual >= caminhaoGrandeAtual.getToleranciaEspera()) {
            if (caminhaoGrandeAtual.getCargaAtual() > 0) {
                devePartir = true;
                motivoPartida = "excedeu a tolerância de espera (" + caminhaoGrandeAtual.getToleranciaEspera() + " min)";
            } else {
                // Tolerância excedida, mas está vazio. Ele continua esperando e reseta seu contador de espera.
                tempoEsperaCaminhaoGrandeAtual = 0;
            }
        }
        // Condição 3: Falta de espaço para o próximo da fila (e o CG tem carga)
        // Esta condição é importante para evitar deadlock se o CG estiver parcialmente cheio mas não o suficiente para o próximo CP.
        else if (!filaCaminhoesPequenos.estaVazia() && caminhaoGrandeAtual.getCargaAtual() > 0) {
            CaminhaoPequeno proximoCP = filaCaminhoesPequenos.primeiroElemento();
            if (caminhaoGrandeAtual.getCargaAtual() + proximoCP.getCargaAtual() > caminhaoGrandeAtual.getCapacidadeMaxima()) {
                devePartir = true;
                motivoPartida = "não há espaço para o próximo CP da fila (" + proximoCP.getPlaca() + ")";
            }
        }

        if (devePartir) {
            System.out.printf("EST %s: Caminhão grande (%s) partindo para aterro (%s). Carga: %dkg.%n",
                    nome, caminhaoGrandeAtual.toString(), motivoPartida, caminhaoGrandeAtual.getCargaAtual());
            CaminhaoGrande caminhaoQuePartiu = caminhaoGrandeAtual;
            // A carga real transportada será pega pelo Simulador antes de chamar caminhaoQuePartiu.descarregar()

            this.caminhaoGrandeAtual = null; // Libera a "vaga" na estação
            this.tempoEsperaCaminhaoGrandeAtual = 0; // Reseta contador

            return caminhaoQuePartiu;
        }
        return null;
    }

    /**
     * Atribui um caminhão grande a esta estação.
     * Só é possível se não houver outro caminhão grande presente.
     * @param caminhao O CaminhaoGrande a ser atribuído.
     * @return true se a atribuição foi bem-sucedida, false caso contrário.
     */
    public boolean atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        if (caminhao == null) {
            System.err.println("ERRO EST " + nome + ": Tentativa de atribuir caminhão grande nulo.");
            return false;
        }
        if (this.caminhaoGrandeAtual != null) {
            System.out.println("EST " + nome + ": Já existe um caminhão grande (" + this.caminhaoGrandeAtual + "). Não é possível atribuir " + caminhao + ".");
            return false;
        }
        this.caminhaoGrandeAtual = caminhao;
        this.tempoEsperaCaminhaoGrandeAtual = 0; // Reseta o tempo de espera do novo caminhão
        System.out.println("EST " + nome + ": Novo caminhão grande (" + caminhao + ") atribuído.");
        return true;
    }

    /**
     * Verifica se o tempo de espera do primeiro caminhão pequeno na fila (para alerta) excedeu
     * o limite máximo configurado para a estação.
     * @return true se o tempo foi excedido e a fila não está vazia, false caso contrário.
     */
    public boolean tempoEsperaExcedido() {
        return !filaCaminhoesPequenos.estaVazia() && tempoPrimeiroCaminhaoNaFila >= getTempoMaximoEspera();
    }

    /**
     * Verifica se a estação precisa de um caminhão grande (não tem um atualmente
     * e há caminhões pequenos na fila esperando).
     * @return true se precisa de um caminhão grande, false caso contrário.
     */
    public boolean precisaCaminhaoGrande() {
        return caminhaoGrandeAtual == null && !filaCaminhoesPequenos.estaVazia();
    }

    // --- Getters ---
    public int getCaminhoesNaFila() {
        return filaCaminhoesPequenos.tamanho();
    }

    public boolean temCaminhaoGrande() {
        return caminhaoGrandeAtual != null;
    }

    public int getCargaCaminhaoGrandeAtual() {
        return (caminhaoGrandeAtual != null) ? caminhaoGrandeAtual.getCargaAtual() : 0;
    }

    /**
     * Retorna o caminhão grande atualmente alocado nesta estação.
     * Necessário para o Simulador poder atualizar a tolerância de espera, por exemplo.
     * @return O CaminhaoGrande atual, ou null se não houver.
     */
    public CaminhaoGrande getCaminhaoGrandeAtual() {
        return this.caminhaoGrandeAtual;
    }

    /**
     * Método legado da interface, agora a lógica principal está em `atribuirCaminhaoGrande`
     * e `processarFila`. Mantido para compatibilidade ou pode ser removido se a interface mudar.
     * @deprecated Usar {@link #atribuirCaminhaoGrande(CaminhaoGrande)} e a lógica de processamento.
     */
    @Override
    @Deprecated
    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        System.out.println("AVISO EST " + nome + ": Método descarregarParaCaminhaoGrande(CG) chamado em EstacaoPadrao (usar atribuir/processar).");
        atribuirCaminhaoGrande(caminhao);
    }
}