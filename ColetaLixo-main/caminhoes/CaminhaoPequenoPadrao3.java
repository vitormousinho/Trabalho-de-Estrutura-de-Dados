package caminhoes;

public class CaminhaoPequenoPadrao3 extends CaminhaoPequeno {

    public CaminhaoPequenoPadrao3() {
        this.capacidade = 8000;
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