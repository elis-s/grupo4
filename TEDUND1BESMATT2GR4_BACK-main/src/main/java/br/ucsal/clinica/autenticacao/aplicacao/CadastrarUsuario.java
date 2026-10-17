package br.ucsal.clinica.autenticacao.aplicacao;

import java.util.Set;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.CodificadorSenhaPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso GerenciarUsuarios - cadastro (RF063, RF013): usuario vinculado a um colaborador (RN037). */
public class CadastrarUsuario {

    public record Comando(String login, String senha, Long colaboradorId, Set<NomePerfil> perfis) {
    }

    private final UsuarioRepositoryPort usuarios;
    private final ColaboradorRepositoryPort colaboradores;
    private final CodificadorSenhaPort codificador;

    public CadastrarUsuario(UsuarioRepositoryPort usuarios, ColaboradorRepositoryPort colaboradores,
            CodificadorSenhaPort codificador) {
        this.usuarios = usuarios;
        this.colaboradores = colaboradores;
        this.codificador = codificador;
    }

    public Usuario executar(Comando comando) {
        if (usuarios.existeLogin(comando.login())) {
            throw new RegraNegocioException("Este login já está em uso.");
        }
        Usuario usuario = new Usuario();
        usuario.setLogin(comando.login().trim());
        usuario.setSenhaHash(codificador.codificar(comando.senha()));
        vincular(usuario, comando.colaboradorId(), null, usuarios, colaboradores);
        usuario.definirPerfis(comando.perfis());
        return usuarios.salvar(usuario);
    }

    /** RN037: o colaborador precisa existir, estar ativo e nao ter outro usuario. */
    static void vincular(Usuario usuario, Long colaboradorId, Long ignorarUsuarioId, UsuarioRepositoryPort usuarios,
            ColaboradorRepositoryPort colaboradores) {
        if (colaboradorId == null) {
            throw new RegraNegocioException("Todo usuário deve estar vinculado a um colaborador (RN037).");
        }
        Colaborador colaborador = colaboradores.buscarPorId(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", colaboradorId));
        if (usuarios.colaboradorPossuiUsuario(colaboradorId, ignorarUsuarioId)) {
            throw new RegraNegocioException("Este colaborador já possui um usuário.");
        }
        usuario.vincularColaborador(new Referencia(colaborador.getId(), colaborador.getNomeCompleto()),
                colaborador.getFuncao().name(), colaborador.isAtivo());
    }
}
