package valkyrie.Backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import valkyrie.Backend.Comum.FileComum;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ImageService {

    private final S3Service s3Service;

    private final FileComum comum;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    public ImageService(S3Service s3Service, FileComum comum) {
        this.s3Service = s3Service;
        this.comum = comum;
    }

    public String processImage(MultipartFile image) throws Exception {

        Path workDir = Files.createTempDirectory("image-processing-");

        try {
            Path imagePath = workDir.resolve(
                    UUID.randomUUID() + ".jpg"
            );

            image.transferTo(imagePath);

            Path resizedImagePath = workDir.resolve(
                    UUID.randomUUID() + "_244x244.jpg"
            );

            resizeImage(imagePath, resizedImagePath);

            String s3Key = "images/" + UUID.randomUUID() + "/image.jpg";

            s3Service.uploadImageToS3(
                    resizedImagePath,
                    s3Key
            );

            return s3Key;

        } catch (Exception e) {
            throw e;
        } finally {
            this.comum.deleteDirectory(workDir);
        }
    }


    private void resizeImage(
            Path imagePath,
            Path outputPath
    ) throws IOException, InterruptedException {

//        String ffmpegPath = "C:\\ffmpeg\\ffmpeg-2026-10-01-git-0b01ed76aa-essentials_build\\bin\\ffmpeg.exe";

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-y",
                "-i", imagePath.toString(),
                "-vf", "scale=224:224,format=gray",
                "-frames:v", "1",
                "-c:v", "mjpeg",
                "-pix_fmt", "gray",
                outputPath.toString()
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        )) {
            while (reader.readLine() != null) {
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException(
                    "Erro ao redimensionar imagem. FFmpeg exit code: "
                            + exitCode
            );
        }
    }
}
