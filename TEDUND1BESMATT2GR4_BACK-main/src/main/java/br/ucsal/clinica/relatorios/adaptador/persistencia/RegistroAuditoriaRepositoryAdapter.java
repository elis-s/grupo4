package br.ucsal.clinica.relatorios.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioJpaRepository;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Textos;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.FiltroAuditoria;
import br.ucsal.clinica.relatorios.dominio.RegistroAuditoria;
import br.ucsal.clinica.relatorios.porta.RegistroAuditoriaRepositoryPort;

/** Adaptador JPA da tabela de auditoria (somente inclusao e consulta). */
@Component
public class RegistroAuditoriaRepositoryAdapter implements RegistroAuditoriaRepositoryPort {

    private static final LocalDateTime INICIO_PADRAO = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FIM_PADRAO = LocalDateTime.of(3000, 1, 1, 0, 0);

    private final RegistroAuditoriaJpaRepository jpa;
    private final UsuarioJpaRepository usuarios;

    public RegistroAuditoriaRepositoryAdapter(RegistroAuditoriaJpaRepository jpa, UsuarioJpaRepository usuarios) {
        this.jpa = jpa;
        this.usuarios = usuarios;
    }

    @Override
    public void registrar(RegistroAuditoria r) {
        RegistroAuditoriaEntity e = new RegistroAuditoriaEntity();
        e.setUsuarioId(r.usuarioId() != null ? r.usuarioId()
                : r.login() == null ? null : usuarios.idPorLogin(r.login()).orElse(null));
        e.setLogin(r.login());
        e.setAcao(r.acao());
        e.setResultado(r.resultado());
        e.setEntidade(r.entidade());
        e.setEntidadeId(r.entidadeId());
        e.setDescricao(r.descricao());
        e.setIpOrigem(r.ipOrigem());
        e.setDataHora(r.dataHora() != null ? r.dataHora() : LocalDateTime.now());
        jpa.save(e);
    }

    @Override
    public Pagina<RegistroAuditoria> listar(FiltroAuditoria f, Paginacao paginacao) {
        Set<AcaoAuditoria> acoes = f.acao() == null ? EnumSet.allOf(AcaoAuditoria.class) : EnumSet.of(f.acao());
        String login = Textos.vazioParaNulo(f.login());
        String entidade = Textos.vazioParaNulo(f.entidade());
        return Paginas.converter(jpa.filtrar(login == null ? "" : login, entidade == null ? "" : entidade, acoes,
                f.inicio() == null ? INICIO_PADRAO : f.inicio().atStartOfDay(),
                f.fim() == null ? FIM_PADRAO : f.fim().plusDays(1).atStartOfDay(),
                Paginas.pedido(paginacao, Sort.by(Sort.Direction.DESC, "dataHora"))),
                e -> new RegistroAuditoria(e.getId(), e.getUsuarioId(), e.getLogin(), e.getAcao(), e.getResultado(),
                        e.getEntidade(), e.getEntidadeId(), e.getDescricao(), e.getIpOrigem(), e.getDataHora()));
    }
}
