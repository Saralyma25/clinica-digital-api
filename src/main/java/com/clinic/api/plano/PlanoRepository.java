package com.clinic.api.plano;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, UUID> {

    // --- BUSCAS BÁSICAS ---

    // 1. Busca todos os planos de um convênio (Ativos e Inativos)
    // Útil para área administrativa
    List<Plano> findByConvenioId(UUID convenioId);

    // 2. Busca genérica por nome do plano (Ex: "Top", "Enfermaria")
    List<Plano> findByNomeContainingIgnoreCase(String nome);

    // --- BUSCAS DE NEGÓCIO (ESSENCIAIS) ---

    // 3. O QUE VOCÊ PEDIU: Busca apenas planos ATIVOS de um convênio específico (Pelo ID)
    // Esse é o método que vai preencher o selectbox da recepcionista!
    List<Plano> findByConvenioIdAndAtivoTrue(UUID convenioId);

    // 4. Busca planos ATIVOS pelo NOME do convênio (Navegação Avançada)
    // O underline (_) diz pro Spring: "Entre no objeto Convenio e busque pelo campo Nome"
    // Ex: Busca todos os planos ativos onde o convênio se chama "Bradesco"
    List<Plano> findByConvenio_NomeContainingIgnoreCaseAndAtivoTrue(String nomeConvenio);

    // 5. Busca Específica dentro do Convênio
    // Ex: Procurar se existe um plano "Flex" dentro do convênio X
    List<Plano> findByConvenioIdAndNomeContainingIgnoreCase(UUID convenioId, String nomePlano);
}