package br.ucsal.clinica.estoque.adaptador.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.estoque.adaptador.web.EstoqueDTO.MovimentacaoResponse;
import br.ucsal.clinica.estoque.adaptador.web.EstoqueDTO.ReposicaoRequest;
import br.ucsal.clinica.estoque.adaptador.web.EstoqueDTO.ReposicaoResponse;
import br.ucsal.clinica.estoque.adaptador.web.EstoqueDTO.RetiradaRequest;
import br.ucsal.clinica.estoque.adaptador.web.EstoqueDTO.RetiradaResponse;
import br.ucsal.clinica.estoque.adaptador.web.EstoqueDTO.SaldoResponse;
import br.ucsal.clinica.estoque.aplicacao.ConsultarMovimentacoes;
import br.ucsal.clinica.estoque.aplicacao.ConsultarSaldoEstoque;
import br.ucsal.clinica.estoque.aplicacao.RegistrarReposicao;
import br.ucsal.clinica.estoque.aplicacao.RegistrarRetiradaMedicamento;
import br.ucsal.clinica.estoque.aplicacao.VerificarNivelCriticoEstoque;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;

/** /api/estoque (RF035 a RF043, RF057, RF058): retirada, reposicao, saldo e movimentacoes. */
@RestController
@RequestMapping("/api/estoque")
@PreAuthorize(Permissoes.ADMIN_OU_USUARIO)
public class EstoqueController {

    private final RegistrarRetiradaMedicamento retirar;
    private final RegistrarReposicao repor;
    private final ConsultarSaldoEstoque saldos;
    private final VerificarNivelCriticoEstoque nivelCritico;
    private final ConsultarMovimentacoes movimentacoes;

    public EstoqueController(RegistrarRetiradaMedicamento retirar, RegistrarReposicao repor,
            ConsultarSaldoEstoque saldos, VerificarNivelCriticoEstoque nivelCritico,
            ConsultarMovimentacoes movimentacoes) {
        this.retirar = retirar;
        this.repor = repor;
        this.saldos = saldos;
        this.nivelCritico = nivelCritico;
        this.movimentacoes = movimentacoes;
    }

    /** RF037 a RF040: POST /api/estoque/retiradas (exemplo da secao 4.2.3). */
    @PostMapping("/retiradas")
    @ResponseStatus(HttpStatus.CREATED)
    public RetiradaResponse registrarRetirada(@RequestBody @Valid RetiradaRequest req) {
        return RetiradaResponse.de(retirar.executar(req.paraComando()));
    }

    /** RF042 / RN017 / RN031: reposicao = entrada no estoque, a qualquer tempo. */
    @PostMapping("/reposicoes")
    @ResponseStatus(HttpStatus.CREATED)
    public ReposicaoResponse registrarReposicao(@RequestBody @Valid ReposicaoRequest req) {
        return ReposicaoResponse.de(repor.executar(req.paraComando()));
    }

    /** RF057: saldo atual de cada medicamento ativo. */
    @GetMapping("/saldos")
    public List<SaldoResponse> saldos() {
        return saldos.executar().stream().map(SaldoResponse::de).toList();
    }

    /** RF058: medicamentos que atingiram a quantidade minima (10 unidades). */
    @GetMapping("/reposicao")
    public List<SaldoResponse> precisamReposicao() {
        return nivelCritico.executar().stream().map(SaldoResponse::de).toList();
    }

    /** RF036 / RF043: movimentacoes com filtros: ?medicamentoId=1&tipo=SAIDA&inicio=2026-10-01&fim=2026-10-31 */
    @GetMapping("/movimentacoes")
    public Pagina<MovimentacaoResponse> movimentacoes(
            @RequestParam(name = "medicamentoId", required = false) Long medicamentoId,
            @RequestParam(name = "tipo", required = false) TipoMovimentacao tipo,
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return movimentacoes.listar(medicamentoId, tipo, inicio, fim, Paginacao.de(page, size))
                .map(MovimentacaoResponse::de);
    }
}
