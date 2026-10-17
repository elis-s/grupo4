package br.ucsal.clinica.administracao.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.dominio.ResponsavelClinica;
import br.ucsal.clinica.administracao.dominio.TelefoneClinica;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Objetos de transferencia da clinica (o Back-End nunca expoe as entidades - secao 4.2.3). */
public final class ClinicaDTO {

    private ClinicaDTO() {
    }

    public record TelefoneDTO(
            @NotBlank @Size(max = 20) String telefone,
            @Size(max = 50) String descricao) {
    }

    public record ResponsavelDTO(
            @NotBlank @Size(max = 150) String nome,
            @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 números") String cpf) {
    }

    public record Request(
            @NotBlank @Size(max = 200) String razaoSocial,
            @NotBlank @Size(max = 200) String nomeFantasia,
            @NotBlank @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 números") String cnpj,
            @NotBlank @Size(max = 200) String logradouro,
            @Size(max = 20) String numero,
            @Size(max = 100) String complemento,
            @NotBlank @Size(max = 100) String bairro,
            @NotBlank @Size(max = 100) String cidade,
            @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "Estado deve ser a sigla (ex.: BA)") String estado,
            @Size(max = 60) String pais,
            @NotBlank @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 números") String cep,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 200) String atividadePrincipal,
            String missao,
            @NotBlank @Size(max = 50) String alvaraNumero,
            LocalDate alvaraDataEmissao,
            LocalDate alvaraDataValidade,
            @NotBlank @Size(max = 50) String licencaSanitariaNumero,
            LocalDate licencaSanitariaEmissao,
            LocalDate licencaSanitariaValidade,
            @NotBlank @Size(max = 30) String registroCrm,
            @NotBlank @Size(max = 150) String diretorTecnicoNome,
            @Size(max = 30) String diretorTecnicoCrm,
            @NotEmpty(message = "Informe ao menos um telefone") List<@Valid TelefoneDTO> telefones,
            @NotEmpty(message = "Informe ao menos um responsável") List<@Valid ResponsavelDTO> responsaveis,
            /** Versao lida (bloqueio otimista): obrigatoria so na edicao. */
            Long versao) {

        public Clinica paraDominio() {
            Clinica c = new Clinica();
            c.setRazaoSocial(razaoSocial);
            c.setNomeFantasia(nomeFantasia);
            c.setCnpj(cnpj);
            c.setLogradouro(logradouro);
            c.setNumero(numero);
            c.setComplemento(complemento);
            c.setBairro(bairro);
            c.setCidade(cidade);
            c.setEstado(estado);
            c.setPais(pais);
            c.setCep(cep);
            c.setEmail(email);
            c.setAtividadePrincipal(atividadePrincipal);
            c.setMissao(missao);
            c.setAlvaraNumero(alvaraNumero);
            c.setAlvaraDataEmissao(alvaraDataEmissao);
            c.setAlvaraDataValidade(alvaraDataValidade);
            c.setLicencaSanitariaNumero(licencaSanitariaNumero);
            c.setLicencaSanitariaEmissao(licencaSanitariaEmissao);
            c.setLicencaSanitariaValidade(licencaSanitariaValidade);
            c.setRegistroCrm(registroCrm);
            c.setDiretorTecnicoNome(diretorTecnicoNome);
            c.setDiretorTecnicoCrm(diretorTecnicoCrm);
            c.setTelefones(new ArrayList<>(telefones.stream()
                    .map(t -> new TelefoneClinica(t.telefone(), t.descricao())).toList()));
            c.setResponsaveis(new ArrayList<>(responsaveis.stream()
                    .map(r -> new ResponsavelClinica(r.nome(), r.cpf())).toList()));
            return c;
        }
    }

    public record Response(
            Long id,
            String razaoSocial,
            String nomeFantasia,
            String cnpj,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String estado,
            String pais,
            String cep,
            String email,
            String atividadePrincipal,
            String missao,
            String alvaraNumero,
            LocalDate alvaraDataEmissao,
            LocalDate alvaraDataValidade,
            String licencaSanitariaNumero,
            LocalDate licencaSanitariaEmissao,
            LocalDate licencaSanitariaValidade,
            String registroCrm,
            String diretorTecnicoNome,
            String diretorTecnicoCrm,
            List<TelefoneDTO> telefones,
            List<ResponsavelDTO> responsaveis,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Clinica c) {
            return new Response(c.getId(), c.getRazaoSocial(), c.getNomeFantasia(), c.getCnpj(),
                    c.getLogradouro(), c.getNumero(), c.getComplemento(), c.getBairro(), c.getCidade(),
                    c.getEstado(), c.getPais(), c.getCep(), c.getEmail(), c.getAtividadePrincipal(),
                    c.getMissao(), c.getAlvaraNumero(), c.getAlvaraDataEmissao(), c.getAlvaraDataValidade(),
                    c.getLicencaSanitariaNumero(), c.getLicencaSanitariaEmissao(), c.getLicencaSanitariaValidade(),
                    c.getRegistroCrm(), c.getDiretorTecnicoNome(), c.getDiretorTecnicoCrm(),
                    c.getTelefones().stream().map(t -> new TelefoneDTO(t.telefone(), t.descricao())).toList(),
                    c.getResponsaveis().stream().map(r -> new ResponsavelDTO(r.nome(), r.cpf())).toList(),
                    c.getVersao(), c.getDataCadastro(), c.getStatus(), c.getMotivoInativacao(), c.getDataInativacao());
        }
    }
}
