package com.vitral.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vitral.entity.CatalogoLivro;

public interface CatalogoLivroRepository extends JpaRepository<CatalogoLivro, Long> {
    Optional<CatalogoLivro> findByIsbn(String isbn);
}
