-- =====================================================================
-- V4 - Usuario de banco exclusivo da aplicacao (RNF018, secoes 5.3 e 6.5)
--
-- O Flyway roda com o usuario dono do banco (cria as tabelas). O Back-End,
-- em execucao, conecta com ${app_db_user}, que:
--   * nao cria nem remove tabelas (sem permissao de administracao);
--   * le, inclui e altera os dados das tabelas de dominio, mas nao exclui;
--   * na tb_auditoria, nas movimentacoes de estoque e no historico clinico
--     (prescricoes e orientacoes) so pode incluir e consultar (RN036, secao 3.4.1).
-- A senha vem da variavel de ambiente DB_APP_PASSWORD (placeholder do Flyway),
-- entao nao fica gravada no codigo-fonte.
-- =====================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${app_db_user}') THEN
        EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE',
                       '${app_db_user}', '${app_db_password}');
    ELSE
        EXECUTE format('ALTER ROLE %I LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE',
                       '${app_db_user}', '${app_db_password}');
    END IF;
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO %I', current_database(), '${app_db_user}');
END $$;

GRANT USAGE ON SCHEMA public TO "${app_db_user}";

-- Dados: consulta, inclusao e alteracao (sem DELETE e sem DDL)
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO "${app_db_user}";
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO "${app_db_user}";

-- Append-only: auditoria, movimentacoes de estoque, prescricoes e orientacoes so recebem inclusoes
REVOKE UPDATE ON tb_auditoria, tb_movimentacao_estoque, tb_medicacao_atendimento, tb_orientacao_enfermagem
    FROM "${app_db_user}";

-- Listas editaveis e limpeza de tokens expirados podem excluir linhas
GRANT DELETE ON tb_usuario_perfil, tb_clinica_telefone, tb_clinica_proprietario,
                tb_colaborador_turno, tb_token_revogado TO "${app_db_user}";

-- Tabelas criadas em migracoes futuras recebem as mesmas permissoes basicas
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE ON TABLES TO "${app_db_user}";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO "${app_db_user}";

-- A aplicacao nao enxerga o historico de migracoes
REVOKE ALL ON flyway_schema_history FROM "${app_db_user}";
