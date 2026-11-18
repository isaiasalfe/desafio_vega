package br.com.vega.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.vega.domain.Ingrediente;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    @Query("""
            select i from Ingrediente i
            join fetch i.tipoIngrediente
            join fetch i.compartimento
            where i.flAtivo = true
            order by i.id
            """)
    List<Ingrediente> findAtivosComRelacionamentos();

    @Query("""
            select i from Ingrediente i
            join fetch i.tipoIngrediente
            join fetch i.compartimento c
            left join fetch c.tipoIngredienteAtual
            where i.id = :id
            """)
    Optional<Ingrediente> findByIdComRelacionamentos(@Param("id") Long id);
}
