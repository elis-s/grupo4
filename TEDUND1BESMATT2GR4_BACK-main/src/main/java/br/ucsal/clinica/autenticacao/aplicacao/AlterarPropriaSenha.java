package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.CodificadorSenhaPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** O proprio usuario troca a sua senha, informando a senha atual (gravada sempre em BCrypt - RNF013). */
public class AlterarPropriaSenha {

    private final SecurityContextPort contexto;
    private final UsuarioRepositoryPort usuarios;
    private final CodificadorSenhaPort codificador;

    public AlterarPropriaSenha(SecurityContextPort contexto, UsuarioRepositoryPort usuarios,
            CodificadorSenhaPort codificador) {
        this.contexto = contexto;
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    public void executar(String senhaAtual, String novaSenha) {
        String login = contexto.usuarioAtual().login();
        Usuario usuario = usuarios.buscarPorLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        if (!codificador.confere(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        usuario.setSenhaHash(codificador.codificar(novaSenha));
        usuarios.salvar(usuario);
    }
}
