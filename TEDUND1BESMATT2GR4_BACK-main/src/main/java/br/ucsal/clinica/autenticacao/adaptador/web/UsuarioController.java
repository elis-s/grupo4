package br.ucsal.clinica.autenticacao.adaptador.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.autenticacao.adaptador.web.UsuarioDTO.CadastroRequest;
import br.ucsal.clinica.autenticacao.adaptador.web.UsuarioDTO.EdicaoRequest;
import br.ucsal.clinica.autenticacao.adaptador.web.UsuarioDTO.Response;
import br.ucsal.clinica.autenticacao.aplicacao.CadastrarUsuario;
import br.ucsal.clinica.autenticacao.aplicacao.ConsultarUsuarios;
import br.ucsal.clinica.autenticacao.aplicacao.EditarUsuario;
import br.ucsal.clinica.autenticacao.aplicacao.InativarUsuario;
import br.ucsal.clinica.autenticacao.aplicacao.ReativarUsuario;
import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/** /api/usuarios (RF013, RF063): somente o administrador gerencia usuarios e perfis. */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize(Permissoes.ADMIN)
public class UsuarioController {

    private final CadastrarUsuario cadastrar;
    private final EditarUsuario editar;
    private final InativarUsuario inativar;
    private final ReativarUsuario reativar;
    private final ConsultarUsuarios consultar;

    public UsuarioController(CadastrarUsuario cadastrar, EditarUsuario editar, InativarUsuario inativar,
            ReativarUsuario reativar, ConsultarUsuarios consultar) {
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.inativar = inativar;
        this.reativar = reativar;
        this.consultar = consultar;
    }

    @GetMapping
    public Pagina<Response> listar(@RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.listar(status, Paginacao.de(page, size)).map(Response::de);
    }

    @GetMapping("/{id}")
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response cadastrar(@RequestBody @Valid CadastroRequest req) {
        return Response.de(cadastrar.executar(req.paraComando()));
    }

    @PutMapping("/{id}")
    public Response editar(@PathVariable("id") Long id, @RequestBody @Valid EdicaoRequest req) {
        return Response.de(editar.executar(id, req.paraComando()));
    }

    @PatchMapping("/{id}/inativar")
    public Response inativar(@PathVariable("id") Long id, @RequestBody @Valid InativacaoRequest req) {
        return Response.de(inativar.executar(id, req.motivo()));
    }

    @PatchMapping("/{id}/reativar")
    public Response reativar(@PathVariable("id") Long id) {
        return Response.de(reativar.executar(id));
    }
}
