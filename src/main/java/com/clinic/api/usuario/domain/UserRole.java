
package com.clinic.api.usuario.domain;

public enum UserRole {
    ADMIN,
    MEDICO,
    RECEPCAO,
    SECRETARIA,// Secretárias e Atendentes
    ENFERMAGEM,   // Enfermeiros e Técnicos
    PACIENTE,
    STAFF         // Vigia, Segurança, Outros (Acesso básico/nenhum ao prontuário)
}