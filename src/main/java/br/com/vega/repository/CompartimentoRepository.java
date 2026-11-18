package br.com.vega.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.vega.domain.Compartimento;
import br.com.vega.domain.NomeTipoIngrediente;

public interface CompartimentoRepository extends JpaRepository<Compartimento, Long> {

    @Query("""
            select c from Compartimento c
            left join fetch c.tipoIngredienteAtual
            where c.quantidadeDisponivel is null or c.quantidadeDisponivel > 0
            order by c.id
            """)
    List<Compartimento> findDisponiveisParaEntrada();

    @Query("""
            select c from Compartimento c
            join fetch c.tipoIngredienteAtual t
            where t.nome = :tipo
              and c.quantidadeArmazenada is not null
              and c.quantidadeArmazenada <> 0
            order by c.id
            """)
    List<Compartimento> findDisponiveisParaVendaPorTipo(@Param("tipo") NomeTipoIngrediente tipo);
}
