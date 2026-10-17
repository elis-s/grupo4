package br.ucsal.clinica.administracao.aplicacao;

import java.util.List;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Caso de uso ConsultarColaboradores (RF010): consulta por id e listagem paginada. */
public class ConsultarColaboradores {

    private final ColaboradorRepositoryPort repositorio;

    public ConsultarColaboradores(ColaboradorRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Colaborador porId(Long id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", id));
    }

    public Pagina<Colaborador> listar(Status status, Paginacao paginacao) {
        return repositorio.listar(status, paginacao);
    }

    /** Medicos ativos: usados para escolher o medico no agendamento e no cadastro do paciente. */
    public List<Colaborador> medicosAtivos() {
        return repositorio.listarMedicosAtivos();
    }
}
