package estacoes;

import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;

public abstract class EstacaoTransferencia {
    protected String nome;
    protected int tempoMaximoEspera; // Novo atributo

    public EstacaoTransferencia(String nome, int tempoMaximoEspera) {
        this.nome = nome;
        this.tempoMaximoEspera = tempoMaximoEspera;
    }

    public int getTempoMaximoEspera() {
        return tempoMaximoEspera;
    }

    public void setTempoMaximoEspera(int tempoMaximoEspera) {
        this.tempoMaximoEspera = tempoMaximoEspera;
    }

    public abstract void receberCaminhaoPequeno(CaminhaoPequeno caminhao);
    public abstract void descarregarParaCaminhaoGrande(CaminhaoGrande caminhao);
}