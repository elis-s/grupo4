package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.porta.TokenRevogadoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;

/**
 * Caso de uso ValidarTokenAcesso (secao 3.2.1): alem da assinatura e da validade (feitas
 * pelo Spring Security), o token precisa nao ter sido revogado no logout (RF045) e o usuario
 * precisa continuar ativo e vinculado a colaborador ativo (RN037).
 */
public class ValidarTokenAcesso {

    /** Resultado da validacao: valido ou o motivo da recusa. */
    public record Resultado(boolean valido, String motivo) {

        static Resultado ok() {
            return new Resultado(true, null);
        }

        static Resultado recusado(String motivo) {
            return new Resultado(false, motivo);
        }
    }

    private final TokenRevogadoRepositoryPort tokensRevogados;
    private final UsuarioRepositoryPort usuarios;

    public ValidarTokenAcesso(TokenRevogadoRepositoryPort tokensRevogados, UsuarioRepositoryPort usuarios) {
        this.tokensRevogados = tokensRevogados;
        this.usuarios = usuarios;
    }

    public Resultado executar(String jti, String login) {
        if (tokensRevogados.estaRevogado(jti)) {
            return Resultado.recusado("Sessão encerrada: faça login novamente.");
        }
        if (!usuarios.podeAcessar(login)) {
            return Resultado.recusado("Usuário inativo ou vinculado a colaborador inativo.");
        }
        return Resultado.ok();
    }
}
