package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso GerenciarUsuarios - inativacao (RF063). */
public class InativarUsuario {

    private final UsuarioRepositoryPort usuarios;
    private final SecurityContextPort contexto;

    public InativarUsuario(UsuarioRepositoryPort usuarios, SecurityContextPort contexto) {
        this.usuarios = usuarios;
        this.contexto = contexto;
    }

    public Usuario executar(Long id, String motivo) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
        if (usuario.getId().equals(contexto.usuarioAtual().id())) {
            throw new RegraNegocioException("Você não pode inativar o seu próprio usuário.");
        }
        usuario.inativar(motivo);
        return usuarios.salvar(usuario);
    }
}
