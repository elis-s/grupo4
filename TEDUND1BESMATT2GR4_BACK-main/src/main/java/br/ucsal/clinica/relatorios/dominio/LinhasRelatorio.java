package br.ucsal.clinica.relatorios.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Visoes de leitura (Read-Only Projections - secao 3.2.6) usadas nos relatorios:
 * cada linha traz somente o que o relatorio exibe.
 */
public final class LinhasRelatorio {

    private LinhasRelatorio() {
    }

    public record LinhaClinica(Long id, String nomeFantasia, String cnpj, String cidade, String estado,
            String status) {
    }

    public record LinhaSetor(Long id, String nome, String clinica, String responsavel, String email, String telefone,
            LocalDateTime dataCadastro, String status) {
    }

    public record LinhaConvenio(Long id, String nomeFantasia, String registroAns, String cnpj, String status) {
    }

    public record LinhaColaborador(Long id, String nomeCompleto, String funcao, String setor, String especialidade,
            String conselho, String status) {
    }

    public record LinhaUsuario(Long id, String login, String colaborador, List<String> perfis,
            LocalDateTime ultimoLogin, String status) {
    }

    public record LinhaPaciente(Long id, String nomeCompleto, LocalDate dataNascimento, String medico,
            String convenio, String status) {
    }

    public record LinhaAgendamento(Long id, LocalDate data, LocalTime hora, String paciente, String medico,
            String situacao, LocalDateTime dataRecepcao, String status) {
    }

    public record LinhaAtendimento(Long id, LocalDateTime inicio, LocalDateTime encerramento, String paciente,
            String colaborador, String tipo, String etapa, boolean internado, String status) {
    }

    public record LinhaMedicamento(Long id, String nome, String fabricante, int estoqueMinimo, long saldo,
            boolean reposicaoNecessaria, String status) {
    }

    public record LinhaSolicitacao(Long id, LocalDateTime dataCadastro, String medicamento, int quantidade,
            String solicitante, String situacao, String analisadoPor, LocalDateTime dataAnalise,
            String motivoReprovacao) {
    }

    public record LinhaMovimentacao(Long id, LocalDateTime dataHora, String tipo, String medicamento, int quantidade,
            String solicitante, String responsavelEntrega, String lote) {
    }
}
