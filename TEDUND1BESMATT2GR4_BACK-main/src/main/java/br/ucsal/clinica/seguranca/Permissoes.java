package br.ucsal.clinica.seguranca;

/**
 * Autorizacao por operacao (secao 6.2, nivel 2): expressoes usadas no @PreAuthorize
 * dos pontos de entrada, conforme a matriz de permissoes por perfil.
 * Perfis: ROLE_ADMINISTRADOR, ROLE_COLABORADOR e ROLE_USUARIO (atendente).
 * Quem acumula perfis recebe a soma das permissoes (RN001).
 */
public final class Permissoes {

    public static final String ADMIN = "hasRole('ADMINISTRADOR')";
    public static final String COLABORADOR = "hasRole('COLABORADOR')";
    public static final String USUARIO = "hasRole('USUARIO')";

    public static final String ADMIN_OU_USUARIO = "hasAnyRole('ADMINISTRADOR','USUARIO')";
    public static final String ADMIN_OU_COLABORADOR = "hasAnyRole('ADMINISTRADOR','COLABORADOR')";
    public static final String AUTENTICADO = "isAuthenticated()";

    private Permissoes() {
    }
}
