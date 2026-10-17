package br.ucsal.clinica.seguranca;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;

/**
 * Configuracao do Spring Security (secoes 6.1 a 6.4).
 *
 * Seguranca em camadas:
 *  1. Origem: o FrontendOriginFilter e o primeiro filtro e descarta (403, sem corpo)
 *     toda requisicao sem a chave X-Frontend-Key colocada pelo proxy do Front-End (RNF015).
 *  2. CORS restrito a origem do Front-End.
 *  3. Autenticacao stateless: toda requisicao, exceto o login, exige um JWT valido,
 *     nao revogado, de usuario ativo (RNF014, RN037).
 *  4. Autorizacao por caminho (aqui), por operacao (@PreAuthorize) e por dado (casos de uso).
 * Usuarios, senhas (BCrypt) e perfis vem do PostgreSQL, sem armazenamento em memoria (RNF004).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.frontend.chave}")
    private String chaveFrontend;

    @Value("${app.frontend.exigir-chave:true}")
    private boolean exigirChaveFrontend;

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, RastroAuditoriaPort rastroAuditoria,
            RespostasSeguranca respostas, JwtAuthenticationConverter conversorJwt) throws Exception {
        http
            // 1. primeiro filtro: descarta o que nao veio do Front-End (secao 6.4)
            .addFilterBefore(new FrontendOriginFilter(chaveFrontend, exigirChaveFrontend, rastroAuditoria),
                    CorsFilter.class)
            .csrf(csrf -> csrf.disable()) // API stateless com token JWT
            .cors(cors -> cors.configurationSource(corsSomenteFrontEnd()))
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable()) // o logout e o caso de uso EncerrarSessao (/api/auth/logout)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(respostas).accessDeniedHandler(respostas))
            // 4. autorizacao por caminho (secao 6.2, nivel 1)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/auth/**").authenticated()
                // consultas de apoio liberadas aos perfis autenticados
                .requestMatchers(HttpMethod.GET, "/api/convenios/**", "/api/colaboradores/medicos",
                        "/api/especialidades/**", "/api/medicamentos/**").authenticated()
                .requestMatchers("/api/clinicas/**", "/api/setores/**", "/api/convenios/**",
                        "/api/colaboradores/**", "/api/usuarios/**", "/api/perfis/**", "/api/auditoria/**",
                        "/api/especialidades/**")
                    .hasRole("ADMINISTRADOR")
                .requestMatchers("/api/atendimentos/**", "/api/orientacoes-enfermagem/**")
                    .hasAnyRole("COLABORADOR", "ADMINISTRADOR")
                .requestMatchers("/api/agendamentos/**", "/api/estoque/**", "/api/medicamentos/**")
                    .hasAnyRole("USUARIO", "ADMINISTRADOR")
                .anyRequest().authenticated())
            // 3. token JWT obrigatorio
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorJwt))
                .authenticationEntryPoint(respostas)
                .accessDeniedHandler(respostas));
        return http.build();
    }

    /** 2. CORS: so a origem do Front-End pode chamar a API pelo navegador. */
    private CorsConfigurationSource corsSomenteFrontEnd() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /** RNF013: BCrypt com custo de encriptacao elevado. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** Secao 6.1: AuthenticationManager com DaoAuthenticationProvider, buscando o usuario no PostgreSQL. */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
