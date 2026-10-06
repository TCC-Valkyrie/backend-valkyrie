package valkyrie.Backend.Connectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

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

    public Boolean ChamaModelo(MultipartFile imagem, String nomeArquivo) {
        Map<String, Object> body = Map.of(
                "imagem", imagem,
                "nomeArquivo", nomeArquivo
        );

        if (mockModelo.equalsIgnoreCase("true")) {
            return restClient
                    .method(HttpMethod.POST)
                    .uri(URLModelo)
                    .retrieve()
                    .body(Boolean.class);
        } else {
            return restClient
                    .method(HttpMethod.POST)
                    .uri(URLModelo)
                    .body(body)
                    .retrieve()
                    .body(Boolean.class);
        }
    }
}
