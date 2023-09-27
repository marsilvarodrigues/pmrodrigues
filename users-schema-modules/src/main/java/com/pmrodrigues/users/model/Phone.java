package com.pmrodrigues.users.model;

import com.pmrodrigues.users.model.enums.PhoneType;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "phones")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@With
public class Phone {
    @Id
    @Column(name = "id", nullable = false)
    @Setter(value = AccessLevel.PRIVATE)
    private UUID id;

    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false,targetEntity = User.class,fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @NotNull
    @ToString.Exclude
    private User owner;

    @NotBlank
    @NotNull
    @Column(name = "phone_number" , length = 20, nullable = false)
    @EqualsAndHashCode.Include
    @Size(min = 9)
    @Pattern(regexp = "\\d{5}[-\\s]?\\d{4}")
    private String phoneNumber;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "phone_type")
    @NotNull
    @EqualsAndHashCode.Include
    private PhoneType type;


    @CreatedDate
    @Column(name = "created_at" )
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at" )
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    public void preInsert() {
        this.id = UUID.randomUUID();
    }
}
