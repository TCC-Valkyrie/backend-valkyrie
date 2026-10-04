package valkyrie.Backend.Mapper;

import valkyrie.Backend.Models.QuantidadeCrimeModel;

import java.util.Date;

public class CrimeMapper {

    public QuantidadeCrimeModel QuantidadeCrimeMapper(Integer quantidade) {
        return new QuantidadeCrimeModel(new Date(), quantidade);
    }
}
