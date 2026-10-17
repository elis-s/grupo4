package br.ucsal.clinica.estoque.adaptador.web;

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

import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.adaptador.web.MedicamentoDTO.Request;
import br.ucsal.clinica.estoque.adaptador.web.MedicamentoDTO.Response;
import br.ucsal.clinica.estoque.aplicacao.CadastrarMedicamento;
import br.ucsal.clinica.estoque.aplicacao.ConsultarMedicamentos;
import br.ucsal.clinica.estoque.aplicacao.EditarMedicamento;
import br.ucsal.clinica.estoque.aplicacao.InativarMedicamento;
import br.ucsal.clinica.estoque.aplicacao.ReativarMedicamento;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/**
 * /api/medicamentos (RF033, RF034, RF055, RF056): o administrador e o usuario (atendente)
 * cadastram e mantem; todos os perfis consultam (o colaborador escolhe o medicamento
 * ao prescrever e ao solicitar).
 */
@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    private final CadastrarMedicamento cadastrar;
    private final EditarMedicamento editar;
    private final InativarMedicamento inativar;
    private final ReativarMedicamento reativar;
    private final ConsultarMedicamentos consultar;

    public MedicamentoController(CadastrarMedicamento cadastrar, EditarMedicamento editar,
            InativarMedicamento inativar, ReativarMedicamento reativar, ConsultarMedicamentos consultar) {
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.inativar = inativar;
        this.reativar = reativar;
        this.consultar = consultar;
    }

    @GetMapping
    @PreAuthorize(Permissoes.AUTENTICADO)
    public Pagina<Response> listar(@RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.listar(status, Paginacao.de(page, size)).map(Response::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permissoes.AUTENTICADO)
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response cadastrar(@RequestBody @Valid Request req) {
        return Response.de(cadastrar.executar(req.paraDominio(), req.estoqueInicial()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response editar(@PathVariable("id") Long id, @RequestBody @Valid Request req) {
        return Response.de(editar.executar(id, req.paraDominio(), req.versao()));
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response inativar(@PathVariable("id") Long id, @RequestBody @Valid InativacaoRequest req) {
        return Response.de(inativar.executar(id, req.motivo()));
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response reativar(@PathVariable("id") Long id) {
        return Response.de(reativar.executar(id));
    }
}
