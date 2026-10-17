package br.ucsal.clinica.autenticacao.adaptador.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.autenticacao.adaptador.web.UsuarioDTO.PerfilResponse;
import br.ucsal.clinica.autenticacao.aplicacao.ConsultarUsuarios;
import br.ucsal.clinica.seguranca.Permissoes;

/** /api/perfis: perfis de acesso disponiveis para atribuir aos usuarios (RF013). */
@RestController
@RequestMapping("/api/perfis")
@PreAuthorize(Permissoes.ADMIN)
public class PerfilController {

    private final ConsultarUsuarios consultar;

    public PerfilController(ConsultarUsuarios consultar) {
        this.consultar = consultar;
    }

    @GetMapping
    public List<PerfilResponse> listar() {
        return consultar.perfis().stream().map(PerfilResponse::de).toList();
    }
}
