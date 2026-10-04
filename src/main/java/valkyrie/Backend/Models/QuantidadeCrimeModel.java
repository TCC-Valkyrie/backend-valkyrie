package valkyrie.Backend.Models;

import java.util.Date;

public class QuantidadeCrimeModel {
    private Date data;
    private Integer quantidadeCrimes;

    public QuantidadeCrimeModel(Date data, Integer quantidadeCrimes) {
        this.data = data;
        this.quantidadeCrimes = quantidadeCrimes;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Integer getQuantidadeCrimes() {
        return quantidadeCrimes;
    }

    public void setQuantidadeCrimes(Integer quantidadeCrimes) {
        this.quantidadeCrimes = quantidadeCrimes;
    }
}
