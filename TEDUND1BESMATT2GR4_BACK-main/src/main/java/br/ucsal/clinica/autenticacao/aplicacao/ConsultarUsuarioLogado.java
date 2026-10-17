package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;

/** Dados de quem esta logado: o Front-End usa para exibir so as funcionalidades dos seus perfis (RF062). */
public class ConsultarUsuarioLogado {

    private final SecurityContextPort contexto;

    public ConsultarUsuarioLogado(SecurityContextPort contexto) {
        this.contexto = contexto;
    }

    public UsuarioAutenticado executar() {
        return contexto.usuarioAtual();
    }
}
