package br.ucsal.clinica.autenticacao.dominio;

import java.util.Set;

/**
 * Ator autenticado na requisicao, fornecido pela SecurityContextPort a todos os
 * modulos (secao 3.2.1): e assim que o sistema identifica o colaborador do
 * atendimento (RN028) e o responsavel pela entrega do medicamento (RN018).
 */
public record UsuarioAutenticado(
        Long id,
        String login,
        String nome,
        Long colaboradorId,
        String funcao,
        Set<NomePerfil> perfis) {

    public boolean temPerfil(NomePerfil perfil) {
        return perfis.contains(perfil);
    }

    public boolean isAdministrador() {
        return temPerfil(NomePerfil.ADMINISTRADOR);
    }

    public static UsuarioAutenticado de(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getLogin(),
                usuario.getColaborador() != null ? usuario.getColaborador().nome() : usuario.getLogin(),
                usuario.getColaborador() != null ? usuario.getColaborador().id() : null,
                usuario.getFuncaoColaborador(), Set.copyOf(usuario.getPerfis()));
    }
}
