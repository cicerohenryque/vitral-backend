package com.vitral.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.recomendacao")
public record RecomendacaoProperties(
        int historicoMaximo,
        int candidatosMaximo,
        int tamanhoHome,
        int retencaoTermoDias,
        int retencaoEventoDias,
        int janelaVisualizacaoMinutos,
        int eventosRepetidosDiaMaximo,
        int compraRecenteDias,
        int tamanhoPaginaMaximo,
        int popularidadeDias,
        Pesos pesos) {

    public RecomendacaoProperties {
        if (historicoMaximo < 1) historicoMaximo = 1000;
        if (candidatosMaximo < 1) candidatosMaximo = 200;
        if (tamanhoHome < 1) tamanhoHome = 20;
        if (retencaoTermoDias < 1) retencaoTermoDias = 30;
        if (retencaoEventoDias < 1) retencaoEventoDias = 365;
        if (janelaVisualizacaoMinutos < 1) janelaVisualizacaoMinutos = 30;
        if (eventosRepetidosDiaMaximo < 1) eventosRepetidosDiaMaximo = 3;
        if (compraRecenteDias < 1) compraRecenteDias = 90;
        if (tamanhoPaginaMaximo < 1) tamanhoPaginaMaximo = 50;
        if (popularidadeDias < 1) popularidadeDias = 90;
        if (pesos == null) pesos = Pesos.padrao();
    }

    public record Pesos(
            double categoria,
            double autor,
            double faixaPreco,
            double sebo,
            double termoPesquisa,
            double popularidade,
            double lancamento,
            double produtoAte30Dias,
            double produtoAte90Dias,
            double produtoAte180Dias) {

        public Pesos {
            if (!Double.isFinite(categoria) || categoria <= 0) categoria = 1.5;
            if (!Double.isFinite(autor) || autor <= 0) autor = 1.2;
            if (!Double.isFinite(faixaPreco) || faixaPreco <= 0) faixaPreco = 0.5;
            if (!Double.isFinite(sebo) || sebo <= 0) sebo = 0.2;
            if (!Double.isFinite(termoPesquisa) || termoPesquisa <= 0) termoPesquisa = 0.8;
            if (!Double.isFinite(popularidade) || popularidade <= 0) popularidade = 0.15;
            if (!Double.isFinite(lancamento) || lancamento <= 0) lancamento = 1.0;
            if (!Double.isFinite(produtoAte30Dias) || produtoAte30Dias <= 0) produtoAte30Dias = 2.0;
            if (!Double.isFinite(produtoAte90Dias) || produtoAte90Dias <= 0) produtoAte90Dias = 1.0;
            if (!Double.isFinite(produtoAte180Dias) || produtoAte180Dias <= 0) produtoAte180Dias = 0.5;
        }

        public static Pesos padrao() {
            return new Pesos(1.5, 1.2, 0.5, 0.2, 0.8, 0.15, 1.0, 2.0, 1.0, 0.5);
        }
    }
}
