package br.ucsal.clinica.administracao.adaptador.web;

import java.time.LocalDateTime;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SetorDTO {

    private SetorDTO() {
    }

    public record Request(
            @NotNull(message = "Informe a clínica (RN004)") Long clinicaId,
            @NotBlank @Size(max = 100) String nome,
            @Email @Size(max = 150) String email,
            @Size(max = 20) String telefone,
            @NotNull(message = "Informe o responsável pelo setor (RN005)") Long responsavelId,
            Long versao) {

        public Setor paraDominio() {
            Setor s = new Setor();
            s.setClinica(Referencia.de(clinicaId));
            s.setNome(nome);
            s.setEmail(email);
            s.setTelefone(telefone);
            s.setResponsavel(Referencia.de(responsavelId));
            return s;
        }
    }

    public record Response(
            Long id,
            Long clinicaId,
            String clinicaNome,
            String nome,
            String email,
            String telefone,
            Long responsavelId,
            String responsavelNome,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Setor s) {
            return new Response(s.getId(), Referencia.idDe(s.getClinica()), Referencia.nomeDe(s.getClinica()),
                    s.getNome(), s.getEmail(), s.getTelefone(),
                    Referencia.idDe(s.getResponsavel()), Referencia.nomeDe(s.getResponsavel()),
                    s.getVersao(), s.getDataCadastro(), s.getStatus(), s.getMotivoInativacao(), s.getDataInativacao());
        }
    }
}
