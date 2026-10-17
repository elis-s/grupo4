package br.ucsal.clinica.administracao.adaptador.persistencia;

import br.ucsal.clinica.administracao.dominio.DiaSemana;
import br.ucsal.clinica.administracao.dominio.Turno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Dias e turnos de atendimento do medico (tb_colaborador_turno - RF047). */
@Getter
@Setter
@Entity
@Table(name = "tb_colaborador_turno")
public class ColaboradorTurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorEntity colaborador;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 3)
    private DiaSemana diaSemana;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Turno turno;

    @Override
    public String toString() {
        return diaSemana + "-" + turno;
    }
}
