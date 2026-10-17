package br.ucsal.clinica.autenticacao.adaptador.web;

import java.time.Instant;
import java.util.List;

import br.ucsal.clinica.autenticacao.aplicacao.AutenticarUsuario;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDTO {

    private AuthDTO() {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o usuário.") String login,
            @NotBlank(message = "Informe a senha.") String senha) {
    }

    /** Token JWT (enviado depois no cabecalho Authorization: Bearer) e os dados do usuario. */
    public record TokenResponse(String token, String tipo, Instant expiraEm, UsuarioLogadoResponse usuario) {

        public static TokenResponse de(AutenticarUsuario.Resultado r) {
            return new TokenResponse(r.token().token(), "Bearer", r.token().expiraEm(),
                    UsuarioLogadoResponse.de(r.usuario()));
        }
    }

    /** Quem esta logado e seus perfis: o Front-End monta os menus a partir daqui (RF062). */
    public record UsuarioLogadoResponse(Long id, String login, String nome, List<String> perfis,
            Long colaboradorId, String funcao) {

        public static UsuarioLogadoResponse de(UsuarioAutenticado u) {
            return new UsuarioLogadoResponse(u.id(), u.login(), u.nome(),
                    u.perfis().stream().map(Enum::name).sorted().toList(), u.colaboradorId(), u.funcao());
        }
    }

    public record TrocaSenhaRequest(
            @NotBlank(message = "Informe a senha atual.") String senhaAtual,
            @NotBlank @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres.") String novaSenha) {
    }
}
