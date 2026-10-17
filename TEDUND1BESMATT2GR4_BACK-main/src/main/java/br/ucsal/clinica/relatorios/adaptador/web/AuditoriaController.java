package br.ucsal.clinica.relatorios.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.relatorios.aplicacao.ConsultarLogsAuditoria;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.FiltroAuditoria;
import br.ucsal.clinica.relatorios.dominio.RegistroAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.seguranca.Permissoes;

/** /api/auditoria (RF014): o administrador monitora os logs de auditoria. */
@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize(Permissoes.ADMIN)
public class AuditoriaController {

    public record RegistroResponse(Long id, String login, AcaoAuditoria acao, ResultadoAuditoria resultado,
            String entidade, Long entidadeId, String descricao, String ipOrigem, LocalDateTime dataHora) {

        static RegistroResponse de(RegistroAuditoria r) {
            return new RegistroResponse(r.id(), r.login(), r.acao(), r.resultado(), r.entidade(), r.entidadeId(),
                    r.descricao(), r.ipOrigem(), r.dataHora());
        }
    }

    private final ConsultarLogsAuditoria consultar;

    public AuditoriaController(ConsultarLogsAuditoria consultar) {
        this.consultar = consultar;
    }

    /** Filtros opcionais: ?login=ana.souza&entidade=paciente&acao=LOGIN_FALHA&inicio=2026-10-01&fim=2026-10-31&page=0&size=50 */
    @GetMapping
    public Pagina<RegistroResponse> listar(
            @RequestParam(name = "login", required = false) String login,
            @RequestParam(name = "entidade", required = false) String entidade,
            @RequestParam(name = "acao", required = false) AcaoAuditoria acao,
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return consultar.executar(new FiltroAuditoria(login, entidade, acao, inicio, fim),
                Paginacao.de(page, size == null ? 50 : size)).map(RegistroResponse::de);
    }
}
