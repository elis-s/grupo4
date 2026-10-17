package br.ucsal.clinica.atendimento.adaptador.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.SolicitacaoRequest;
import br.ucsal.clinica.atendimento.aplicacao.ConsultarMinhasSolicitacoes;
import br.ucsal.clinica.atendimento.aplicacao.SolicitarMedicamento;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.estoque.adaptador.web.SolicitacaoMedicamentoDTO.Response;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/**
 * /api/solicitacoes-medicamento - lado do Atendimento (RF023, RF038): o colaborador solicita
 * e acompanha as proprias solicitacoes. A aprovacao fica no modulo de Estoque e Farmacia.
 */
@RestController
@RequestMapping("/api/solicitacoes-medicamento")
public class SolicitacaoMedicamentoController {

    private final SolicitarMedicamento solicitar;
    private final ConsultarMinhasSolicitacoes minhas;

    public SolicitacaoMedicamentoController(SolicitarMedicamento solicitar, ConsultarMinhasSolicitacoes minhas) {
        this.solicitar = solicitar;
        this.minhas = minhas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.COLABORADOR)
    public Response solicitar(@RequestBody @Valid SolicitacaoRequest req) {
        return Response.de(solicitar.executar(new SolicitarMedicamento.Comando(req.medicamentoId(), req.quantidade(),
                req.justificativa(), req.atendimentoId())));
    }

    @GetMapping("/minhas")
    @PreAuthorize(Permissoes.COLABORADOR)
    public Pagina<Response> minhas(@RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return minhas.executar(Paginacao.de(page, size)).map(Response::de);
    }
}
