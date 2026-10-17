package br.ucsal.clinica.autenticacao.porta;

import br.ucsal.clinica.autenticacao.dominio.TokenEmitido;
import br.ucsal.clinica.autenticacao.dominio.Usuario;

/** Emite o token JWT assinado (HMAC-SHA256) com o usuario, os perfis e a expiracao (secao 6.1). */
public interface EmissorTokenPort {

    TokenEmitido emitir(Usuario usuario);
}
