package com.pmrodrigues.users.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.pmrodrigues.commons.stringutils.PasswordGenerator.generatePassword;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "firstName" , length = 200, nullable = false)
    @NotNull
    @NotBlank
    private String firstName;

    @Column(name = "lastName" , length = 200, nullable = false)
    @NotNull
    @NotBlank
    private String lastName;

    @EqualsAndHashCode.Include
    @Column(name = "email" , length = 200, nullable = false, unique = true)
    @NotNull
    @NotBlank
    @Email
    private String email;

    @CreatedDate
    @Column(name = "created_at" )
    private Instant createdAt;

    @Column(name = "expired_date")
    private LocalDateTime expiredDate;

    @Column(name = "externalId")
    private UUID externalId;
    @Transient
    @JsonIgnore
    private String password;

    @PrePersist
    public void preInsert() {
        this.id = UUID.randomUUID();
        if( this.password == null ){
            val password = generatePassword();
            this.password = password.getCleanPassword();
        }
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public boolean isNew() {
        return this.externalId == null;
    }
}