package valkyrie.Backend.Models.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultado_crime")
public class ResultadoCrime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "resultado")
    private Boolean resultado;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    public ResultadoCrime() {
    }

    public ResultadoCrime(Boolean resultado, Double accuracy, LocalDateTime dataHora) {
        this.resultado = resultado;
        this.accuracy = accuracy;
        this.dataHora = dataHora;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getResultado() {
        return resultado;
    }

    public void setResultado(Boolean resultado) {
        this.resultado = resultado;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
