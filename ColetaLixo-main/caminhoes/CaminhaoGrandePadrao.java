package caminhoes;

/**
 * Implementação concreta padrão de um CaminhaoGrande.
 * Utiliza a capacidade máxima definida na classe abstrata.
 */
public class CaminhaoGrandePadrao extends CaminhaoGrande {

    // Constante para a tolerância de espera padrão, caso não seja informada.
    public static final int TOLERANCIA_ESPERA_PADRAO_MINUTOS = 30;

    /**
     * Construtor que permite definir a tolerância de espera.
     * @param toleranciaEspera O tempo de tolerância em minutos (deve ser >= 1).
     */
    public CaminhaoGrandePadrao(int toleranciaEspera) {
        super(toleranciaEspera);
    }

    /**
     * Construtor padrão que utiliza a tolerância de espera padrão (30 minutos).
     */
    public CaminhaoGrandePadrao() {
        super(TOLERANCIA_ESPERA_PADRAO_MINUTOS); // Usa a constante
    }

    // Herda todos os métodos de CaminhaoGrande.
    // Poderia adicionar métodos específicos ou sobrescrever existentes se necessário.
}