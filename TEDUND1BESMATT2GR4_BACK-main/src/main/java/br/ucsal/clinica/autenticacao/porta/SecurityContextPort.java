package br.ucsal.clinica.autenticacao.porta;

import br.ucsal.clinica.autenticacao.dominio.SessaoToken;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;

/**
 * Secao 3.2.1: servico provido pelo Modulo de Autenticacao para que todos os modulos
 * consultem o ator autenticado e suas permissoes sem acoplar-se ao Spring Security.
 */
public interface SecurityContextPort {

    UsuarioAutenticado usuarioAtual();

    SessaoToken sessaoAtual();
}
