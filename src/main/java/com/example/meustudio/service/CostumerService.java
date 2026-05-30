package com.example.meustudio.service;


import com.example.meustudio.infra.repository.CostumerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CostumerService {

    private final CostumerRepository costumerRepository;
        public void deleteByName(Integer id){
            CostumerRepository.deleteByName();

        }


}
