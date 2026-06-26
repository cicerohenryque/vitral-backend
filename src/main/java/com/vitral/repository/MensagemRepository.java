package com.vitral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vitral.entity.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    @Query("SELECT m FROM Mensagem m WHERE m.remetente.id = :userId OR m.destinatario.id = :userId ORDER BY m.createdAt DESC")
    Page<Mensagem> findByUsuario(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM Mensagem m WHERE (m.remetente.id = :userA AND m.destinatario.id = :userB) OR (m.remetente.id = :userB AND m.destinatario.id = :userA) ORDER BY m.createdAt ASC")
    Page<Mensagem> findConversa(@Param("userA") Long userA, @Param("userB") Long userB, Pageable pageable);
}
