package br.ucsal.clinica.pacientes.adaptador.web;

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

import br.ucsal.clinica.auditoria.Auditavel;
import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.adaptador.web.PacienteDTO.Request;
import br.ucsal.clinica.pacientes.adaptador.web.PacienteDTO.Response;
import br.ucsal.clinica.pacientes.aplicacao.CadastrarPaciente;
import br.ucsal.clinica.pacientes.aplicacao.ConsultarHistoricoPEP;
import br.ucsal.clinica.pacientes.aplicacao.ConsultarPacientes;
import br.ucsal.clinica.pacientes.aplicacao.EditarPaciente;
import br.ucsal.clinica.pacientes.aplicacao.InativarPaciente;
import br.ucsal.clinica.pacientes.aplicacao.ReativarPaciente;
import br.ucsal.clinica.pacientes.aplicacao.RegistrarRecepcaoPaciente;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/**
 * /api/pacientes (RF027 a RF029, RF049, RF050): recepcao, cadastro (com o prontuario)
 * e consulta. O atendente e o administrador cadastram; todos os perfis consultam.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final CadastrarPaciente cadastrar;
    private final EditarPaciente editar;
    private final InativarPaciente inativar;
    private final ReativarPaciente reativar;
    private final ConsultarPacientes consultar;
    private final ConsultarHistoricoPEP prontuario;
    private final RegistrarRecepcaoPaciente recepcao;

    public PacienteController(CadastrarPaciente cadastrar, EditarPaciente editar, InativarPaciente inativar,
            ReativarPaciente reativar, ConsultarPacientes consultar, ConsultarHistoricoPEP prontuario,
            RegistrarRecepcaoPaciente recepcao) {
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.inativar = inativar;
        this.reativar = reativar;
        this.consultar = consultar;
        this.prontuario = prontuario;
        this.recepcao = recepcao;
    }

    /** RF029: ?nome=maria&status=ATIVO&page=0&size=20 */
    @GetMapping
    @PreAuthorize(Permissoes.AUTENTICADO)
    public Pagina<Response> listar(@RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.listar(status, nome, Paginacao.de(page, size)).map(Response::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permissoes.AUTENTICADO)
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    /** RF030: prontuario do paciente (consulta a dado clinico: auditada). */
    @GetMapping("/{id}/prontuario")
    @PreAuthorize(Permissoes.AUTENTICADO)
    @Auditavel(acao = AcaoAuditoria.CONSULTAR, entidade = "prontuario", descricao = "Consulta ao prontuário")
    public ProntuarioDTO.Response prontuario(@PathVariable("id") Long id) {
        return ProntuarioDTO.Response.de(prontuario.porPaciente(id));
    }

    /** RF027 + InicializarProntuario: o prontuario e criado na mesma transacao. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response cadastrar(@RequestBody @Valid Request req) {
        return Response.de(cadastrar.executar(req.paraDominio()));
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

    /** RF028: registrar a recepcao do paciente a partir do agendamento. */
    @PostMapping("/recepcao")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public RecepcaoDTO.Response registrarRecepcao(@RequestBody @Valid RecepcaoDTO.Request req) {
        return RecepcaoDTO.Response.de(recepcao.executar(req.agendamentoId()));
    }
}
