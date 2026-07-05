package com.vitral.service;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.vitral.config.CnpjConsultaProperties;
import com.vitral.enumerations.StatusConsultaCnpj;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SerproCnpjConsultaClient implements CnpjConsultaClient {
    private final CnpjConsultaProperties properties;

    @Override
    public Resultado consultar(String cnpj) {
        if (!properties.enabled()) return indisponivel("Consulta automatica nao configurada");
        if (vazio(properties.clientId()) || vazio(properties.clientSecret()) || vazio(properties.tokenUrl()) || vazio(properties.consultaUrl())) {
            return indisponivel("Credenciais da consulta CNPJ incompletas");
        }
        try {
            var form = new LinkedMultiValueMap<String, String>();
            form.add("grant_type", "client_credentials");
            JsonNode token = RestClient.create().post().uri(properties.tokenUrl())
                    .headers(h -> h.setBasicAuth(properties.clientId(), properties.clientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JsonNode.class);
            String accessToken = token == null ? null : token.path("access_token").asText(null);
            if (vazio(accessToken)) return indisponivel("Provedor nao retornou token de acesso");
            String url = properties.consultaUrl().replace("{cnpj}", cnpj);
            JsonNode body = RestClient.create().get().uri(url)
                    .headers(h -> h.setBearerAuth(accessToken)).retrieve().body(JsonNode.class);
            if (body == null) return indisponivel("Provedor retornou resposta vazia");
            String razaoSocial = primeiroTexto(body, "nomeEmpresarial", "razaoSocial", "nome");
            String codigo = body.path("situacaoCadastral").path("codigo").asText("");
            String descricao = body.path("situacaoCadastral").path("descricao").asText("");
            boolean ativa = "2".equals(codigo) || "02".equals(codigo) || "ATIVA".equalsIgnoreCase(descricao);
            return new Resultado(razaoSocial, ativa ? StatusConsultaCnpj.ATIVA : StatusConsultaCnpj.INATIVA,
                    ativa ? "CNPJ ativo na Receita Federal" : "CNPJ com situacao cadastral nao ativa");
        } catch (RuntimeException e) {
            return indisponivel("Consulta CNPJ temporariamente indisponivel");
        }
    }

    private String primeiroTexto(JsonNode body, String... campos) {
        for (String campo : campos) if (body.hasNonNull(campo) && !body.path(campo).asText().isBlank()) return body.path(campo).asText();
        return null;
    }

    private Resultado indisponivel(String mensagem) { return new Resultado(null, StatusConsultaCnpj.INDISPONIVEL, mensagem); }
    private boolean vazio(String valor) { return valor == null || valor.isBlank(); }
}
