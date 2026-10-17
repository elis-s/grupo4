package br.ucsal.clinica.relatorios.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ClinicaJpaRepository;
import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioJpaRepository;
import br.ucsal.clinica.administracao.adaptador.persistencia.SetorJpaRepository;
import br.ucsal.clinica.atendimento.adaptador.persistencia.AgendamentoEntity;
import br.ucsal.clinica.atendimento.adaptador.persistencia.AgendamentoJpaRepository;
import br.ucsal.clinica.atendimento.adaptador.persistencia.AtendimentoEntity;
import br.ucsal.clinica.atendimento.adaptador.persistencia.AtendimentoJpaRepository;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.PerfilEntity;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioJpaRepository;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.adaptador.persistencia.MedicamentoEntity;
import br.ucsal.clinica.estoque.adaptador.persistencia.MedicamentoJpaRepository;
import br.ucsal.clinica.estoque.adaptador.persistencia.MovimentacaoEstoqueEntity;
import br.ucsal.clinica.estoque.adaptador.persistencia.MovimentacaoEstoqueJpaRepository;
import br.ucsal.clinica.estoque.adaptador.persistencia.SolicitacaoMedicamentoEntity;
import br.ucsal.clinica.estoque.adaptador.persistencia.SolicitacaoMedicamentoJpaRepository;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.pacientes.adaptador.persistencia.PacienteJpaRepository;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaAgendamento;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaAtendimento;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaClinica;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaColaborador;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaConvenio;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMedicamento;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMovimentacao;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaPaciente;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaSetor;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaSolicitacao;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaUsuario;
import br.ucsal.clinica.relatorios.porta.RelatorioConsultaPort;

/**
 * Visoes de leitura dos relatorios (Read-Only Projections - secao 3.2.6): consultas
 * somente leitura sobre as tabelas dos modulos, convertidas em linhas de relatorio.
 */
@Component
public class RelatorioConsultaAdapter implements RelatorioConsultaPort {

    private final ClinicaJpaRepository clinicas;
    private final SetorJpaRepository setores;
    private final ConvenioJpaRepository convenios;
    private final ColaboradorJpaRepository colaboradores;
    private final UsuarioJpaRepository usuarios;
    private final PacienteJpaRepository pacientes;
    private final AgendamentoJpaRepository agendamentos;
    private final AtendimentoJpaRepository atendimentos;
    private final MedicamentoJpaRepository medicamentos;
    private final SolicitacaoMedicamentoJpaRepository solicitacoes;
    private final MovimentacaoEstoqueJpaRepository movimentacoes;

    public RelatorioConsultaAdapter(ClinicaJpaRepository clinicas, SetorJpaRepository setores,
            ConvenioJpaRepository convenios, ColaboradorJpaRepository colaboradores, UsuarioJpaRepository usuarios,
            PacienteJpaRepository pacientes, AgendamentoJpaRepository agendamentos,
            AtendimentoJpaRepository atendimentos, MedicamentoJpaRepository medicamentos,
            SolicitacaoMedicamentoJpaRepository solicitacoes, MovimentacaoEstoqueJpaRepository movimentacoes) {
        this.clinicas = clinicas;
        this.setores = setores;
        this.convenios = convenios;
        this.colaboradores = colaboradores;
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.agendamentos = agendamentos;
        this.atendimentos = atendimentos;
        this.medicamentos = medicamentos;
        this.solicitacoes = solicitacoes;
        this.movimentacoes = movimentacoes;
    }

