package valkyrie.Backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Connectors.ModeloConnector;
import valkyrie.Backend.Enum.BucketType;
import valkyrie.Backend.Exceptions.ArquivoInvalidoException;
import valkyrie.Backend.Models.Entities.ResultadoCrime;
import valkyrie.Backend.Models.ModeloResponse;
import valkyrie.Backend.Repository.ResultadoCrimeRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final ResultadoCrimeRepository repository;
    private final ModeloConnector modeloConnector;
    private final S3Service s3Service;
    private final CrimeVideoService crimeVideoService;

    public FileService(ResultadoCrimeRepository repository, ModeloConnector modeloConnector, S3Service s3Service,
            CrimeVideoService crimeVideoService) {
        this.repository = repository;
        this.modeloConnector = modeloConnector;
        this.s3Service = s3Service;
        this.crimeVideoService = crimeVideoService;
    }

    private ResultadoCrime inserir(
            ModeloResponse resultado) {
        ResultadoCrime crime = new ResultadoCrime();

        var isCrime = resultado.getPredictions().stream()
                .anyMatch(prediction -> prediction.getConfidence() >= 0.70
                        && java.util.Set.of(
                                "arma",
                                "assalto",
                                "furto").contains(
                                        prediction.getClasse().toLowerCase(
                                                java.util.Locale.ROOT)));

        OptionalDouble optAccuracy = resultado.getPredictions().stream().mapToDouble(value -> value.getConfidence())
                .average();

        Double accuracy = 0.0;

        if (optAccuracy.isPresent())
            accuracy = optAccuracy.getAsDouble();

        crime.setResultado(isCrime);
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
        System.out.println("Entrou em ChamaModelo()");
        String nomeArquivoClient = "";

        String contentType = imagem.getContentType();
        nomeArquivoClient = s3Service.uploadSS3(imagem, BucketType.RAW);

        System.out.println("Arquivo subido para RAW: " + nomeArquivoClient);
        try {
            if (contentType != null && contentType.startsWith("video/")) {
                s3Service.uploadSS3(imagem, BucketType.TRUSTED);
                nomeArquivoClient = crimeVideoService.processVideo(imagem);
            } else if (contentType != null && contentType.startsWith("image/")) {
                s3Service.uploadSS3(imagem, BucketType.TRUSTED);
                nomeArquivoClient = crimeVideoService.processImage(imagem);
            } else {
                throw new ArquivoInvalidoException("Arquivo não é imagem nem video");
            }
        } catch (IOException io) {
            throw new Exception("Erro no bucket");
        }

        if (nomeArquivoClient.equalsIgnoreCase(""))
            throw new Exception("Erro ao subir arquivo S3");

        ModeloResponse isCrime = modeloConnector.ChamaModelo(nomeArquivoClient);

        return this.inserir(isCrime);
    }
}
