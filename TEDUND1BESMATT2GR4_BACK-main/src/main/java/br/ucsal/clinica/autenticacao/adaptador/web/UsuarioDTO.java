package br.ucsal.clinica.autenticacao.adaptador.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import br.ucsal.clinica.autenticacao.aplicacao.CadastrarUsuario;
import br.ucsal.clinica.autenticacao.aplicacao.EditarUsuario;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.autenticacao.dominio.Perfil;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class UsuarioDTO {

    private UsuarioDTO() {
    }

    /** RF063: usuario sempre vinculado a um colaborador (RN037), com um ou mais perfis (RN001). */
    public record CadastroRequest(
            @NotBlank @Size(min = 3, max = 60)
            @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "O login só pode ter letras, números, ponto, hífen e underline.")
            String login,
            @NotBlank @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.") String senha,
            @NotNull(message = "Informe o colaborador (RN037).") Long colaboradorId,
            @NotEmpty(message = "Informe ao menos um perfil.") Set<NomePerfil> perfis) {

        public CadastrarUsuario.Comando paraComando() {
            return new CadastrarUsuario.Comando(login, senha, colaboradorId, perfis);
        }
    }

    /** Na edicao a senha e opcional: so troca se vier preenchida. */
    public record EdicaoRequest(
            @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.") String novaSenha,
            @NotNull(message = "Informe o colaborador (RN037).") Long colaboradorId,
            @NotEmpty(message = "Informe ao menos um perfil.") Set<NomePerfil> perfis,
            Long versao) {

        public EditarUsuario.Comando paraComando() {
            return new EditarUsuario.Comando(novaSenha, colaboradorId, perfis, versao);
        }
    }

    public record Response(
            Long id,
            String login,
            Long colaboradorId,
            String colaboradorNome,
            boolean colaboradorAtivo,
            List<String> perfis,
            LocalDateTime ultimoLogin,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Usuario u) {
            return new Response(u.getId(), u.getLogin(), Referencia.idDe(u.getColaborador()),
                    Referencia.nomeDe(u.getColaborador()), u.isColaboradorAtivo(),
                    u.getPerfis().stream().map(Enum::name).sorted().toList(), u.getUltimoLogin(),
                    u.getVersao(), u.getDataCadastro(), u.getStatus(), u.getMotivoInativacao(), u.getDataInativacao());
        }
    }

    public record PerfilResponse(Long id, NomePerfil nome, String descricao) {

        public static PerfilResponse de(Perfil p) {
            return new PerfilResponse(p.id(), p.nome(), p.descricao());
        }
    }
}
