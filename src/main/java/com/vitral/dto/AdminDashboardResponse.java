package com.vitral.dto;

import java.util.Map;

public record AdminDashboardResponse(long usuarios, long usuariosAtivos, long sebos, long produtos,
        long produtosAtivos, long pedidos, Map<String, Long> pedidosPorStatus) {}
