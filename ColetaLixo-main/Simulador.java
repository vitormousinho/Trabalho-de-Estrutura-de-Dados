import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

// Importe as classes necessárias
import Estruturas.Lista;
import Estruturas.Fila;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.StatusCaminhao;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;
import caminhoes.CaminhaoGrandePadrao;

public class Simulador implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Timer timer;
    private int tempoSimulado = 0; // Em minutos simulados
    private boolean pausado = false;
    private transient Random random = new Random();

    // Novo atributo para estatísticas
    private Estatisticas estatisticas = new Estatisticas();

    // Contadores para números totais
    private int totalCaminhoesGrandes = 0;
    private int caminhoesGrandesEmUso = 0;

    // --- Atributos para guardar os elementos ---
    private Lista<CaminhaoPequeno> listaCaminhoesPequenos;
    private Lista<CaminhaoGrande> listaCaminhoesGrandes;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;

    // Listas adicionais para acompanhar o estado dos caminhões
    private Lista<CaminhaoPequeno> caminhoesEmViagem = new Lista<>();
    private Lista<CaminhaoPequeno> caminhoesEmColeta = new Lista<>();

    // Novo atributo para tolerância de espera padrão dos caminhões grandes
    private int toleranciaCaminhoesGrandes = 30; // valor padrão em minutos

    // --- PARÂMETROS DE CONFIGURAÇÃO (Horário de Pico e Tempos de Viagem) ---
    private int picoManhaInicio = 7 * 60; // 420
    private int picoManhaFim = 9 * 60 - 1; // 539
    private int picoTardeInicio = 17 * 60; // 1020
    private int picoTardeFim = 19 * 60 - 1; // 1139

    private int minViagemForaPico = 15;
    private int maxViagemForaPico = 45;
    private int minViagemPico = 30;
    private int maxViagemPico = 90;

    // Novo parâmetro para intervalo de estatísticas
    private int intervaloEstatisticas = 60; // a cada 60 minutos (1 hora simulada)

    // Construtor
    public Simulador() {
        this.listaCaminhoesPequenos = new Lista<>();
        this.listaCaminhoesGrandes = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
        if (random == null) {
            random = new Random();
        }
    }

    // --- Métodos Setters para as listas ---
    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) { this.listaCaminhoesPequenos = lista; }
    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) {
        this.listaCaminhoesGrandes = lista;
        this.totalCaminhoesGrandes = lista.tamanho();
    }
    public void setListaZonas(Lista<ZonaUrbana> lista) { this.listaZonas = lista; }
    public void setListaEstacoes(Lista<EstacaoTransferencia> lista) { this.listaEstacoes = lista; }

    // Novo setter para tolerância de caminhões grandes
    public void setToleranciaCaminhoesGrandes(int tolerancia) {
        if (tolerancia < 1) {
            throw new IllegalArgumentException("A tolerância deve ser pelo menos 1 minuto.");
        }
        this.toleranciaCaminhoesGrandes = tolerancia;

        // Atualiza a tolerância em todos os caminhões grandes existentes
        for (int i = 0; i < listaCaminhoesGrandes.tamanho(); i++) {
            CaminhaoGrande caminhao = listaCaminhoesGrandes.obter(i);
            caminhao.setToleranciaEspera(tolerancia);
        }
    }

    // --- MÉTODOS DE CONTROLE ---
    public void iniciar() {
        if (timer != null) {
            System.out.println("Simulação já estava iniciada. Para reiniciar, encerre primeiro.");
            return;
        }
        System.out.println("Simulação iniciada...");
        pausado = false;
        tempoSimulado = 0;

        // Inicializar listas auxiliares
        caminhoesEmViagem = new Lista<>();
        caminhoesEmColeta = new Lista<>();

        // Inicializar contador de caminhões grandes
        totalCaminhoesGrandes = listaCaminhoesGrandes.tamanho();
        caminhoesGrandesEmUso = 0;

        // Resetar estatísticas
        estatisticas.resetar();

        // Preparar caminhões pequenos
        distribuirCaminhoesPorZonas();

        // Iniciar timer
        if (random == null) {
            random = new Random();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!pausado) {
                    tempoSimulado = (tempoSimulado + 1) % (24 * 60); // Avança 1 minuto e faz o ciclo diário
                    atualizarSimulacao();
                }
            }
        }, 0, 50); // Atualiza a cada 50ms reais
    }

    // Método para distribuir caminhões pequenos pelas zonas inicialmente
    private void distribuirCaminhoesPorZonas() {
        if (listaZonas.estaVazia() || listaCaminhoesPequenos.estaVazia()) {
            return;
        }

        // Distribui os caminhões pelas zonas para iniciar a coleta
        for (int i = 0; i < listaCaminhoesPequenos.tamanho(); i++) {
            CaminhaoPequeno caminhao = listaCaminhoesPequenos.obter(i);

            // Escolhe uma zona aleatória para o caminhão
            int idxZona = random.nextInt(listaZonas.tamanho());
            ZonaUrbana zona = listaZonas.obter(idxZona);

            // Define estado e destino do caminhão
            caminhao.setStatus(StatusCaminhao.COLETANDO);
            caminhao.definirDestino(zona);

            // Transfere o caminhão para a lista de coleta
            caminhoesEmColeta.adicionar(caminhao);
        }

        // Limpa a lista original
        while (!listaCaminhoesPequenos.estaVazia()) {
            listaCaminhoesPequenos.remover(0);
        }

        System.out.println("Caminhões pequenos distribuídos pelas zonas para iniciar a coleta.");
    }

    public void pausar() {
        System.out.println("Simulação pausada.");
        pausado = true;
    }

    public void continuarSimulacao() {
        if(timer == null) {
            System.out.println("Simulação não iniciada. Use iniciar().");
            return;
        }
        System.out.println("Simulação retomada.");
        pausado = false;
    }

    public void encerrar() {
        System.out.println("Simulação encerrada.");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // Gerar relatório final
        try {
            String nomeArquivo = "relatorio_simulacao.txt";
            estatisticas.salvarRelatorio(nomeArquivo);
            System.out.println("Relatório final salvo em " + nomeArquivo);
            System.out.println("\n" + estatisticas.gerarRelatorio());
        } catch (IOException e) {
            System.err.println("Erro ao salvar relatório: " + e.getMessage());
        }
    }

    // --- Método para salvar o estado ---
    public void gravar(String caminho) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(this);
            System.out.println("Simulação salva em " + caminho);
        }
    }

    // --- Método para carregar o estado ---
    public static Simulador carregar(String caminho) throws IOException, ClassNotFoundException {
        Simulador sim = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            sim = (Simulador) ois.readObject();
            sim.timer = null;
            sim.pausado = true;
            sim.random = new Random();
            System.out.println("Simulação carregada de " + caminho + ". Está pausada.");
        }
        if (sim == null) {
            throw new IOException("Falha ao carregar o objeto Simulador, objeto nulo após deserialização.");
        }
        return sim;
    }

    // --- MÉTODO AUXILIAR para verificar horário de pico ---
    private boolean isHorarioDePico(int tempoAtualMinutos) {
        boolean manha = (tempoAtualMinutos >= picoManhaInicio && tempoAtualMinutos <= picoManhaFim);
        boolean tarde = (tempoAtualMinutos >= picoTardeInicio && tempoAtualMinutos <= picoTardeFim);
        return manha || tarde;
    }

    // --- MÉTODO AUXILIAR para calcular tempo de viagem ---
    private int calcularTempoViagemPequeno() {
        int min, max;
        if (isHorarioDePico(this.tempoSimulado)) {
            min = this.minViagemPico;
            max = this.maxViagemPico;
        } else {
            min = this.minViagemForaPico;
            max = this.maxViagemForaPico;
        }
        if (min >= max) return min;
        return random.nextInt(max - min + 1) + min;
    }

    // --- MÉTODO AUXILIAR para adicionar um novo caminhão grande ---
    public void adicionarCaminhaoGrande() {
        CaminhaoGrande novoCaminhao = new CaminhaoGrandePadrao(this.toleranciaCaminhoesGrandes);
        listaCaminhoesGrandes.adicionar(novoCaminhao);
        totalCaminhoesGrandes++;
        estatisticas.registrarNovoCaminhaoGrande();
        System.out.println("NOVO CAMINHÃO GRANDE ADICIONADO! Total agora: " + totalCaminhoesGrandes);
    }

    // --- LÓGICA PRINCIPAL DA SIMULAÇÃO ---
    private void atualizarSimulacao() {
        int hora = tempoSimulado / 60;
        int minuto = tempoSimulado % 60;

        // Log menos verboso - apenas a cada minuto completo
        if (minuto % 1 == 0) {
            System.out.printf("Tempo simulado: %02d:%02d %s%n", hora, minuto, isHorarioDePico(tempoSimulado) ? "(PICO)" : "");
        }

        // 1. Gerenciar geração de lixo (a cada hora simulada)
        if (minuto == 0) {
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                zona.gerarLixo();
                estatisticas.registrarGeracaoLixo(zona.getNome(), zona.getLixoAcumulado());
            }

            // Reiniciar contadores de viagens diárias dos caminhões pequenos
            reiniciarViagensDiariasCaminhoes();
        }

        // 2. Processar caminhões pequenos
        processarCaminhoesEmColeta();
        processarCaminhoesEmViagem();

        // 3. Processar estações de transferência
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            // Precisa fazer cast para acessar os métodos específicos da implementação
            if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);

                // Processar a fila de caminhões pequenos
                estacao.processarFila();

                // Gerenciar tempo de espera do caminhão grande atual
                estacao.gerenciarTempoEsperaCaminhaoGrande();

                // Verificar se o tempo de espera foi excedido e se precisa de um novo caminhão grande
                if (estacao.tempoEsperaExcedido()) {
                    System.out.println("ALERTA! Tempo de espera excedido na " + estacao.getNome());

                    // Adicionar novo caminhão grande se necessário
                    adicionarCaminhaoGrande();

                    // Tentar encontrar um caminhão grande disponível e atribuir à estação
                    atribuirCaminhaoGrandeParaEstacao(estacao);
                }

                // Verificar se a estação precisa de um caminhão grande (não tem nenhum atualmente)
                if (estacao.precisaCaminhaoGrande()) {
                    System.out.println("Estação " + estacao.getNome() + " precisa de um caminhão grande.");
                    atribuirCaminhaoGrandeParaEstacao(estacao);
                }
            }
        }

        // 4. Atualizar estatísticas
        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);

        // 5. Relatório a cada hora simulada (a cada 60 minutos)
        if (tempoSimulado % intervaloEstatisticas == 0 && tempoSimulado != 0) { // Evita imprimir no tempo 0
            imprimirRelatorioHorario(hora);
        }
    }

    // Método para reiniciar os contadores de viagens diárias dos caminhões
    private void reiniciarViagensDiariasCaminhoes() {
        // Reinicia os caminhões em coleta
        for (int i = 0; i < caminhoesEmColeta.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesEmColeta.obter(i);
            caminhao.reiniciarViagensDiarias();
        }

        // Reinicia os caminhões em viagem
        for (int i = 0; i < caminhoesEmViagem.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesEmViagem.obter(i);
            caminhao.reiniciarViagensDiarias();
        }

        // Reinicia os caminhões na lista principal (ociosos)
        for (int i = 0; i < listaCaminhoesPequenos.tamanho(); i++) {
            CaminhaoPequeno caminhao = listaCaminhoesPequenos.obter(i);
            caminhao.reiniciarViagensDiarias();
        }
    }

    // Método para processar caminhões em coleta
    private void processarCaminhoesEmColeta() {
        for (int i = 0; i < caminhoesEmColeta.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesEmColeta.obter(i);
            ZonaUrbana zona = caminhao.getZonaAtual();

            if (zona != null && !caminhao.estaCheio()) {
                // Tenta coletar lixo da zona atual
                int lixoDisponivel = zona.getLixoAcumulado();
                int capacidadeRestante = caminhao.getCapacidade() - caminhao.getCargaAtual();
                int quantidadeAColetada = Math.min(lixoDisponivel, capacidadeRestante);

                if (quantidadeAColetada > 0) {
                    if (caminhao.coletar(quantidadeAColetada)) {
                        int lixoColetado = zona.coletarLixo(quantidadeAColetada);
                        estatisticas.registrarColetaLixo(zona.getNome(), lixoColetado);

                        System.out.println("Caminhão " + caminhao.getPlaca() +
                                " coletou " + lixoColetado + "kg da zona " +
                                zona.getNome() + ". Carga atual: " +
                                caminhao.getCargaAtual() + "kg");
                    }
                }
            }

            // Se o caminhão estiver cheio, direciona para uma estação
            if (caminhao.estaCheio()) {
                // Escolhe uma estação aleatoriamente
                int indiceEstacao = random.nextInt(listaEstacoes.tamanho());
                EstacaoTransferencia estacao = listaEstacoes.obter(indiceEstacao);

                // Configura o caminhão para viagem
                caminhao.setStatus(StatusCaminhao.VIAJANDO_ESTACAO);
                caminhao.definirDestino(estacao);
                int tempoViagem = calcularTempoViagemPequeno();
                caminhao.definirTempoViagem(tempoViagem);
                caminhao.registrarViagem();

                System.out.println("Caminhão " + caminhao.getPlaca() +
                        " está cheio e se dirigindo para " +
                        estacao.getNome() + ". Tempo de viagem: " +
                        tempoViagem + " minutos");

                // Move o caminhão da lista de coleta para a lista de viagem
                caminhoesEmViagem.adicionar(caminhao);
                caminhoesEmColeta.remover(i);
                i--; // Ajuste do índice após remoção
            }
        }
    }

    // Método para processar caminhões em viagem
    private void processarCaminhoesEmViagem() {
        for (int i = 0; i < caminhoesEmViagem.tamanho(); i++) {
            CaminhaoPequeno caminhao = caminhoesEmViagem.obter(i);

            // Processa o tempo de viagem
            boolean viagemCompleta = caminhao.processarViagem();

            if (viagemCompleta) {
                // Verifica se está indo para uma estação
                if (caminhao.getStatus() == StatusCaminhao.VIAJANDO_ESTACAO &&
                        caminhao.getEstacaoDestino() != null) {

                    EstacaoTransferencia estacao = caminhao.getEstacaoDestino();
                    System.out.println("Caminhão " + caminhao.getPlaca() +
                            " chegou à " + estacao.getNome());

                    // Envia o caminhão para a estação
                    estacao.receberCaminhaoPequeno(caminhao);
                    caminhao.setStatus(StatusCaminhao.NA_FILA);

                    // Remove da lista de viagem
                    caminhoesEmViagem.remover(i);
                    i--; // Ajuste do índice após remoção
                }
                // Verifica se está retornando para uma zona
                else if (caminhao.getStatus() == StatusCaminhao.RETORNANDO_ZONA &&
                        caminhao.getZonaAtual() != null) {

                    ZonaUrbana zona = caminhao.getZonaAtual();
                    System.out.println("Caminhão " + caminhao.getPlaca() +
                            " retornou à zona " + zona.getNome() +
                            " para continuar a coleta");

                    // Coloca o caminhão em modo de coleta
                    caminhao.setStatus(StatusCaminhao.COLETANDO);

                    // Move o caminhão da lista de viagem para a lista de coleta
                    caminhoesEmColeta.adicionar(caminhao);
                    caminhoesEmViagem.remover(i);
                    i--; // Ajuste do índice após remoção
                }
            }
        }
    }

    // Método auxiliar para atribuir um caminhão grande disponível a uma estação
    private void atribuirCaminhaoGrandeParaEstacao(EstacaoPadrao estacao) {
        // Procura por um caminhão grande "disponível"
        if (listaCaminhoesGrandes.tamanho() == 0) {
            adicionarCaminhaoGrande();
        }

        // Atribui o primeiro caminhão da lista
        if (listaCaminhoesGrandes.tamanho() > 0) {
            CaminhaoGrande caminhao = listaCaminhoesGrandes.obter(0);
            estacao.atribuirCaminhaoGrande(caminhao);

            // Remove da lista principal
            listaCaminhoesGrandes.remover(0);

            // Incrementa o contador de caminhões em uso
            caminhoesGrandesEmUso++;
        }
    }

    // Novo método para imprimir relatório horário mais detalhado
    private void imprimirRelatorioHorario(int hora) {
        System.out.println("\n=== RELATÓRIO DE STATUS: HORA " + hora + " ===");
        System.out.println(" > Frota: " +
                (listaCaminhoesPequenos.tamanho() + caminhoesEmColeta.tamanho() + caminhoesEmViagem.tamanho()) +
                " caminhões pequenos totais (" +
                listaCaminhoesPequenos.tamanho() + " ociosos, " +
                caminhoesEmColeta.tamanho() + " em coleta, " +
                caminhoesEmViagem.tamanho() + " em viagem)");
        System.out.println(" > Caminhões grandes: " + totalCaminhoesGrandes + " totais (" +
                listaCaminhoesGrandes.tamanho() + " disponíveis, " +
                caminhoesGrandesEmUso + " em uso)");

        // Relatório de lixo por zona
        System.out.println(" > Lixo acumulado por zona:");
        int lixoTotalNasZonas = 0;
        for (int i = 0; i < listaZonas.tamanho(); i++) {
            ZonaUrbana zona = listaZonas.obter(i);
            int lixoNaZona = zona.getLixoAcumulado();
            lixoTotalNasZonas += lixoNaZona;
            System.out.println("   - " + zona.getNome() + ": " + lixoNaZona + "kg");
        }
        System.out.println("   Total de lixo nas zonas: " + lixoTotalNasZonas + "kg");

        // Relatório de filas nas estações
        System.out.println(" > Status das estações:");
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);
                System.out.println("   - " + estacao.getNome() + ": "
                        + estacao.getCaminhoesNaFila() + " caminhões na fila");
            }
        }

        System.out.println("============================================\n");
    }

    // Getters para estatísticas
    public Estatisticas getEstatisticas() {
        return estatisticas;
    }

    // Getter para tempo simulado
    public int getTempoSimulado() {
        return tempoSimulado;
    }

    // Getter para total de caminhões grandes
    public int getTotalCaminhoesGrandes() {
        return totalCaminhoesGrandes;
    }

    // Getter para caminhões grandes em uso
    public int getCaminhoesGrandesEmUso() {
        return caminhoesGrandesEmUso;
    }

    // Getter para tolerância de caminhões grandes
    public int getToleranciaCaminhoesGrandes() {
        return toleranciaCaminhoesGrandes;
    }
}