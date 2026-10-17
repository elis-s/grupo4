package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso GerenciarUsuarios - reativacao: so com o colaborador vinculado ativo (RN037). */
public class ReativarUsuario {

    private final UsuarioRepositoryPort usuarios;

    public ReativarUsuario(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario executar(Long id) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
        if (!usuario.isColaboradorAtivo()) {
            throw new RegraNegocioException("O colaborador vinculado a este usuário está inativo (RN037).");
        }
        usuario.reativar();
        return usuarios.salvar(usuario);
    }
}
