package caminhoes;

// Esta é AGORA a única implementação concreta de CaminhaoPequeno
public class CaminhaoPequenoPadrao extends CaminhaoPequeno {

    // O construtor RECEBE a capacidade desejada
    public CaminhaoPequenoPadrao(int capacidade) {
        // Passa a capacidade para o construtor da superclasse abstrata
        super(capacidade);
    }

    // A implementação do método abstrato é feita AQUI
    @Override
    public boolean coletar(int quantidade) {
        if (quantidade <= 0) {
            return false; // Não coleta quantidade negativa ou zero
        }
        if (cargaAtual + quantidade <= capacidade) {
            cargaAtual += quantidade;
            return true;
        }
        // Se não couber exatamente, não coleta (poderia coletar até encher como alternativa)
        return false;
    }

    // Poderia ter um toString() mais específico se quisesse,
    // mas por enquanto herdará o toString() de CaminhaoPequeno
}