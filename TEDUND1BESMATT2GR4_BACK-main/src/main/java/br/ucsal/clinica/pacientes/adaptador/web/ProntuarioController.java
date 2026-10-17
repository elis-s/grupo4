package br.ucsal.clinica.pacientes.adaptador.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.auditoria.Auditavel;
import br.ucsal.clinica.compartilhado.adaptador.web.InativacaoRequest;
import br.ucsal.clinica.pacientes.aplicacao.ConsultarHistoricoPEP;
import br.ucsal.clinica.pacientes.aplicacao.InativarProntuario;
import br.ucsal.clinica.pacientes.aplicacao.ReativarProntuario;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/** /api/prontuarios (RF030, RF031, RF051): consulta e manutencao do prontuario eletronico. */
@RestController
@RequestMapping("/api/prontuarios")
public class ProntuarioController {

    private final ConsultarHistoricoPEP consultar;
    private final InativarProntuario inativar;
    private final ReativarProntuario reativar;

    public ProntuarioController(ConsultarHistoricoPEP consultar, InativarProntuario inativar,
            ReativarProntuario reativar) {
        this.consultar = consultar;
        this.inativar = inativar;
        this.reativar = reativar;
    }

    /** ConsultarHistoricoPEP: toda consulta a prontuario fica registrada na auditoria (secao 6.6). */
    @GetMapping("/{id}")
    @PreAuthorize(Permissoes.AUTENTICADO)
    @Auditavel(acao = AcaoAuditoria.CONSULTAR, entidade = "prontuario", descricao = "Consulta ao prontuário")
    public ProntuarioDTO.Response buscar(@PathVariable("id") Long id) {
        return ProntuarioDTO.Response.de(consultar.porProntuario(id));
    }

    /** RF051 / RN029: inativa o prontuario quando houver restricao (nunca exclui). */
    @PatchMapping("/{id}/inativar")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public ProntuarioDTO.Resumo inativar(@PathVariable("id") Long id, @RequestBody @Valid InativacaoRequest req) {
        return ProntuarioDTO.Resumo.de(inativar.executar(id, req.motivo()));
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    public ProntuarioDTO.Resumo reativar(@PathVariable("id") Long id) {
        return ProntuarioDTO.Resumo.de(reativar.executar(id));
    }
}
