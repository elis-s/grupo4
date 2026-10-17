package br.ucsal.clinica.seguranca;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;

/**
 * Secao 6.1 / DA007: implementacao propria do UserDetailsService, que consulta o
 * usuario e seus perfis no PostgreSQL pelo adaptador JPA. Nao e usado o
 * InMemoryUserDetailsManager (RNF004).
 * Usuario inativo, ou vinculado a colaborador inativo, e carregado desabilitado
 * e nao consegue autenticar (RN037).
 */
@Component
public class UsuarioDetailsServiceAdapter implements UserDetailsService {

    private final UsuarioRepositoryPort usuarios;

    public UsuarioDetailsServiceAdapter(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarios.buscarPorLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        String[] perfis = usuario.getPerfis().stream().map(p -> "ROLE_" + p.name()).toArray(String[]::new);
        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .disabled(!usuario.podeAcessar())
                .authorities(perfis)
                .build();
    }
}
