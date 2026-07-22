package com.example.meustudio.Auth;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import jakarta.persistence.*;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name ="users")
public class User {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private UUID id;

@Column
    private String username;
@Column
private String password;
@Column
private String email;
@Column
private String telefone;
@Column
private String role;
@Column
private LocalDateTime criadoEm;
@Column
private LocalDateTime atualizadoEm;


@PrePersist
    public void prePersist(){
    LocalDateTime now = LocalDateTime.now();
    this.criadoEm = now;
    this.atualizadoEm = now;
}


}
