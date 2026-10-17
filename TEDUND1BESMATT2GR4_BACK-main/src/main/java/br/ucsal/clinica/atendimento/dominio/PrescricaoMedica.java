package br.ucsal.clinica.atendimento.dominio;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/** RF020: medicamento utilizado ou prescrito no atendimento, com a dosagem (EmitirPrescricaoMedica). */
public record PrescricaoMedica(Long id, Referencia medicamento, String dosagem, String posologia) {

    public PrescricaoMedica {
        if (medicamento == null || medicamento.id() == null) {
            throw new RegraNegocioException("Informe o medicamento prescrito.");
        }
        dosagem = Textos.obrigatorio(dosagem, "Informe a dosagem do medicamento.");
        posologia = Textos.vazioParaNulo(posologia);
    }
}
