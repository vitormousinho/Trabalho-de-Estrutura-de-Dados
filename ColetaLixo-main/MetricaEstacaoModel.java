import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MetricaEstacaoModel {
    private final SimpleStringProperty nomeEstacao;
    private final SimpleIntegerProperty caminhoesAtendidos;
    private final SimpleDoubleProperty tempoMedioEspera;
    private final SimpleIntegerProperty lixoTransferido;

    public MetricaEstacaoModel(String nomeEstacao, int caminhoesAtendidos, double tempoMedioEspera, int lixoTransferido) {
        this.nomeEstacao = new SimpleStringProperty(nomeEstacao);
        this.caminhoesAtendidos = new SimpleIntegerProperty(caminhoesAtendidos);
        this.tempoMedioEspera = new SimpleDoubleProperty(tempoMedioEspera);
        this.lixoTransferido = new SimpleIntegerProperty(lixoTransferido);
    }

    public String getNomeEstacao() {
        return nomeEstacao.get();
    }

    public SimpleStringProperty nomeEstacaoProperty() {
        return nomeEstacao;
    }

    public int getCaminhoesAtendidos() {
        return caminhoesAtendidos.get();
    }

    public SimpleIntegerProperty caminhoesAtendidosProperty() {
        return caminhoesAtendidos;
    }

    public double getTempoMedioEspera() {
        return tempoMedioEspera.get();
    }

    public SimpleDoubleProperty tempoMedioEsperaProperty() {
        return tempoMedioEspera;
    }

    public int getLixoTransferido() {
        return lixoTransferido.get();
    }

    public SimpleIntegerProperty lixoTransferidoProperty() {
        return lixoTransferido;
    }
}