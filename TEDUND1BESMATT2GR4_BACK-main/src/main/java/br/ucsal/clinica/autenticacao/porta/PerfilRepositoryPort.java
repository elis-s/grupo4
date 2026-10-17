package br.ucsal.clinica.autenticacao.porta;

import java.util.List;

import br.ucsal.clinica.autenticacao.dominio.Perfil;

/** Porta de leitura dos perfis de acesso (tb_perfil). */
public interface PerfilRepositoryPort {

    List<Perfil> listar();
}
