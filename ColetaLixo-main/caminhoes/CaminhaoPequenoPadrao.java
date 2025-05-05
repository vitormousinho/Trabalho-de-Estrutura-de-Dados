package caminhoes;

/**
 * Implementação concreta padrão de um CaminhaoPequeno.
 * Define a lógica de coleta: coleta o máximo possível até a capacidade limite.
 */
public class CaminhaoPequenoPadrao extends CaminhaoPequeno {

    /**
     * Construtor que define a capacidade do caminhão.
     * @param capacidade A capacidade em kg (deve ser > 0).
     */
    public CaminhaoPequenoPadrao(int capacidade) {
        // Passa a capacidade para o construtor da superclasse abstrata
        super(capacidade);
    }

    /**
     * Tenta coletar uma quantidade de lixo.
     * Se a quantidade solicitada couber, coleta tudo.
     * Se a quantidade for maior que o espaço restante, coleta apenas o
     * necessário para encher o caminhão.
     *
     * @param quantidade a quantidade de lixo a tentar coletar (deve ser > 0).
     * @return A quantidade de lixo efetivamente coletada, ou 0 se a quantidade
     * solicitada for inválida ou o caminhão já estiver cheio.
     */
    @Override
    public int coletar(int quantidade) { // Retorna int (quantidade coletada)
        if (quantidade <= 0) {
            System.err.println("AVISO: Tentativa de coletar quantidade inválida (" + quantidade + ") pelo caminhão " + getPlaca());
            return 0; // Não coleta quantidade negativa ou zero
        }
        if (estaCheio()) {
            // System.out.println("DEBUG: Caminhão " + getPlaca() + " já está cheio, não pode coletar mais."); // Log opcional
            return 0; // Já está cheio
        }

        // Calcula quanto espaço ainda resta no caminhão
        int espacoDisponivel = capacidade - cargaAtual;
        // Determina quanto pode ser coletado (o mínimo entre o solicitado e o disponível)
        int quantidadeColetada = Math.min(quantidade, espacoDisponivel);

        if (quantidadeColetada > 0) {
            cargaAtual += quantidadeColetada;
            // System.out.println("DEBUG: Caminhão " + getPlaca() + " coletou " + quantidadeColetada + "kg."); // Log opcional
            return quantidadeColetada; // Retorna quanto foi coletado
        } else {
            // Isso pode acontecer se espacoDisponivel for 0, o que já é coberto por estaCheio()
            // System.err.println("AVISO: Não foi possível coletar lixo (quantidadeColetada=0) para caminhão " + getPlaca()); // Log opcional
            return 0;
        }
    }

    // Herda o toString() de CaminhaoPequeno, que já é informativo.
    // Poderia sobrescrever se precisasse de algo mais específico para este tipo.
}