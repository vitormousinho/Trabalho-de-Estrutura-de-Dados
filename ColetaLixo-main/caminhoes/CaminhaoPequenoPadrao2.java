package caminhoes;

public class CaminhaoPequenoPadrao2 extends CaminhaoPequeno {

    public CaminhaoPequenoPadrao2() {
        this.capacidade = 4000;
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