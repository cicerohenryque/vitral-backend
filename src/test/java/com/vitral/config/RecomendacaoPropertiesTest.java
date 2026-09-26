package com.vitral.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RecomendacaoPropertiesTest {

    @Test
    void usaPesosPadraoSegurosQuandoConfiguracaoNaoFoiInformada() {
        RecomendacaoProperties properties = new RecomendacaoProperties(1000, 200, 20, 30, 365, 30, 3, 90, 50, 90, null);

        assertThat(properties.pesos()).isEqualTo(RecomendacaoProperties.Pesos.padrao());
    }

    @Test
    void preservaPesosCustomizadosValidos() {
        RecomendacaoProperties.Pesos pesos = new RecomendacaoProperties.Pesos(
                2.0, 1.8, 0.7, 0.3, 1.1, 0.25, 1.5, 3.0, 1.5, 0.7);
        RecomendacaoProperties properties = new RecomendacaoProperties(1000, 200, 20, 30, 365, 30, 3, 90, 50, 90, pesos);

        assertThat(properties.pesos()).isEqualTo(pesos);
    }

    @Test
    void substituiPesosNaoFinitosPorDefaultsSeguros() {
        RecomendacaoProperties.Pesos pesos = new RecomendacaoProperties.Pesos(
                Double.NaN, Double.POSITIVE_INFINITY, 0, -1, 0, 0, 0, 0, 0, 0);

        assertThat(pesos).isEqualTo(RecomendacaoProperties.Pesos.padrao());
    }
}
