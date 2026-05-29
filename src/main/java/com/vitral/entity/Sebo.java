package com.vitral.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sebo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sebo extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(length = 2000)
    private String descricao;

    @Column(length = 20)
    private String telefone;

    @Column(length = 9)
    private String cep;

    @Column(length = 255)
    private String logradouro;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(name = "horario_funcionamento", length = 255)
    private String horarioFuncionamento;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;
}
