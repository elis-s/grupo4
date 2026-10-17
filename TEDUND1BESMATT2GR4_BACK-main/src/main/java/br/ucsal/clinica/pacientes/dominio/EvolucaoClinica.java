package br.ucsal.clinica.pacientes.dominio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uma evolucao clinica registrada no PEP: o atendimento com sintomas, diagnostico,
 * tratamento, medicamentos prescritos e orientacoes de enfermagem (historico do paciente).
 */
public record EvolucaoClinica(
        Long atendimentoId,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraEncerramento,
        String tipo,
        String etapa,
        String colaboradorResponsavel,
        String convenio,
        String sintomas,
        String diagnostico,
        String tratamento,
        boolean internado,
        List<String> medicamentos,
        List<String> orientacoesEnfermagem,
        String status) {
}
