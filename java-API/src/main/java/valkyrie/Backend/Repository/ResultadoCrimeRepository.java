package valkyrie.Backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import valkyrie.Backend.Models.Entities.ResultadoCrime;
import valkyrie.Backend.Models.Entities.ConsultarCrimesPorMesResponse;

import java.util.List;

public interface ResultadoCrimeRepository
        extends JpaRepository<ResultadoCrime, Integer> {

    @Query("""
        SELECT r
        FROM ResultadoCrime r
        ORDER BY r.dataHora DESC
        LIMIT 100
    """)
    List<ResultadoCrime> buscarTodos();


    @Query(value = """
        SELECT 
            DATE_FORMAT(data_hora, '%Y-%m') AS mes,
            COUNT(*) AS quantidade
        FROM resultado_crime
        WHERE resultado = true
          AND data_hora >= DATE_SUB(NOW(), INTERVAL 12 MONTH)
        GROUP BY DATE_FORMAT(data_hora, '%Y-%m')
        ORDER BY mes
        """, nativeQuery = true)
    List<Object[]> quantidadeCrimesPorMes();
}
