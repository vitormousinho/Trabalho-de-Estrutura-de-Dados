package zonas;

import java.util.Random;

public class ZonaUrbana {
    private String nome;
    private int lixoAcumulado;
    // Novos atributos para configuração de geração
    private int geracaoMinima; // em kg por dia
    private int geracaoMaxima; // em kg por dia
    private Random random;

    public ZonaUrbana(String nome) {
        this.nome = nome;
        this.lixoAcumulado = 0;
        // Valores padrão (você pode ajustá-los conforme necessário)
        this.geracaoMinima = 100;
        this.geracaoMaxima = 500;
        this.random = new Random();
    }

    // Novo método para configurar os intervalos
    public void setIntervaloGeracao(int minimo, int maximo) {
        if (minimo <= 0 || maximo <= 0 || minimo > maximo) {
            throw new IllegalArgumentException("Intervalos de geração inválidos");
        }
        this.geracaoMinima = minimo;
        this.geracaoMaxima = maximo;
    }

    public void gerarLixo() {
        // Usar os intervalos configurados
        int quantidade = random.nextInt(geracaoMaxima - geracaoMinima + 1) + geracaoMinima;
        lixoAcumulado += quantidade;
        System.out.println(nome + ": Gerou " + quantidade + "kg de lixo. Total: " + lixoAcumulado + "kg.");
    }

    public int coletarLixo(int quantidade) {
        int coletado = Math.min(quantidade, lixoAcumulado);
        lixoAcumulado -= coletado;
        return coletado;
    }

    public int getLixoAcumulado() {
        return lixoAcumulado;
    }

    public String getNome() {
        return nome;
    }

    // Adicione estes dois métodos getters
    public int getGeracaoMinima() {
        return geracaoMinima;
    }

    public int getGeracaoMaxima() {
        return geracaoMaxima;
    }
}