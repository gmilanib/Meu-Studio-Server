package com.example.meustudio.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
    boolean existsByEmail(String email);
    Optional<Cliente> findByEmail(String email);

    @Query("select c from Cliente c where lower(trim(c.nome)) = lower(trim(:nome))")
    List<Cliente> findByNomeNormalizado(@Param("nome") String nome);
}
