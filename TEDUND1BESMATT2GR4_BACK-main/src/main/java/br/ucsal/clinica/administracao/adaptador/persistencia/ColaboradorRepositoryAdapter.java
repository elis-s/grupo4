package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.administracao.dominio.HorarioAtendimento;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Adaptador JPA da porta de persistencia dos colaboradores. */
@Component
public class ColaboradorRepositoryAdapter implements ColaboradorRepositoryPort {

    private static final Sort ORDEM = Sort.by("nomeCompleto");

    private final ColaboradorJpaRepository jpa;
    private final SetorJpaRepository setores;
    private final EspecialidadeJpaRepository especialidades;

    public ColaboradorRepositoryAdapter(ColaboradorJpaRepository jpa, SetorJpaRepository setores,
            EspecialidadeJpaRepository especialidades) {
        this.jpa = jpa;
        this.setores = setores;
        this.especialidades = especialidades;
    }

    @Override
    public Optional<Colaborador> buscarPorId(Long id) {
        return jpa.findById(id).map(ColaboradorRepositoryAdapter::paraDominio);
    }

    @Override
    public Colaborador salvar(Colaborador colaborador) {
        ColaboradorEntity e;
        if (colaborador.isNovo()) {
            e = new ColaboradorEntity();
            e.receberStatusDe(colaborador);
        } else {
            e = jpa.findById(colaborador.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", colaborador.getId()));
            e.prepararAlteracao(colaborador);
        }
        e.setNomeCompleto(colaborador.getNomeCompleto());
        e.setCpf(colaborador.getCpf());
        e.setEmail(colaborador.getEmail());
        e.setTelefone(colaborador.getTelefone());
        e.setFuncao(colaborador.getFuncao());
        e.setSetor(setores.getReferenceById(colaborador.getSetor().id()));
        e.setHoraInicio(colaborador.getHoraInicio());
        e.setHoraFim(colaborador.getHoraFim());
        e.setCargaHorariaSemanal(colaborador.getCargaHorariaSemanal() == null ? null
                : colaborador.getCargaHorariaSemanal().shortValue());
        e.setConselhoSigla(colaborador.getConselhoSigla());
        e.setConselhoUf(colaborador.getConselhoUf());
        e.setConselhoNumero(colaborador.getConselhoNumero());
        e.setEspecialidade(colaborador.getEspecialidade() == null ? null
                : especialidades.getReferenceById(colaborador.getEspecialidade().id()));

        // Dias e turnos: mantem os que nao mudaram (evita violar a chave unica)
        List<HorarioAtendimento> novos = colaborador.getHorariosAtendimento();
        e.getTurnos().removeIf(atual -> novos.stream()
                .noneMatch(n -> n.diaSemana() == atual.getDiaSemana() && n.turno() == atual.getTurno()));
        for (HorarioAtendimento h : novos) {
            boolean existe = e.getTurnos().stream()
                    .anyMatch(t -> t.getDiaSemana() == h.diaSemana() && t.getTurno() == h.turno());
            if (!existe) {
                ColaboradorTurnoEntity turno = new ColaboradorTurnoEntity();
                turno.setColaborador(e);
                turno.setDiaSemana(h.diaSemana());
                turno.setTurno(h.turno());
                e.getTurnos().add(turno);
            }
        }
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<Colaborador> listar(Status status, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(status == null ? jpa.findAll(pedido) : jpa.findByStatus(status, pedido),
                ColaboradorRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeCpf(String cpf, Long ignorarId) {
        return ignorarId == null ? jpa.existsByCpf(cpf) : jpa.existsByCpfAndIdNot(cpf, ignorarId);
    }

    @Override
    public boolean existeRegistroConselho(String sigla, String uf, String numero, Long ignorarId) {
        return ignorarId == null
                ? jpa.existsByConselhoSiglaAndConselhoUfAndConselhoNumero(sigla, uf, numero)
                : jpa.existsByConselhoSiglaAndConselhoUfAndConselhoNumeroAndIdNot(sigla, uf, numero, ignorarId);
    }

    @Override
    public List<Colaborador> listarMedicosAtivos() {
        return jpa.findByFuncaoAndStatusOrderByNomeCompleto(Funcao.MEDICO, Status.ATIVO).stream()
                .map(ColaboradorRepositoryAdapter::paraDominio).toList();
    }

    public static Colaborador paraDominio(ColaboradorEntity e) {
        Colaborador c = new Colaborador();
        e.copiarControlePara(c);
        c.setNomeCompleto(e.getNomeCompleto());
        c.setCpf(e.getCpf());
        c.setEmail(e.getEmail());
        c.setTelefone(e.getTelefone());
        c.setFuncao(e.getFuncao());
        c.setSetor(new Referencia(e.getSetor().getId(), e.getSetor().getNome()));
        c.setHoraInicio(e.getHoraInicio());
        c.setHoraFim(e.getHoraFim());
        c.setCargaHorariaSemanal(e.getCargaHorariaSemanal() == null ? null : e.getCargaHorariaSemanal().intValue());
        c.setConselhoSigla(e.getConselhoSigla());
        c.setConselhoUf(e.getConselhoUf());
        c.setConselhoNumero(e.getConselhoNumero());
        EspecialidadeEntity esp = e.getEspecialidade();
        c.setEspecialidade(esp == null ? null : new Referencia(esp.getId(), esp.getNome()));
        c.setHorariosAtendimento(new ArrayList<>(e.getTurnos().stream()
                .map(t -> new HorarioAtendimento(t.getDiaSemana(), t.getTurno())).toList()));
        return c;
    }
}
