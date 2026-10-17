package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioEntity;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioJpaRepository;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;

/** Adaptador JPA da porta das solicitacoes de medicamento. */
@Component
public class SolicitacaoMedicamentoRepositoryAdapter implements SolicitacaoMedicamentoRepositoryPort {

    private static final Sort ORDEM = Sort.by(Sort.Direction.DESC, "dataCadastro");

    private final SolicitacaoMedicamentoJpaRepository jpa;
    private final MedicamentoJpaRepository medicamentos;
    private final ColaboradorJpaRepository colaboradores;
    private final UsuarioJpaRepository usuarios;

    public SolicitacaoMedicamentoRepositoryAdapter(SolicitacaoMedicamentoJpaRepository jpa,
            MedicamentoJpaRepository medicamentos, ColaboradorJpaRepository colaboradores,
            UsuarioJpaRepository usuarios) {
        this.jpa = jpa;
        this.medicamentos = medicamentos;
        this.colaboradores = colaboradores;
        this.usuarios = usuarios;
    }

    @Override
    public Optional<SolicitacaoMedicamento> buscarPorId(Long id) {
        return jpa.findById(id).map(SolicitacaoMedicamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public SolicitacaoMedicamento salvar(SolicitacaoMedicamento s) {
        SolicitacaoMedicamentoEntity e;
        if (s.isNovo()) {
            e = new SolicitacaoMedicamentoEntity();
            e.receberStatusDe(s);
        } else {
            e = jpa.findById(s.getId()).orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", s.getId()));
            e.prepararAlteracao(s);
        }
        e.setMedicamento(medicamentos.getReferenceById(s.getMedicamento().id()));
        e.setSolicitante(colaboradores.getReferenceById(s.getSolicitante().id()));
        e.setAtendimentoId(s.getAtendimentoId());
        e.setQuantidade(s.getQuantidade());
        e.setJustificativa(s.getJustificativa());
        e.setSituacao(s.getSituacao());
        e.setAnalisadoPor(s.getAnalisadoPor() == null ? null : usuarios.getReferenceById(s.getAnalisadoPor().id()));
        e.setDataAnalise(s.getDataAnalise());
        e.setMotivoReprovacao(s.getMotivoReprovacao());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<SolicitacaoMedicamento> listar(SituacaoSolicitacao situacao, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(situacao == null ? jpa.findAll(pedido) : jpa.findBySituacao(situacao, pedido),
                SolicitacaoMedicamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public Pagina<SolicitacaoMedicamento> listarDoSolicitante(Long colaboradorId, Paginacao paginacao) {
        return Paginas.converter(jpa.findBySolicitanteId(colaboradorId, Paginas.pedido(paginacao, ORDEM)),
                SolicitacaoMedicamentoRepositoryAdapter::paraDominio);
    }

    public static SolicitacaoMedicamento paraDominio(SolicitacaoMedicamentoEntity e) {
        SolicitacaoMedicamento s = new SolicitacaoMedicamento();
        e.copiarControlePara(s);
        s.setMedicamento(new Referencia(e.getMedicamento().getId(), e.getMedicamento().getNome()));
        s.setSolicitante(new Referencia(e.getSolicitante().getId(), e.getSolicitante().getNomeCompleto()));
        s.setAtendimentoId(e.getAtendimentoId());
        s.setQuantidade(e.getQuantidade());
        s.setJustificativa(e.getJustificativa());
        s.setSituacao(e.getSituacao());
        UsuarioEntity analisou = e.getAnalisadoPor();
        s.setAnalisadoPor(analisou == null ? null : new Referencia(analisou.getId(), analisou.getLogin()));
        s.setDataAnalise(e.getDataAnalise());
        s.setMotivoReprovacao(e.getMotivoReprovacao());
        return s;
    }
}
