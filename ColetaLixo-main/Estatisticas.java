import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import Estruturas.Lista; // Usa a Lista customizada
import caminhoes.CaminhaoGrande; // Import necessário

/**
 * Classe responsável por coletar, calcular e apresentar estatísticas da simulação
 * de coleta de lixo.
 */
public class Estatisticas implements Serializable {
    private static final long serialVersionUID = 2L; // Incrementa versão devido a novas métricas

    // --- Métricas Gerais ---
    private int lixoTotalGerado = 0;
    private int lixoTotalColetado = 0;
    private int lixoTotalTransportado = 0; // Lixo efetivamente levado ao aterro pelos caminhões grandes

    // --- Métricas de Caminhões Grandes ---
    private int totalInicialCaminhoesGrandes = 0; // Quantos começaram
    private int caminhoesGrandesAdicionados = 0;  // Quantos foram adicionados durante a simulação
    private int maxCaminhoesGrandesEmUsoSimultaneo = 0; // Máximo de CGs operando ao mesmo tempo
    private int viagensCaminhoesGrandesAterro = 0;    // Quantas viagens os CGs fizeram ao aterro

    // --- Métricas de Caminhões Pequenos ---
    private int totalCaminhoesPequenosAtendidosEstacao = 0; // Quantos CPs foram descarregados nas estações
    private long tempoTotalEsperaFilaPequenos = 0;      // Soma do tempo de espera de todos CPs nas filas das estações

    // --- Estruturas Internas para Dados Detalhados ---

    // Estrutura para dados por Zona
    private static class EntradaZona implements Serializable {
        private static final long serialVersionUID = 1L;
        String nomeZona;
        int lixoGerado;
        int lixoColetado;

        EntradaZona(String nome) { this.nomeZona = nome; this.lixoGerado = 0; this.lixoColetado = 0; }
        @Override public String toString() { return nomeZona + "(Ger: " + lixoGerado + ", Col: " + lixoColetado + ")"; }
    }

    // Estrutura para dados por Estação
    private static class EntradaEstacao implements Serializable {
        private static final long serialVersionUID = 1L;
        String nomeEstacao;
        int caminhoesPequenosRecebidos; // Quantos CPs chegaram à estação
        int caminhoesPequenosDescarregados; // Quantos CPs foram efetivamente descarregados
        long tempoTotalEsperaFila; // Tempo acumulado de espera na fila para esta estação
        // Adicionar lixo transferido se necessário para getLixoTransferidoPorEstacao
        // int lixoTransferido;

        EntradaEstacao(String nome) {
            this.nomeEstacao = nome;
            this.caminhoesPequenosRecebidos = 0;
            this.caminhoesPequenosDescarregados = 0;
            this.tempoTotalEsperaFila = 0;
            // this.lixoTransferido = 0;
        }
        @Override public String toString() { return nomeEstacao + "(Rec: " + caminhoesPequenosRecebidos + ", Desc: " + caminhoesPequenosDescarregados + ")"; }
    }

    // Listas customizadas para armazenar as entradas
    private Lista<EntradaZona> estatisticasZonas;
    private Lista<EntradaEstacao> estatisticasEstacoes;

    /**
     * Construtor. Inicializa as listas de estatísticas.
     */
    public Estatisticas() {
        resetar(); // Inicializa tudo no resetar
    }

    /**
     * Reseta todas as métricas para o estado inicial (zero).
     * Chamado no início de uma nova simulação.
     */
    public final void resetar() {
        lixoTotalGerado = 0;
        lixoTotalColetado = 0;
        lixoTotalTransportado = 0;
        totalInicialCaminhoesGrandes = 0;
        caminhoesGrandesAdicionados = 0;
        maxCaminhoesGrandesEmUsoSimultaneo = 0;
        viagensCaminhoesGrandesAterro = 0;
        totalCaminhoesPequenosAtendidosEstacao = 0;
        tempoTotalEsperaFilaPequenos = 0;

        estatisticasZonas = new Lista<>();
        estatisticasEstacoes = new Lista<>();
        // System.out.println("DEBUG: Estatísticas resetadas."); // Log pode ser útil para depuração
    }


    // --- Métodos Privados para Gerenciar Entradas ---

