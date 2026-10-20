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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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

        System.out.println("Entrou em ChamaModelo()");

        if (Boolean.parseBoolean(mockModelo)) {
            ModeloResponse mock = new ModeloResponse();
            mock.setNomeArquivo(nomeArquivo);
            mock.setPredictions(java.util.Collections.emptyList());
            return mock;
        }

        MultipartBodyBuilder multipartBuilder = new MultipartBodyBuilder();

        ByteArrayResource arquivoResource = new ByteArrayResource(imagem.getBytes()) {
            @Override
            public String getFilename() {
                return imagem.getOriginalFilename() != null
                        ? imagem.getOriginalFilename()
                        : nomeArquivo;
            }
        };

        multipartBuilder.part("imagem", arquivoResource)
                .contentType(imagem.getContentType() != null
                        ? MediaType.parseMediaType(imagem.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM);

        multipartBuilder.part("nomeArquivo", nomeArquivo)
                .contentType(MediaType.TEXT_PLAIN);

        return restClient.post()
                .uri(URLModelo)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipartBuilder.build())
                .retrieve()
                .body(ModeloResponse.class);
    }
}
