package caminhoes;

public abstract class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    // Outros atributos comuns

    // Construtor para definir a capacidade via subclasse
    protected CaminhaoPequeno(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser positiva.");
        }
        this.capacidade = capacidade;
        this.cargaAtual = 0;
        // Inicializar outros atributos
    }

    // Metodo de coleta AINDA abstrato aqui
    public abstract boolean coletar(int quantidade);

    // Métodos concretos comuns
    public boolean estaCheio() {
        return cargaAtual >= capacidade;
    }

    public int descarregar() {
        int carga = cargaAtual;
        cargaAtual = 0;
        return carga;
    }

    public int getCargaAtual() {
        return cargaAtual;
    }

    public int getCapacidade() {
        return capacidade;
    }

}