package br.ucsal.clinica.pacientes.dominio;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/**
 * Prontuario eletronico do paciente - PEP (RF030, RF031, RF051): relacao 1:1 estrita
 * com o paciente (secao 3.4.2). Pode ser inativado, nunca excluido (RN029).
 */
public class ProntuarioEletronico extends EntidadeDominio {

    private Long pacienteId;
    private String numero;

    /** DA012: o prontuario nasce junto com o paciente, no mesmo caso de uso e na mesma transacao. */
    public static ProntuarioEletronico paraPaciente(Long pacienteId) {
        if (pacienteId == null) {
            throw new RegraNegocioException("Não existe prontuário sem paciente (RN008).");
        }
        ProntuarioEletronico p = new ProntuarioEletronico();
        p.pacienteId = pacienteId;
        p.numero = String.format("PR-%06d", pacienteId);
        return p;
    }

    /** RN029: prontuario inativo nao recebe novos atendimentos. */
    public void garantirAtivoParaAtendimento() {
        if (!isAtivo()) {
            throw new RegraNegocioException("O prontuário " + numero + " está inativo: " + getMotivoInativacao());
        }
    }

    // ---------------- getters e setters ----------------

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
