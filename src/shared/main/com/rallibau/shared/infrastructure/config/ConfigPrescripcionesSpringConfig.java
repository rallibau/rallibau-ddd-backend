package com.rallibau.shared.infrastructure.config;

import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.config.ConfigPrescipciones;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ConfigPrescripcionesSpringConfig implements ConfigPrescipciones {


    @Value("${prescripciones.conf.usar_campo_nemo}")
    private Boolean PRESCRIPCION_USAR_CAMPO_NEMO;


    @Override
    public boolean UsarCampoNemos() {
        return PRESCRIPCION_USAR_CAMPO_NEMO;
    }
}
