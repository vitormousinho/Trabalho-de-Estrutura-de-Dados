package caminhoes;

public abstract class CaminhaoGrande {
    protected int capacidadeMaxima = 20000;
    protected int cargaAtual;
    protected int toleranciaEspera; // Tempo em minutos que o caminhão vai esperar

    public CaminhaoGrande(int toleranciaEspera) {
        this.cargaAtual = 0;
        this.toleranciaEspera = toleranciaEspera;
    }

    public void carregar(int quantidade) {
        cargaAtual += quantidade;
        if (cargaAtual > capacidadeMaxima) {
            cargaAtual = capacidadeMaxima;
        }
    }

    public boolean prontoParaPartir() {
        return cargaAtual >= capacidadeMaxima;
    }

    public void descarregar() {
        System.out.println("Caminhão grande partiu para o aterro com " + cargaAtual + "kg.");
        cargaAtual = 0;
    }

    // Getters e setters para tolerância de espera
    public int getToleranciaEspera() {
        return toleranciaEspera;
    }

    public void setToleranciaEspera(int toleranciaEspera) {
        if (toleranciaEspera < 1) {
            throw new IllegalArgumentException("A tolerância de espera deve ser pelo menos 1 minuto.");
        }
        this.toleranciaEspera = toleranciaEspera;
    }

    // Getter para carga atual
    public int getCargaAtual() {
        return cargaAtual;
    }

    // Getter para capacidade máxima
    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}