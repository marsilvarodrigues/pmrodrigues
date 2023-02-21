package com.pmrodrigues.users.model;

import com.pmrodrigues.users.model.enums.AddressType;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "address")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Address {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false,targetEntity = User.class,fetch = FetchType.LAZY)
    private User owner;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "address_type", nullable = false)
    @NotNull
    private AddressType addressType;

    @Column(name = "address1", nullable = false)
    @NotNull
    @NotBlank
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "zipcode", nullable = false)
    @NotNull
    @NotBlank
    private String zipcode;

    @Column(name = "neightboor", nullable = false)
    @NotNull
    @NotBlank
    private String neightboor;

    @Column(name = "city", nullable = false)
    @NotNull
    @NotBlank
    private String city;

    @ManyToOne(optional = false, targetEntity = State.class)
    @JoinColumn(name = "state_id", referencedColumnName = "id", nullable = false)
    @NotNull
    private State state;

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
