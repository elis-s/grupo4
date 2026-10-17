package br.ucsal.clinica.autenticacao.dominio;

/** Perfil de acesso cadastrado em tb_perfil. */
public record Perfil(Long id, NomePerfil nome, String descricao) {
}
