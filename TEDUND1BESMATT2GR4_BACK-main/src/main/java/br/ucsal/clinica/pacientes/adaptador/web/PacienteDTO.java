package br.ucsal.clinica.pacientes.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PacienteDTO {

    private PacienteDTO() {
    }

    /** RF027 e RF052: identificacao, medico, especialidade e, se houver, convenio com carteirinha. */
    public record Request(
            @NotBlank @Size(max = 150) String nomeCompleto,
            @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 números") String cpf,
            @NotNull @Past(message = "A data de nascimento deve estar no passado") LocalDate dataNascimento,
            @Pattern(regexp = "[MFO]", message = "Sexo deve ser M, F ou O") String sexo,
            @Size(max = 20) String telefone,
            @Email @Size(max = 150) String email,
            @NotNull(message = "Informe o médico") Long medicoId,
            @NotNull(message = "Informe a especialidade médica") Long especialidadeId,
            Long convenioId,
            @Size(max = 40) String numeroCarteirinha,
            Long versao) {

        public Paciente paraDominio() {
            Paciente p = new Paciente();
            p.setNomeCompleto(nomeCompleto);
            p.setCpf(cpf);
            p.setDataNascimento(dataNascimento);
            p.setSexo(sexo);
            p.setTelefone(telefone);
            p.setEmail(email);
            p.setMedico(Referencia.de(medicoId));
            p.setEspecialidade(Referencia.de(especialidadeId));
            p.setConvenio(Referencia.de(convenioId));
            p.setNumeroCarteirinha(numeroCarteirinha);
            return p;
        }
    }

    public record Response(
            Long id,
            String nomeCompleto,
            String cpf,
            LocalDate dataNascimento,
            String sexo,
            String telefone,
            String email,
            Long medicoId,
            String medicoNome,
            Long especialidadeId,
            String especialidadeNome,
            Long convenioId,
            String convenioNome,
            String numeroCarteirinha,
            Long prontuarioId,
            String prontuarioNumero,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Paciente p) {
            return new Response(p.getId(), p.getNomeCompleto(), p.getCpf(), p.getDataNascimento(), p.getSexo(),
                    p.getTelefone(), p.getEmail(), Referencia.idDe(p.getMedico()), Referencia.nomeDe(p.getMedico()),
                    Referencia.idDe(p.getEspecialidade()), Referencia.nomeDe(p.getEspecialidade()),
                    Referencia.idDe(p.getConvenio()),
                    p.getConvenio() != null ? p.getConvenio().nome() : "Particular",
                    p.getNumeroCarteirinha(), Referencia.idDe(p.getProntuario()), Referencia.nomeDe(p.getProntuario()),
                    p.getVersao(), p.getDataCadastro(), p.getStatus(), p.getMotivoInativacao(), p.getDataInativacao());
        }
    }
}
