package br.ucsal.clinica.administracao.adaptador.web;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.DiaSemana;
import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.administracao.dominio.HorarioAtendimento;
import br.ucsal.clinica.administracao.dominio.Turno;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ColaboradorDTO {

    private ColaboradorDTO() {
    }

    public record HorarioDTO(@NotNull DiaSemana diaSemana, @NotNull Turno turno) {
    }

    /** RF010 (todos), RF047 (dados do medico) e RF048 (demais profissionais). */
    public record Request(
            @NotBlank @Size(max = 150) String nomeCompleto,
            @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 números") String cpf,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 20) String telefone,
            @NotNull(message = "Informe a função") Funcao funcao,
            @NotNull(message = "Informe o local de trabalho (setor)") Long setorId,
            @NotNull(message = "Informe o início do horário de trabalho") LocalTime horaInicio,
            @NotNull(message = "Informe o fim do horário de trabalho") LocalTime horaFim,
            @NotNull @Min(1) @Max(60) Integer cargaHorariaSemanal,
            @Size(max = 10) String conselhoSigla,
            @Pattern(regexp = "[A-Z]{2}", message = "UF do conselho deve ser a sigla (ex.: BA)") String conselhoUf,
            @Size(max = 20) String conselhoNumero,
            Long especialidadeId,
            List<@Valid HorarioDTO> horariosAtendimento,
            Long versao) {

        public Colaborador paraDominio() {
            Colaborador c = new Colaborador();
            c.setNomeCompleto(nomeCompleto);
            c.setCpf(cpf);
            c.setEmail(email);
            c.setTelefone(telefone);
            c.setFuncao(funcao);
            c.setSetor(Referencia.de(setorId));
            c.setHoraInicio(horaInicio);
            c.setHoraFim(horaFim);
            c.setCargaHorariaSemanal(cargaHorariaSemanal);
            c.setConselhoSigla(conselhoSigla);
            c.setConselhoUf(conselhoUf);
            c.setConselhoNumero(conselhoNumero);
            c.setEspecialidade(Referencia.de(especialidadeId));
            c.setHorariosAtendimento(horariosAtendimento == null ? new ArrayList<>()
                    : new ArrayList<>(horariosAtendimento.stream()
                            .map(h -> new HorarioAtendimento(h.diaSemana(), h.turno())).toList()));
            return c;
        }
    }

    public record Response(
            Long id,
            String nomeCompleto,
            String cpf,
            String email,
            String telefone,
            Funcao funcao,
            Long setorId,
            String setorNome,
            LocalTime horaInicio,
            LocalTime horaFim,
            Integer cargaHorariaSemanal,
            String conselhoSigla,
            String conselhoUf,
            String conselhoNumero,
            Long especialidadeId,
            String especialidadeNome,
            List<HorarioDTO> horariosAtendimento,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Colaborador c) {
            return new Response(c.getId(), c.getNomeCompleto(), c.getCpf(), c.getEmail(), c.getTelefone(),
                    c.getFuncao(), Referencia.idDe(c.getSetor()), Referencia.nomeDe(c.getSetor()),
                    c.getHoraInicio(), c.getHoraFim(), c.getCargaHorariaSemanal(),
                    c.getConselhoSigla(), c.getConselhoUf(), c.getConselhoNumero(),
                    Referencia.idDe(c.getEspecialidade()), Referencia.nomeDe(c.getEspecialidade()),
                    c.getHorariosAtendimento().stream().map(h -> new HorarioDTO(h.diaSemana(), h.turno())).toList(),
                    c.getVersao(), c.getDataCadastro(), c.getStatus(), c.getMotivoInativacao(), c.getDataInativacao());
        }
    }

    /** Versao resumida para listas de selecao (ex.: escolher o medico no agendamento). */
    public record Resumo(Long id, String nomeCompleto, Funcao funcao, String especialidade) {

        public static Resumo de(Colaborador c) {
            return new Resumo(c.getId(), c.getNomeCompleto(), c.getFuncao(), Referencia.nomeDe(c.getEspecialidade()));
        }
    }
}
