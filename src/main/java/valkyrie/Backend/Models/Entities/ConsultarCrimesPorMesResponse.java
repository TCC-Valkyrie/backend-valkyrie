package valkyrie.Backend.Models.Entities;

import jakarta.persistence.Column;

public class ConsultarCrimesPorMesResponse {
    private Integer mes;
    private Integer quantidade;

    public ConsultarCrimesPorMesResponse() {
    }

    public ConsultarCrimesPorMesResponse(Integer mes, Integer quantidade) {
        this.mes = mes;
        this.quantidade = quantidade;
    }
}
