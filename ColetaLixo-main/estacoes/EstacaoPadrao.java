package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import Estruturas.Fila;
import Estruturas.Lista; // Import adicionado

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
            if (filaCaminhoesPequenos.tamanho() == 1) {
                tempoPrimeiroCaminhaoNaFila = 0;
            }
        } else {
            System.err.println("AVISO EST " + nome + ": Tentativa de adicionar caminhão nulo à fila.");
        }
    }

    @Override
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        System.err.println("AVISO EST " + nome + ": Método receberCaminhaoPequeno(caminhao) SEM tempoSimuladoAtual foi chamado." +
                " O tempo de espera na fila não será calculado com precisão para o CP " + (caminhao != null ? caminhao.getPlaca() : "NULO") + ".");
        if (caminhao != null) {
            caminhao.setTempoChegadaNaFila(-1);
            filaCaminhoesPequenos.adicionar(caminhao);
            if (filaCaminhoesPequenos.tamanho() == 1) {
                tempoPrimeiroCaminhaoNaFila = 0;
            }
        }
    }

    public ResultadoProcessamentoFila processarFila(int tempoSimuladoAtual) {
        if (filaCaminhoesPequenos.estaVazia()) {
            tempoPrimeiroCaminhaoNaFila = 0;
            return new ResultadoProcessamentoFila(null, 0);
        }

        tempoPrimeiroCaminhaoNaFila++;

        if (caminhaoGrandeAtual == null) {
            return new ResultadoProcessamentoFila(null, 0);
        }

        CaminhaoPequeno caminhaoPequeno = filaCaminhoesPequenos.primeiroElemento();

        if (caminhaoGrandeAtual.getCargaAtual() + caminhaoPequeno.getCargaAtual() <= caminhaoGrandeAtual.getCapacidadeMaxima()) {
            caminhaoPequeno = filaCaminhoesPequenos.remover();

            int cargaDescarregada = caminhaoPequeno.descarregar();
            caminhaoGrandeAtual.carregar(cargaDescarregada);

            long tempoDeEspera = 0;
            if (caminhaoPequeno.getTempoChegadaNaFila() != -1) {
                tempoDeEspera = tempoSimuladoAtual - caminhaoPequeno.getTempoChegadaNaFila();
                if (tempoDeEspera < 0) {
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

            tempoPrimeiroCaminhaoNaFila = 0;

            if (caminhaoGrandeAtual.prontoParaPartir()) {
                System.out.println("EST " + nome + ": Caminhão grande ("+caminhaoGrandeAtual+") ficou cheio após descarregar CP " + caminhaoPequeno.getPlaca() + ".");
            }
            return new ResultadoProcessamentoFila(caminhaoPequeno, tempoDeEspera);
        } else {
            System.out.println("EST " + nome + ": Caminhão grande ("+caminhaoGrandeAtual+") sem espaço para carga de CP " + caminhaoPequeno.getPlaca() +
                    " (" + caminhaoPequeno.getCargaAtual() + "kg). CG precisa partir.");
            return new ResultadoProcessamentoFila(null, 0);
        }
    }

    public CaminhaoGrande gerenciarTempoEsperaCaminhaoGrande() {
        if (caminhaoGrandeAtual == null) {
            tempoEsperaCaminhaoGrandeAtual = 0;
            return null;
        }

        tempoEsperaCaminhaoGrandeAtual++;
        boolean devePartir = false;
        String motivoPartida = "";

        if (caminhaoGrandeAtual.prontoParaPartir()) {
            devePartir = true;
            motivoPartida = "atingiu a capacidade máxima";
        }
        else if (tempoEsperaCaminhaoGrandeAtual >= caminhaoGrandeAtual.getToleranciaEspera()) {
            if (caminhaoGrandeAtual.getCargaAtual() > 0) {
                devePartir = true;
                motivoPartida = "excedeu a tolerância de espera (" + caminhaoGrandeAtual.getToleranciaEspera() + " min)";
            } else {
                tempoEsperaCaminhaoGrandeAtual = 0;
            }
        }
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
            this.caminhaoGrandeAtual = null;
            this.tempoEsperaCaminhaoGrandeAtual = 0;
            return caminhaoQuePartiu;
        }
        return null;
    }

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
        this.tempoEsperaCaminhaoGrandeAtual = 0;
        System.out.println("EST " + nome + ": Novo caminhão grande (" + caminhao + ") atribuído.");
        return true;
    }

    public boolean tempoEsperaExcedido() {
        return !filaCaminhoesPequenos.estaVazia() && tempoPrimeiroCaminhaoNaFila >= getTempoMaximoEspera();
    }

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

    public CaminhaoGrande getCaminhaoGrandeAtual() {
        return this.caminhaoGrandeAtual;
    }

    /**
     * Retorna uma cópia da lista de caminhões pequenos na fila.
     * Isso é útil para exibir na UI sem modificar a fila original ou causar problemas de concorrência.
     * @return Uma nova Lista contendo os caminhões da fila.
     */
    public Lista<CaminhaoPequeno> getFilaCaminhoesPequenosSnapshot() {
        Lista<CaminhaoPequeno> snapshot = new Lista<>();
        if (!filaCaminhoesPequenos.estaVazia()) {
            Fila<CaminhaoPequeno> tempFila = new Fila<>();
            while(!filaCaminhoesPequenos.estaVazia()) {
                CaminhaoPequeno cp = filaCaminhoesPequenos.remover();
                snapshot.adicionar(cp);
                tempFila.adicionar(cp);
            }
            // Restaurar a fila original
            while(!tempFila.estaVazia()) {
                filaCaminhoesPequenos.adicionar(tempFila.remover());
            }
        }
        return snapshot;
    }

    @Override
    @Deprecated
    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        System.out.println("AVISO EST " + nome + ": Método descarregarParaCaminhaoGrande(CG) chamado em EstacaoPadrao (usar atribuir/processar).");
        atribuirCaminhaoGrande(caminhao);
    }
}