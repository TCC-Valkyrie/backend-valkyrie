package valkyrie.Backend.Connectors;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public class ModeloConnector {

    private final RestClient restClient;

    public ModeloConnector(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public Boolean ChamaModelo(MultipartFile imagem) {
        Map<String, MultipartFile> body = Map.of(
                "imagem", imagem
        );

        return restClient
                .method(HttpMethod.POST)
                .uri("http://localhost:8080/file/teste")
                .retrieve()
                .body(Boolean.class);
    }
}
