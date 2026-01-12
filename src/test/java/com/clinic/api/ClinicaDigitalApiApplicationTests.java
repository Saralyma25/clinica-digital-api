package com.clinic.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Desativado no CI pois requer Banco de Dados Real") // <--- ADICIONE ISSO
class ClinicaDigitalApiApplicationTests {

    @Test
    void contextLoads() {
    }

}