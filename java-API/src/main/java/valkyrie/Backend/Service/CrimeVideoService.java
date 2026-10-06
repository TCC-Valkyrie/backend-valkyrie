package valkyrie.Backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CrimeVideoService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket.client}")
    private String bucket;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    public CrimeVideoService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String processVideo(MultipartFile video) throws Exception {

        Path workDir = Files.createTempDirectory("video-processing-");

        try {
            Path videoPath = workDir.resolve(
                    UUID.randomUUID() + ".mp4"
            );

            video.transferTo(videoPath);

            Path framesDir = workDir.resolve("frames");
            Files.createDirectories(framesDir);

            extractFrames(videoPath, framesDir);

            Path zipPath = workDir.resolve("frames.zip");
            createZip(framesDir, zipPath);

            String s3Key = "videos/" + UUID.randomUUID() + "/frames.zip";
            uploadToS3(zipPath, s3Key);

            return s3Key;

        } catch (Exception e) {
            throw e;
        } finally {
            deleteDirectory(workDir);
        }
    }

    private void extractFrames(
            Path videoPath,
            Path framesDir
    ) throws IOException, InterruptedException {

        Path outputPattern = framesDir.resolve("frame_%06d.jpg");
        
//        String ffmpegPath = "C:\\ffmpeg\\ffmpeg-2026-10-01-git-0b01ed76aa-essentials_build\\bin\\ffmpeg.exe";

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i",
                videoPath.toString(),
                "-vf",
                "fps=1/0.3,scale=224:224",
                "-q:v",
                "2",
                outputPattern.toString()
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
                    "Erro ao extrair frames. FFmpeg exit code: " + exitCode
            );
        }
    }

    private void createZip(
            Path framesDir,
            Path zipPath
    ) throws IOException {

        try (
                ZipOutputStream zipOutputStream =
                        new ZipOutputStream(
                                Files.newOutputStream(zipPath)
                        )
        ) {

            try (var files = Files.list(framesDir)) {

                files
                        .filter(Files::isRegularFile)
                        .sorted()
                        .forEach(file -> {

                            try {
                                ZipEntry zipEntry = new ZipEntry(
                                        file.getFileName().toString()
                                );

                                zipOutputStream.putNextEntry(zipEntry);

                                Files.copy(
                                        file,
                                        zipOutputStream
                                );

                                zipOutputStream.closeEntry();

                            } catch (IOException e) {
                                throw new RuntimeException(
                                        "Erro ao adicionar arquivo ao ZIP",
                                        e
                                );
                            }
                        });
            }
        }
    }

    private void uploadToS3(
            Path zipPath,
            String s3Key
    ) throws IOException {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType("application/zip")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromFile(zipPath)
        );
    }

    private void deleteDirectory(Path directory) {

        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });

        } catch (IOException ignored) {
        }
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

            uploadImageToS3(
                    resizedImagePath,
                    s3Key
            );

            return s3Key;

        } catch (Exception e) {
            throw e;
        } finally {
            deleteDirectory(workDir);
        }
    }


    private void resizeImage(
            Path imagePath,
            Path outputPath
    ) throws IOException, InterruptedException {

//        String ffmpegPath = "C:\\ffmpeg\\ffmpeg-2026-10-01-git-0b01ed76aa-essentials_build\\bin\\ffmpeg.exe";

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i",
                imagePath.toString(),
                "-vf",
                "scale=244:244",
                "-q:v",
                "2",
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

    private void uploadImageToS3(
            Path imagePath,
            String s3Key
    ) throws IOException {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType("image/jpeg")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromFile(imagePath)
        );
    }
}
