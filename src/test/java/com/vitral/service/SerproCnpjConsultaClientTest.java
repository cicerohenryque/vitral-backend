package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.vitral.config.CnpjConsultaProperties;
import com.vitral.enumerations.StatusConsultaCnpj;

class SerproCnpjConsultaClientTest {

    @Test
    void retornaIndisponivelQuandoConsultaDesabilitada() {
        var client = new SerproCnpjConsultaClient(new CnpjConsultaProperties(false, null, null, null, null));

        var result = client.consultar("12345678000195");

        assertThat(result.status()).isEqualTo(StatusConsultaCnpj.INDISPONIVEL);
        assertThat(result.mensagem()).contains("nao configurada");
    }

    @Test
    void retornaIndisponivelQuandoCredenciaisIncompletas() {
        var client = new SerproCnpjConsultaClient(new CnpjConsultaProperties(true, " ", "http://consulta", "id", "secret"));

        var result = client.consultar("12345678000195");

        assertThat(result.status()).isEqualTo(StatusConsultaCnpj.INDISPONIVEL);
        assertThat(result.mensagem()).contains("incompletas");
    }

    @Test
    void retornaIndisponivelQuandoProviderFalha() {
        var client = new SerproCnpjConsultaClient(new CnpjConsultaProperties(true,
                "http://localhost:1/token", "http://localhost:1/cnpj/{cnpj}", "id", "secret"));

        var result = client.consultar("12345678000195");

        assertThat(result.status()).isEqualTo(StatusConsultaCnpj.INDISPONIVEL);
        assertThat(result.mensagem()).contains("temporariamente");
    }
}
