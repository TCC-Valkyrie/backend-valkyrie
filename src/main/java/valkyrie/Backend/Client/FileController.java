package valkyrie.Backend.Client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import valkyrie.Backend.Models.Entities.ResultadoCrime;
import valkyrie.Backend.Models.Entities.ConsultarCrimesPorMesResponse;
import valkyrie.Backend.Service.FileService;

import java.util.List;

@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoCrime> upload(@RequestParam("imagem") MultipartFile imagem) {
        try {
            if (imagem.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            System.out.println("Nome: " + imagem.getOriginalFilename());
            System.out.println("Tipo: " + imagem.getContentType());
            System.out.println("Tamanho: " + imagem.getSize());

            ResultadoCrime retorno = fileService.ChamaModelo(imagem);
            return ResponseEntity.ok(retorno);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<Object[]>> ConsultaQuantidadeCrimesMes() {
        try {
            List<Object[]> retorno;
            retorno = fileService.quantidadeCrimesPorMes();
            if (retorno == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(retorno);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping()
    public ResponseEntity<List<ResultadoCrime>> ConsultaTodosCrimes() {
        try {
            List<ResultadoCrime> retorno;
            retorno = fileService.buscarTodos();
            if (retorno == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(retorno);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/teste")
    public ResponseEntity<Boolean> Teste() {
       return ResponseEntity.ok(true);
    }
}
