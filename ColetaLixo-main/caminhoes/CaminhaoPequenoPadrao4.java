package caminhoes;

public class CaminhaoPequenoPadrao4 extends CaminhaoPequeno {

    public CaminhaoPequenoPadrao4() {
        this.capacidade = 10000;
        this.cargaAtual = 0;
    }

    @Override
    public boolean coletar(int quantidade) {
        if (cargaAtual + quantidade <= capacidade) {
            cargaAtual += quantidade;
            return true;
        }
        return false;
    }
}