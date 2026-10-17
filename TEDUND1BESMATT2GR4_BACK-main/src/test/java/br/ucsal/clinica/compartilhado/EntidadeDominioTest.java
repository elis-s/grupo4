package br.ucsal.clinica.compartilhado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.compartilhado.dominio.ConflitoVersaoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** RN002, RN003, RN022, RN023 e bloqueio otimista: regras comuns de todas as entidades. */
class EntidadeDominioTest {

    @Test
    void inativacaoExigeMotivoEPreservaORegistro() {
        Convenio convenio = new Convenio();
        assertThrows(RegraNegocioException.class, () -> convenio.inativar("  "));

        convenio.inativar("Contrato encerrado");
        assertEquals(Status.INATIVO, convenio.getStatus());
        assertEquals("Contrato encerrado", convenio.getMotivoInativacao());
        assertNotNull(convenio.getDataInativacao());
        assertThrows(RegraNegocioException.class, () -> convenio.inativar("de novo"));
    }

    @Test
    void reativacaoLimpaOMotivo() {
        Convenio convenio = new Convenio();
        convenio.inativar("Teste");
        convenio.reativar();
        assertEquals(Status.ATIVO, convenio.getStatus());
        assertNull(convenio.getMotivoInativacao());
    }

    @Test
    void edicaoComVersaoAntigaEhRecusada() {
        Convenio convenio = new Convenio();
        convenio.restaurarControle(1L, 3L, null, Status.ATIVO, null, null);
        convenio.verificarVersao(3L);
        assertThrows(ConflitoVersaoException.class, () -> convenio.verificarVersao(2L));
    }
}
