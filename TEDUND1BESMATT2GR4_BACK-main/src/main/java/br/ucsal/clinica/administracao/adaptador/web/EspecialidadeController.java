package br.ucsal.clinica.administracao.adaptador.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ucsal.clinica.administracao.aplicacao.MapearEspecialidadeMedica;
import br.ucsal.clinica.administracao.aplicacao.ConsultarEspecialidades;
import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.seguranca.Permissoes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Especialidades medicas (MapearEspecialidadeMedica): usadas no cadastro do medico e do paciente. */
@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadeController {

    public record EspecialidadeRequest(@NotBlank @Size(max = 100) String nome) {
    }

    public record EspecialidadeResponse(Long id, String nome, Status status) {

        static EspecialidadeResponse de(Especialidade e) {
            return new EspecialidadeResponse(e.id(), e.nome(), e.status());
        }
    }

    private final ConsultarEspecialidades consultar;
    private final MapearEspecialidadeMedica cadastrar;

    public EspecialidadeController(ConsultarEspecialidades consultar, MapearEspecialidadeMedica cadastrar) {
        this.consultar = consultar;
        this.cadastrar = cadastrar;
    }

    @GetMapping
    @PreAuthorize(Permissoes.AUTENTICADO)
    public List<EspecialidadeResponse> listar() {
        return consultar.ativas().stream().map(EspecialidadeResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissoes.ADMIN)
    public EspecialidadeResponse cadastrar(@RequestBody @Valid EspecialidadeRequest req) {
        return EspecialidadeResponse.de(cadastrar.executar(req.nome()));
    }
}
