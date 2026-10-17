package br.ucsal.clinica.administracao.adaptador.web;

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

import br.ucsal.clinica.administracao.adaptador.web.SetorDTO.Request;
import br.ucsal.clinica.administracao.adaptador.web.SetorDTO.Response;
import br.ucsal.clinica.administracao.aplicacao.CadastrarSetor;
import br.ucsal.clinica.administracao.aplicacao.ConsultarSetores;
import br.ucsal.clinica.administracao.aplicacao.EditarSetor;
import br.ucsal.clinica.administracao.aplicacao.InativarSetor;
import br.ucsal.clinica.administracao.aplicacao.ReativarSetor;
import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/**
 * Setores (RF004 a RF006): somente o administrador.
 * Adaptador de entrada: recebe a requisicao, valida o formato (Bean Validation),
 * converte o DTO e aciona o caso de uso. Nao contem regras de negocio (secao 5.2).
 * Nao existe DELETE: a inativacao e uma operacao propria (secao 3.4.1).
 */
@RestController
@RequestMapping("/api/setores")
public class SetorController {

    private final CadastrarSetor cadastrar;
    private final EditarSetor editar;
    private final InativarSetor inativar;
    private final ReativarSetor reativar;
    private final ConsultarSetores consultar;

    public SetorController(CadastrarSetor cadastrar, EditarSetor editar, InativarSetor inativar,
            ReativarSetor reativar, ConsultarSetores consultar) {
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.inativar = inativar;
        this.reativar = reativar;
        this.consultar = consultar;
    }

    /** Listagem paginada: ?status=ATIVO&page=0&size=20 */
    @GetMapping
    @PreAuthorize(Permissoes.ADMIN)
    public Pagina<Response> listar(@RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.listar(status, Paginacao.de(page, size)).map(Response::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permissoes.ADMIN)
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.ADMIN)
    public Response cadastrar(@RequestBody @Valid Request req) {
        return Response.de(cadastrar.executar(req.paraDominio()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(Permissoes.ADMIN)
    public Response editar(@PathVariable("id") Long id, @RequestBody @Valid Request req) {
        return Response.de(editar.executar(id, req.paraDominio(), req.versao()));
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize(Permissoes.ADMIN)
    public Response inativar(@PathVariable("id") Long id, @RequestBody @Valid InativacaoRequest req) {
        return Response.de(inativar.executar(id, req.motivo()));
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize(Permissoes.ADMIN)
    public Response reativar(@PathVariable("id") Long id) {
        return Response.de(reativar.executar(id));
    }
}
