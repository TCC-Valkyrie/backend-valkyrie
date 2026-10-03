package valkyrie.Backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Connectors.ModeloConnector;
import valkyrie.Backend.Models.Entities.ResultadoCrime;
import valkyrie.Backend.Models.Entities.ConsultarCrimesPorMesResponse;
import valkyrie.Backend.Repository.ResultadoCrimeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    private final ResultadoCrimeRepository repository;
    private final ModeloConnector modeloConnector;
    public FileService(ResultadoCrimeRepository repository, ModeloConnector modeloConnector) {
        this.repository = repository;
        this.modeloConnector = modeloConnector;
    }

    private ResultadoCrime inserir(
            Boolean resultado,
            Double accuracy
    ) {
        ResultadoCrime crime = new ResultadoCrime();

        crime.setResultado(resultado);
        crime.setAccuracy(accuracy);
        crime.setDataHora(LocalDateTime.now());

        return repository.save(crime);
    }

    public List<ResultadoCrime> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<Object[]> quantidadeCrimesPorMes() {
        return repository.quantidadeCrimesPorMes();
    }

    public ResultadoCrime ChamaModelo(MultipartFile imagem) {

        Boolean isCrime = modeloConnector.ChamaModelo(imagem);

        return this.inserir(isCrime, 100.00);
    }
}
