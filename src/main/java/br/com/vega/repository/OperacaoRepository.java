package br.com.vega.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.vega.domain.Operacao;
import br.com.vega.domain.TipoOperacao;

public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    List<Operacao> findByIngredienteIdInAndTipoOperacao(Collection<Long> idsIngrediente, TipoOperacao tipoOperacao);

    Optional<Operacao> findFirstByIngredienteIdAndTipoOperacaoOrderByIdAsc(Long idIngrediente, TipoOperacao tipoOperacao);

    @Query("""
            select o from Operacao o
            join fetch o.ingrediente
            join fetch o.compartimento
            """)
    List<Operacao> findAllComRelacionamentos(Sort sort);
}