    @Override
    public List<LinhaClinica> clinicas() {
        return clinicas.findAll(Sort.by("nomeFantasia")).stream()
                .map(c -> new LinhaClinica(c.getId(), c.getNomeFantasia(), c.getCnpj(), c.getCidade(), c.getEstado(),
                        c.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaSetor> setores() {
        return setores.findAll(Sort.by("nome")).stream()
                .map(s -> new LinhaSetor(s.getId(), s.getNome(), s.getClinica().getNomeFantasia(),
                        s.getResponsavel() != null ? s.getResponsavel().getNomeCompleto() : null, s.getEmail(),
                        s.getTelefone(), s.getDataCadastro(), s.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaConvenio> convenios() {
        return convenios.findAll(Sort.by("nomeFantasia")).stream()
                .map(c -> new LinhaConvenio(c.getId(), c.getNomeFantasia(), c.getRegistroAns(), c.getCnpj(),
                        c.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaColaborador> colaboradores() {
        return colaboradores.findAll(Sort.by("nomeCompleto")).stream().map(this::linhaColaborador).toList();
    }

    private LinhaColaborador linhaColaborador(ColaboradorEntity c) {
        String conselho = c.getConselhoNumero() == null ? null
                : c.getConselhoSigla() + "-" + c.getConselhoUf() + " " + c.getConselhoNumero();
        return new LinhaColaborador(c.getId(), c.getNomeCompleto(), c.getFuncao().name(), c.getSetor().getNome(),
                c.getEspecialidade() != null ? c.getEspecialidade().getNome() : null, conselho, c.getStatus().name());
    }

    @Override
    public List<LinhaUsuario> usuarios() {
        return usuarios.findAll(Sort.by("login")).stream()
                .map(u -> new LinhaUsuario(u.getId(), u.getLogin(), u.getColaborador().getNomeCompleto(),
                        u.getPerfis().stream().map(PerfilEntity::getNome).map(Enum::name).sorted().toList(),
                        u.getUltimoLogin(), u.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaPaciente> pacientes() {
        return pacientes.findAll(Sort.by("nomeCompleto")).stream()
                .map(p -> new LinhaPaciente(p.getId(), p.getNomeCompleto(), p.getDataNascimento(),
                        p.getMedico() != null ? p.getMedico().getNomeCompleto() : null,
                        p.getConvenio() != null ? p.getConvenio().getNomeFantasia() : "Particular",
                        p.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaAgendamento> agendamentos(Long medicoId) {
        List<AgendamentoEntity> lista = medicoId == null
                ? agendamentos.findAll(Sort.by(Sort.Direction.DESC, "dataPrevista", "horaPrevista"))
                : agendamentos.findByMedicoIdOrderByDataPrevistaDescHoraPrevistaDesc(medicoId);
        return lista.stream()
                .map(a -> new LinhaAgendamento(a.getId(), a.getDataPrevista(), a.getHoraPrevista(),
                        a.getPaciente().getNomeCompleto(), a.getMedico().getNomeCompleto(), a.getSituacao().name(),
                        a.getDataRecepcao(), a.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaAtendimento> atendimentos(Long colaboradorId) {
        List<AtendimentoEntity> lista = colaboradorId == null
                ? atendimentos.findAll(Sort.by(Sort.Direction.DESC, "dataHoraInicio"))
                : atendimentos.findByMedicoIdOrderByDataHoraInicioDesc(colaboradorId);
        return lista.stream()
                .map(a -> new LinhaAtendimento(a.getId(), a.getDataHoraInicio(), a.getDataHoraEncerramento(),
                        a.getProntuario().getPaciente().getNomeCompleto(), a.getMedico().getNomeCompleto(),
                        a.getTipo().name(), a.getEtapa().name(), a.isInternado(), a.getStatus().name()))
                .toList();
    }

    @Override
    public List<LinhaMedicamento> medicamentos() {
        return medicamentos.findAll(Sort.by("nome")).stream().map(this::linhaMedicamento).toList();
    }

    private LinhaMedicamento linhaMedicamento(MedicamentoEntity m) {
        Long saldo = medicamentos.saldo(m.getId());
        long valor = saldo == null ? 0 : saldo;
        return new LinhaMedicamento(m.getId(), m.getNome(), m.getFabricante(), m.getEstoqueMinimo(), valor,
                m.getStatus() == Status.ATIVO && valor <= m.getEstoqueMinimo(), m.getStatus().name());
    }

    @Override
    public List<LinhaSolicitacao> solicitacoes(Long solicitanteId, Set<SituacaoSolicitacao> situacoes) {
        List<SolicitacaoMedicamentoEntity> lista;
        if (solicitanteId != null) {
            lista = solicitacoes.findBySolicitanteIdOrderByDataCadastroDesc(solicitanteId);
        } else if (situacoes != null) {
            lista = solicitacoes.findBySituacaoInOrderByDataAnaliseDesc(situacoes);
        } else {
            lista = solicitacoes.findAllByOrderByDataCadastroDesc();
        }
        return lista.stream()
                .filter(s -> situacoes == null || situacoes.contains(s.getSituacao()))
                .map(s -> new LinhaSolicitacao(s.getId(), s.getDataCadastro(), s.getMedicamento().getNome(),
                        s.getQuantidade(), s.getSolicitante().getNomeCompleto(), s.getSituacao().name(),
                        s.getAnalisadoPor() != null ? s.getAnalisadoPor().getLogin() : null, s.getDataAnalise(),
                        s.getMotivoReprovacao()))
                .toList();
    }

    @Override
    public List<LinhaMovimentacao> movimentacoes(LocalDateTime inicio, LocalDateTime fim, Long responsavelEntregaId) {
        List<MovimentacaoEstoqueEntity> lista;
        if (responsavelEntregaId != null) {
            lista = movimentacoes.findByEntreguePorIdOrderByDataHoraDesc(responsavelEntregaId);
        } else if (inicio != null && fim != null) {
            lista = movimentacoes.findByDataHoraBetweenOrderByDataHoraDesc(inicio, fim.minusNanos(1000));
        } else {
            lista = movimentacoes.findAll(Sort.by(Sort.Direction.DESC, "dataHora"));
        }
        return lista.stream()
                .filter(m -> inicio == null || !m.getDataHora().isBefore(inicio))
                .filter(m -> fim == null || m.getDataHora().isBefore(fim))
                .map(m -> new LinhaMovimentacao(m.getId(), m.getDataHora(), m.getTipo().name(),
                        m.getMedicamento().getNome(), m.getQuantidade(),
                        m.getSolicitante() != null ? m.getSolicitante().getNomeCompleto() : null,
                        m.getEntreguePor() != null ? m.getEntreguePor().getLogin() : null, m.getLote()))
                .toList();
    }
}
