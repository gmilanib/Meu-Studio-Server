package com.example.meustudio.procedimento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, UUID>, JpaSpecificationExecutor<Procedimento> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    @Query("select distinct p.categoria from Procedimento p where p.categoria is not null order by p.categoria")
    List<String> categorias();

    // Serializa alteração de situação e lançamento, evitando desativação entre validação e gravação.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Procedimento p where p.id = :id")
    Optional<Procedimento> buscarParaAtualizar(@Param("id") UUID id);
}
