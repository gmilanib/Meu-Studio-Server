package com.example.meustudio.infra.repository;

import com.example.meustudio.infra.entity.Costumer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostumerRepository extends JpaRepository<Costumer, Long> {

    @Transactional
    Costumer findByName(String name);

    @Transactional
    Costumer deleteByName(String name);


}
