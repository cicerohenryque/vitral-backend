package com.vitral.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.AccountResponse;
import com.vitral.dto.AtualizarPerfilRequest;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.mapper.AccountMapper;
import com.vitral.repository.AccountRepository;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ConflictException;
import org.springframework.http.HttpStatus;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AnonimizacaoContaService anonimizacaoContaService;

    @Transactional
    public AccountResponse atualizarPerfil(Account account, AtualizarPerfilRequest request) {
        String cpf = somenteDigitos(request.cpf());
        String cep = somenteDigitos(request.cep());
        if (!cpfValido(cpf)) {
            throw new BusinessException("CPF invalido", HttpStatus.BAD_REQUEST);
        }
        if (accountRepository.existsByCpfAndIdNot(cpf, account.getId())) {
            throw new ConflictException("CPF ja cadastrado");
        }
        if (cep.length() != 8) {
            throw new BusinessException("CEP deve conter 8 numeros", HttpStatus.BAD_REQUEST);
        }
        account.setName(request.name());
        account.setFotoUrl(request.fotoUrl());
        account.setCpf(cpf);
        account.setCep(cep);
        account.setLogradouro(request.logradouro().trim());
        account.setNumero(request.numero().trim());
        account.setComplemento(normalizarOpcional(request.complemento()));
        account.setBairro(request.bairro().trim());
        account.setCidade(request.cidade().trim());
        account.setEstado(request.estado().toUpperCase(Locale.ROOT));
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) return false;
        int soma1 = 0;
        int soma2 = 0;
        for (int i = 0; i < 9; i++) {
            int digito = cpf.charAt(i) - '0';
            soma1 += digito * (10 - i);
            soma2 += digito * (11 - i);
        }
        int d1 = 11 - soma1 % 11;
        if (d1 >= 10) d1 = 0;
        soma2 += d1 * 2;
        int d2 = 11 - soma2 % 11;
        if (d2 >= 10) d2 = 0;
        return cpf.charAt(9) - '0' == d1 && cpf.charAt(10) - '0' == d2;
    }

    @Transactional
    public MensagemResponse excluirConta(Account account) {
        anonimizacaoContaService.anonimizarUsuario(account);
        return new MensagemResponse("Conta excluida com sucesso");
    }
}
