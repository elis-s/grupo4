package br.ucsal.clinica.atendimento.adaptador.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
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

import br.ucsal.clinica.atendimento.adaptador.web.AgendamentoDTO.Request;
import br.ucsal.clinica.atendimento.adaptador.web.AgendamentoDTO.Response;
import br.ucsal.clinica.atendimento.aplicacao.AgendarConsulta;
import br.ucsal.clinica.atendimento.aplicacao.ConsultarAgendamentos;
import br.ucsal.clinica.atendimento.aplicacao.EditarAgendamento;
import br.ucsal.clinica.atendimento.aplicacao.InativarAgendamento;
import br.ucsal.clinica.atendimento.aplicacao.ReativarAgendamento;
import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/** /api/agendamentos (RF025, RF026, RF053, RF054): administrador e usuario (atendente). */
@RestController
@RequestMapping("/api/agendamentos")
@PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
public class AgendamentoController {

    private final AgendarConsulta agendar;
    private final EditarAgendamento editar;
    private final InativarAgendamento inativar;
    private final ReativarAgendamento reativar;
    private final ConsultarAgendamentos consultar;

    public AgendamentoController(AgendarConsulta agendar, EditarAgendamento editar, InativarAgendamento inativar,
            ReativarAgendamento reativar, ConsultarAgendamentos consultar) {
        this.agendar = agendar;
        this.editar = editar;
        this.inativar = inativar;
        this.reativar = reativar;
        this.consultar = consultar;
    }

    /** Filtros opcionais: ?data=2026-10-20&medicoId=1&pacienteId=2&page=0&size=20 */
    @GetMapping
    public Pagina<Response> listar(
            @RequestParam(name = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(name = "medicoId", required = false) Long medicoId,
            @RequestParam(name = "pacienteId", required = false) Long pacienteId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.listar(data, medicoId, pacienteId, Paginacao.de(page, size)).map(Response::de);
    }

    @GetMapping("/{id}")
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response agendar(@RequestBody @Valid Request req) {
        return Response.de(agendar.executar(req.paraDominio()));
    }

    @PutMapping("/{id}")
    public Response editar(@PathVariable("id") Long id, @RequestBody @Valid Request req) {
        return Response.de(editar.executar(id, req.paraDominio(), req.versao()));
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
