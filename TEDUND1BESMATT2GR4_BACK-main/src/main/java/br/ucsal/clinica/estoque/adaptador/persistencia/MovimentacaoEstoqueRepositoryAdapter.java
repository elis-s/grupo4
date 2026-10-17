package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioEntity;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioJpaRepository;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import br.ucsal.clinica.estoque.porta.MovimentacaoEstoqueRepositoryPort;

/** Adaptador JPA da porta das movimentacoes de estoque. */
@Component
public class MovimentacaoEstoqueRepositoryAdapter implements MovimentacaoEstoqueRepositoryPort {

    private static final LocalDateTime INICIO_PADRAO = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FIM_PADRAO = LocalDateTime.of(3000, 1, 1, 0, 0);

    private final MovimentacaoEstoqueJpaRepository jpa;
    private final MedicamentoJpaRepository medicamentos;
    private final SolicitacaoMedicamentoJpaRepository solicitacoes;
    private final ColaboradorJpaRepository colaboradores;
    private final UsuarioJpaRepository usuarios;

    public MovimentacaoEstoqueRepositoryAdapter(MovimentacaoEstoqueJpaRepository jpa,
            MedicamentoJpaRepository medicamentos, SolicitacaoMedicamentoJpaRepository solicitacoes,
            ColaboradorJpaRepository colaboradores, UsuarioJpaRepository usuarios) {
        this.jpa = jpa;
        this.medicamentos = medicamentos;
        this.solicitacoes = solicitacoes;
        this.colaboradores = colaboradores;
        this.usuarios = usuarios;
    }

    @Override
    public MovimentacaoEstoque registrar(MovimentacaoEstoque m) {
        MovimentacaoEstoqueEntity e = new MovimentacaoEstoqueEntity();
        e.setMedicamento(medicamentos.getReferenceById(m.getMedicamento().id()));
        e.setTipo(m.getTipo());
        e.setQuantidade(m.getQuantidade());
        e.setDataHora(m.getDataHora());
        e.setLote(m.getLote());
        e.setDataValidade(m.getDataValidade());
        e.setDataAquisicao(m.getDataAquisicao());
        e.setSolicitacao(m.getSolicitacaoId() == null ? null : solicitacoes.getReferenceById(m.getSolicitacaoId()));
        e.setSolicitante(m.getSolicitante() == null ? null : colaboradores.getReferenceById(m.getSolicitante().id()));
        e.setEntreguePor(m.getResponsavelEntrega() == null ? null
                : usuarios.getReferenceById(m.getResponsavelEntrega().id()));
        e.setObservacao(m.getObservacao());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<MovimentacaoEstoque> listar(Long medicamentoId, TipoMovimentacao tipo, LocalDateTime inicio,
            LocalDateTime fim, Paginacao paginacao) {
        Set<TipoMovimentacao> tipos = tipo == null ? EnumSet.allOf(TipoMovimentacao.class) : EnumSet.of(tipo);
        return Paginas.converter(jpa.filtrar(medicamentoId == null ? 0L : medicamentoId, tipos,
                inicio == null ? INICIO_PADRAO : inicio, fim == null ? FIM_PADRAO : fim,
                Paginas.pedido(paginacao, Sort.by(Sort.Direction.DESC, "dataHora"))),
                MovimentacaoEstoqueRepositoryAdapter::paraDominio);
    }

    public static MovimentacaoEstoque paraDominio(MovimentacaoEstoqueEntity e) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setId(e.getId());
        m.setMedicamento(new Referencia(e.getMedicamento().getId(), e.getMedicamento().getNome()));
        m.setTipo(e.getTipo());
        m.setQuantidade(e.getQuantidade());
        m.setDataHora(e.getDataHora());
        m.setLote(e.getLote());
        m.setDataValidade(e.getDataValidade());
        m.setDataAquisicao(e.getDataAquisicao());
        m.setSolicitacaoId(e.getSolicitacao() != null ? e.getSolicitacao().getId() : null);
        ColaboradorEntity solicitante = e.getSolicitante();
        m.setSolicitante(solicitante == null ? null : new Referencia(solicitante.getId(), solicitante.getNomeCompleto()));
        UsuarioEntity entregou = e.getEntreguePor();
        m.setResponsavelEntrega(entregou == null ? null : new Referencia(entregou.getId(), entregou.getLogin()));
        m.setObservacao(e.getObservacao());
        m.setRegistradoPor(e.getCriadoPor());
        return m;
    }
}
