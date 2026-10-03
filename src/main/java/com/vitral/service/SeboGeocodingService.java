package com.vitral.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vitral.entity.Sebo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeboGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(SeboGeocodingService.class);

    private final GeocodingClient client;

    public void geocodificar(Sebo sebo) {
        if (sebo.getCidade() == null || sebo.getUf() == null) {
            log.info("geocodificacao_sebo status=ignorada motivo=endereco_incompleto seboId={}", sebo.getId());
            sebo.setLatitude(null);
            sebo.setLongitude(null);
            return;
        }
        try {
            boolean encontrado = buscarEAplicar(sebo, enderecoCompleto(sebo));
            if (!encontrado) {
                encontrado = buscarEAplicar(sebo, enderecoCidade(sebo));
            }
            if (encontrado) {
                log.info("geocodificacao_sebo status=sucesso seboId={} cidade={} uf={}",
                        sebo.getId(), sebo.getCidade(), sebo.getUf());
            } else {
                log.warn("geocodificacao_sebo status=sem_resultado seboId={} cidade={} uf={}",
                        sebo.getId(), sebo.getCidade(), sebo.getUf());
            }
        } catch (RuntimeException exception) {
            log.warn("geocodificacao_sebo status=erro seboId={} tipoErro={}",
                    sebo.getId(), exception.getClass().getSimpleName(), exception);
            sebo.setLatitude(null);
            sebo.setLongitude(null);
        }
    }

    private boolean buscarEAplicar(Sebo sebo, String query) {
        return client.buscarCoordenadas(query)
                .map(coordenadas -> {
                    sebo.setLatitude(coordenadas.latitude());
                    sebo.setLongitude(coordenadas.longitude());
                    return true;
                })
                .orElseGet(() -> {
                    sebo.setLatitude(null);
                    sebo.setLongitude(null);
                    return false;
                });
    }

    private String enderecoCompleto(Sebo sebo) {
        StringBuilder query = new StringBuilder();
        if (sebo.getLogradouro() != null) {
            query.append(sebo.getLogradouro()).append(", ");
        }
        query.append(sebo.getCidade()).append(" - ").append(sebo.getUf()).append(", Brasil");
        return query.toString();
    }

    private String enderecoCidade(Sebo sebo) {
        return sebo.getCidade() + " - " + sebo.getUf() + ", Brasil";
    }
}
