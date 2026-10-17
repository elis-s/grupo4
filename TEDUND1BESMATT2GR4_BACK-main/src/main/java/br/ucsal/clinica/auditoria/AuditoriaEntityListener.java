package br.ucsal.clinica.auditoria;

import java.util.List;
import java.util.Map;

import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;

/**
 * Ouvinte de entidade JPA (secao 3.4.3 e DA014): as insercoes e as alteracoes
 * (inclusive de status) sao capturadas aqui, sem poluir os casos de uso. Cada
 * operacao gera um evento com o usuario, a data/hora, a acao e o delta da
 * modificacao, gravado depois na tabela imutavel tb_auditoria.
 */
public class AuditoriaEntityListener {

    @PostPersist
    public void aposInserir(Object entidade) {
        String rotulo = EstadoAuditavel.rotulo(entidade);
        PublicadorAuditoria.publicar(EventoAuditoria.agora(AcaoAuditoria.CRIAR, ResultadoAuditoria.SUCESSO,
                EstadoAuditavel.nomeEntidade(entidade), EstadoAuditavel.id(entidade),
                rotulo != null ? "Cadastro: " + rotulo : "Cadastro"));
        if (entidade instanceof EntidadeJpa jpa) {
            jpa.setEstadoAnterior(EstadoAuditavel.de(entidade));
        }
    }

    @PostUpdate
    public void aposAlterar(Object entidade) {
        Map<String, String> depois = EstadoAuditavel.de(entidade);
        Map<String, String> antes = entidade instanceof EntidadeJpa jpa ? jpa.getEstadoAnterior() : null;

        AcaoAuditoria acao = AcaoAuditoria.EDITAR;
        String detalhes = "";
        if (antes != null) {
            List<String> mudancas = EstadoAuditavel.diferencas(antes, depois);
            if (mudancas.isEmpty()) {
                return; // so mudou controle tecnico (versao, data de atualizacao): nada a registrar
            }
            acao = acaoPelaMudanca(antes, depois);
            detalhes = " | " + String.join("; ", mudancas);
        }
        if (entidade instanceof EntidadeJpa jpa) {
            jpa.setEstadoAnterior(depois);
        }
        String rotulo = EstadoAuditavel.rotulo(entidade);
        PublicadorAuditoria.publicar(EventoAuditoria.agora(acao, ResultadoAuditoria.SUCESSO,
                EstadoAuditavel.nomeEntidade(entidade), EstadoAuditavel.id(entidade),
                descricao(acao) + (rotulo != null ? ": " + rotulo : "") + detalhes));
    }

    private static AcaoAuditoria acaoPelaMudanca(Map<String, String> antes, Map<String, String> depois) {
        String statusAntes = antes.get("status");
        String statusDepois = depois.get("status");
        if (statusAntes != null && !statusAntes.equals(statusDepois)) {
            return "INATIVO".equals(statusDepois) ? AcaoAuditoria.INATIVAR : AcaoAuditoria.REATIVAR;
        }
        for (String campo : List.of("situacao", "etapa")) {
            String a = antes.get(campo);
            if (a != null && !a.equals(depois.get(campo))) {
                return AcaoAuditoria.MUDAR_STATUS;
            }
        }
        return AcaoAuditoria.EDITAR;
    }

    private static String descricao(AcaoAuditoria acao) {
        return switch (acao) {
            case INATIVAR -> "Inativação";
            case REATIVAR -> "Reativação";
            case MUDAR_STATUS -> "Mudança de status";
            default -> "Alteração";
        };
    }
}
