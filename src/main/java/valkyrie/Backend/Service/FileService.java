package valkyrie.Backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.BindParam;
import valkyrie.Backend.Mapper.CrimeMapper;
import valkyrie.Backend.Models.QuantidadeCrimeModel;

@Service
public class FileService {

    @BindParam
    private CrimeMapper mapper;

    public QuantidadeCrimeModel GetCrimes(Integer mes) {
        //temp
        return mapper.QuantidadeCrimeMapper(50);
    }
}
