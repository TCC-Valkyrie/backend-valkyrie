package valkyrie.Backend.Connectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Models.ModeloResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;


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

        ByteArrayResource arquivoResource = new ByteArrayResource(imagem.getBytes()) {
            @Override
            public String getFilename() {
                return imagem.getOriginalFilename() != null
                        ? imagem.getOriginalFilename()
                        : "imagem.png";
            }
        };

        HttpHeaders headersImagem = new HttpHeaders();
        headersImagem.setContentType(
                imagem.getContentType() != null
                        ? MediaType.parseMediaType(imagem.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM
        );
        headersImagem.setContentDispositionFormData(
                "imagem",
                arquivoResource.getFilename()
        );

        HttpEntity<ByteArrayResource> parteImagem =
                new HttpEntity<>(arquivoResource, headersImagem);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("imagem", parteImagem);
        body.add("nomeArquivo", nomeArquivo);

        return restClient.post()
                .uri(URLModelo)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(ModeloResponse.class);
    }
}
