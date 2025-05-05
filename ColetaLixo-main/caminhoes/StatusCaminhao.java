package caminhoes;

/**
 * Enum que representa os possíveis estados de um caminhão pequeno durante a simulação.
 * Este enum é utilizado para controlar o fluxo dos caminhões na simulação.
 */
public enum StatusCaminhao {
    /**
     * Caminhão está ocioso, aguardando ser designado para uma zona
     */
    OCIOSO,

    /**
     * Caminhão está coletando lixo em uma zona
     */
    COLETANDO,

    /**
     * Caminhão está em viagem para uma estação de transferência
     */
    VIAJANDO_ESTACAO,

    /**
     * Caminhão está aguardando na fila de uma estação
     */
    NA_FILA,

    /**
     * Caminhão está descarregando na estação
     */
    DESCARREGANDO,

    /**
     * Caminhão está retornando para uma zona após descarregar
     */
    RETORNANDO_ZONA,

    /**
     * Caminhão atingiu o limite diário de viagens e está inativo
     */
    INATIVO_LIMITE_VIAGENS
}