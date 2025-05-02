package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import Estruturas.Fila;

public class EstacaoPadrao extends EstacaoTransferencia {

    private int lixoArmazenado;
    private Fila<CaminhaoPequeno> filaCaminhoes; // Fila para caminhões pequenos
    private int tempoEsperaAtual; // Contador do tempo de espera atual

    public EstacaoPadrao(String nome, int tempoMaximoEspera) {
        super(nome, tempoMaximoEspera);
        this.lixoArmazenado = 0;
        this.filaCaminhoes = new Fila<>();
        this.tempoEsperaAtual = 0;
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

    // Método para processar a fila de caminhões (chamado pelo simulador)
    public void processarFila(CaminhaoGrande caminhaoGrande) {
        if (filaCaminhoes.estaVazia()) {
            tempoEsperaAtual++; // Incrementa o tempo de espera se não há caminhões
            return;
        }

        // Processa o primeiro caminhão da fila
        CaminhaoPequeno caminhao = filaCaminhoes.remover();
        int descarregado = caminhao.descarregar();
        lixoArmazenado += descarregado;
        System.out.println("Estação " + nome + " recebeu " + descarregado + "kg de lixo. Total armazenado: " + lixoArmazenado + "kg");

        // Reseta o contador de tempo de espera após processar um caminhão
        tempoEsperaAtual = 0;
    }

    // Verifica se o tempo de espera foi excedido
    public boolean tempoEsperaExcedido() {
        return tempoEsperaAtual >= tempoMaximoEspera && !filaCaminhoes.estaVazia();
    }

    // Retorna o número de caminhões na fila
    public int getCaminhoesNaFila() {
        return filaCaminhoes.tamanho();
    }

    @Override
    public void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao) {
        caminhao.carregar(lixoArmazenado);
        System.out.println("Estação " + nome + " carregou caminhão grande com " + lixoArmazenado + "kg.");
        lixoArmazenado = 0;
    }
}