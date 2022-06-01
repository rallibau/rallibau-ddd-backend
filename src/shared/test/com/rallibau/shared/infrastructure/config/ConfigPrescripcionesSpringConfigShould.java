package com.rallibau.shared.infrastructure.config;


import com.rallibau.apps.monolith.MonolithApplication;
import com.rallibau.shared.domain.config.ConfigPrescipciones;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;

@ContextConfiguration(classes = MonolithApplication.class)
@SpringBootTest
public class ConfigPrescripcionesSpringConfigShould {

    @Autowired
    private ConfigPrescipciones configPrescipciones;

    @BeforeEach
    public void setUp() {
        this.configPrescipciones = configPrescipciones;
    }

    //@Test()
    public void usar_campos_nemos_is_not_null() {
        Boolean usarCamposNemos = configPrescipciones.UsarCampoNemos();
        assertThat("usarCamposNemos is not null", usarCamposNemos != null);
    }
}
