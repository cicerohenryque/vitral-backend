package com.vitral.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vitral.dto.ProdutoRequest;
import com.vitral.entity.CatalogoLivro;
import com.vitral.exception.BusinessException;
import com.vitral.repository.CatalogoLivroRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoLivroService {

    private final CatalogoLivroRepository repository;

    @Transactional
    public CatalogoLivro obterOuCriar(ProdutoRequest request) {
        if (request.isbn() == null || request.isbn().isBlank()) {
            return null;
        }
        String isbn = normalizarEValidar(request.isbn());
        return repository.findByIsbn(isbn).orElseGet(() -> repository.save(CatalogoLivro.builder()
                .isbn(isbn)
                .titulo(request.titulo())
                .autores(request.autor())
                .capaUrl(request.fotoUrl())
                .build()));
    }

    String normalizarEValidar(String valor) {
        String isbn = valor.replaceAll("[-\\s]", "").toUpperCase();
        if (!isbn.matches("(?:\\d{13}|\\d{9}[\\dX])") || !checksumValido(isbn)) {
            throw new BusinessException("ISBN invalido", HttpStatus.BAD_REQUEST);
        }
        return isbn;
    }

    private boolean checksumValido(String isbn) {
        if (isbn.length() == 13) {
            int soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.digit(isbn.charAt(i), 10) * (i % 2 == 0 ? 1 : 3);
            }
            return soma % 10 == 0;
        }
        int soma = 0;
        for (int i = 0; i < 10; i++) {
            int digito = isbn.charAt(i) == 'X' ? 10 : Character.digit(isbn.charAt(i), 10);
            soma += digito * (10 - i);
        }
        return soma % 11 == 0;
    }
}
