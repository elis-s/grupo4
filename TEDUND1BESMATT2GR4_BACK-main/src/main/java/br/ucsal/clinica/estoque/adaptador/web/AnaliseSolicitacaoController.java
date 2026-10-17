package br.ucsal.clinica.estoque.adaptador.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.estoque.adaptador.web.SolicitacaoMedicamentoDTO.ReprovacaoRequest;
import br.ucsal.clinica.estoque.adaptador.web.SolicitacaoMedicamentoDTO.Response;
import br.ucsal.clinica.estoque.aplicacao.AprovarReprovarSolicitacao;
import br.ucsal.clinica.estoque.aplicacao.ConsultarSolicitacoes;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/**
 * /api/solicitacoes-medicamento - lado do Estoque e Farmacia (RF059, RN033): o usuario
 * (atendente) consulta a fila e aprova ou reprova cada solicitacao antes da retirada.
 * O registro da solicitacao pelo colaborador fica no modulo de Atendimento.
 */
@RestController
@RequestMapping("/api/solicitacoes-medicamento")
public class AnaliseSolicitacaoController {

    private final ConsultarSolicitacoes consultar;
    private final AprovarReprovarSolicitacao analisar;

    public AnaliseSolicitacaoController(ConsultarSolicitacoes consultar, AprovarReprovarSolicitacao analisar) {
        this.consultar = consultar;
        this.analisar = analisar;
    }

    /** Fila de solicitacoes: ?situacao=PENDENTE&page=0&size=20 */
    @GetMapping
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Pagina<Response> listar(@RequestParam(name = "situacao", required = false) SituacaoSolicitacao situacao,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.listar(situacao, Paginacao.de(page, size)).map(Response::de);
    }

    /** O colaborador so consegue ver as proprias solicitacoes (regra por dado no caso de uso). */
    @GetMapping("/{id}")
    @PreAuthorize(Permissoes.AUTENTICADO)
    public Response buscar(@PathVariable("id") Long id) {
        return Response.de(consultar.porId(id));
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response aprovar(@PathVariable("id") Long id) {
        return Response.de(analisar.aprovar(id));
    }

    @PatchMapping("/{id}/reprovar")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public Response reprovar(@PathVariable("id") Long id, @RequestBody @Valid ReprovacaoRequest req) {
        return Response.de(analisar.reprovar(id, req.motivo()));
    }
}
