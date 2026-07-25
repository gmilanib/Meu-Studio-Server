package com.example.meustudio.Auth;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name ="users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@Column (unique = true)
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

@PreUpdate
    public void preUpdate(){
    LocalDateTime now = LocalDateTime.now();
    this.atualizadoEm = now;
}

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
