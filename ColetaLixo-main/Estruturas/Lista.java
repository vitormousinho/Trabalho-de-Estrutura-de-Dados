package Estruturas;

import java.util.Arrays; // Import necessário para Arrays.copyOf

public class Lista<T> {

    private Object[] elementos;
    private int tamanho;
    private static final int CAPACIDADE_INICIAL = 10;

    public Lista() {
        this.elementos = new Object[CAPACIDADE_INICIAL];
        this.tamanho = 0;
    }

    private void garantirCapacidade() {
        if (tamanho == elementos.length) {
            int novaCapacidade = elementos.length * 2;
            elementos = Arrays.copyOf(elementos, novaCapacidade);
        }
    }

    public boolean adicionar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("Não é permitido adicionar elementos nulos à lista.");
        }
        garantirCapacidade();
        this.elementos[tamanho] = elemento;
        this.tamanho++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido: " + indice + ". Tamanho atual: " + tamanho);
        }
        return (T) this.elementos[indice];
    }

    public int tamanho() {
        return this.tamanho;
    }

    public boolean estaVazia() {
        return this.tamanho == 0;
    }

    public void limpar() {
        this.elementos = new Object[CAPACIDADE_INICIAL];
        this.tamanho = 0;
    }

    @Override
    public String toString() {
        if (tamanho == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tamanho; i++) {
            sb.append(elementos[i]);
            if (i < tamanho - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public T remover(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido para remoção: " + indice + ". Tamanho atual: " + tamanho);
        }
        T elementoRemovido = (T) elementos[indice];
        int numMovidos = tamanho - indice - 1;
        if (numMovidos > 0) {
            System.arraycopy(elementos, indice + 1, elementos, indice, numMovidos);
        }
        tamanho--;
        elementos[tamanho] = null;
        return elementoRemovido;
    }
}