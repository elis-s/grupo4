package br.ucsal.clinica.administracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ucsal.clinica.administracao.aplicacao.CadastrarColaborador;
import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.DiaSemana;
import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.administracao.dominio.HorarioAtendimento;
import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.dominio.Turno;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.testes.Fakes;

/** RF047 / RN024: o medico precisa de especialidade, conselho, registro e dias/turnos. */
class CadastrarColaboradorTest {

    static class Setores extends Fakes.Memoria<Setor> implements SetorRepositoryPort {
        @Override
        public boolean existeNomeNaClinica(Long clinicaId, String nome, Long ignorarId) {
            return false;
        }

        @Override
        public Pagina<Setor> listar(Status status, Paginacao paginacao) {
            return super.listar(status, paginacao);
        }
    }

    private Fakes.Colaboradores colaboradores;
    private CadastrarColaborador cadastrar;

    @BeforeEach
    void preparar() {
        colaboradores = new Fakes.Colaboradores();
        Setores setores = new Setores();
        Setor consultorios = new Setor();
        consultorios.setNome("Consultórios");
        setores.salvar(consultorios);
        Fakes.Especialidades especialidades = new Fakes.Especialidades();
        especialidades.salvar(new Especialidade(null, "Clínica Geral", Status.ATIVO));
        cadastrar = new CadastrarColaborador(colaboradores, setores, especialidades);
    }

    private Colaborador medico() {
        Colaborador c = new Colaborador();
        c.setNomeCompleto("Dra. Teste");
        c.setCpf("12345678901");
        c.setEmail("dra@teste.com");
        c.setFuncao(Funcao.MEDICO);
        c.setSetor(Referencia.de(1L));
        c.setHoraInicio(LocalTime.of(8, 0));
        c.setHoraFim(LocalTime.of(17, 0));
        c.setCargaHorariaSemanal(40);
        return c;
    }

    @Test
    void medicoSemDadosDoConselhoEhRecusado() {
        Colaborador c = medico();
        c.setEspecialidade(Referencia.de(1L));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> cadastrar.executar(c));
        assertTrue(erro.getMessage().contains("RN024"));
    }

    @Test
    void medicoCompletoEhCadastradoComSetorEEspecialidade() {
        Colaborador c = medico();
        c.setEspecialidade(Referencia.de(1L));
        c.setConselhoSigla("CRM");
        c.setConselhoUf("BA");
        c.setConselhoNumero("12345");
        c.setHorariosAtendimento(List.of(new HorarioAtendimento(DiaSemana.SEG, Turno.MANHA)));

        Colaborador salvo = cadastrar.executar(c);
        assertEquals("Consultórios", salvo.getSetor().nome());
        assertEquals("Clínica Geral", salvo.getEspecialidade().nome());
        assertEquals(1, colaboradores.todos().size());
    }

    @Test
    void atendenteNaoPrecisaDeConselho() {
        Colaborador c = medico();
        c.setFuncao(Funcao.ATENDENTE);
        Colaborador salvo = cadastrar.executar(c);
        assertEquals(Funcao.ATENDENTE, salvo.getFuncao());
    }
}
