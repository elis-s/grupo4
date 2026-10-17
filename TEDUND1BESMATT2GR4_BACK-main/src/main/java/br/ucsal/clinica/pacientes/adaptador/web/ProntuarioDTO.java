package br.ucsal.clinica.pacientes.adaptador.web;

import java.time.LocalDateTime;
import java.util.List;

import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.dominio.EvolucaoClinica;
import br.ucsal.clinica.pacientes.dominio.ProntuarioCompleto;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;

public final class ProntuarioDTO {

    private ProntuarioDTO() {
    }

    /** PEP completo (RF030, RN026): dados do paciente, convenio e historico de evolucoes clinicas. */
    public record Response(
            Long id,
            String numero,
            Status status,
            String motivoInativacao,
            Long versao,
            LocalDateTime dataCadastro,
            PacienteDTO.Response paciente,
            List<EvolucaoClinica> evolucoes) {

        public static Response de(ProntuarioCompleto pc) {
            ProntuarioEletronico p = pc.prontuario();
            return new Response(p.getId(), p.getNumero(), p.getStatus(), p.getMotivoInativacao(), p.getVersao(),
                    p.getDataCadastro(), PacienteDTO.Response.de(pc.paciente()), pc.evolucoes());
        }
    }

    /** Resumo devolvido na inativacao e reativacao do prontuario (RF051). */
    public record Resumo(Long id, String numero, Long pacienteId, Status status, String motivoInativacao,
            LocalDateTime dataInativacao, Long versao) {

        public static Resumo de(ProntuarioEletronico p) {
            return new Resumo(p.getId(), p.getNumero(), p.getPacienteId(), p.getStatus(), p.getMotivoInativacao(),
                    p.getDataInativacao(), p.getVersao());
        }
    }
}
