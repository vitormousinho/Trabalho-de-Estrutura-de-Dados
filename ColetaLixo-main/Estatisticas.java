import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import Estruturas.Lista;

/**
 * Classe responsável por coletar e calcular estatísticas da simulação.
 * Implementa estruturas próprias em vez de usar HashMap/Map do Java.
 */
public class Estatisticas implements Serializable {
    private static final long serialVersionUID = 1L;

    // Métricas gerais
    private int lixoTotalGerado = 0;
    private int lixoTotalColetado = 0;
    private int lixoTotalTransportado = 0;

    // Métricas de caminhões
    private int maxCaminhoesGrandesEmUso = 0;
    private int caminhoesGrandesAdicionados = 0;
    private int viagensCaminhoesGrandes = 0;

    // Métricas de tempo
    private long tempoTotalEsperaCaminhoesPequenos = 0;
    private int numCaminhoesPequenosAtendidos = 0;

    // Classes internas próprias para associação chave-valor
    private class EntradaZona implements Serializable {
        String nomeZona;
        int lixoGerado;
        int lixoColetado;

        public EntradaZona(String nome) {
            this.nomeZona = nome;
            this.lixoGerado = 0;
            this.lixoColetado = 0;
        }
    }

    private class EntradaEstacao implements Serializable {
        String nomeEstacao;
        int caminhoesAtendidos;
        long tempoTotalEspera;

        public EntradaEstacao(String nome) {
            this.nomeEstacao = nome;
            this.caminhoesAtendidos = 0;
            this.tempoTotalEspera = 0;
        }
    }

    // Listas próprias usando a classe Lista já desenvolvida
    private Lista<EntradaZona> estatisticasZonas;
    private Lista<EntradaEstacao> estatisticasEstacoes;

    public Estatisticas() {
        estatisticasZonas = new Lista<>();
        estatisticasEstacoes = new Lista<>();
    }

    /**
     * Busca uma entrada para a zona ou cria uma nova se não existir
     */
    private EntradaZona obterEntradaZona(String nomeZona) {
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            if (entrada.nomeZona.equals(nomeZona)) {
                return entrada;
            }
        }

