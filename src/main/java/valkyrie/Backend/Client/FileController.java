package valkyrie.Backend.Client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import valkyrie.Backend.Models.QuantidadeCrimeModel;
import valkyrie.Backend.Service.FileService;

@Controller
@RequestMapping("/file")
public class FileController {

    @BindParam
    private FileService fileService;

    @GetMapping("/crimes")
    public ResponseEntity<QuantidadeCrimeModel> GetCrimes(@RequestParam("mes") Integer mes) {
        return ResponseEntity.ok(fileService.GetCrimes(mes));
    }
}
