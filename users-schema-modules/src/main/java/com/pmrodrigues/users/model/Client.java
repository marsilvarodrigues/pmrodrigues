package com.pmrodrigues.users.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@EntityListeners(AuditingEntityListener.class)
@With
public class Client extends User {

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<Address> addresses;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<Phone> phones;

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

    public Client add(@NonNull Phone phone) {
        if( phones == null ) phones = new ArrayList<>();

        if( !phones.contains(phone) ) phones.add(phone.withOwner(this));
        return this;
    }

    public Client add(@NonNull Address address) {
        if( addresses == null ) addresses = new ArrayList<>();
        if( !addresses.contains(address) ) addresses.add(address.withOwner(this));
        return this;
    }

    public Client remove(@NonNull Address address) {
        if( addresses != null && addresses.contains(address))
            this.addresses.remove(address);
        return this;
    }

    public Client remove(@NonNull Phone phone) {
        if( phones != null && phones.contains(phone))
            this.phones.remove(phone);
        return this;
    }

    public Long getAge() {
        return Long.valueOf(Period.between(birthday, LocalDate.now()).getYears());
    }

    @Override
    public Client withExternalId(final UUID uuid){
        super.withExternalId(uuid);
        return this;
    }

    public Client withFirstName(@NonNull String firstName){
        super.withFirstName(firstName);
        return this;
    }

    public Client withLastName(@NonNull String lastName) {
        super.withLastName(lastName);
        return this;
    }

    public Client withId(@NonNull UUID id) {
        super.withId(id);
        return this;
    }

}
