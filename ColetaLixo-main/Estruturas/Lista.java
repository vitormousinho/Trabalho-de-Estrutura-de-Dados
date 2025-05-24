package Estruturas;

import java.util.Arrays;

/**
 * Implementação de uma lista dinâmica genérica baseada em array.
 * A capacidade da lista aumenta automaticamente conforme necessário.
 * Não permite elementos nulos.
 *
 * @param <T> o tipo de elemento que a lista irá armazenar.
 */
public class Lista<T> {

    private Object[] elementos;
    private int tamanho;
    private static final int CAPACIDADE_INICIAL = 10;

    /**
     * Constrói uma lista vazia com capacidade inicial padrão.
     */
    public Lista() {
        this.elementos = new Object[CAPACIDADE_INICIAL];
        this.tamanho = 0;
    }

    /**
     * Garante que a lista tenha capacidade suficiente para adicionar mais um elemento.
     * Se necessário, dobra a capacidade atual do array interno.
     */
    private void garantirCapacidade() {
        if (tamanho == elementos.length) {
            int novaCapacidade = elementos.length * 2;
            // Considerar um log se a capacidade aumentar muito frequentemente
            // System.out.println("DEBUG: Aumentando capacidade da Lista para " + novaCapacidade);
            elementos = Arrays.copyOf(elementos, novaCapacidade);
        }
    }

    /**
     * Adiciona um elemento ao final da lista.
     *
     * @param elemento o elemento a ser adicionado (não pode ser nulo).
     * @return true sempre (a adição sempre é possível, exceto por OutOfMemoryError).
     * @throws IllegalArgumentException se o elemento for nulo.
     */
    public boolean adicionar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("Não é permitido adicionar elementos nulos à lista.");
        }
        garantirCapacidade();
        this.elementos[tamanho] = elemento;
        this.tamanho++;
        return true;
    }

    /**
     * Obtém o elemento em um índice específico da lista.
     *
     * @param indice o índice do elemento a ser obtido (0 <= indice < tamanho).
     * @return o elemento no índice especificado.
     * @throws IndexOutOfBoundsException se o índice for inválido.
     */
    @SuppressWarnings("unchecked")
    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido: " + indice + ". Tamanho atual: " + tamanho);
        }
        return (T) this.elementos[indice];
    }

    /**
     * Retorna o número de elementos na lista.
     *
     * @return o tamanho atual da lista.
     */
    public int tamanho() {
        return this.tamanho;
    }

    /**
     * Verifica se a lista está vazia.
     *
     * @return true se a lista não contém elementos, false caso contrário.
     */
    public boolean estaVazia() {
        return this.tamanho == 0;
    }

    /**
     * Remove todos os elementos da lista, restaurando sua capacidade inicial.
     */
    public void limpar() {
        // Recria o array em vez de apenas setar elementos para null para liberar memória mais explicitamente
        this.elementos = new Object[CAPACIDADE_INICIAL];
        this.tamanho = 0;
    }

    /**
     * Retorna uma representação em String da lista.
     *
     * @return A string no formato "[elemento1, elemento2, ..., elementoN]".
     */
    @Override
    public String toString() {
        if (tamanho == 0) {
            return "[]";
        }
        // Usa StringBuilder para eficiência na concatenação de strings em loop
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tamanho; i++) {
            sb.append(elementos[i]); // Confia no toString() dos elementos
            if (i < tamanho - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Remove o elemento no índice especificado da lista.
     * Os elementos subsequentes são deslocados para a esquerda.
     *
     * @param indice o índice do elemento a ser removido (0 <= indice < tamanho).
     * @return o elemento que foi removido da lista.
     * @throws IndexOutOfBoundsException se o índice for inválido.
     */
    @SuppressWarnings("unchecked")
    public T remover(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido para remoção: " + indice + ". Tamanho atual: " + tamanho);
        }
        T elementoRemovido = (T) elementos[indice];
        int numMovidos = tamanho - indice - 1;
        if (numMovidos > 0) {
            // Desloca os elementos posteriores uma posição para a esquerda
            System.arraycopy(elementos, indice + 1, elementos, indice, numMovidos);
        }
        tamanho--;
        elementos[tamanho] = null; // Ajuda o garbage collector, limpando a referência antiga
        return elementoRemovido;
    }
}