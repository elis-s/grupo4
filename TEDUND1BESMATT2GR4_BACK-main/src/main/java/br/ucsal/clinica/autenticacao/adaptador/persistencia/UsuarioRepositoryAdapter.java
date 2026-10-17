package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Adaptador JPA da porta de persistencia dos usuarios. */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private static final Sort ORDEM = Sort.by("login");

    private final UsuarioJpaRepository jpa;
    private final PerfilJpaRepository perfis;
    private final ColaboradorJpaRepository colaboradores;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa, PerfilJpaRepository perfis,
            ColaboradorJpaRepository colaboradores) {
        this.jpa = jpa;
        this.perfis = perfis;
        this.colaboradores = colaboradores;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(UsuarioRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return jpa.buscarPorLogin(login).map(UsuarioRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeLogin(String login) {
        return jpa.existsByLogin(login);
    }

    @Override
    public boolean colaboradorPossuiUsuario(Long colaboradorId, Long ignorarUsuarioId) {
        return ignorarUsuarioId == null
                ? jpa.existsByColaboradorId(colaboradorId)
                : jpa.existsByColaboradorIdAndIdNot(colaboradorId, ignorarUsuarioId);
    }

    @Override
    public void registrarLogin(Long usuarioId) {
        jpa.registrarLogin(usuarioId, LocalDateTime.now());
    }

    @Override
    public boolean podeAcessar(String login) {
        return jpa.existsByLoginAndStatusAndColaboradorStatus(login, Status.ATIVO, Status.ATIVO);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity e;
        if (usuario.isNovo()) {
            e = new UsuarioEntity();
            e.receberStatusDe(usuario);
        } else {
            e = jpa.findById(usuario.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuario.getId()));
            e.prepararAlteracao(usuario);
        }
        e.setLogin(usuario.getLogin());
        e.setSenhaHash(usuario.getSenhaHash());
        e.setColaborador(colaboradores.getReferenceById(usuario.getColaborador().id()));
        var novos = perfis.findByNomeIn(usuario.getPerfis());
        if (novos.size() != usuario.getPerfis().size()) {
            throw new RegraNegocioException("Perfil de acesso inválido. Use ADMINISTRADOR, COLABORADOR ou USUARIO.");
        }
        e.getPerfis().clear();
        e.getPerfis().addAll(novos);
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<Usuario> listar(Status status, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(status == null ? jpa.findAll(pedido) : jpa.findByStatus(status, pedido),
                UsuarioRepositoryAdapter::paraDominio);
    }

    static Usuario paraDominio(UsuarioEntity e) {
        Usuario u = new Usuario();
        e.copiarControlePara(u);
        u.setLogin(e.getLogin());
        u.setSenhaHash(e.getSenhaHash());
        ColaboradorEntity c = e.getColaborador();
        u.setColaborador(new Referencia(c.getId(), c.getNomeCompleto()));
        u.setFuncaoColaborador(c.getFuncao() != null ? c.getFuncao().name() : null);
        u.setColaboradorAtivo(c.getStatus() == Status.ATIVO);
        u.setPerfis(e.getPerfis().stream().map(PerfilEntity::getNome).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        u.setUltimoLogin(e.getUltimoLogin());
        return u;
    }
}
