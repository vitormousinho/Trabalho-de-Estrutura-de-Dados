package Estruturas;

public class Fila<T> {
    private No inicio;
    private No fim;
    private int tamanho;

    private class No {
        private T elemento;
        private No proximo;

        public No(T elemento) {
            this.elemento = elemento;
            this.proximo = null;
        }
    }

    public Fila() {
        this.inicio = null;
        this.fim = null;
        this.tamanho = 0;
    }

    public void adicionar(T elemento) {
        No novoNo = new No(elemento);

        if (estaVazia()) {
            inicio = novoNo;
        } else {
            fim.proximo = novoNo;
        }

        fim = novoNo;
        tamanho++;
    }

    public T remover() {
        if (estaVazia()) {
            throw new RuntimeException("Fila vazia!");
        }

        T elementoRemovido = inicio.elemento;
        inicio = inicio.proximo;

        if (inicio == null) {
            fim = null;
        }

        tamanho--;
        return elementoRemovido;
    }

    public T primeiroElemento() {
        if (estaVazia()) {
            throw new RuntimeException("Fila vazia!");
        }

        return inicio.elemento;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }
}