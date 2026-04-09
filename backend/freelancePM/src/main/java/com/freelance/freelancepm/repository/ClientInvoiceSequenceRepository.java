package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.ClientInvoiceSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientInvoiceSequenceRepository extends JpaRepository<ClientInvoiceSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ClientInvoiceSequence> findByClientIdAndYear(Integer clientId, int year);
}
