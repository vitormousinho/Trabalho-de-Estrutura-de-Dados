package caminhoes;

public class CaminhaoGrandePadrao extends CaminhaoGrande {
    // Construtor com tolerância de espera
    public CaminhaoGrandePadrao(int toleranciaEspera) {
        super(toleranciaEspera);
    }

    // Construtor padrão que define uma tolerância de espera padrão
    public CaminhaoGrandePadrao() {
        // Tolerância padrão de 30 minutos
        super(30);
    }
}