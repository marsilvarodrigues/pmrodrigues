package com.pmrodrigues.users.service;

import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface DataService<I, E> {
    E create(@NonNull E entity);

    void update(@NonNull I id, @NonNull E entity) throws OperationNotAllowedException;

    Page<E> findAll(@NonNull E entity, @NonNull PageRequest pageRequest);

    E findById(I id);

    void delete(@NonNull I id);
}
