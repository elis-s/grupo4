package br.ucsal.clinica.autenticacao.porta;

import java.util.Optional;

import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Porta de persistencia dos usuarios (tb_usuario e tb_usuario_perfil). */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorLogin(String login);

    boolean existeLogin(String login);

    boolean colaboradorPossuiUsuario(Long colaboradorId, Long ignorarUsuarioId);

    /** Atualiza a data do ultimo login, sem alterar a versao do cadastro. */
    void registrarLogin(Long usuarioId);

    /** RN037: o usuario esta ativo e o colaborador vinculado tambem. */
    boolean podeAcessar(String login);

    Usuario salvar(Usuario usuario);

    Pagina<Usuario> listar(Status status, Paginacao paginacao);
}
