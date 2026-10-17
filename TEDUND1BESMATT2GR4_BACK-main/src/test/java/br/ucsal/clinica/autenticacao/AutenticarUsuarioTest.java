package br.ucsal.clinica.autenticacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ucsal.clinica.autenticacao.aplicacao.AutenticarUsuario;
import br.ucsal.clinica.autenticacao.dominio.CredenciaisInvalidasException;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.autenticacao.dominio.TokenEmitido;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.testes.Fakes;

/** RF044, RN037 e auditoria do login (secao 6.1). */
class AutenticarUsuarioTest {

    static class Usuarios extends Fakes.Memoria<Usuario> implements UsuarioRepositoryPort {
        @Override
        public Optional<Usuario> buscarPorLogin(String login) {
            return registros.values().stream().filter(u -> u.getLogin().equals(login)).findFirst();
        }

        @Override
        public boolean existeLogin(String login) {
            return buscarPorLogin(login).isPresent();
        }

        @Override
        public boolean colaboradorPossuiUsuario(Long colaboradorId, Long ignorarUsuarioId) {
            return false;
        }

        @Override
        public void registrarLogin(Long usuarioId) {
        }

        @Override
        public boolean podeAcessar(String login) {
            return buscarPorLogin(login).map(Usuario::podeAcessar).orElse(false);
        }

        @Override
        public Pagina<Usuario> listar(Status status, Paginacao paginacao) {
            return super.listar(status, paginacao);
        }
    }

    private Usuarios usuarios;
    private Fakes.Rastro rastro;
    private AutenticarUsuario autenticar;

    @BeforeEach
    void preparar() {
        usuarios = new Usuarios();
        rastro = new Fakes.Rastro();
        Usuario ana = new Usuario();
        ana.setLogin("ana.souza");
        ana.setSenhaHash("$2a$hash");
        ana.vincularColaborador(new Referencia(3L, "Ana Beatriz Souza"), "MEDICO", true);
        ana.definirPerfis(List.of(NomePerfil.COLABORADOR));
        usuarios.salvar(ana);
        autenticar = new AutenticarUsuario(
                (login, senha) -> {
                    if (!"senha123".equals(senha)) {
                        throw new CredenciaisInvalidasException("Login ou senha inválidos.");
                    }
                },
                usuarios, usuario -> new TokenEmitido("token-" + usuario.getLogin(), "jti", Instant.now()), rastro);
    }

    @Test
    void loginValidoEmiteTokenERegistraNaAuditoria() {
        AutenticarUsuario.Resultado r = autenticar.executar("ana.souza", "senha123");
        assertEquals("token-ana.souza", r.token().token());
        assertEquals(Set.of(NomePerfil.COLABORADOR), r.usuario().perfis());
        assertEquals(AcaoAuditoria.LOGIN, rastro.eventos.get(0).acao());
    }

    @Test
    void senhaErradaRegistraTentativaSemSucesso() {
        assertThrows(CredenciaisInvalidasException.class, () -> autenticar.executar("ana.souza", "errada"));
        assertEquals(AcaoAuditoria.LOGIN_FALHA, rastro.eventos.get(0).acao());
        assertEquals("ana.souza", rastro.eventos.get(0).login());
    }

    @Test
    void colaboradorInativoNaoAcessa() {
        usuarios.buscarPorLogin("ana.souza").orElseThrow().setColaboradorAtivo(false); // RN037
        assertThrows(CredenciaisInvalidasException.class, () -> autenticar.executar("ana.souza", "senha123"));
        assertEquals(AcaoAuditoria.LOGIN_FALHA, rastro.eventos.get(0).acao());
    }

    @Test
    void usuarioPrecisaDeColaborador() {
        Usuario semColaborador = new Usuario();
        assertThrows(RuntimeException.class, () -> semColaborador.vincularColaborador(null, null, true));
    }
}
