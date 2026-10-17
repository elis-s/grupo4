package br.ucsal.clinica.autenticacao.adaptador.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.autenticacao.adaptador.web.AuthDTO.LoginRequest;
import br.ucsal.clinica.autenticacao.adaptador.web.AuthDTO.TokenResponse;
import br.ucsal.clinica.autenticacao.adaptador.web.AuthDTO.TrocaSenhaRequest;
import br.ucsal.clinica.autenticacao.adaptador.web.AuthDTO.UsuarioLogadoResponse;
import br.ucsal.clinica.autenticacao.aplicacao.AlterarPropriaSenha;
import br.ucsal.clinica.autenticacao.aplicacao.AutenticarUsuario;
import br.ucsal.clinica.autenticacao.aplicacao.ConsultarUsuarioLogado;
import br.ucsal.clinica.autenticacao.aplicacao.EncerrarSessao;
import br.ucsal.clinica.autenticacao.aplicacao.RenovarToken;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/** /api/auth (secao 4.2): login, logout e renovacao do token. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUsuario autenticar;
    private final RenovarToken renovar;
    private final EncerrarSessao encerrar;
    private final ConsultarUsuarioLogado usuarioLogado;
    private final AlterarPropriaSenha alterarSenha;

    public AuthController(AutenticarUsuario autenticar, RenovarToken renovar, EncerrarSessao encerrar,
            ConsultarUsuarioLogado usuarioLogado, AlterarPropriaSenha alterarSenha) {
        this.autenticar = autenticar;
        this.renovar = renovar;
        this.encerrar = encerrar;
        this.usuarioLogado = usuarioLogado;
        this.alterarSenha = alterarSenha;
    }

    /** RF044: unica requisicao que nao exige token (RNF014). */
    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        return TokenResponse.de(autenticar.executar(req.login(), req.senha()));
    }

    /** RenovarToken: troca o token atual por um novo, enquanto o usuario estiver ativo. */
    @PostMapping("/renovar")
    @PreAuthorize(Permissoes.AUTENTICADO)
    public TokenResponse renovar() {
        return TokenResponse.de(renovar.executar());
    }

    /** RF045: encerra a sessao revogando o token. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Permissoes.AUTENTICADO)
    public void logout() {
        encerrar.executar();
    }

    @GetMapping("/me")
    @PreAuthorize(Permissoes.AUTENTICADO)
    public UsuarioLogadoResponse me() {
        return UsuarioLogadoResponse.de(usuarioLogado.executar());
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Permissoes.AUTENTICADO)
    public void trocarSenha(@RequestBody @Valid TrocaSenhaRequest req) {
        alterarSenha.executar(req.senhaAtual(), req.novaSenha());
    }
}
