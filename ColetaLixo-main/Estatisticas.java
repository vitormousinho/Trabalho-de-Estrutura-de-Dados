import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import Estruturas.Lista;
import caminhoes.CaminhaoGrande;
import java.util.Locale;

/**
 * Classe responsável por coletar, calcular e apresentar estatísticas da simulação
 * de coleta de lixo.
 */
public class Estatisticas implements Serializable {
    private static final long serialVersionUID = 3L; // Incrementa versão devido a novas métricas

    // --- Métricas Gerais ---
    private int lixoTotalGerado = 0;
    private int lixoTotalColetado = 0;
    private int lixoTotalTransportado = 0; // Lixo efetivamente levado ao aterro pelos caminhões grandes

    // --- Métricas de Caminhões Grandes ---
    private int totalInicialCaminhoesGrandes = 0;
    private int caminhoesGrandesAdicionados = 0;
    private int maxCaminhoesGrandesEmUsoSimultaneo = 0;
    private int viagensCaminhoesGrandesAterro = 0;

    // --- Métricas de Caminhões Pequenos ---
    private int totalCaminhoesPequenosAtendidosEstacao = 0;
    private long tempoTotalEsperaFilaPequenos = 0;

    // Inner classes para dados detalhados (públicas para acesso pelo Controller)
    public static class EntradaZona implements Serializable {
        private static final long serialVersionUID = 1L;
        public String nomeZona;
        public int lixoGerado;
        public int lixoColetado;

        EntradaZona(String nome) { this.nomeZona = nome; this.lixoGerado = 0; this.lixoColetado = 0; }
        @Override public String toString() { return nomeZona + "(Ger: " + lixoGerado + ", Col: " + lixoColetado + ")"; }
    }

    public static class EntradaEstacao implements Serializable {
        private static final long serialVersionUID = 2L;
        public String nomeEstacao;
        public int caminhoesPequenosRecebidos;
        public int caminhoesPequenosDescarregados;
        public long tempoTotalEsperaFila;
        public int lixoTransferidoCG; // Lixo transferido por CG desta estação

        EntradaEstacao(String nome) {
            this.nomeEstacao = nome;
            this.caminhoesPequenosRecebidos = 0;
            this.caminhoesPequenosDescarregados = 0;
            this.tempoTotalEsperaFila = 0;
            this.lixoTransferidoCG = 0;
        }

        public void adicionarLixoTransferidoCG(int quantidade) {
            if (quantidade > 0) {
                this.lixoTransferidoCG += quantidade;
            }
        }

        public int getLixoTransferidoCG() {
            return lixoTransferidoCG;
        }

        @Override public String toString() { return nomeEstacao + "(Rec: " + caminhoesPequenosRecebidos + ", Desc: " + caminhoesPequenosDescarregados + ", TransfCG: " + lixoTransferidoCG + ")"; }
    }

    // Lista para armazenar as entradas das zonas e estações
    private Lista<EntradaZona> estatisticasZonas;
    private Lista<EntradaEstacao> estatisticasEstacoes;

