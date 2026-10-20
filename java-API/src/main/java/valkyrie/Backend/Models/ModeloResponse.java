package valkyrie.Backend.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ModeloResponse {

    private String nomeArquivo;
    private List<Prediction> predictions;

    public ModeloResponse() {
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }

    public static class Prediction {

        @JsonProperty("class")
        private String classe;
        private double confidence;
        private List<Double> bbox;

        public Prediction() {
        }

        public String getClasse() {
            return classe;
        }

        public void setClasse(String classe) {
            this.classe = classe;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public List<Double> getBbox() {
            return bbox;
        }

        public void setBbox(List<Double> bbox) {
            this.bbox = bbox;
        }
    }
}