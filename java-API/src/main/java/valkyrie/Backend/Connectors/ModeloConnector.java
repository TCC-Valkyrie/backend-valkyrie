package valkyrie.Backend.Connectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import valkyrie.Backend.Models.ModeloResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class ModeloConnector {

    private final WebClient webClient;

    @Value("${modelo.url}")
    private String URLModelo;
    @Value("${modelo.mock}")
    private String mockModelo;

    public ModeloConnector(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public ModeloResponse ChamaModelo(
            String nomeArquivo) throws IOException {

        if (Boolean.parseBoolean(mockModelo)) {
            ModeloResponse mock = new ModeloResponse();
            mock.setNomeArquivo(nomeArquivo);
            mock.setPredictions(Collections.emptyList());
            return mock;
        }

        MultipartBodyBuilder multipart = new MultipartBodyBuilder();

        multipart.part("objectId", nomeArquivo)
                .contentType(MediaType.TEXT_PLAIN);

        try {
            return webClient.post()
                    .uri(URLModelo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(multipart.build())
                    .retrieve()
                    .bodyToMono(ModeloResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Status do modelo: " + e.getStatusCode());
            System.err.println("Resposta do modelo: " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