    /** Busca ou cria uma entrada para a zona especificada. */
    private EntradaZona obterOuCriarEntradaZona(String nomeZona) {
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            if (entrada.nomeZona.equals(nomeZona)) {
                return entrada;
            }
        }
        EntradaZona novaEntrada = new EntradaZona(nomeZona);
        estatisticasZonas.adicionar(novaEntrada);
        return novaEntrada;
    }

    /** Busca ou cria uma entrada para a estação especificada. */
    private EntradaEstacao obterOuCriarEntradaEstacao(String nomeEstacao) {
        for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
            EntradaEstacao entrada = estatisticasEstacoes.obter(i);
            if (entrada.nomeEstacao.equals(nomeEstacao)) {
                return entrada;
            }
        }
        EntradaEstacao novaEntrada = new EntradaEstacao(nomeEstacao);
        estatisticasEstacoes.adicionar(novaEntrada);
        return novaEntrada;
    }

    // --- Métodos Públicos para Registrar Eventos ---

    /** Registra a quantidade inicial de caminhões grandes no início da simulação. */
    public void registrarTotalInicialCaminhoesGrandes(int totalInicial) {
        this.totalInicialCaminhoesGrandes = totalInicial;
    }

    /** Registra a geração de lixo em uma zona específica. */
    public void registrarGeracaoLixo(String nomeZona, int quantidadeGerada) {
        if (quantidadeGerada <= 0) return;
        lixoTotalGerado += quantidadeGerada;
        EntradaZona entrada = obterOuCriarEntradaZona(nomeZona);
        entrada.lixoGerado += quantidadeGerada;
    }

    /** Registra a coleta de lixo de uma zona específica por um caminhão pequeno. */
    public void registrarColetaLixo(String nomeZona, int quantidadeColetada) {
        if (quantidadeColetada <= 0) return;
        lixoTotalColetado += quantidadeColetada;
        EntradaZona entrada = obterOuCriarEntradaZona(nomeZona);
        entrada.lixoColetado += quantidadeColetada;
    }

    /** Registra a chegada de um caminhão pequeno a uma estação. */
    public void registrarChegadaEstacao(String nomeEstacao) {
        EntradaEstacao entrada = obterOuCriarEntradaEstacao(nomeEstacao);
        entrada.caminhoesPequenosRecebidos++;
    }

    /**
     * Registra que um caminhão pequeno foi atendido (descarregou) em uma estação.
     * @param nomeEstacao Nome da estação.
     * @param tempoEspera O tempo (em minutos) que o caminhão esperou na fila.
     */
    public void registrarAtendimentoCaminhaoPequeno(String nomeEstacao, long tempoEspera) {
        totalCaminhoesPequenosAtendidosEstacao++;
        tempoTotalEsperaFilaPequenos += tempoEspera;

        EntradaEstacao entrada = obterOuCriarEntradaEstacao(nomeEstacao);
        entrada.caminhoesPequenosDescarregados++;
        entrada.tempoTotalEsperaFila += tempoEspera;
    }

    /** Registra que um caminhão grande partiu para o aterro. */
    public void registrarTransporteLixo(int quantidadeTransportada) {
        if (quantidadeTransportada <= 0) return;
        lixoTotalTransportado += quantidadeTransportada;
        viagensCaminhoesGrandesAterro++;
        // Se precisar rastrear lixo transferido por estação, seria aqui,
        // mas precisaria saber de qual estação o CG partiu.
        // Se um CG é exclusivo de uma estação enquanto carrega, pode-se inferir.
    }

    /** Registra que um novo caminhão grande foi adicionado à frota durante a simulação. */
    public void registrarNovoCaminhaoGrande() {
        caminhoesGrandesAdicionados++;
    }

    /** Atualiza o número máximo de caminhões grandes que estiveram em uso simultaneamente. */
    public void atualizarMaxCaminhoesGrandesEmUso(int caminhoesEmUsoAtualmente) {
        if (caminhoesEmUsoAtualmente > maxCaminhoesGrandesEmUsoSimultaneo) {
            maxCaminhoesGrandesEmUsoSimultaneo = caminhoesEmUsoAtualmente;
        }
    }

    // --- Métodos Públicos para Calcular e Obter Estatísticas ---

    /** Calcula o tempo médio de espera na fila para os caminhões pequenos atendidos. */
    public double calcularTempoMedioEsperaFilaPequenos() {
        if (totalCaminhoesPequenosAtendidosEstacao == 0) return 0.0;
        return (double) tempoTotalEsperaFilaPequenos / totalCaminhoesPequenosAtendidosEstacao;
    }

    /** Calcula o percentual de lixo coletado em relação ao total gerado. */
    public double calcularPercentualLixoColetado() {
        if (lixoTotalGerado == 0) return 0.0; // Evita divisão por zero se nenhum lixo for gerado
        return (double) lixoTotalColetado * 100.0 / lixoTotalGerado;
    }

    /**
     * Estima o número mínimo de caminhões grandes que seriam necessários para
     * transportar todo o lixo coletado, assumindo uma viagem por dia por caminhão grande.
     * @return Uma estimativa do número mínimo de caminhões grandes.
     */
    public int calcularEstimativaCaminhoesGrandesNecessarios() {
        if (lixoTotalColetado == 0) return 0; // Se não há lixo coletado, não precisa de CG

        double capacidadeCG = CaminhaoGrande.CAPACIDADE_MAXIMA_KG;
        if (capacidadeCG <= 0) return 1; // Evita divisão por zero e supõe ao menos 1 se houver lixo

        // Arredonda para cima para garantir que todo o lixo seja transportado
        int caminhoesMinimos = (int) Math.ceil((double) lixoTotalColetado / capacidadeCG);
        return Math.max(0, caminhoesMinimos); // Garante que não seja negativo (embora ceil deva cuidar disso)
    }

    // --- Getters para acesso aos valores das estatísticas ---
    public int getLixoTotalGerado() { return lixoTotalGerado; }
    public int getLixoTotalColetado() { return lixoTotalColetado; }
    public int getLixoTotalTransportado() { return lixoTotalTransportado; }
    public int getTotalInicialCaminhoesGrandes() { return totalInicialCaminhoesGrandes; }
    public int getCaminhoesGrandesAdicionados() { return caminhoesGrandesAdicionados; }
    public int getMaxCaminhoesGrandesEmUsoSimultaneo() { return maxCaminhoesGrandesEmUsoSimultaneo; }
    public int getViagensCaminhoesGrandesAterro() { return viagensCaminhoesGrandesAterro; }
    public int getTotalCaminhoesPequenosAtendidosEstacao() { return totalCaminhoesPequenosAtendidosEstacao; }
    public long getTempoTotalEsperaFilaPequenos() { return tempoTotalEsperaFilaPequenos; }

    // --- NOVOS Getters para a UI ---
    public int getLixoGeradoPorZona(String nomeZona) {
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            if (entrada.nomeZona.equals(nomeZona)) {
                return entrada.lixoGerado;
            }
        }
        return 0; // Retorna 0 se a zona não for encontrada
    }

    public int getLixoColetadoPorZona(String nomeZona) {
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            if (entrada.nomeZona.equals(nomeZona)) {
                return entrada.lixoColetado;
            }
        }
        return 0; // Retorna 0 se a zona não for encontrada
    }

    public double getTempoMedioEsperaPorEstacao(String nomeEstacao) {
        for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
            EntradaEstacao entrada = estatisticasEstacoes.obter(i);
            if (entrada.nomeEstacao.equals(nomeEstacao)) {
                if (entrada.caminhoesPequenosDescarregados == 0) return 0.0;
                return (double) entrada.tempoTotalEsperaFila / entrada.caminhoesPequenosDescarregados;
            }
        }
        return 0.0; // Retorna 0.0 se a estação não for encontrada
    }

    public int getLixoTransferidoPorEstacao(String nomeEstacao) {
        // Esta métrica não está sendo explicitamente rastreada por estação.
        // O lixoTotalTransportado é geral. Para ter por estação,
        // precisaríamos saber de qual estação cada CG partiu ao registrar o transporte.
        // Por agora, retornaremos 0 ou um valor placeholder.
        // Se a lógica do simulador garante que um CG está associado a uma estação
        // enquanto carrega, você poderia somar a carga dos CGs que partiram daquela estação.
        // Para este exemplo, vamos retornar 0, indicando que precisa de implementação.
        return 0; // Placeholder - Implementação necessária se este dado for crucial.
        // Uma forma seria modificar registrarTransporteLixo para incluir a estação de origem do CG.
    }


    /**
     * Gera um relatório textual completo das estatísticas coletadas.
     * @return Uma String formatada contendo o relatório.
     */
    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();
        String separadorLinha = "\n--------------------------------------------------------\n";
        sb.append("\n========== RELATÓRIO FINAL DE ESTATÍSTICAS ==========\n");

        sb.append("\n--- MÉTRICAS GERAIS DE LIXO ---\n");
        sb.append(String.format("Lixo Total Gerado:        %,10d kg%n", lixoTotalGerado));
        sb.append(String.format("Lixo Total Coletado (CP): %,10d kg (%.2f%% do gerado)%n", lixoTotalColetado, calcularPercentualLixoColetado()));
        sb.append(String.format("Lixo Total Transportado(CG):%,10d kg%n", lixoTotalTransportado));
        int lixoNaoColetado = lixoTotalGerado - lixoTotalColetado;
        sb.append(String.format("Lixo Não Coletado (Zonas):%,10d kg%n", Math.max(0, lixoNaoColetado)));


        sb.append(separadorLinha);
        sb.append("--- MÉTRICAS DE CAMINHÕES GRANDES (CG) ---\n");
        sb.append(String.format("Frota Inicial:            %d%n", totalInicialCaminhoesGrandes));
        sb.append(String.format("Adicionados Durante Sim.: %d%n", caminhoesGrandesAdicionados));
        sb.append(String.format("Frota Final Total:        %d%n", totalInicialCaminhoesGrandes + caminhoesGrandesAdicionados));
        sb.append(String.format("Máximo em Uso Simultâneo: %d%n", maxCaminhoesGrandesEmUsoSimultaneo));
        sb.append(String.format("Viagens para Aterro:      %d%n", viagensCaminhoesGrandesAterro));

        sb.append(separadorLinha);
        sb.append("--- MÉTRICAS DE CAMINHÕES PEQUENOS (CP) ---\n");
        sb.append(String.format("Total Atendidos Estações: %d%n", totalCaminhoesPequenosAtendidosEstacao));
        sb.append(String.format("Tempo Médio Espera Fila:  %.2f minutos%n", calcularTempoMedioEsperaFilaPequenos()));

        sb.append(separadorLinha);
        sb.append("--- MÉTRICAS POR ZONA ---\n");
        if (estatisticasZonas.estaVazia()) {
            sb.append("(Nenhuma zona processada)\n");
        } else {
            for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
                EntradaZona entrada = estatisticasZonas.obter(i);
                double percColetadoZona = (entrada.lixoGerado == 0) ? 0.0 : (double) entrada.lixoColetado * 100.0 / entrada.lixoGerado;
                sb.append(String.format("  - %-10s: Gerado: %,7d kg | Coletado: %,7d kg (%.2f%%)%n",
                        entrada.nomeZona, entrada.lixoGerado, entrada.lixoColetado, percColetadoZona));
            }
        }

        sb.append(separadorLinha);
        sb.append("--- MÉTRICAS POR ESTAÇÃO ---\n");
        if (estatisticasEstacoes.estaVazia()) {
            sb.append("(Nenhuma estação processada)\n");
        } else {
            for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
                EntradaEstacao entrada = estatisticasEstacoes.obter(i);
                double tempoMedioEsperaEstacao = (entrada.caminhoesPequenosDescarregados == 0) ? 0.0 : (double) entrada.tempoTotalEsperaFila / entrada.caminhoesPequenosDescarregados;
                sb.append(String.format("  - %-10s: CP Recebidos: %5d | CP Descarregados: %5d | T. Médio Espera Fila: %.2f min%n",
                        entrada.nomeEstacao, entrada.caminhoesPequenosRecebidos, entrada.caminhoesPequenosDescarregados, tempoMedioEsperaEstacao));
            }
        }

        sb.append(separadorLinha);
        sb.append("--- CONCLUSÃO ESTIMADA ---\n");
        sb.append(String.format("Estimativa Mínima de CGs (baseado no lixo coletado): %d%n", calcularEstimativaCaminhoesGrandesNecessarios()));

        sb.append("\n========================================================\n");
        return sb.toString();
    }

    /**
     * Salva o relatório gerado em um arquivo de texto.
     * @param nomeArquivo O caminho/nome do arquivo onde salvar o relatório.
     * @throws IOException Se ocorrer um erro durante a escrita do arquivo.
     */
    public void salvarRelatorio(String nomeArquivo) throws IOException {
        System.out.println("Salvando relatório em: " + nomeArquivo); // Este System.out será capturado pela GUI
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo), true)) { // true para autoFlush
            writer.print(gerarRelatorio());
        }
        System.out.println("Relatório salvo com sucesso em " + nomeArquivo + ".");
    }
}