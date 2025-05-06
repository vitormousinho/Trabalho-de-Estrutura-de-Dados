package estacoes;

import caminhoes.CaminhaoPequeno;

public class ResultadoProcessamentoFila {
    private CaminhaoPequeno caminhaoProcessado;
    private long tempoDeEspera;

    public ResultadoProcessamentoFila(CaminhaoPequeno caminhaoProcessado, long tempoDeEspera) {
        this.caminhaoProcessado = caminhaoProcessado;
        this.tempoDeEspera = tempoDeEspera;
    }

    public CaminhaoPequeno getCaminhaoProcessado() {
        return caminhaoProcessado;
    }

    public long getTempoDeEspera() {
        return tempoDeEspera;
    }

    public boolean foiProcessado() {
        return caminhaoProcessado != null;
    }
}