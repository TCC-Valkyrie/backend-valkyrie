package valkyrie.Backend.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import valkyrie.Backend.Enum.BucketType;
import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket.raw}")
    private String bucketRaw;

    @Value("${aws.s3.bucket.trusted}")
    private String bucketTrusted;

    @Value("${aws.s3.bucket.client}")
    private String bucketClient;

    public String uploadSS3(MultipartFile arquivo, BucketType bucketType) throws IOException {

        String nomeOriginal = arquivo.getOriginalFilename();

        String nomeArquivo = UUID.randomUUID() + "-" + nomeOriginal;

        String bucketDestino = bucketRaw;

        if (bucketType.equals(BucketType.CLIENT)) {
            bucketDestino = bucketClient;
        } else if (bucketType.equals(BucketType.TRUSTED)) {
            bucketDestino = bucketTrusted;
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketDestino)
                .key(nomeArquivo)
                .contentType(arquivo.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        arquivo.getInputStream(),
                        arquivo.getSize()
                )
        );

        return nomeArquivo;
    }
}
