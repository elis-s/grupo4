package br.ucsal.clinica.administracao.adaptador.web;

import java.time.LocalDateTime;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ConvenioDTO {

    private ConvenioDTO() {
    }

    public record Request(
            @NotBlank @Size(max = 200) String razaoSocial,
            @NotBlank @Size(max = 200) String nomeFantasia,
            @NotBlank @Size(max = 20) String registroAns,
            @NotBlank @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 números") String cnpj,
            @Size(max = 30) String inscricaoMunicipal,
            @NotBlank @Size(max = 30) String codigoPrestador,
            @Email @Size(max = 150) String email,
            @Size(max = 20) String telefone,
            Long versao) {

        public Convenio paraDominio() {
            Convenio c = new Convenio();
            c.setRazaoSocial(razaoSocial);
            c.setNomeFantasia(nomeFantasia);
            c.setRegistroAns(registroAns);
            c.setCnpj(cnpj);
            c.setInscricaoMunicipal(inscricaoMunicipal);
            c.setCodigoPrestador(codigoPrestador);
            c.setEmail(email);
            c.setTelefone(telefone);
            return c;
        }
    }

    public record Response(
            Long id,
            String razaoSocial,
            String nomeFantasia,
            String registroAns,
            String cnpj,
            String inscricaoMunicipal,
            String codigoPrestador,
            String email,
            String telefone,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Convenio c) {
            return new Response(c.getId(), c.getRazaoSocial(), c.getNomeFantasia(), c.getRegistroAns(),
                    c.getCnpj(), c.getInscricaoMunicipal(), c.getCodigoPrestador(), c.getEmail(), c.getTelefone(),
                    c.getVersao(), c.getDataCadastro(), c.getStatus(), c.getMotivoInativacao(), c.getDataInativacao());
        }
    }
}
