package br.ucsal.clinica.atendimento.adaptador.web;

import java.util.List;

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

import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.EvolucaoRequest;
import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.FinalizacaoRequest;
import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.PrescricaoRequest;
import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.Response;
import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.TriagemRequest;
import br.ucsal.clinica.atendimento.aplicacao.ConsultarAgendamentos;
import br.ucsal.clinica.atendimento.aplicacao.ConsultarAtendimentos;
import br.ucsal.clinica.atendimento.aplicacao.EmitirPrescricaoMedica;
import br.ucsal.clinica.atendimento.aplicacao.FinalizarAtendimento;
import br.ucsal.clinica.atendimento.aplicacao.InativarAtendimento;
import br.ucsal.clinica.atendimento.aplicacao.IniciarAtendimentoClinico;
import br.ucsal.clinica.atendimento.aplicacao.RealizarTriagemPaciente;
import br.ucsal.clinica.atendimento.aplicacao.RegistrarEvolucaoClinica;
import br.ucsal.clinica.auditoria.Auditavel;
import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/**
 * /api/atendimentos (RF016 a RF021, RF032): o colaborador registra o atendimento no
 * prontuario; o administrador consulta (matriz da secao 6.2).
 * Fluxo: POST (triagem) -> PATCH /iniciar -> PUT (evolucao) / POST /prescricoes -> PATCH /finalizar.
 */
@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final RealizarTriagemPaciente triagem;
    private final IniciarAtendimentoClinico iniciar;
    private final RegistrarEvolucaoClinica evolucao;
    private final EmitirPrescricaoMedica prescricao;
    private final FinalizarAtendimento finalizar;
    private final InativarAtendimento inativar;
    private final ConsultarAtendimentos consultar;
    private final ConsultarAgendamentos agenda;

    public AtendimentoController(RealizarTriagemPaciente triagem, IniciarAtendimentoClinico iniciar,
            RegistrarEvolucaoClinica evolucao, EmitirPrescricaoMedica prescricao, FinalizarAtendimento finalizar,
            InativarAtendimento inativar, ConsultarAtendimentos consultar, ConsultarAgendamentos agenda) {
        this.triagem = triagem;
        this.iniciar = iniciar;
        this.evolucao = evolucao;
        this.prescricao = prescricao;
        this.finalizar = finalizar;
        this.inativar = inativar;
        this.consultar = consultar;
        this.agenda = agenda;
    }

    /** Todos os atendimentos (consulta do administrador). */
    @GetMapping
    @PreAuthorize(Permissoes.ADMIN)
    public Pagina<Response> todos(@RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.todos(Paginacao.de(page, size)).map(Response::de);
    }

    /** Atendimentos do colaborador logado. */
    @GetMapping("/meus")
    @PreAuthorize(Permissoes.COLABORADOR)
    public Pagina<Response> meus(@RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.meus(Paginacao.de(page, size)).map(Response::de);
    }

    /** Agenda do colaborador logado (consultas de hoje em diante). */
    @GetMapping("/agenda")
    @PreAuthorize(Permissoes.COLABORADOR)
    public List<AgendamentoDTO.Response> minhaAgenda() {
        return agenda.minhaAgenda().stream().map(AgendamentoDTO.Response::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permissoes.ADMIN_OU_COLABORADOR)
    @Auditavel(acao = AcaoAuditoria.CONSULTAR, entidade = "atendimento", descricao = "Consulta ao atendimento")
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    /** RealizarTriagemPaciente: abre o atendimento (etapa EM_TRIAGEM). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.COLABORADOR)
    public Response realizarTriagem(@RequestBody @Valid TriagemRequest req) {
        return Response.de(triagem.executar(req.paraComando()));
    }

    /** IniciarAtendimentoClinico: EM_TRIAGEM -> EM_ATENDIMENTO. */
    @PatchMapping("/{id}/iniciar")
    @PreAuthorize(Permissoes.COLABORADOR)
    public Response iniciar(@PathVariable("id") Long id) {
        return Response.de(iniciar.executar(id));
    }

    /** RF017 a RF019: sintomas, diagnostico e tratamento. */
    @PutMapping("/{id}")
    @PreAuthorize(Permissoes.COLABORADOR)
    public Response registrarEvolucao(@PathVariable("id") Long id, @RequestBody @Valid EvolucaoRequest req) {
        return Response.de(evolucao.executar(id, req.paraComando()));
    }

    /** RF020 - EmitirPrescricaoMedica: medicamentos e dosagens. */
    @PostMapping("/{id}/prescricoes")
    @PreAuthorize(Permissoes.COLABORADOR)
    public Response prescrever(@PathVariable("id") Long id, @RequestBody @Valid PrescricaoRequest req) {
        return Response.de(prescricao.executar(id, req.paraItens()));
    }

    /** RF032 - FinalizarAtendimento: o agendamento vinculado passa para FINALIZADO. */
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize(Permissoes.COLABORADOR)
    public Response finalizar(@PathVariable("id") Long id, @RequestBody @Valid FinalizacaoRequest req) {
        return Response.de(finalizar.executar(id, req.paraComando()));
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize(Permissoes.ADMIN_OU_COLABORADOR)
    public Response inativar(@PathVariable("id") Long id, @RequestBody @Valid InativacaoRequest req) {
        return Response.de(inativar.executar(id, req.motivo()));
    }
}
