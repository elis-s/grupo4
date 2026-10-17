package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Adaptador JPA da porta de persistencia dos convenios. */
@Component
public class ConvenioRepositoryAdapter implements ConvenioRepositoryPort {

    private static final Sort ORDEM = Sort.by("nomeFantasia");

    private final ConvenioJpaRepository jpa;

    public ConvenioRepositoryAdapter(ConvenioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Convenio> buscarPorId(Long id) {
        return jpa.findById(id).map(ConvenioRepositoryAdapter::paraDominio);
    }

    @Override
    public Convenio salvar(Convenio convenio) {
        ConvenioEntity entidade;
        if (convenio.isNovo()) {
            entidade = new ConvenioEntity();
            entidade.receberStatusDe(convenio);
        } else {
            entidade = jpa.findById(convenio.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", convenio.getId()));
            entidade.prepararAlteracao(convenio);
        }
        entidade.setRazaoSocial(convenio.getRazaoSocial());
        entidade.setNomeFantasia(convenio.getNomeFantasia());
        entidade.setRegistroAns(convenio.getRegistroAns());
        entidade.setCnpj(convenio.getCnpj());
        entidade.setInscricaoMunicipal(convenio.getInscricaoMunicipal());
        entidade.setCodigoPrestador(convenio.getCodigoPrestador());
        entidade.setEmail(convenio.getEmail());
        entidade.setTelefone(convenio.getTelefone());
        return paraDominio(jpa.saveAndFlush(entidade));
    }

    @Override
    public Pagina<Convenio> listar(Status status, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(status == null ? jpa.findAll(pedido) : jpa.findByStatus(status, pedido),
                ConvenioRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeCnpj(String cnpj, Long ignorarId) {
        return ignorarId == null ? jpa.existsByCnpj(cnpj) : jpa.existsByCnpjAndIdNot(cnpj, ignorarId);
    }

    @Override
    public boolean existeRegistroAns(String registroAns, Long ignorarId) {
        return ignorarId == null
                ? jpa.existsByRegistroAns(registroAns)
                : jpa.existsByRegistroAnsAndIdNot(registroAns, ignorarId);
    }

    static Convenio paraDominio(ConvenioEntity e) {
        Convenio c = new Convenio();
        e.copiarControlePara(c);
        c.setRazaoSocial(e.getRazaoSocial());
        c.setNomeFantasia(e.getNomeFantasia());
        c.setRegistroAns(e.getRegistroAns());
        c.setCnpj(e.getCnpj());
        c.setInscricaoMunicipal(e.getInscricaoMunicipal());
        c.setCodigoPrestador(e.getCodigoPrestador());
        c.setEmail(e.getEmail());
        c.setTelefone(e.getTelefone());
        return c;
    }
}
