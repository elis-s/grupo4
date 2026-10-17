package br.ucsal.clinica.atendimento.adaptador.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.OrientacaoRequest;
import br.ucsal.clinica.atendimento.adaptador.web.AtendimentoDTO.OrientacaoResponse;
import br.ucsal.clinica.atendimento.aplicacao.ConsultarOrientacoesEnfermagem;
import br.ucsal.clinica.atendimento.aplicacao.RegistrarOrientacaoEnfermagem;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/** /api/orientacoes-enfermagem (RF022): o colaborador registra; o administrador consulta. */
@RestController
@RequestMapping("/api/orientacoes-enfermagem")
public class OrientacaoEnfermagemController {

    private final RegistrarOrientacaoEnfermagem registrar;
    private final ConsultarOrientacoesEnfermagem consultar;

    public OrientacaoEnfermagemController(RegistrarOrientacaoEnfermagem registrar,
            ConsultarOrientacoesEnfermagem consultar) {
        this.registrar = registrar;
        this.consultar = consultar;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.COLABORADOR)
    public OrientacaoResponse registrar(@RequestBody @Valid OrientacaoRequest req) {
        return OrientacaoResponse.de(registrar.executar(req.atendimentoId(), req.orientacao()));
    }

    /** ?atendimentoId=10 */
    @GetMapping
    @PreAuthorize(Permissoes.ADMIN_OU_COLABORADOR)
    public List<OrientacaoResponse> listar(@RequestParam(name = "atendimentoId") Long atendimentoId) {
        return consultar.doAtendimento(atendimentoId).stream().map(OrientacaoResponse::de).toList();
    }
}
