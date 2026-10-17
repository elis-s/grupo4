package br.ucsal.clinica.relatorios.adaptador.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.auditoria.Auditavel;
import br.ucsal.clinica.relatorios.aplicacao.EmitirBoletimMovimentacaoFarmacia;
import br.ucsal.clinica.relatorios.aplicacao.EmitirRelatorioAdministrativoCompleto;
import br.ucsal.clinica.relatorios.aplicacao.EmitirRelatorioMovimentacaoColaborador;
import br.ucsal.clinica.relatorios.aplicacao.EmitirRelatorioUsuario;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.Relatorios.BoletimMovimentacaoFarmacia;
import br.ucsal.clinica.relatorios.dominio.Relatorios.RelatorioAdministrativo;
import br.ucsal.clinica.relatorios.dominio.Relatorios.RelatorioMovimentacaoColaborador;
import br.ucsal.clinica.relatorios.dominio.Relatorios.RelatorioUsuario;
import br.ucsal.clinica.seguranca.Permissoes;

/**
 * /api/relatorios: cada perfil emite o seu (o Front-End formata e imprime).
 * Toda emissao fica registrada na auditoria (secao 6.7).
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final EmitirRelatorioAdministrativoCompleto administrativo;
    private final EmitirRelatorioMovimentacaoColaborador colaborador;
    private final EmitirRelatorioUsuario usuario;
    private final EmitirBoletimMovimentacaoFarmacia estoque;

    public RelatorioController(EmitirRelatorioAdministrativoCompleto administrativo,
            EmitirRelatorioMovimentacaoColaborador colaborador, EmitirRelatorioUsuario usuario,
            EmitirBoletimMovimentacaoFarmacia estoque) {
        this.administrativo = administrativo;
        this.colaborador = colaborador;
        this.usuario = usuario;
        this.estoque = estoque;
    }

    /** RF015 / RF061 / RN034: administrador - completo, com a identificacao do emissor. */
    @GetMapping("/administrativo")
    @PreAuthorize(Permissoes.ADMIN)
    @Auditavel(acao = AcaoAuditoria.EMITIR_RELATORIO, entidade = "relatorio", descricao = "Relatório administrativo completo")
    public RelatorioAdministrativo administrativo() {
        return administrativo.executar();
    }

    /** RF024 / RN020: colaborador - somente a propria movimentacao. */
    @GetMapping("/minha-movimentacao")
    @PreAuthorize(Permissoes.COLABORADOR)
    @Auditavel(acao = AcaoAuditoria.EMITIR_RELATORIO, entidade = "relatorio", descricao = "Relatório de movimentação do colaborador")
    public RelatorioMovimentacaoColaborador minhaMovimentacao() {
        return colaborador.executar();
    }

    /** RF060: usuario (atendente) - cadastros, setores e aprovacao/reprovacao das solicitacoes. */
    @GetMapping("/atendente")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    @Auditavel(acao = AcaoAuditoria.EMITIR_RELATORIO, entidade = "relatorio", descricao = "Relatório do atendente")
    public RelatorioUsuario atendente() {
        return usuario.executar();
    }

    /** RF043: movimentacoes de estoque e medicamentos. ?inicio=2026-10-01&fim=2026-10-31 */
    @GetMapping("/estoque")
    @PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
    @Auditavel(acao = AcaoAuditoria.EMITIR_RELATORIO, entidade = "relatorio", descricao = "Boletim de movimentação da farmácia")
    public BoletimMovimentacaoFarmacia estoque(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return estoque.executar(inicio, fim);
    }
}
