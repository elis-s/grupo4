package br.ucsal.clinica.testes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.autenticacao.dominio.SessaoToken;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.SaldoEstoque;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;
import br.ucsal.clinica.estoque.porta.MovimentacaoEstoqueRepositoryPort;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;

/**
 * Implementacoes em memoria das portas, para testar os casos de uso sem Spring e sem
 * banco de dados - exatamente o que a Arquitetura Limpa permite (secao 3.1: testabilidade isolada).
 */
public final class Fakes {

    private Fakes() {
    }

    /** Armazenamento em memoria que simula o id gerado e a versao (@Version). */
    public static class Memoria<T extends EntidadeDominio> {

        protected final Map<Long, T> registros = new LinkedHashMap<>();
        private long sequencia = 0;

        public Optional<T> buscarPorId(Long id) {
            return Optional.ofNullable(registros.get(id));
        }

        public T salvar(T entidade) {
            Long id = entidade.isNovo() ? ++sequencia : entidade.getId();
            long versao = entidade.getVersao() == null ? 0 : entidade.getVersao() + 1;
            LocalDateTime cadastro = entidade.getDataCadastro() == null ? LocalDateTime.now() : entidade.getDataCadastro();
            entidade.restaurarControle(id, versao, cadastro, entidade.getStatus(), entidade.getMotivoInativacao(),
                    entidade.getDataInativacao());
            registros.put(id, entidade);
            return entidade;
        }

        public Pagina<T> listar(Status status, Paginacao paginacao) {
            List<T> itens = registros.values().stream().filter(e -> status == null || e.getStatus() == status).toList();
            return new Pagina<>(itens, 0, itens.size(), itens.size(), 1);
        }

        public List<T> todos() {
            return new ArrayList<>(registros.values());
        }
    }

    // ---------------- seguranca e auditoria ----------------

    public static class Contexto implements SecurityContextPort {

        public UsuarioAutenticado usuario;

        public static Contexto de(Long usuarioId, String login, Long colaboradorId, String funcao, NomePerfil... perfis) {
            Contexto c = new Contexto();
            c.usuario = new UsuarioAutenticado(usuarioId, login, "Nome de " + login, colaboradorId, funcao, Set.of(perfis));
            return c;
        }

        @Override
        public UsuarioAutenticado usuarioAtual() {
            return usuario;
        }

        @Override
        public SessaoToken sessaoAtual() {
            return new SessaoToken("jti-teste", Instant.now().plusSeconds(1800));
        }
    }

    public record Evento(String login, AcaoAuditoria acao, ResultadoAuditoria resultado, String descricao) {
    }

    public static class Rastro implements RastroAuditoriaPort {

        public final List<Evento> eventos = new ArrayList<>();

        @Override
        public void registrar(AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade, Long entidadeId,
                String descricao) {
            eventos.add(new Evento(null, acao, resultado, descricao));
        }

        @Override
        public void registrarComo(String login, AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade,
                Long entidadeId, String descricao) {
            eventos.add(new Evento(login, acao, resultado, descricao));
        }
    }

    // ---------------- administracao ----------------

    public static class Colaboradores extends Memoria<Colaborador> implements ColaboradorRepositoryPort {

        @Override
        public boolean existeCpf(String cpf, Long ignorarId) {
            return registros.values().stream().anyMatch(c -> c.getCpf().equals(cpf) && !c.getId().equals(ignorarId));
        }

        @Override
        public boolean existeRegistroConselho(String sigla, String uf, String numero, Long ignorarId) {
            return false;
        }

        @Override
        public List<Colaborador> listarMedicosAtivos() {
            return registros.values().stream().filter(c -> c.isMedico() && c.isAtivo()).toList();
        }
    }

    public static class Convenios extends Memoria<Convenio> implements ConvenioRepositoryPort {

        @Override
        public boolean existeCnpj(String cnpj, Long ignorarId) {
            return false;
        }

        @Override
        public boolean existeRegistroAns(String registroAns, Long ignorarId) {
            return false;
        }
    }

    public static class Especialidades implements EspecialidadeRepositoryPort {

        public final Map<Long, Especialidade> registros = new HashMap<>();

        @Override
        public Optional<Especialidade> buscarPorId(Long id) {
            return Optional.ofNullable(registros.get(id));
        }

        @Override
        public boolean existeNome(String nome) {
            return registros.values().stream().anyMatch(e -> e.nome().equalsIgnoreCase(nome));
        }

        @Override
        public Especialidade salvar(Especialidade e) {
            Especialidade salva = new Especialidade((long) registros.size() + 1, e.nome(), e.status());
            registros.put(salva.id(), salva);
            return salva;
        }

        @Override
        public List<Especialidade> listarAtivas() {
            return new ArrayList<>(registros.values());
        }
    }

    // ---------------- pacientes ----------------

    public static class Pacientes extends Memoria<Paciente> implements PacienteRepositoryPort {

        @Override
        public Pagina<Paciente> listar(Status status, String nome, Paginacao paginacao) {
            return listar(status, paginacao);
        }

        @Override
        public boolean existeCpf(String cpf, Long ignorarId) {
            return registros.values().stream()
                    .anyMatch(p -> cpf.equals(p.getCpf()) && !p.getId().equals(ignorarId));
        }
    }

