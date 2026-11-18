package br.com.vega.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.vega.domain.NomeTipoIngrediente;
import br.com.vega.domain.TipoIngrediente;

public interface TipoIngredienteRepository extends JpaRepository<TipoIngrediente, Long> {

    Optional<TipoIngrediente> findByNome(NomeTipoIngrediente nome);

    @Query("""
            select t.nome, coalesce(sum(c.quantidadeArmazenada), 0)
            from TipoIngrediente t
            left join Compartimento c on c.tipoIngredienteAtual = t
            group by t.id, t.nome
            order by t.id
            """)
    List<Object[]> somarQuantidadeArmazenadaPorTipo();
}
