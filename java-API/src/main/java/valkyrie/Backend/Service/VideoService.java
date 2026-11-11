package valkyrie.Backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import valkyrie.Backend.Comum.FileComum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class VideoService {
    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    private final FileComum comum;

    private final S3Service s3Service;

    public VideoService(S3Service s3Service, FileComum comum) {
        this.comum = comum;
        this.s3Service = s3Service;
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
            this.s3Service.uploadVideoToS3(zipPath, s3Key);

            return s3Key;

        } catch (Exception e) {
            throw e;
        } finally {
            comum.deleteDirectory(workDir);
        }
    }

    private void extractFrames(
            Path videoPath,
            Path framesDir
    ) throws IOException, InterruptedException {

        Path outputPattern = framesDir.resolve("frame_%06d.jpg");
        
        //String ffmpegPath = "C:\\ffmpeg\\ffmpeg-2026-10-01-git-0b01ed76aa-essentials_build\\bin\\ffmpeg.exe";

        ProcessBuilder processBuilder = new ProcessBuilder(
            ffmpegPath,
            "-i",
            videoPath.toString(),
            "-vf",
            "fps=1/0.3,scale=224:224,format=gray",
                "-pix_fmt", "gray",
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


}