    public Estatisticas() {
        resetar();
    }

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
    }

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

    public void registrarTotalInicialCaminhoesGrandes(int totalInicial) {
        this.totalInicialCaminhoesGrandes = totalInicial;
    }

    public void registrarGeracaoLixo(String nomeZona, int quantidadeGerada) {
        if (quantidadeGerada <= 0) return;
        lixoTotalGerado += quantidadeGerada;
        EntradaZona entrada = obterOuCriarEntradaZona(nomeZona);
        entrada.lixoGerado += quantidadeGerada;
    }

    public void registrarColetaLixo(String nomeZona, int quantidadeColetada) {
        if (quantidadeColetada <= 0) return;
        lixoTotalColetado += quantidadeColetada;
        EntradaZona entrada = obterOuCriarEntradaZona(nomeZona);
        entrada.lixoColetado += quantidadeColetada;
    }

    public void registrarChegadaEstacao(String nomeEstacao) {
        EntradaEstacao entrada = obterOuCriarEntradaEstacao(nomeEstacao);
        entrada.caminhoesPequenosRecebidos++;
    }

    public void registrarAtendimentoCaminhaoPequeno(String nomeEstacao, long tempoEspera) {
        totalCaminhoesPequenosAtendidosEstacao++;
        tempoTotalEsperaFilaPequenos += tempoEspera;

        EntradaEstacao entrada = obterOuCriarEntradaEstacao(nomeEstacao);
        entrada.caminhoesPequenosDescarregados++;
        entrada.tempoTotalEsperaFila += tempoEspera;
    }

    public void registrarTransporteLixoCGPorEstacao(String nomeEstacaoOrigem, int quantidadeTransportada) {
        if (quantidadeTransportada <= 0) return;
        lixoTotalTransportado += quantidadeTransportada;
        EntradaEstacao entradaEstacao = obterOuCriarEntradaEstacao(nomeEstacaoOrigem);
        entradaEstacao.adicionarLixoTransferidoCG(quantidadeTransportada);
    }

    public void registrarTransporteLixo(int quantidadeTransportada) {
        if (quantidadeTransportada > 0) { // Apenas para garantir que é uma viagem válida
            viagensCaminhoesGrandesAterro++;
        }
    }

    public void registrarNovoCaminhaoGrande() {
        caminhoesGrandesAdicionados++;
    }

    public void atualizarMaxCaminhoesGrandesEmUso(int caminhoesEmUsoAtualmente) {
        if (caminhoesEmUsoAtualmente > maxCaminhoesGrandesEmUsoSimultaneo) {
            maxCaminhoesGrandesEmUsoSimultaneo = caminhoesEmUsoAtualmente;
        }
    }

    public double calcularTempoMedioEsperaFilaPequenos() {
        if (totalCaminhoesPequenosAtendidosEstacao == 0) return 0.0;
        return (double) tempoTotalEsperaFilaPequenos / totalCaminhoesPequenosAtendidosEstacao;
    }

    public double calcularPercentualLixoColetado() {
        if (lixoTotalGerado == 0) return 0.0;
        return (double) lixoTotalColetado * 100.0 / lixoTotalGerado;
    }

    public int calcularEstimativaCaminhoesGrandesNecessarios() {
        if (lixoTotalColetado == 0) return 0;
        double capacidadeCG = CaminhaoGrande.CAPACIDADE_MAXIMA_KG;
        if (capacidadeCG <= 0) return 1;
        int caminhoesMinimos = (int) Math.ceil((double) lixoTotalColetado / capacidadeCG);
        return Math.max(0, caminhoesMinimos);
    }

    // --- Getters para UI ---
    public int getLixoTotalGerado() { return lixoTotalGerado; }
    public int getLixoTotalColetado() { return lixoTotalColetado; }
    public int getLixoTotalTransportado() { return lixoTotalTransportado; }
    public int getTotalInicialCaminhoesGrandes() { return totalInicialCaminhoesGrandes; }
    public int getCaminhoesGrandesAdicionados() { return caminhoesGrandesAdicionados; }
    public int getMaxCaminhoesGrandesEmUsoSimultaneo() { return maxCaminhoesGrandesEmUsoSimultaneo; }
    public int getViagensCaminhoesGrandesAterro() { return viagensCaminhoesGrandesAterro; }
    public int getTotalCaminhoesPequenosAtendidosEstacao() { return totalCaminhoesPequenosAtendidosEstacao; }

    public Lista<EntradaZona> getEstatisticasZonas() {
        return estatisticasZonas;
    }

    public Lista<EntradaEstacao> getEstatisticasEstacoes() {
        return estatisticasEstacoes;
    }

    public int getLixoGeradoPorZona(String nomeZona) {
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            if (entrada.nomeZona.equals(nomeZona)) {
                return entrada.lixoGerado;
            }
        }
        return 0;
    }

    public int getLixoColetadoPorZona(String nomeZona) {
        for (int i = 0; i < estatisticasZonas.tamanho(); i++) {
            EntradaZona entrada = estatisticasZonas.obter(i);
            if (entrada.nomeZona.equals(nomeZona)) {
                return entrada.lixoColetado;
            }
        }
        return 0;
    }

    public double getTempoMedioEsperaPorEstacao(String nomeEstacao) {
        for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
            EntradaEstacao entrada = estatisticasEstacoes.obter(i);
            if (entrada.nomeEstacao.equals(nomeEstacao)) {
                if (entrada.caminhoesPequenosDescarregados == 0) return 0.0;
                return (double) entrada.tempoTotalEsperaFila / entrada.caminhoesPequenosDescarregados;
            }
        }
        return 0.0;
    }

    public int getLixoTransferidoPorEstacao(String nomeEstacao) {
        for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
            EntradaEstacao entrada = estatisticasEstacoes.obter(i);
            if (entrada.nomeEstacao.equals(nomeEstacao)) {
                return entrada.getLixoTransferidoCG();
            }
        }
        return 0;
    }

    public String gerarRelatorio() {
        // Este método é mantido para logs e como um fallback textual.
        // A UI gráfica usará os getters individuais.
        StringBuilder sb = new StringBuilder();
        Locale brLocale = new Locale("pt", "BR");

        sb.append("          RELATÓRIO TEXTUAL DA SIMULAÇÃO (FALLBACK)\n");
        sb.append("=================================================================================\n\n");

        sb.append("Lixo Gerado por Zona (Texto):\n");
        String[] nomesZonas = {"Sul", "Norte", "Centro", "Leste", "Sudeste"};
        if (estatisticasZonas != null) {
            for (String nomeZona : nomesZonas) {
                int lixoGerado = 0;
                for(int i=0; i < estatisticasZonas.tamanho(); i++){
                    EntradaZona entrada = estatisticasZonas.obter(i);
                    if(entrada.nomeZona.equals(nomeZona)){
                        lixoGerado = entrada.lixoGerado;
                        break;
                    }
                }
                sb.append(String.format(brLocale, "%-10s: %,10d kg\n", nomeZona, lixoGerado));
            }
        } else {
            sb.append(" (Dados de zonas indisponíveis para relatório textual)\n");
        }
        sb.append("\n");

        sb.append("Métricas por Estação (Texto):\n");
        if (estatisticasEstacoes != null && !estatisticasEstacoes.estaVazia()) {
            for (int i = 0; i < estatisticasEstacoes.tamanho(); i++) {
                EntradaEstacao entrada = estatisticasEstacoes.obter(i);
                sb.append(String.format(brLocale, "Estação %-10s: Atendidos: %,d | TME: %.1f min | Lixo Transf.: %,d kg\n",
                        entrada.nomeEstacao,
                        entrada.caminhoesPequenosDescarregados,
                        getTempoMedioEsperaPorEstacao(entrada.nomeEstacao),
                        entrada.getLixoTransferidoCG()));
            }
        } else {
            sb.append(" (Dados de estações indisponíveis para relatório textual)\n");
        }
        sb.append("\n");


        sb.append("--- Totais Gerais ---\n");
        sb.append(String.format(brLocale, "Lixo Total Gerado: %,d kg\n", getLixoTotalGerado()));
        sb.append(String.format(brLocale, "Lixo Total Coletado: %,d kg (%.2f%%)\n", getLixoTotalColetado(), calcularPercentualLixoColetado()));
        sb.append(String.format(brLocale, "Lixo Total Transportado (CG): %,d kg\n", getLixoTotalTransportado()));
        sb.append(String.format(brLocale, "Caminhões Grandes Adicionados: %d\n", getCaminhoesGrandesAdicionados()));
        sb.append(String.format(brLocale, "Estimativa Mínima de CGs: %d\n", calcularEstimativaCaminhoesGrandesNecessarios()));

        sb.append("=================================================================================\n");
        return sb.toString();
    }

    public void salvarRelatorio(String nomeArquivo) throws IOException {
        System.out.println("Salvando relatório textual em: " + nomeArquivo);
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo), true)) {
            writer.print(gerarRelatorio()); // Salva o relatório textual
        }
        System.out.println("Relatório textual salvo com sucesso em " + nomeArquivo + ".");
    }
}