import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

// Importe as classes necessárias, incluindo sua Lista e as classes do modelo
import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;


public class Simulador implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Timer timer; // transient para não tentar salvar o Timer
    private int tempoSimulado = 0;
    private boolean pausado = false;

    // --- NOVOS ATRIBUTOS para guardar os elementos da simulação ---
    private Lista<CaminhaoPequeno> listaCaminhoesPequenos;
    private Lista<CaminhaoGrande> listaCaminhoesGrandes;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;
    // Adicione aqui outros atributos que o simulador precise gerenciar (parâmetros, estatísticas, etc.)

    // Construtor padrão (pode adicionar inicializações se precisar)
    public Simulador() {
        // Inicializa as listas para evitar NullPointerException
        // No Java, é boa prática inicializar coleções no construtor ou na declaração
        this.listaCaminhoesPequenos = new Lista<>();
        this.listaCaminhoesGrandes = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
    }

    // --- NOVOS MÉTODOS para configurar o simulador com os elementos ---
    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) {
        this.listaCaminhoesPequenos = lista;
    }

    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) {
        this.listaCaminhoesGrandes = lista;
    }

    public void setListaZonas(Lista<ZonaUrbana> lista) {
        this.listaZonas = lista;
    }

    public void setListaEstacoes(Lista<EstacaoTransferencia> lista) {
        this.listaEstacoes = lista;
    }
    // --- Fim dos novos métodos ---


    public void iniciar() {
        if (timer != null) {
            System.out.println("Simulação já estava iniciada. Para reiniciar, encerre primeiro.");
            return;
        }
        System.out.println("Simulação iniciada...");
        pausado = false; // Garante que não comece pausado
        tempoSimulado = 0; // Reseta o tempo ao iniciar
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!pausado) {
                    tempoSimulado++;
                    atualizarSimulacao();
                }
            }
        }, 0, 1000); // Atualiza a cada 1000 ms (1 segundo real)
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
            timer = null; // Importante para poder reiniciar depois
        }
    }

    // Método para salvar o estado (precisa ajustar o que salvar)
    public void gravar(String caminho) throws IOException {
        // Atenção: Salvar o estado completo com listas customizadas pode exigir
        // que todas as classes envolvidas (Lista, Caminhao, Zona, Estacao)
        // também implementem Serializable.
        // O Timer não pode ser salvo (por isso é transient).
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(this); // Salva o objeto Simulador (e tudo que ele contém)
            System.out.println("Simulação salva em " + caminho);
        }
    }

    // Método para carregar o estado
    public static Simulador carregar(String caminho) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            Simulador sim = (Simulador) ois.readObject();
            // O timer não foi salvo, então precisa ser recriado se quiser continuar
            // Mas NÃO iniciamos automaticamente ao carregar, o usuário deve chamar continuar() ou iniciar()
            sim.timer = null;
            sim.pausado = true; // Carrega em estado pausado por segurança
            System.out.println("Simulação carregada de " + caminho + ". Está pausada.");
            return sim;
        }
    }

    // --- LÓGICA PRINCIPAL DA SIMULAÇÃO (Ainda precisa ser implementada!) ---
    private void atualizarSimulacao() {
        System.out.println("Tempo simulado: " + tempoSimulado + " minutos"); // Ou outra unidade de tempo

        // Aqui virá a lógica principal:
        // 1. Gerar lixo nas zonas (iterar sobre listaZonas)
        //    - Ex: for (int i = 0; i < listaZonas.tamanho(); i++) { listaZonas.obter(i).gerarLixo(); }
        // 2. Atualizar estado dos caminhões pequenos (iterar sobre listaCaminhoesPequenos)
        //    - Se ocioso, mandar para uma zona coletar.
        //    - Se viajando, decrementar tempo de viagem.
        //    - Se coletando, tentar coletar lixo da zona atual. Verificar se encheu.
        //    - Se cheio, definir rota para uma estação de transferência.
        // 3. Atualizar estado dos caminhões grandes (iterar sobre listaCaminhoesGrandes)
        //    - Se na estação carregando, verificar tempo de espera / se encheu.
        //    - Se cheio ou tempo excedido, mandar para o aterro.
        //    - Se viajando, decrementar tempo de viagem.
        // 4. Atualizar estado das estações (iterar sobre listaEstacoes)
        //    - Processar fila de caminhões pequenos (se houver caminhão grande disponível).
        //    - Verificar necessidade de novos caminhões grandes.
        // 5. Coletar estatísticas.

        // Exemplo simples de acesso às listas (para você ver que funciona):
        if (tempoSimulado % 10 == 0) { // A cada 10 passos de tempo
            System.out.println(" > Status: " + listaCaminhoesPequenos.tamanho() + " caminhões pequenos, "
                    + listaCaminhoesGrandes.tamanho() + " caminhões grandes, "
                    + listaZonas.tamanho() + " zonas, "
                    + listaEstacoes.tamanho() + " estações.");
            // Poderia imprimir o estado do primeiro caminhão, por exemplo:
            if (!listaCaminhoesPequenos.estaVazia()) {
                System.out.println(" > Estado do primeiro caminhão pequeno: " + listaCaminhoesPequenos.obter(0));
            }
        }
    }
}