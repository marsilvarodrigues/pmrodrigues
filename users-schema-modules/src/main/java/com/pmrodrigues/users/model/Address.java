package com.pmrodrigues.users.model;

import com.pmrodrigues.users.model.enums.AddressType;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "address")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@With
@ToString
public class Address {

    @Id
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Exclude
    @Setter(value = AccessLevel.PRIVATE)
    private UUID id;

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false,targetEntity = User.class,fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @NotNull
    @ToString.Exclude
    private User owner;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "address_type", nullable = false)
    @NotNull
    @EqualsAndHashCode.Include
    private AddressType addressType;

    @Column(name = "address1", nullable = false)
    @NotNull
    @NotBlank
    @EqualsAndHashCode.Include
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "zipcode", nullable = false)
    @NotNull
    @NotBlank
    @Size(min = 9)
    @Pattern(regexp = "\\d{5}[-\\s]?\\d{3}")
    @EqualsAndHashCode.Include
    private String zipcode;

    @Column(name = "neighbor", nullable = false)
    @NotNull
    @NotBlank
    @EqualsAndHashCode.Include
    private String neighbor;

    @Column(name = "city", nullable = false)
    @NotNull
    @NotBlank
    @EqualsAndHashCode.Include
    private String city;

    @ManyToOne(optional = false, targetEntity = State.class)
    @JoinColumn(name = "state_id", referencedColumnName = "id", nullable = false)
    @NotNull
    @EqualsAndHashCode.Include
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
