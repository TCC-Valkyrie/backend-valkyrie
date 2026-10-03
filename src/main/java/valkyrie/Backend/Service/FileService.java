package valkyrie.Backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Connectors.ModeloConnector;
import valkyrie.Backend.Enum.BucketType;
import valkyrie.Backend.Models.Entities.ResultadoCrime;
import valkyrie.Backend.Models.Entities.ConsultarCrimesPorMesResponse;
import valkyrie.Backend.Repository.ResultadoCrimeRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    private final ResultadoCrimeRepository repository;
    private final ModeloConnector modeloConnector;
    private final S3Service s3Service;

    public FileService(ResultadoCrimeRepository repository, ModeloConnector modeloConnector, S3Service s3Service) {
        this.repository = repository;
        this.modeloConnector = modeloConnector;
        this.s3Service = s3Service;
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

    public ResultadoCrime ChamaModelo(MultipartFile imagem) throws Exception {
        String nomeArquivoClient = "";

        try {
            s3Service.upload(imagem, BucketType.RAW);
            s3Service.upload(imagem, BucketType.TRUSTED);
            nomeArquivoClient = s3Service.upload(imagem, BucketType.CLIENT);
        } catch (IOException io) {
            throw new Exception("Erro no bucket");
        }

        Boolean isCrime = modeloConnector.ChamaModelo(imagem, nomeArquivoClient);
        return this.inserir(isCrime, 100.00);
    }
}
