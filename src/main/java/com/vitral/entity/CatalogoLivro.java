package com.vitral.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalogo_livro")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoLivro extends BaseEntity {

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(length = 500)
    private String autores;

    @Column(length = 255)
    private String editora;

    @Column(length = 100)
    private String edicao;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @Column(length = 50)
    private String idioma;

    @Column(name = "capa_url", length = 500)
    private String capaUrl;
}
