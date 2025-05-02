package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import Estruturas.Fila;

public class EstacaoPadrao extends EstacaoTransferencia {

    private int lixoArmazenado;
    private Fila<CaminhaoPequeno> filaCaminhoes; // Fila para caminhões pequenos
    private int tempoEsperaAtual; // Contador do tempo de espera atual
    private CaminhaoGrande caminhaoAtual; // Caminhão grande atualmente na estação
    private int tempoEsperaCaminhaoGrande; // Contador de tempo do caminhão grande atual

    public EstacaoPadrao(String nome, int tempoMaximoEspera) {
        super(nome, tempoMaximoEspera);
        this.lixoArmazenado = 0;
        this.filaCaminhoes = new Fila<>();
        this.tempoEsperaAtual = 0;
        this.caminhaoAtual = null;
        this.tempoEsperaCaminhaoGrande = 0;
    }

    // Construtor alternativo com tempo padrão se não for especificado
    public EstacaoPadrao(String nome) {
        this(nome, 30); // 30 minutos como tempo padrão de espera
    }

    @Override
    public void receberCaminhaoPequeno(CaminhaoPequeno caminhao) {
        // Adiciona caminhão à fila
        filaCaminhoes.adicionar(caminhao);
        System.out.println("Estação " + nome + ": Caminhão entrou na fila. Total na fila: " + filaCaminhoes.tamanho());
    }

    // Método para processar a fila de caminhões (chamado pelo simulador a cada ciclo)
    public void processarFila() {
        // Se não há caminhões pequenos na fila, incrementa o tempo de espera e retorna
        if (filaCaminhoes.estaVazia()) {
            tempoEsperaAtual = 0; // Reseta o tempo se não há caminhões esperando
            return;
        }

        // Se já existe um caminhão na fila, incrementa o tempo de espera
        tempoEsperaAtual++;

        // Verifica se há um caminhão grande disponível
        if (caminhaoAtual == null) {
            // Não temos caminhão grande, precisamos solicitar um
            System.out.println("Estação " + nome + ": Solicitando um caminhão grande para atender a fila.");
            return; // Retorna e aguarda o simulador atribuir um caminhão grande
        }

        // Processa o primeiro caminhão da fila se temos um caminhão grande
        CaminhaoPequeno caminhao = filaCaminhoes.primeiroElemento(); // Apenas olha, não remove ainda

        // Verifica se o caminhão grande tem espaço para a carga do caminhão pequeno
        if (caminhaoAtual.getCargaAtual() + caminhao.getCargaAtual() <= caminhaoAtual.getCapacidadeMaxima()) {
            // Remove o caminhão da fila agora que sabemos que podemos processá-lo
            caminhao = filaCaminhoes.remover();

            // Transfere a carga
            int descarregado = caminhao.descarregar();
            caminhaoAtual.carregar(descarregado);
            lixoArmazenado += descarregado;

            System.out.println("Estação " + nome + ": Caminhão " + caminhao.getPlaca() +
                    " descarregou " + descarregado + "kg. Caminhão grande agora com " +
                    caminhaoAtual.getCargaAtual() + "kg.");

            // Reseta o contador de tempo de espera após processar um caminhão
            tempoEsperaAtual = 0;

            // Verifica se o caminhão grande está cheio
            if (caminhaoAtual.prontoParaPartir()) {
                enviarCaminhaoGrandeParaAterro();
            }
        } else {
            // Caminhão grande não tem espaço suficiente, precisa partir
            System.out.println("Estação " + nome + ": Caminhão grande sem espaço suficiente para mais lixo.");
            enviarCaminhaoGrandeParaAterro();
        }
    }

    // Método para gerenciar o tempo de espera do caminhão grande
    public void gerenciarTempoEsperaCaminhaoGrande() {
        if (caminhaoAtual == null) {
            return; // Não há caminhão para gerenciar
        }

        tempoEsperaCaminhaoGrande++;

        // Verifica se o tempo de espera do caminhão grande foi excedido
        if (tempoEsperaCaminhaoGrande >= caminhaoAtual.getToleranciaEspera()) {
            // Se o caminhão já tem alguma carga, envia para o aterro
            if (caminhaoAtual.getCargaAtual() > 0) {
                System.out.println("Estação " + nome + ": Tempo de espera do caminhão grande excedido. Enviando para o aterro.");
                enviarCaminhaoGrandeParaAterro();
            } else {
                // Se o caminhão está vazio e não há fila, pode ficar aguardando
                if (filaCaminhoes.estaVazia()) {
                    System.out.println("Estação " + nome + ": Caminhão grande vazio e sem fila. Continuando a espera.");
                    // Pode manter o contador para continuar monitorando
                }
            }
        }
    }

    // Método para enviar o caminhão grande atual para o aterro
    private void enviarCaminhaoGrandeParaAterro() {
        if (caminhaoAtual != null) {
            caminhaoAtual.descarregar();
            caminhaoAtual = null; // Remove a referência ao caminhão
            tempoEsperaCaminhaoGrande = 0; // Reseta o contador
        }
    }

    // Método para atribuir um novo caminhão grande à estação
    public void atribuirCaminhaoGrande(CaminhaoGrande caminhao) {
        if (this.caminhaoAtual != null) {
            System.out.println("Estação " + nome + ": Já existe um caminhão grande aqui. Não é possível atribuir outro.");
            return;
        }
        this.caminhaoAtual = caminhao;
        this.tempoEsperaCaminhaoGrande = 0;
        System.out.println("Estação " + nome + ": Novo caminhão grande atribuído.");
    }

    // Verifica se o tempo de espera da fila de caminhões pequenos foi excedido
    public boolean tempoEsperaExcedido() {
        return tempoEsperaAtual >= tempoMaximoEspera && !filaCaminhoes.estaVazia();
    }

    // Verifica se precisa de um caminhão grande
    public boolean precisaCaminhaoGrande() {
        return caminhaoAtual == null && !filaCaminhoes.estaVazia();
    }

    // Retorna o número de caminhões na fila
    public int getCaminhoesNaFila() {
        return filaCaminhoes.tamanho();
    }

    // Método existente descarregarParaCaminhaoGrande não é mais necessário da forma original
    @Override
    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        // Este método é mantido para compatibilidade, mas agora a lógica está em processarFila()
        atribuirCaminhaoGrande(caminhao);
    }
}