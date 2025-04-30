package caminhoes;

public abstract class CaminhaoPequeno {
    protected int capacidade;
    protected int cargaAtual;
    // Adicione outros atributos comuns se necessário (id, status, etc.)

    // Construtor para definir a capacidade via subclasse
    protected CaminhaoPequeno(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser positiva.");
        }
        this.capacidade = capacidade;
        this.cargaAtual = 0;
        // Inicializar outros atributos comuns
    }

    // Método de coleta AINDA abstrato aqui
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

    // Adicione outros métodos get/set/lógica comuns se precisar
    @Override
    public String toString() {
        // Exemplo de toString útil para depuração
        return "CaminhaoPequeno[Cap=" + capacidade + ", Carga=" + cargaAtual + "]";
    }
}