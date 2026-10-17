package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.SaldoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Adaptador JPA da porta dos medicamentos e dos saldos de estoque. */
@Component
public class MedicamentoRepositoryAdapter implements MedicamentoRepositoryPort {

    private static final Sort ORDEM = Sort.by("nome");

    private final MedicamentoJpaRepository jpa;

    public MedicamentoRepositoryAdapter(MedicamentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Medicamento> buscarPorId(Long id) {
        return jpa.findById(id).map(MedicamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public Medicamento salvar(Medicamento medicamento) {
        MedicamentoEntity e;
        if (medicamento.isNovo()) {
            e = new MedicamentoEntity();
            e.receberStatusDe(medicamento);
        } else {
            e = jpa.findById(medicamento.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamento.getId()));
            e.prepararAlteracao(medicamento);
        }
        e.setNome(medicamento.getNome());
        e.setDescricao(medicamento.getDescricao());
        e.setFabricante(medicamento.getFabricante());
        e.setArmazenamento(medicamento.getArmazenamento());
        e.setUnidade(medicamento.getUnidade());
        e.setEstoqueMinimo(medicamento.getEstoqueMinimo());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<Medicamento> listar(Status status, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(status == null ? jpa.findAll(pedido) : jpa.findByStatus(status, pedido),
                MedicamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeNomeFabricante(String nome, String fabricante, Long ignorarId) {
        return ignorarId == null
                ? jpa.existsByNomeIgnoreCaseAndFabricanteIgnoreCase(nome, fabricante)
                : jpa.existsByNomeIgnoreCaseAndFabricanteIgnoreCaseAndIdNot(nome, fabricante, ignorarId);
    }

    @Override
    public long saldo(Long medicamentoId) {
        Long saldo = jpa.saldo(medicamentoId);
        return saldo == null ? 0 : saldo;
    }

    @Override
    public List<SaldoEstoque> saldos() {
        return jpa.saldos().stream().map(MedicamentoRepositoryAdapter::paraSaldo).toList();
    }

    @Override
    public List<SaldoEstoque> abaixoDoMinimo() {
        return jpa.abaixoDoMinimo().stream().map(MedicamentoRepositoryAdapter::paraSaldo).toList();
    }

    @Override
    public void bloquearParaMovimentar(Long medicamentoId) {
        jpa.buscarParaMovimentar(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));
    }

    private static SaldoEstoque paraSaldo(MedicamentoJpaRepository.LinhaSaldo l) {
        return new SaldoEstoque(l.getMedicamentoId(), l.getNome(), l.getFabricante(),
                l.getEstoqueMinimo() == null ? 0 : l.getEstoqueMinimo(), l.getSaldo() == null ? 0 : l.getSaldo(),
                Status.valueOf(l.getStatus()));
    }

    public static Medicamento paraDominio(MedicamentoEntity e) {
        Medicamento m = new Medicamento();
        e.copiarControlePara(m);
        m.setNome(e.getNome());
        m.setDescricao(e.getDescricao());
        m.setFabricante(e.getFabricante());
        m.setArmazenamento(e.getArmazenamento());
        m.setUnidade(e.getUnidade());
        m.setEstoqueMinimo(e.getEstoqueMinimo());
        return m;
    }
}
