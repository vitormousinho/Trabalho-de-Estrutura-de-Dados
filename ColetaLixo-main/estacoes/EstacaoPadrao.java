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
    private int tempoPrimeiroCaminhaoNaFila; // Contador de tempo que o PRIMEIRO caminhão pequeno está esperando

    /**
     * Construtor da EstacaoPadrao.
     * @param nome Nome da estação.
     * @param tempoMaximoEsperaPequenos Tempo máximo (em minutos) que um caminhão pequeno pode esperar
     * na fila antes de acionar a adição de um novo caminhão grande.
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
     * Construtor alternativo com tempo máximo de espera padrão (30 minutos).
     * @param nome Nome da estação.
     */
    public EstacaoPadrao(String nome) {
        this(nome, 30); // Chama o outro construtor com valor padrão
    }

    /**
     * Recebe um caminhão pequeno que chegou à estação e o adiciona à fila de espera.
     * @param caminhao O CaminhaoPequeno que chegou.
     */
    @Override
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        if (caminhao != null) {
            filaCaminhoesPequenos.adicionar(caminhao);
            // System.out.printf("EST %s: Caminhão %s entrou na fila. Total: %d%n",
            //                    nome, caminhao.getPlaca(), filaCaminhoesPequenos.tamanho());
            // Se a fila estava vazia, reseta o contador de tempo do primeiro
            if (filaCaminhoesPequenos.tamanho() == 1) {
                tempoPrimeiroCaminhaoNaFila = 0;
            }
        } else {
            System.err.println("AVISO: Tentativa de adicionar caminhão nulo à fila da estação " + nome);
        }
    }

    /**
     * Processa a fila de caminhões pequenos, tentando descarregar o primeiro
     * caminhão da fila no caminhão grande atual.
     * Incrementa o contador de tempo de espera do primeiro caminhão na fila.
     *
     * @return O CaminhaoPequeno que acabou de ser processado (descarregado),
     * ou null se nenhum caminhão foi processado neste ciclo.
     */
    public CaminhaoPequeno processarFila() {
        // Se não há caminhões pequenos na fila, não há o que processar
        if (filaCaminhoesPequenos.estaVazia()) {
            tempoPrimeiroCaminhaoNaFila = 0; // Reseta o tempo se não há ninguém esperando
            return null; // Nenhum caminhão processado
        }

        // Se há caminhões na fila, incrementa o tempo de espera do primeiro
        tempoPrimeiroCaminhaoNaFila++;

        // Verifica se há um caminhão grande presente para receber a carga
        if (caminhaoGrandeAtual == null) {
            // Não há caminhão grande, não podemos processar a fila agora.
            // A estação sinalizará que precisa de um (método precisaCaminhaoGrande).
            // System.out.println("EST " + nome + ": Fila com " + filaCaminhoesPequenos.tamanho() + ", mas aguardando caminhão grande."); // Log opcional
            return null; // Nenhum caminhão processado
        }

        // Temos caminhão grande e temos fila. Vamos processar o primeiro.
        CaminhaoPequeno caminhaoPequeno = filaCaminhoesPequenos.primeiroElemento(); // Apenas olha (peek)

        // Verifica se o caminhão grande tem espaço para a carga INTEIRA do caminhão pequeno
        // (Poderia ser alterado para descarregar parcial se desejado, mas atual é mais simples)
        if (caminhaoGrandeAtual.getCargaAtual() + caminhaoPequeno.getCargaAtual() <= caminhaoGrandeAtual.getCapacidadeMaxima()) {

            // Remove o caminhão da fila AGORA que sabemos que podemos processá-lo
            caminhaoPequeno = filaCaminhoesPequenos.remover();

            // Transfere a carga
            int cargaDescarregada = caminhaoPequeno.descarregar(); // Esvazia o caminhão pequeno
            caminhaoGrandeAtual.carregar(cargaDescarregada); // Carrega o caminhão grande

            System.out.printf("EST %s: Caminhão %s descarregou %dkg. CG Carga: %d/%d kg. Fila restante: %d.%n",
                    nome, caminhaoPequeno.getPlaca(), cargaDescarregada,
                    caminhaoGrandeAtual.getCargaAtual(), caminhaoGrandeAtual.getCapacidadeMaxima(),
                    filaCaminhoesPequenos.tamanho());

            // Reseta o contador de tempo de espera do (novo) primeiro da fila (se houver)
            tempoPrimeiroCaminhaoNaFila = 0;

            // Verifica se o caminhão grande ficou cheio após descarregar
            if (caminhaoGrandeAtual.prontoParaPartir()) {
                System.out.println("EST " + nome + ": Caminhão grande ficou cheio e partirá para o aterro.");
                // A partida será tratada no método gerenciarTempoEsperaCaminhaoGrande ou pelo Simulador
            }

            return caminhaoPequeno; // Retorna o caminhão que foi processado

        } else {
            // Caminhão grande não tem espaço suficiente para a carga completa do pequeno.
            // O caminhão grande atual precisa partir para o aterro para liberar espaço.
            System.out.println("EST " + nome + ": Caminhão grande sem espaço para carga de " + caminhaoPequeno.getPlaca() +
                    " (" + caminhaoPequeno.getCargaAtual() + "kg). Caminhão grande precisa partir.");
            // A partida será tratada no método gerenciarTempoEsperaCaminhaoGrande
            return null; // Nenhum caminhão pequeno foi processado neste ciclo
        }
    }

    /**
     * Gerencia o tempo de espera do caminhão grande atual.
     * Se o tempo exceder a tolerância e o caminhão tiver carga, ele parte para o aterro.
     *
     * @return O CaminhaoGrande que partiu para o aterro, ou null se nenhum partiu.
     */
    public CaminhaoGrande gerenciarTempoEsperaCaminhaoGrande() {
        if (caminhaoGrandeAtual == null) {
            tempoEsperaCaminhaoGrandeAtual = 0; // Não há caminhão, reseta contador
            return null; // Nenhum caminhão para gerenciar
        }

        // Incrementa o tempo de espera do caminhão grande
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
                // Tolerância excedida, mas está vazio. Ele continua esperando.
                // System.out.println("EST " + nome + ": Tolerância do CG excedida, mas está vazio. Continua esperando."); // Log opcional
                tempoEsperaCaminhaoGrandeAtual = 0; // Reinicia o contador de espera dele? Ou deixa acumular? Vamos reiniciar.
            }
        }
        // Condição 3: Falta de espaço (detectada em processarFila, mas precisa acionar partida)
        // Esta condição é implícita: se processarFila não conseguiu adicionar carga por falta de espaço,
        // o caminhão grande deve partir. Podemos verificar isso aqui também.
        else if (!filaCaminhoesPequenos.estaVazia()) {
            CaminhaoPequeno proximo = filaCaminhoesPequenos.primeiroElemento();
            if (caminhaoGrandeAtual.getCargaAtual() + proximo.getCargaAtual() > caminhaoGrandeAtual.getCapacidadeMaxima() && caminhaoGrandeAtual.getCargaAtual() > 0) {
                devePartir = true;
                motivoPartida = "não há espaço para o próximo caminhão (" + proximo.getPlaca() + ")";
            }
        }


        // Se alguma condição de partida foi atendida
        if (devePartir) {
            System.out.printf("EST %s: Caminhão grande partindo para aterro (%s). Carga: %dkg.%n",
                    nome, motivoPartida, caminhaoGrandeAtual.getCargaAtual());
            CaminhaoGrande caminhaoQuePartiu = caminhaoGrandeAtual;
            caminhaoQuePartiu.descarregar(); // Simula a viagem e esvazia a carga

            // Libera a "vaga" na estação
            this.caminhaoGrandeAtual = null;
            this.tempoEsperaCaminhaoGrandeAtual = 0; // Reseta contador

            return caminhaoQuePartiu; // Retorna o caminhão que partiu
        }

        return null; // Nenhum caminhão partiu
    }

    /**
     * Atribui um caminhão grande a esta estação.
     * Só é possível se não houver outro caminhão grande presente.
     * @param caminhao O CaminhaoGrande a ser atribuído.
     * @return true se a atribuição foi bem-sucedida, false caso contrário.
     */
    public boolean atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        if (caminhao == null) {
            System.err.println("ERRO: Tentativa de atribuir caminhão grande nulo à estação " + nome);
            return false;
        }
        if (this.caminhaoGrandeAtual != null) {
            System.out.println("EST " + nome + ": Já existe um caminhão grande aqui. Não é possível atribuir outro.");
            return false; // Já tem um caminhão
        }
        this.caminhaoGrandeAtual = caminhao;
        this.tempoEsperaCaminhaoGrandeAtual = 0; // Reseta o tempo de espera do novo caminhão
        System.out.println("EST " + nome + ": Novo caminhão grande atribuído.");
        return true;
    }

    /**
     * Verifica se o tempo de espera do primeiro caminhão pequeno na fila excedeu
     * o limite máximo configurado para a estação.
     * @return true se o tempo foi excedido e a fila não está vazia, false caso contrário.
     */
    public boolean tempoEsperaExcedido() {
        // Usa o tempoMaximoEspera definido no construtor da superclasse
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

    /**
     * Retorna o número de caminhões pequenos atualmente na fila de espera.
     * @return O tamanho da fila.
     */
    public int getCaminhoesNaFila() {
        return filaCaminhoesPequenos.tamanho();
    }

    /**
     * Verifica se há um caminhão grande atualmente alocado nesta estação.
     * @return true se há um caminhão grande, false caso contrário.
     */
    public boolean temCaminhaoGrande() {
        return caminhaoGrandeAtual != null;
    }

    /**
     * Retorna a carga atual do caminhão grande alocado na estação.
     * @return A carga em kg, ou 0 se não houver caminhão grande.
     */
    public int getCargaCaminhaoGrandeAtual() {
        return (caminhaoGrandeAtual != null) ? caminhaoGrandeAtual.getCargaAtual() : 0;
    }


    /**
     * Método legado da interface, agora a lógica principal está em `atribuirCaminhaoGrande`
     * e `processarFila`. Mantido para compatibilidade ou pode ser removido se a interface mudar.
     * @deprecated Usar {@link #atribuirCaminhaoGrande(CaminhaoGrande)} e a lógica de processamento.
     */
    @Override
    @Deprecated
    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        System.out.println("AVISO: Método descarregarParaCaminhaoGrande(CG) chamado em EstacaoPadrao (usar atribuir/processar).");
        atribuirCaminhaoGrande(caminhao);
    }
}