    public static class Prontuarios extends Memoria<ProntuarioEletronico> implements ProntuarioRepositoryPort {

        @Override
        public Optional<ProntuarioEletronico> buscarPorPaciente(Long pacienteId) {
            return registros.values().stream().filter(p -> p.getPacienteId().equals(pacienteId)).findFirst();
        }
    }

    // ---------------- atendimento ----------------

    public static class Agendamentos extends Memoria<Agendamento> implements AgendamentoRepositoryPort {

        public int bloqueios = 0;

        @Override
        public Pagina<Agendamento> listar(LocalDate data, Long medicoId, Long pacienteId, Paginacao paginacao) {
            return listar((Status) null, paginacao);
        }

        @Override
        public List<Agendamento> agendaDoMedico(Long medicoId, LocalDate aPartirDe) {
            return registros.values().stream().filter(a -> a.getMedico().id().equals(medicoId)).toList();
        }

        @Override
        public boolean horarioOcupado(Long medicoId, LocalDate data, LocalTime hora, Long ignorarId) {
            return registros.values().stream().anyMatch(a -> a.isAtivo() && a.getMedico().id().equals(medicoId)
                    && a.getDataPrevista().equals(data) && a.getHoraPrevista().equals(hora)
                    && !a.getId().equals(ignorarId));
        }

        @Override
        public void bloquearAgendaDoMedico(Long medicoId) {
            bloqueios++;
        }
    }

    public static class Atendimentos extends Memoria<AtendimentoClinico> implements AtendimentoRepositoryPort {

        @Override
        public Pagina<AtendimentoClinico> listar(Paginacao paginacao) {
            return listar((Status) null, paginacao);
        }

        @Override
        public Pagina<AtendimentoClinico> listarDoColaborador(Long colaboradorId, Paginacao paginacao) {
            return listar((Status) null, paginacao);
        }

        @Override
        public boolean existeParaAgendamento(Long agendamentoId) {
            return registros.values().stream().anyMatch(a -> agendamentoId.equals(a.getAgendamentoId()));
        }
    }

    // ---------------- estoque ----------------

    public static class Medicamentos extends Memoria<Medicamento> implements MedicamentoRepositoryPort {

        public final Map<Long, Long> saldos = new HashMap<>();
        public int bloqueios = 0;

        @Override
        public boolean existeNomeFabricante(String nome, String fabricante, Long ignorarId) {
            return false;
        }

        @Override
        public long saldo(Long medicamentoId) {
            return saldos.getOrDefault(medicamentoId, 0L);
        }

        @Override
        public List<SaldoEstoque> saldos() {
            return registros.values().stream().map(m -> new SaldoEstoque(m.getId(), m.getNome(), m.getFabricante(),
                    m.getEstoqueMinimo(), saldo(m.getId()), m.getStatus())).toList();
        }

        @Override
        public List<SaldoEstoque> abaixoDoMinimo() {
            return saldos().stream().filter(SaldoEstoque::reposicaoNecessaria).toList();
        }

        @Override
        public void bloquearParaMovimentar(Long medicamentoId) {
            bloqueios++;
        }
    }

    /** Ao registrar uma movimentacao, atualiza o saldo do medicamento (como a view do banco). */
    public static class Movimentacoes implements MovimentacaoEstoqueRepositoryPort {

        public final List<MovimentacaoEstoque> registros = new ArrayList<>();
        private final Medicamentos medicamentos;

        public Movimentacoes(Medicamentos medicamentos) {
            this.medicamentos = medicamentos;
        }

        @Override
        public MovimentacaoEstoque registrar(MovimentacaoEstoque m) {
            m.setId((long) registros.size() + 1);
            registros.add(m);
            long sinal = m.getTipo() == TipoMovimentacao.ENTRADA ? 1 : -1;
            medicamentos.saldos.merge(m.getMedicamento().id(), sinal * m.getQuantidade(), Long::sum);
            return m;
        }

        @Override
        public Pagina<MovimentacaoEstoque> listar(Long medicamentoId, TipoMovimentacao tipo, LocalDateTime inicio,
                LocalDateTime fim, Paginacao paginacao) {
            return new Pagina<>(registros, 0, registros.size(), registros.size(), 1);
        }
    }

    public static class Solicitacoes extends Memoria<SolicitacaoMedicamento>
            implements SolicitacaoMedicamentoRepositoryPort {

        @Override
        public Pagina<SolicitacaoMedicamento> listar(SituacaoSolicitacao situacao, Paginacao paginacao) {
            List<SolicitacaoMedicamento> itens = registros.values().stream()
                    .filter(s -> situacao == null || s.getSituacao() == situacao).toList();
            return new Pagina<>(itens, 0, itens.size(), itens.size(), 1);
        }

        @Override
        public Pagina<SolicitacaoMedicamento> listarDoSolicitante(Long colaboradorId, Paginacao paginacao) {
            List<SolicitacaoMedicamento> itens = registros.values().stream()
                    .filter(s -> s.getSolicitante().id().equals(colaboradorId)).toList();
            return new Pagina<>(itens, 0, itens.size(), itens.size(), 1);
        }
    }
}
