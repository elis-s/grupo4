package br.ucsal.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sistema de Controle de Atendimento da Clinica Medica Academica - Back-End (Java + Spring).
 *
 * Organizacao (Arquitetura Limpa - secoes 3 e 5.2), por modulo:
 *   dominio/                  entidades e regras de negocio em Java puro
 *   aplicacao/                casos de uso (um por classe)
 *   porta/                    interfaces que o nucleo precisa da infraestrutura
 *   adaptador/web/            controllers REST e DTOs (adaptadores de entrada)
 *   adaptador/persistencia/   entidades JPA, repositorios Spring Data e mapeadores (adaptadores de saida)
 * Modulos: autenticacao, administracao, pacientes, atendimento, estoque, relatorios.
 * Transversais: seguranca, auditoria, compartilhado.
 */
@SpringBootApplication
public class ClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }
}
