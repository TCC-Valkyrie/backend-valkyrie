package valkyrie.Backend.Connectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Models.ModeloResponse;

import java.io.IOException;
import java.util.Map;

@Component
public class ModeloConnector {

    private final RestClient restClient;

    @Value("${modelo.url}")
    private String URLModelo;
    @Value("${modelo.mock}")
    private String mockModelo;


    public ModeloConnector(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public ModeloResponse ChamaModelo(
            MultipartFile imagem,
            String nomeArquivo
    ) throws IOException {

        if (Boolean.parseBoolean(mockModelo)) {
            ModeloResponse mock = new ModeloResponse();
            mock.setNomeArquivo(nomeArquivo);
            mock.setPredictions(java.util.Collections.emptyList());
            return mock;
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part(
                "imagem",
                new ByteArrayResource(imagem.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imagem.getOriginalFilename() != null
                                ? imagem.getOriginalFilename()
                                : nomeArquivo;
                    }
                }
        ).contentType(
                imagem.getContentType() != null
                        ? MediaType.parseMediaType(imagem.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM
        );

        builder.part("nomeArquivo", nomeArquivo);

        MultiValueMap<String, HttpEntity<?>> parts = builder.build();

        return restClient.post()
                .uri(URLModelo)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(ModeloResponse.class);
    }
}
