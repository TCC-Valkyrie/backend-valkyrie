package valkyrie.Backend.Connectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import valkyrie.Backend.Models.ModeloResponse;
import java.io.IOException;
import java.util.Collections;
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
            String nomeArquivo) throws IOException {

        if (Boolean.parseBoolean(mockModelo)) {
            ModeloResponse mock = new ModeloResponse();
            mock.setNomeArquivo(nomeArquivo);
            mock.setPredictions(Collections.emptyList());
            return mock;
        }

        Map<String, Object> body = Map.of(
                "objectId", nomeArquivo
        );

        try {
            return restClient
                    .method(HttpMethod.POST)
                    .uri(URLModelo)
                    .body(body)
                    .retrieve()
                    .body(ModeloResponse.class);
        } catch (WebClientResponseException e) {
            System.err.println("Status do modelo: " + e.getStatusCode());
            System.err.println("Resposta do modelo: " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
