package valkyrie.Backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Connectors.ModeloConnector;
import valkyrie.Backend.Enum.BucketType;
import valkyrie.Backend.Exceptions.ArquivoInvalidoException;
import valkyrie.Backend.Models.Entities.ResultadoCrime;
import valkyrie.Backend.Models.ModeloResponse;
import valkyrie.Backend.Repository.ResultadoCrimeRepository;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class FileService {

    private final ResultadoCrimeRepository repository;
    private final ModeloConnector modeloConnector;
    private final S3Service s3Service;
    private final VideoService videoService;
    private final ImageService imageService;

    public FileService(ResultadoCrimeRepository repository, ModeloConnector modeloConnector, S3Service s3Service,
            VideoService videoService, ImageService imageService) {
        this.repository = repository;
        this.modeloConnector = modeloConnector;
        this.s3Service = s3Service;
        this.videoService = videoService;
        this.imageService = imageService;
    }

    private ResultadoCrime inserir(
            ModeloResponse resultado) {
        ResultadoCrime crime = new ResultadoCrime();

        var isCrime = resultado.getPredictions().stream()
                .anyMatch(prediction -> prediction.getConfidence() >= 0.70
                        && java.util.Set.of(
                                "burglary",
                                "fighting",
                                "robbery").contains(
                                        prediction.getClasse().toLowerCase(
                                                java.util.Locale.ROOT)));

        OptionalDouble optAccuracy = resultado.getPredictions().stream().mapToDouble(value -> value.getConfidence())
                .average();

        Double accuracy = 0.0;

        if (optAccuracy.isPresent())
            accuracy = optAccuracy.getAsDouble() * 100;

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

        nomeArquivoClient = trataImagemVideo(contentType, imagem);

        if (nomeArquivoClient.equalsIgnoreCase(""))
            throw new Exception("Erro ao subir arquivo S3");

        ModeloResponse isCrime = modeloConnector.ChamaModelo(nomeArquivoClient);

        System.out.println(isCrime);

        return this.inserir(isCrime);
    }

    private String trataImagemVideo(String contentType, MultipartFile imagem) throws Exception {
        String nomeArquivoClient = "";
        try {
            if (contentType != null && contentType.startsWith("video/")) {
                s3Service.uploadSS3(imagem, BucketType.TRUSTED);
                nomeArquivoClient = videoService.processVideo(imagem);
            } else if (contentType != null && contentType.startsWith("image/")) {
                s3Service.uploadSS3(imagem, BucketType.TRUSTED);
                nomeArquivoClient = imageService.processImage(imagem);
            } else {
                throw new ArquivoInvalidoException("Arquivo não é imagem nem video");
            }

            return nomeArquivoClient;
        } catch (IOException io) {
            throw new Exception("Erro ao tentar tratar imagem ou subir no bucket.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
