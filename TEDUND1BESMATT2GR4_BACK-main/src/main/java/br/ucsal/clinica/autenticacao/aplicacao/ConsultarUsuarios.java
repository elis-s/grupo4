package br.ucsal.clinica.autenticacao.aplicacao;

import java.util.List;

import br.ucsal.clinica.autenticacao.dominio.Perfil;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.PerfilRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Consulta dos usuarios e dos perfis de acesso (RF013, RF063). */
public class ConsultarUsuarios {

    private final UsuarioRepositoryPort usuarios;
    private final PerfilRepositoryPort perfis;

    public ConsultarUsuarios(UsuarioRepositoryPort usuarios, PerfilRepositoryPort perfis) {
        this.usuarios = usuarios;
        this.perfis = perfis;
    }

    public Usuario porId(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    public Pagina<Usuario> listar(Status status, Paginacao paginacao) {
        return usuarios.listar(status, paginacao);
    }

    public List<Perfil> perfis() {
        return perfis.listar();
    }
}
