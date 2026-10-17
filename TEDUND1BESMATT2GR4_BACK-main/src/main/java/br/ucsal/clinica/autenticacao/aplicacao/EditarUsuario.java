package br.ucsal.clinica.autenticacao.aplicacao;

import java.util.Set;

import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.CodificadorSenhaPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/** Caso de uso GerenciarPerfisAcesso / GerenciarUsuarios - edicao (RF013, RF063). */
public class EditarUsuario {

    /** A senha e opcional na edicao: so e trocada se vier preenchida. */
    public record Comando(String novaSenha, Long colaboradorId, Set<NomePerfil> perfis, Long versao) {
    }

    private final UsuarioRepositoryPort usuarios;
    private final ColaboradorRepositoryPort colaboradores;
    private final CodificadorSenhaPort codificador;

    public EditarUsuario(UsuarioRepositoryPort usuarios, ColaboradorRepositoryPort colaboradores,
            CodificadorSenhaPort codificador) {
        this.usuarios = usuarios;
        this.colaboradores = colaboradores;
        this.codificador = codificador;
    }

    public Usuario executar(Long id, Comando comando) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
        usuario.verificarVersao(comando.versao());
        if (!Textos.vazio(comando.novaSenha())) {
            usuario.setSenhaHash(codificador.codificar(comando.novaSenha()));
        }
        CadastrarUsuario.vincular(usuario, comando.colaboradorId(), id, usuarios, colaboradores);
        usuario.definirPerfis(comando.perfis());
        return usuarios.salvar(usuario);
    }
}
