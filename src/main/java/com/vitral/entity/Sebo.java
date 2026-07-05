package com.vitral.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.OffsetDateTime;
import com.vitral.enumerations.StatusVerificacaoSebo;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @Column(length = 2000)
    private String descricao;

    @Column(length = 20)
    private String telefone;

    @Column(length = 14, unique = true)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_verificacao", nullable = false, length = 20)
    @Builder.Default
    private StatusVerificacaoSebo statusVerificacao = StatusVerificacaoSebo.PENDENTE;

    @Column(name = "verificado_em")
    private OffsetDateTime verificadoEm;

    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;

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
