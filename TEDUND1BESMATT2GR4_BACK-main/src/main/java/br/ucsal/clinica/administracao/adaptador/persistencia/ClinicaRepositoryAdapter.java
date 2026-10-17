package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.dominio.ResponsavelClinica;
import br.ucsal.clinica.administracao.dominio.TelefoneClinica;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Adaptador JPA que implementa a porta de persistencia da clinica, com o mapeamento entidade <-> dominio. */
@Component
public class ClinicaRepositoryAdapter implements ClinicaRepositoryPort {

    private static final Sort ORDEM = Sort.by("nomeFantasia");

    private final ClinicaJpaRepository jpa;

    public ClinicaRepositoryAdapter(ClinicaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Clinica> buscarPorId(Long id) {
        return jpa.findById(id).map(ClinicaRepositoryAdapter::paraDominio);
    }

    @Override
    public Clinica salvar(Clinica clinica) {
        ClinicaEntity entidade;
        if (clinica.isNovo()) {
            entidade = new ClinicaEntity();
            entidade.receberStatusDe(clinica);
        } else {
            entidade = jpa.findById(clinica.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", clinica.getId()));
            entidade.prepararAlteracao(clinica);
        }
        aplicar(clinica, entidade);
        return paraDominio(jpa.saveAndFlush(entidade));
    }

    @Override
    public Pagina<Clinica> listar(Status status, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(status == null ? jpa.findAll(pedido) : jpa.findByStatus(status, pedido),
                ClinicaRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeCnpj(String cnpj, Long ignorarId) {
        return ignorarId == null ? jpa.existsByCnpj(cnpj) : jpa.existsByCnpjAndIdNot(cnpj, ignorarId);
    }

    static Clinica paraDominio(ClinicaEntity e) {
        Clinica c = new Clinica();
        e.copiarControlePara(c);
        c.setRazaoSocial(e.getRazaoSocial());
        c.setNomeFantasia(e.getNomeFantasia());
        c.setCnpj(e.getCnpj());
        c.setLogradouro(e.getLogradouro());
        c.setNumero(e.getNumero());
        c.setComplemento(e.getComplemento());
        c.setBairro(e.getBairro());
        c.setCidade(e.getCidade());
        c.setEstado(e.getEstado());
        c.setPais(e.getPais());
        c.setCep(e.getCep());
        c.setEmail(e.getEmail());
        c.setAtividadePrincipal(e.getAtividadePrincipal());
        c.setMissao(e.getMissao());
        c.setAlvaraNumero(e.getAlvaraNumero());
        c.setAlvaraDataEmissao(e.getAlvaraDataEmissao());
        c.setAlvaraDataValidade(e.getAlvaraDataValidade());
        c.setLicencaSanitariaNumero(e.getLicencaSanitariaNumero());
        c.setLicencaSanitariaEmissao(e.getLicencaSanitariaEmissao());
        c.setLicencaSanitariaValidade(e.getLicencaSanitariaValidade());
        c.setRegistroCrm(e.getRegistroCrm());
        c.setDiretorTecnicoNome(e.getDiretorTecnicoNome());
        c.setDiretorTecnicoCrm(e.getDiretorTecnicoCrm());
        c.setTelefones(new ArrayList<>(e.getTelefones().stream()
                .map(t -> new TelefoneClinica(t.getTelefone(), t.getDescricao())).toList()));
        c.setResponsaveis(new ArrayList<>(e.getProprietarios().stream()
                .map(p -> new ResponsavelClinica(p.getNome(), p.getCpf())).toList()));
        return c;
    }

    private static void aplicar(Clinica c, ClinicaEntity e) {
        e.setRazaoSocial(c.getRazaoSocial());
        e.setNomeFantasia(c.getNomeFantasia());
        e.setCnpj(c.getCnpj());
        e.setLogradouro(c.getLogradouro());
        e.setNumero(c.getNumero());
        e.setComplemento(c.getComplemento());
        e.setBairro(c.getBairro());
        e.setCidade(c.getCidade());
        e.setEstado(c.getEstado());
        e.setPais(c.getPais());
        e.setCep(c.getCep());
        e.setEmail(c.getEmail());
        e.setAtividadePrincipal(c.getAtividadePrincipal());
        e.setMissao(c.getMissao());
        e.setAlvaraNumero(c.getAlvaraNumero());
        e.setAlvaraDataEmissao(c.getAlvaraDataEmissao());
        e.setAlvaraDataValidade(c.getAlvaraDataValidade());
        e.setLicencaSanitariaNumero(c.getLicencaSanitariaNumero());
        e.setLicencaSanitariaEmissao(c.getLicencaSanitariaEmissao());
        e.setLicencaSanitariaValidade(c.getLicencaSanitariaValidade());
        e.setRegistroCrm(c.getRegistroCrm());
        e.setDiretorTecnicoNome(c.getDiretorTecnicoNome());
        e.setDiretorTecnicoCrm(c.getDiretorTecnicoCrm());

        // Listas: mantem as linhas que continuam iguais (evita apagar e inserir o mesmo telefone/nome)
        e.getTelefones().removeIf(atual -> c.getTelefones().stream()
                .noneMatch(n -> n.telefone().equals(atual.getTelefone())));
        for (TelefoneClinica t : c.getTelefones()) {
            ClinicaTelefoneEntity linha = e.getTelefones().stream()
                    .filter(x -> x.getTelefone().equals(t.telefone())).findFirst().orElse(null);
            if (linha == null) {
                linha = new ClinicaTelefoneEntity();
                linha.setClinica(e);
                linha.setTelefone(t.telefone());
                e.getTelefones().add(linha);
            }
            linha.setDescricao(t.descricao());
        }
        e.getProprietarios().removeIf(atual -> c.getResponsaveis().stream()
                .noneMatch(n -> n.nome().equalsIgnoreCase(atual.getNome())));
        for (ResponsavelClinica r : c.getResponsaveis()) {
            ClinicaProprietarioEntity linha = e.getProprietarios().stream()
                    .filter(x -> x.getNome().equalsIgnoreCase(r.nome())).findFirst().orElse(null);
            if (linha == null) {
                linha = new ClinicaProprietarioEntity();
                linha.setClinica(e);
                linha.setNome(r.nome());
                e.getProprietarios().add(linha);
            }
            linha.setCpf(r.cpf());
        }
    }
}
