package zonas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe que representa o mapa urbano da cidade, contendo as distâncias entre zonas
 * e métodos para encontrar zonas próximas.
 */
public class MapaUrbano implements Serializable {
    private static final long serialVersionUID = 1L;

    // Matriz de distâncias entre zonas (em unidades arbitrárias)
    private int[][] distancias;

    // Mapeamento de nomes de zonas para índices na matriz
    private Map<String, Integer> indicesZonas;

    // Lista de zonas registradas
    private List<String> zonasRegistradas;

    /**
     * Construtor que inicializa o mapa urbano com as zonas de Teresina.
     */
    public MapaUrbano() {
        indicesZonas = new HashMap<>();
        zonasRegistradas = new ArrayList<>();

        // Registra as 5 zonas de Teresina
        registrarZona("Sul");
        registrarZona("Norte");
        registrarZona("Centro");
        registrarZona("Leste");
        registrarZona("Sudeste");

        // Inicializa a matriz de distâncias
        int numZonas = zonasRegistradas.size();
        distancias = new int[numZonas][numZonas];

        // Define as distâncias entre as zonas (valores hipotéticos, ajuste conforme necessário)
        // Valores menores indicam zonas mais próximas
        // Considerando: Sul(0), Norte(1), Centro(2), Leste(3), Sudeste(4)

        // Sul para outras
        setDistancia("Sul", "Sul", 0);
        setDistancia("Sul", "Norte", 8);
        setDistancia("Sul", "Centro", 3);
        setDistancia("Sul", "Leste", 5);
        setDistancia("Sul", "Sudeste", 4);

        // Norte para outras
        setDistancia("Norte", "Norte", 0);
        setDistancia("Norte", "Centro", 5);
        setDistancia("Norte", "Leste", 4);
        setDistancia("Norte", "Sudeste", 6);

        // Centro para outras
        setDistancia("Centro", "Centro", 0);
        setDistancia("Centro", "Leste", 2);
        setDistancia("Centro", "Sudeste", 3);

        // Leste para outras
        setDistancia("Leste", "Leste", 0);
        setDistancia("Leste", "Sudeste", 3);

        // Sudeste para Sudeste
        setDistancia("Sudeste", "Sudeste", 0);
    }

    /**
     * Registra uma zona no mapa.
     * @param nomeZona Nome da zona a ser registrada.
     */
    private void registrarZona(String nomeZona) {
        if (!indicesZonas.containsKey(nomeZona)) {
            int indice = zonasRegistradas.size();
            indicesZonas.put(nomeZona, indice);
            zonasRegistradas.add(nomeZona);
        }
    }

    /**
     * Define a distância entre duas zonas.
     * Como a matriz é simétrica, a distância é definida em ambas as direções.
     * @param zona1 Nome da primeira zona.
     * @param zona2 Nome da segunda zona.
     * @param distancia Valor da distância entre as zonas.
     */
    private void setDistancia(String zona1, String zona2, int distancia) {
        if (indicesZonas.containsKey(zona1) && indicesZonas.containsKey(zona2)) {
            int idx1 = indicesZonas.get(zona1);
            int idx2 = indicesZonas.get(zona2);
            distancias[idx1][idx2] = distancia;
            distancias[idx2][idx1] = distancia; // Matriz simétrica
        }
    }

    /**
     * Obtém a distância entre duas zonas.
     * @param zona1 Nome da primeira zona.
     * @param zona2 Nome da segunda zona.
     * @return A distância entre as zonas, ou -1 se alguma zona não estiver registrada.
     */
    public int getDistancia(String zona1, String zona2) {
        if (indicesZonas.containsKey(zona1) && indicesZonas.containsKey(zona2)) {
            int idx1 = indicesZonas.get(zona1);
            int idx2 = indicesZonas.get(zona2);
            return distancias[idx1][idx2];
        }
        return -1; // Zona não encontrada
    }

    /**
     * Obtém a distância entre duas zonas usando seus índices na matriz.
     * @param idxZona1 Índice da primeira zona.
     * @param idxZona2 Índice da segunda zona.
     * @return A distância entre as zonas, ou -1 se algum índice for inválido.
     */
    public int getDistancia(int idxZona1, int idxZona2) {
        if (idxZona1 >= 0 && idxZona1 < distancias.length &&
                idxZona2 >= 0 && idxZona2 < distancias.length) {
            return distancias[idxZona1][idxZona2];
        }
        return -1; // Índice inválido
    }

    /**
     * Encontra a zona mais próxima de uma zona de origem dentre uma lista de zonas candidatas.
     * @param zonaOrigem Nome da zona de origem.
     * @param zonasCandidatas Lista de nomes de zonas candidatas.
     * @return O nome da zona mais próxima, ou null se não for possível determinar.
     */
    public String encontrarZonaMaisProxima(String zonaOrigem, List<String> zonasCandidatas) {
        if (!indicesZonas.containsKey(zonaOrigem) || zonasCandidatas == null || zonasCandidatas.isEmpty()) {
            return null;
        }

        String zonaMaisProxima = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (String zona : zonasCandidatas) {
            if (indicesZonas.containsKey(zona)) {
                int dist = getDistancia(zonaOrigem, zona);
                if (dist >= 0 && dist < menorDistancia) {
                    menorDistancia = dist;
                    zonaMaisProxima = zona;
                }
            }
        }

        return zonaMaisProxima;
    }

    /**
     * Retorna o índice de uma zona na matriz de distâncias.
     * @param nomeZona Nome da zona.
     * @return O índice da zona, ou -1 se não estiver registrada.
     */
    public int getIndiceZona(String nomeZona) {
        return indicesZonas.getOrDefault(nomeZona, -1);
    }

    /**
     * Retorna o nome de uma zona a partir de seu índice.
     * @param indiceZona Índice da zona.
     * @return O nome da zona, ou null se o índice for inválido.
     */
    public String getNomeZona(int indiceZona) {
        if (indiceZona >= 0 && indiceZona < zonasRegistradas.size()) {
            return zonasRegistradas.get(indiceZona);
        }
        return null;
    }
}