        // Se não existir, cria uma nova entrada
        EntradaZona novaEntrada = new EntradaZona(nomeZona);
        estatisticasZonas.adicionar(novaEntrada);
        return novaEntrada;
    }

    /**
     * Busca uma entrada para a estação ou cria uma nova se não existir
     */
    private EntradaEstacao obterEntradaEstacao(String nomeEstacao) {
        for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
            EntradaEstacao entrada = estatisticasEstacoes.obter(i);
            if (entrada.nomeEstacao.equals(nomeEstacao)) {
                return entrada;
            }
        }

        // Se não existir, cria uma nova entrada
        EntradaEstacao novaEntrada = new EntradaEstacao(nomeEstacao);
        estatisticasEstacoes.adicionar(novaEntrada);
        return novaEntrada;
    }

    /**
     * Registra geração de lixo em uma zona
     */
    public void registrarGeracaoLixo(String nomeZona, int quantidade) {
        lixoTotalGerado += quantidade;
        EntradaZona entrada = obterEntradaZona(nomeZona);
        entrada.lixoGerado += quantidade;
    }

    /**
     * Registra coleta de lixo de uma zona
     */
    public void registrarColetaLixo(String nomeZona, int quantidade) {
        lixoTotalColetado += quantidade;
        EntradaZona entrada = obterEntradaZona(nomeZona);
        entrada.lixoColetado += quantidade;
    }

    /**
     * Registra transporte de lixo por um caminhão grande
     */
    public void registrarTransporteLixo(int quantidade) {
        lixoTotalTransportado += quantidade;
        viagensCaminhoesGrandes++;
    }

    /**
     * Registra adição de um novo caminhão grande
     */
    public void registrarNovoCaminhaoGrande() {
        caminhoesGrandesAdicionados++;
    }

    /**
     * Atualiza o número máximo de caminhões grandes em uso
     */
    public void atualizarMaxCaminhoesGrandesEmUso(int caminhoesEmUso) {
        if (caminhoesEmUso > maxCaminhoesGrandesEmUso) {
            maxCaminhoesGrandesEmUso = caminhoesEmUso;
        }
    }

    /**
     * Registra o atendimento de um caminhão pequeno em uma estação
     */
    public void registrarAtendimentoCaminhaoPequeno(String nomeEstacao, long tempoEspera) {
        tempoTotalEsperaCaminhoesPequenos += tempoEspera;
        numCaminhoesPequenosAtendidos++;

        EntradaEstacao entrada = obterEntradaEstacao(nomeEstacao);
        entrada.caminhoesAtendidos++;
        entrada.tempoTotalEspera += tempoEspera;
    }

    /**
     * Calcula o tempo médio de espera dos caminhões pequenos
     */
    public double calcularTempoMedioEspera() {
        if (numCaminhoesPequenosAtendidos == 0) return 0;
        return (double) tempoTotalEsperaCaminhoesPequenos / numCaminhoesPequenosAtendidos;
    }

    /**
     * Calcula o número mínimo de caminhões grandes necessários
     * baseado na geração diária de lixo e na capacidade dos caminhões
     */
    public int calcularCaminhoesGrandesNecessarios() {
        // Estimativa simples baseada na quantidade total de lixo transportado
        // dividido pela capacidade de um caminhão grande (20.000 kg)
        double lixoMedioDiario = lixoTotalGerado / (viagensCaminhoesGrandes > 0 ?
                viagensCaminhoesGrandes : 1);

        // Considerando 20.000 kg por caminhão grande (20 toneladas)
        int caminhoesMinimos = (int) Math.ceil(lixoMedioDiario / 20000.0);

        // Garantir ao menos 1 caminhão
        return Math.max(1, caminhoesMinimos);
    }

    /**
     * Gera um relatório completo das estatísticas
     */
    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== RELATÓRIO DE ESTATÍSTICAS ==========\n\n");

        sb.append("--- MÉTRICAS GERAIS ---\n");
        sb.append("Lixo Total Gerado: ").append(lixoTotalGerado).append(" kg\n");
        sb.append("Lixo Total Coletado: ").append(lixoTotalColetado).append(" kg (")
                .append(String.format("%.2f", (lixoTotalColetado * 100.0 /
                        (lixoTotalGerado > 0 ? lixoTotalGerado : 1)))).append("%)\n");
        sb.append("Lixo Total Transportado: ").append(lixoTotalTransportado).append(" kg\n\n");

        sb.append("--- MÉTRICAS DE CAMINHÕES ---\n");
        sb.append("Caminhões Grandes Adicionados: ").append(caminhoesGrandesAdicionados).append("\n");
        sb.append("Máximo de Caminhões Grandes Em Uso: ").append(maxCaminhoesGrandesEmUso).append("\n");
        sb.append("Viagens de Caminhões Grandes: ").append(viagensCaminhoesGrandes).append("\n");
        sb.append("Caminhões Pequenos Atendidos: ").append(numCaminhoesPequenosAtendidos).append("\n\n");

        sb.append("--- MÉTRICAS DE TEMPO ---\n");
        sb.append("Tempo Médio de Espera: ").append(String.format("%.2f", calcularTempoMedioEspera()))
                .append(" minutos\n\n");

        sb.append("--- MÉTRICAS POR ZONA ---\n");
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            double percentual = (entrada.lixoColetado * 100.0) /
                    (entrada.lixoGerado > 0 ? entrada.lixoGerado : 1);

            sb.append(entrada.nomeZona).append(": ")
                    .append(entrada.lixoGerado).append("kg gerado, ")
                    .append(entrada.lixoColetado).append("kg coletado (")
                    .append(String.format("%.2f", percentual)).append("%)\n");
        }
        sb.append("\n");

        sb.append("--- MÉTRICAS POR ESTAÇÃO ---\n");
        for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
            EntradaEstacao entrada = estatisticasEstacoes.obter(i);
            double tempoMedio = entrada.caminhoesAtendidos > 0 ?
                    (double)entrada.tempoTotalEspera / entrada.caminhoesAtendidos : 0;

            sb.append(entrada.nomeEstacao).append(": ")
                    .append(entrada.caminhoesAtendidos).append(" caminhões atendidos, tempo médio de espera: ")
                    .append(String.format("%.2f", tempoMedio)).append(" minutos\n");
        }
        sb.append("\n");

        sb.append("--- CONCLUSÃO ---\n");
        sb.append("Número mínimo estimado de caminhões grandes necessários: ")
                .append(calcularCaminhoesGrandesNecessarios()).append("\n");

        sb.append("\n=================================================\n");
        return sb.toString();
    }

    /**
     * Salva o relatório em um arquivo
     */
    public void salvarRelatorio(String nomeArquivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.print(gerarRelatorio());
        }
    }

    /**
     * Reseta todas as estatísticas
     */
    public void resetar() {
        lixoTotalGerado = 0;
        lixoTotalColetado = 0;
        lixoTotalTransportado = 0;
        maxCaminhoesGrandesEmUso = 0;
        caminhoesGrandesAdicionados = 0;
        viagensCaminhoesGrandes = 0;
        tempoTotalEsperaCaminhoesPequenos = 0;
        numCaminhoesPequenosAtendidos = 0;

        // Limpar as listas
        estatisticasZonas = new Lista<>();
        estatisticasEstacoes = new Lista<>();
    }
}