package caminhoes;

public class CaminhaoPequenoPadrao extends CaminhaoPequeno {

    // O construtor RECEBE a capacidade desejada
    public CaminhaoPequenoPadrao(int capacidade) {
        // Passa a capacidade para o construtor da superclasse abstrata
        super(capacidade);
    }

    // A implementação do metodo abstrato é feita AQUI
    @Override
    public boolean coletar(int quantidade) {
        if (quantidade <= 0) {
            return false;
        }
        if (cargaAtual + quantidade <= capacidade) {
            cargaAtual += quantidade;
            return true;
        }
        return false;
    }

    // Outros métodos específicos desta classe, se houver, podem ser adicionados.
    // Por enquanto, ela só implementa o metodo abstrato.
}