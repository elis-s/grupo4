-- =====================================================================
-- Sistema de Controle de Atendimento da Clinica Medica Academica
-- V1 - Estrutura do banco (PostgreSQL)
-- Documento de Requisitos e Arquitetura - Grupo 4
--
-- Convencoes (secao 5.3 do documento):
--   * Tabelas com prefixo tb_.
--   * Tabelas de dominio tem: status, data_cadastro, data_inativacao e
--     motivo_inativacao (inativacao logica - RN002, RN003, RN022, DA011).
--   * versao = bloqueio otimista (@Version - RNF021, DA013).
--   * criado_por / atualizado_por / data_atualizacao sao preenchidos pelo
--     AuditingEntityListener do Spring Data (secao 3.4.3).
--   * Nada e excluido, apenas inativado.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Funcoes de protecao
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION bloquear_exclusao() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Exclusão não permitida na tabela "%". Use a inativação (status = INATIVO).', TG_TABLE_NAME
        USING ERRCODE = 'restrict_violation';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validar_inativacao() RETURNS trigger AS $$
BEGIN
    IF NEW.status = 'INATIVO' THEN
        IF NEW.motivo_inativacao IS NULL OR btrim(NEW.motivo_inativacao) = '' THEN
            RAISE EXCEPTION 'Informe o motivo da inativação na tabela "%".', TG_TABLE_NAME
                USING ERRCODE = 'check_violation';
        END IF;
        IF NEW.data_inativacao IS NULL THEN
            NEW.data_inativacao := now();
        END IF;
    ELSE
        NEW.motivo_inativacao := NULL;
        NEW.data_inativacao := NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION bloquear_alteracao() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Registros de "%" não podem ser alterados.', TG_TABLE_NAME
        USING ERRCODE = 'restrict_violation';
END;
$$ LANGUAGE plpgsql;

-- =====================================================================
-- MODULO 3.2.1 - AUTENTICACAO E CONTROLE DE ACESSO
-- =====================================================================
CREATE TABLE tb_perfil (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(30)  NOT NULL UNIQUE,   -- ADMINISTRADOR, COLABORADOR, USUARIO
    descricao   VARCHAR(200)
);

-- =====================================================================
-- MODULO 3.2.2 - GESTAO ADMINISTRATIVA E ESTRUTURAL
-- =====================================================================
CREATE TABLE tb_clinica (
    id                          BIGSERIAL PRIMARY KEY,
    razao_social                VARCHAR(200) NOT NULL,
    nome_fantasia               VARCHAR(200) NOT NULL,
    cnpj                        CHAR(14)     NOT NULL UNIQUE CHECK (cnpj ~ '^[0-9]{14}$'),
    logradouro                  VARCHAR(200) NOT NULL,
    numero                      VARCHAR(20),
    complemento                 VARCHAR(100),
    bairro                      VARCHAR(100) NOT NULL,
    cidade                      VARCHAR(100) NOT NULL,
    estado                      CHAR(2)      NOT NULL,
    pais                        VARCHAR(60)  NOT NULL DEFAULT 'Brasil',
    cep                         CHAR(8)      NOT NULL CHECK (cep ~ '^[0-9]{8}$'),
    email                       VARCHAR(150) NOT NULL,
    atividade_principal         VARCHAR(200) NOT NULL,
    missao                      TEXT,
    alvara_numero               VARCHAR(50)  NOT NULL,
    alvara_data_emissao         DATE,
    alvara_data_validade        DATE,
    licenca_sanitaria_numero    VARCHAR(50)  NOT NULL,
    licenca_sanitaria_emissao   DATE,
    licenca_sanitaria_validade  DATE,
    registro_crm                VARCHAR(30)  NOT NULL,
    diretor_tecnico_nome        VARCHAR(150) NOT NULL,
    diretor_tecnico_crm         VARCHAR(30),
    data_cadastro               TIMESTAMP    NOT NULL DEFAULT now(),
    status                      VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao                      BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao           TEXT,
    data_inativacao             TIMESTAMP,
    criado_por                  VARCHAR(60),
    atualizado_por              VARCHAR(60),
    data_atualizacao            TIMESTAMP
);

CREATE TABLE tb_clinica_telefone (
    id          BIGSERIAL PRIMARY KEY,
    clinica_id  BIGINT      NOT NULL REFERENCES tb_clinica(id),
    telefone    VARCHAR(20) NOT NULL,
    descricao   VARCHAR(50),
    UNIQUE (clinica_id, telefone)
);

CREATE TABLE tb_clinica_proprietario (
    id          BIGSERIAL PRIMARY KEY,
    clinica_id  BIGINT       NOT NULL REFERENCES tb_clinica(id),
    nome        VARCHAR(150) NOT NULL,
    cpf         CHAR(11)     CHECK (cpf ~ '^[0-9]{11}$'),
    UNIQUE (clinica_id, nome)
);

-- Setor: sempre de UMA clinica (RN004) e no maximo UM responsavel (RN005)
CREATE TABLE tb_setor (
    id                  BIGSERIAL PRIMARY KEY,
    clinica_id          BIGINT       NOT NULL REFERENCES tb_clinica(id),
    nome                VARCHAR(100) NOT NULL,
    email               VARCHAR(150),
    telefone            VARCHAR(20),
    responsavel_id      BIGINT,
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao              BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60),
    atualizado_por      VARCHAR(60),
    data_atualizacao    TIMESTAMP,
    UNIQUE (clinica_id, nome)
);

CREATE TABLE tb_convenio (
    id                      BIGSERIAL PRIMARY KEY,
    razao_social            VARCHAR(200) NOT NULL,
    nome_fantasia           VARCHAR(200) NOT NULL,
    registro_ans            VARCHAR(20)  NOT NULL UNIQUE,
    cnpj                    CHAR(14)     NOT NULL UNIQUE CHECK (cnpj ~ '^[0-9]{14}$'),
    inscricao_municipal     VARCHAR(30),
    codigo_prestador        VARCHAR(30)  NOT NULL,
    email                   VARCHAR(150),
    telefone                VARCHAR(20),
    data_cadastro           TIMESTAMP    NOT NULL DEFAULT now(),
    status                  VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao                  BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao       TEXT,
    data_inativacao         TIMESTAMP,
    criado_por              VARCHAR(60),
    atualizado_por          VARCHAR(60),
    data_atualizacao        TIMESTAMP
);

CREATE TABLE tb_especialidade (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(100) NOT NULL UNIQUE,
    status  VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO'))
);

CREATE TABLE tb_colaborador (
    id                          BIGSERIAL PRIMARY KEY,
    nome_completo               VARCHAR(150) NOT NULL,
    cpf                         CHAR(11)     NOT NULL UNIQUE CHECK (cpf ~ '^[0-9]{11}$'),
    email                       VARCHAR(150) NOT NULL,
    telefone                    VARCHAR(20),
    funcao                      VARCHAR(30)  NOT NULL CHECK (funcao IN (
                                    'MEDICO','ENFERMEIRO','AUXILIAR_ENFERMAGEM','FARMACEUTICO',
                                    'FONOAUDIOLOGO','NUTRICIONISTA','FISIOTERAPEUTA','PSICOLOGO',
                                    'ASSISTENTE_SOCIAL','ADMINISTRADOR','ATENDENTE','VIGILANTE','TECNICO')),
    setor_id                    BIGINT       NOT NULL REFERENCES tb_setor(id),
    hora_inicio                 TIME         NOT NULL,
    hora_fim                    TIME         NOT NULL,
    carga_horaria_semanal       SMALLINT     NOT NULL CHECK (carga_horaria_semanal BETWEEN 1 AND 60),
    conselho_sigla              VARCHAR(10),
    conselho_uf                 CHAR(2),
    conselho_numero             VARCHAR(20),
    especialidade_id            BIGINT REFERENCES tb_especialidade(id),
    data_cadastro               TIMESTAMP    NOT NULL DEFAULT now(),
    status                      VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao                      BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao           TEXT,
    data_inativacao             TIMESTAMP,
    criado_por                  VARCHAR(60),
    atualizado_por              VARCHAR(60),
    data_atualizacao            TIMESTAMP,
    CONSTRAINT ck_colaborador_medico CHECK (
        funcao <> 'MEDICO' OR (especialidade_id IS NOT NULL AND conselho_sigla IS NOT NULL
                               AND conselho_uf IS NOT NULL AND conselho_numero IS NOT NULL)),
    CONSTRAINT uq_colaborador_conselho UNIQUE (conselho_sigla, conselho_uf, conselho_numero)
);

ALTER TABLE tb_setor
    ADD CONSTRAINT fk_setor_responsavel FOREIGN KEY (responsavel_id) REFERENCES tb_colaborador(id);

CREATE TABLE tb_colaborador_turno (
    id              BIGSERIAL PRIMARY KEY,
    colaborador_id  BIGINT      NOT NULL REFERENCES tb_colaborador(id),
    dia_semana      VARCHAR(3)  NOT NULL CHECK (dia_semana IN ('SEG','TER','QUA','QUI','SEX','SAB','DOM')),
    turno           VARCHAR(5)  NOT NULL CHECK (turno IN ('MANHA','TARDE','NOITE')),
    UNIQUE (colaborador_id, dia_semana, turno)
);

-- Usuario (secao 6.1): pode acumular perfis (RN001), senha em BCrypt (RNF013)
-- e SEMPRE vinculado a um colaborador (RN037)
CREATE TABLE tb_usuario (
    id                  BIGSERIAL PRIMARY KEY,
    login               VARCHAR(60)  NOT NULL UNIQUE,
    senha_hash          VARCHAR(100) NOT NULL CHECK (senha_hash LIKE '$2%'),
    colaborador_id      BIGINT       NOT NULL UNIQUE REFERENCES tb_colaborador(id),
    ultimo_login        TIMESTAMP,
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao              BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60),
    atualizado_por      VARCHAR(60),
    data_atualizacao    TIMESTAMP
);

CREATE TABLE tb_usuario_perfil (
    usuario_id  BIGINT NOT NULL REFERENCES tb_usuario(id),
    perfil_id   BIGINT NOT NULL REFERENCES tb_perfil(id),
    PRIMARY KEY (usuario_id, perfil_id)
);

-- Logout (RF045, secao 6.1): o identificador (jti) do token fica aqui ate expirar
CREATE TABLE tb_token_revogado (
    id                  BIGSERIAL PRIMARY KEY,
    jti                 VARCHAR(64)  NOT NULL UNIQUE,
    usuario_id          BIGINT       NOT NULL REFERENCES tb_usuario(id),
    data_expiracao      TIMESTAMP    NOT NULL,
    data_revogacao      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX ix_token_revogado_expiracao ON tb_token_revogado (data_expiracao);

-- =====================================================================
-- MODULO 3.2.3 - PACIENTES E PRONTUARIOS ELETRONICOS
-- =====================================================================
CREATE TABLE tb_paciente (
    id                          BIGSERIAL PRIMARY KEY,
    nome_completo               VARCHAR(150) NOT NULL,
    cpf                         CHAR(11)     UNIQUE CHECK (cpf ~ '^[0-9]{11}$'),
    data_nascimento             DATE         NOT NULL,
    sexo                        CHAR(1)      CHECK (sexo IN ('M','F','O')),
    telefone                    VARCHAR(20),
    email                       VARCHAR(150),
    medico_id                   BIGINT       REFERENCES tb_colaborador(id),
    especialidade_id            BIGINT       REFERENCES tb_especialidade(id),
    convenio_id                 BIGINT       REFERENCES tb_convenio(id),
    numero_carteirinha          VARCHAR(40),
    data_cadastro               TIMESTAMP    NOT NULL DEFAULT now(),
    status                      VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao                      BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao           TEXT,
    data_inativacao             TIMESTAMP,
    criado_por                  VARCHAR(60),
    atualizado_por              VARCHAR(60),
    data_atualizacao            TIMESTAMP,
    CONSTRAINT ck_paciente_carteirinha CHECK (convenio_id IS NULL OR numero_carteirinha IS NOT NULL)
);

-- Secao 3.4.2: prontuario 1:1 com paciente (FK NOT NULL + UNIQUE)
CREATE TABLE tb_prontuario (
    id                  BIGSERIAL PRIMARY KEY,
    paciente_id         BIGINT       NOT NULL UNIQUE REFERENCES tb_paciente(id),
    numero              VARCHAR(20)  NOT NULL UNIQUE,
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao              BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60),
    atualizado_por      VARCHAR(60),
    data_atualizacao    TIMESTAMP
);

-- "Nem paciente sem prontuario": verificado no fim da transacao
CREATE OR REPLACE FUNCTION verificar_paciente_tem_prontuario() RETURNS trigger AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM tb_prontuario WHERE paciente_id = NEW.id) THEN
        RAISE EXCEPTION 'O paciente % precisa ter um prontuário vinculado.', NEW.id
            USING ERRCODE = 'foreign_key_violation';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_paciente_prontuario
    AFTER INSERT ON tb_paciente
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION verificar_paciente_tem_prontuario();

-- =====================================================================
-- MODULO 3.2.4 - AGENDAMENTO E ATENDIMENTO CLINICO
-- Status do agendamento (DA015): situacao AGENDADO/FINALIZADO + status INATIVO
-- =====================================================================
CREATE TABLE tb_agendamento (
    id                  BIGSERIAL PRIMARY KEY,
    medico_id           BIGINT       NOT NULL REFERENCES tb_colaborador(id),
    paciente_id         BIGINT       NOT NULL REFERENCES tb_paciente(id),
    data_prevista       DATE         NOT NULL,
    hora_prevista       TIME         NOT NULL,
    situacao            VARCHAR(12)  NOT NULL DEFAULT 'AGENDADO' CHECK (situacao IN ('AGENDADO','FINALIZADO')),
    observacao          TEXT,
    data_recepcao       TIMESTAMP,
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao              BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60),
    atualizado_por      VARCHAR(60),
    data_atualizacao    TIMESTAMP
);

CREATE UNIQUE INDEX uq_agendamento_medico_horario
    ON tb_agendamento (medico_id, data_prevista, hora_prevista)
    WHERE status = 'ATIVO';

-- =====================================================================
-- Atendimento clinico: etapas internas EM_TRIAGEM -> EM_ATENDIMENTO -> FINALIZADO
-- =====================================================================
CREATE TABLE tb_atendimento (
    id                      BIGSERIAL PRIMARY KEY,
    prontuario_id           BIGINT       NOT NULL REFERENCES tb_prontuario(id),
    medico_id               BIGINT       NOT NULL REFERENCES tb_colaborador(id),
    agendamento_id          BIGINT       UNIQUE REFERENCES tb_agendamento(id),
    convenio_id             BIGINT       REFERENCES tb_convenio(id),
    tipo                    VARCHAR(12)  NOT NULL CHECK (tipo IN ('URGENCIA','EMERGENCIA','CONSULTA','REVISAO')),
    etapa                   VARCHAR(15)  NOT NULL DEFAULT 'EM_TRIAGEM'
                            CHECK (etapa IN ('EM_TRIAGEM','EM_ATENDIMENTO','FINALIZADO')),
    data_hora_inicio        TIMESTAMP    NOT NULL DEFAULT now(),
    sintomas                TEXT         NOT NULL,
    diagnostico             TEXT,
    tratamento              TEXT,
    data_hora_encerramento  TIMESTAMP,
    internado               BOOLEAN      NOT NULL DEFAULT FALSE,
    data_cadastro           TIMESTAMP    NOT NULL DEFAULT now(),
    status                  VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao                  BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao       TEXT,
    data_inativacao         TIMESTAMP,
    criado_por              VARCHAR(60),
    atualizado_por          VARCHAR(60),
    data_atualizacao        TIMESTAMP,
    CONSTRAINT ck_atendimento_horario CHECK (data_hora_encerramento IS NULL OR data_hora_encerramento >= data_hora_inicio),
    CONSTRAINT ck_atendimento_etapa CHECK ((etapa = 'FINALIZADO') = (data_hora_encerramento IS NOT NULL))
);

-- RN010: ao encerrar o atendimento, o agendamento passa para FINALIZADO
CREATE OR REPLACE FUNCTION finalizar_agendamento() RETURNS trigger AS $$
BEGIN
    IF NEW.data_hora_encerramento IS NOT NULL AND NEW.agendamento_id IS NOT NULL THEN
        UPDATE tb_agendamento SET situacao = 'FINALIZADO', data_atualizacao = now()
         WHERE id = NEW.agendamento_id AND situacao <> 'FINALIZADO';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_atendimento_finaliza_agendamento
    AFTER INSERT OR UPDATE OF data_hora_encerramento ON tb_atendimento
    FOR EACH ROW EXECUTE FUNCTION finalizar_agendamento();

-- Secao 3.4.1: historico clinico imutavel. Depois de FINALIZADO, o registro clinico
-- do atendimento nao muda mais (so o status, em caso de inativacao).
CREATE OR REPLACE FUNCTION proteger_atendimento_finalizado() RETURNS trigger AS $$
BEGIN
    IF OLD.etapa = 'FINALIZADO' AND (
           NEW.sintomas IS DISTINCT FROM OLD.sintomas
        OR NEW.diagnostico IS DISTINCT FROM OLD.diagnostico
        OR NEW.tratamento IS DISTINCT FROM OLD.tratamento
        OR NEW.tipo IS DISTINCT FROM OLD.tipo
        OR NEW.etapa IS DISTINCT FROM OLD.etapa
        OR NEW.data_hora_inicio IS DISTINCT FROM OLD.data_hora_inicio
        OR NEW.data_hora_encerramento IS DISTINCT FROM OLD.data_hora_encerramento
        OR NEW.prontuario_id IS DISTINCT FROM OLD.prontuario_id
        OR NEW.medico_id IS DISTINCT FROM OLD.medico_id) THEN
        RAISE EXCEPTION 'Atendimento finalizado: o registro clínico não pode ser alterado.'
            USING ERRCODE = 'restrict_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_atendimento_finalizado
    BEFORE UPDATE ON tb_atendimento
    FOR EACH ROW EXECUTE FUNCTION proteger_atendimento_finalizado();

-- =====================================================================
-- MODULO 3.2.5 - ESTOQUE E FARMACIA
-- =====================================================================
CREATE TABLE tb_medicamento (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(150) NOT NULL,
    descricao           TEXT         NOT NULL,
    fabricante          VARCHAR(150) NOT NULL,
    armazenamento       VARCHAR(25)  NOT NULL CHECK (armazenamento IN ('REFRIGERADO','TEMPERATURA_AMBIENTE')),
    unidade             VARCHAR(20)  NOT NULL DEFAULT 'UNIDADE',
    estoque_minimo      INTEGER      NOT NULL DEFAULT 10 CHECK (estoque_minimo >= 0),   -- RN015: 10 unidades
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao              BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60),
    atualizado_por      VARCHAR(60),
    data_atualizacao    TIMESTAMP,
    UNIQUE (nome, fabricante)
);

-- Medicacao indicada no atendimento, com dosagem (RF020)
CREATE TABLE tb_medicacao_atendimento (
    id              BIGSERIAL PRIMARY KEY,
    atendimento_id  BIGINT       NOT NULL REFERENCES tb_atendimento(id),
    medicamento_id  BIGINT       NOT NULL REFERENCES tb_medicamento(id),
    dosagem         VARCHAR(100) NOT NULL,
    posologia       VARCHAR(200),
    UNIQUE (atendimento_id, medicamento_id)
);

-- Prescricao so entra enquanto o atendimento nao foi finalizado (historico imutavel)
CREATE OR REPLACE FUNCTION proteger_prescricao() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM tb_atendimento WHERE id = NEW.atendimento_id AND etapa = 'FINALIZADO') THEN
        RAISE EXCEPTION 'Atendimento finalizado: não é possível incluir medicamentos.'
            USING ERRCODE = 'restrict_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_medicacao_atendimento_finalizado
    BEFORE INSERT ON tb_medicacao_atendimento
    FOR EACH ROW EXECUTE FUNCTION proteger_prescricao();

-- Solicitacao de medicamento (RF023, RN019, RN033):
--   PENDENTE -> APROVADA ou REPROVADA (pelo usuario/atendente - RF059)
--   APROVADA -> ENTREGUE (quando a retirada e registrada no estoque - RF037)
CREATE TABLE tb_solicitacao_medicamento (
    id                  BIGSERIAL PRIMARY KEY,
    medicamento_id      BIGINT       NOT NULL REFERENCES tb_medicamento(id),
    solicitante_id      BIGINT       NOT NULL REFERENCES tb_colaborador(id),
    atendimento_id      BIGINT       REFERENCES tb_atendimento(id),
    quantidade          INTEGER      NOT NULL CHECK (quantidade > 0),
    justificativa       TEXT         NOT NULL,
    situacao            VARCHAR(10)  NOT NULL DEFAULT 'PENDENTE'
                        CHECK (situacao IN ('PENDENTE','APROVADA','REPROVADA','ENTREGUE')),
    analisado_por_id    BIGINT       REFERENCES tb_usuario(id),
    data_analise        TIMESTAMP,
    motivo_reprovacao   TEXT,
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    versao              BIGINT       NOT NULL DEFAULT 0,
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60),
    atualizado_por      VARCHAR(60),
    data_atualizacao    TIMESTAMP,
    CONSTRAINT ck_solicitacao_analise CHECK (situacao = 'PENDENTE' OR (analisado_por_id IS NOT NULL AND data_analise IS NOT NULL)),
    CONSTRAINT ck_solicitacao_reprovacao CHECK (situacao <> 'REPROVADA' OR motivo_reprovacao IS NOT NULL)
);

-- Movimentacao: o saldo so muda por aqui (RN013, RN014, RN018)
CREATE TABLE tb_movimentacao_estoque (
    id                  BIGSERIAL PRIMARY KEY,
    medicamento_id      BIGINT       NOT NULL REFERENCES tb_medicamento(id),
    tipo                VARCHAR(7)   NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA')),
    quantidade          INTEGER      NOT NULL CHECK (quantidade > 0),
    data_hora           TIMESTAMP    NOT NULL DEFAULT now(),
    lote                VARCHAR(40),
    data_validade       DATE,
    data_aquisicao      DATE,
    solicitacao_id      BIGINT       REFERENCES tb_solicitacao_medicamento(id),
    solicitante_id      BIGINT       REFERENCES tb_colaborador(id),
    entregue_por_id     BIGINT       REFERENCES tb_usuario(id),
    observacao          TEXT,
    criado_por          VARCHAR(60),
    CONSTRAINT ck_mov_entrada CHECK (tipo <> 'ENTRADA' OR (data_validade IS NOT NULL AND data_aquisicao IS NOT NULL)),
    CONSTRAINT ck_mov_saida   CHECK (tipo <> 'SAIDA'   OR (solicitante_id IS NOT NULL AND entregue_por_id IS NOT NULL))
);

CREATE INDEX ix_mov_medicamento ON tb_movimentacao_estoque (medicamento_id, data_hora);

-- Uma solicitacao aprovada gera no maximo uma retirada
CREATE UNIQUE INDEX uq_mov_solicitacao ON tb_movimentacao_estoque (solicitacao_id) WHERE solicitacao_id IS NOT NULL;

CREATE VIEW vw_estoque_saldo AS
SELECT m.id                AS medicamento_id,
       m.nome,
       m.fabricante,
       m.estoque_minimo,
       m.status,
       COALESCE(SUM(CASE WHEN mv.tipo = 'ENTRADA' THEN mv.quantidade ELSE -mv.quantidade END), 0) AS saldo
  FROM tb_medicamento m
  LEFT JOIN tb_movimentacao_estoque mv ON mv.medicamento_id = m.id
 GROUP BY m.id;

-- RN015/RN016: reposicao necessaria quando saldo <= estoque minimo
CREATE VIEW vw_estoque_reposicao AS
SELECT * FROM vw_estoque_saldo
 WHERE status = 'ATIVO' AND saldo <= estoque_minimo;

-- Uma saida nao pode deixar o saldo negativo
CREATE OR REPLACE FUNCTION validar_saldo_saida() RETURNS trigger AS $$
DECLARE
    v_saldo BIGINT;
BEGIN
    IF NEW.tipo = 'SAIDA' THEN
        PERFORM 1 FROM tb_medicamento WHERE id = NEW.medicamento_id FOR UPDATE;
        SELECT saldo INTO v_saldo FROM vw_estoque_saldo WHERE medicamento_id = NEW.medicamento_id;
        IF v_saldo < NEW.quantidade THEN
            RAISE EXCEPTION 'Saldo insuficiente: disponível %, solicitado %.', v_saldo, NEW.quantidade
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_mov_valida_saldo
    BEFORE INSERT ON tb_movimentacao_estoque
    FOR EACH ROW EXECUTE FUNCTION validar_saldo_saida();

CREATE TRIGGER trg_mov_sem_update BEFORE UPDATE ON tb_movimentacao_estoque
    FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao();

-- =====================================================================
-- ORIENTACOES PARA A ENFERMAGEM (RF022)
-- =====================================================================
CREATE TABLE tb_orientacao_enfermagem (
    id                  BIGSERIAL PRIMARY KEY,
    atendimento_id      BIGINT       NOT NULL REFERENCES tb_atendimento(id),
    medico_id           BIGINT       NOT NULL REFERENCES tb_colaborador(id),
    orientacao          TEXT         NOT NULL,
    data_hora           TIMESTAMP    NOT NULL DEFAULT now(),
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT now(),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO','INATIVO')),
    motivo_inativacao   TEXT,
    data_inativacao     TIMESTAMP,
    criado_por          VARCHAR(60)
);

-- =====================================================================
-- MODULO 3.2.6 - RELATORIOS E AUDITORIA
-- tb_auditoria e append-only (RN036): sem UPDATE e sem DELETE
-- =====================================================================
CREATE TABLE tb_auditoria (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT       REFERENCES tb_usuario(id),
    login               VARCHAR(60),
    acao                VARCHAR(20)  NOT NULL CHECK (acao IN (
                            'LOGIN','LOGIN_FALHA','LOGOUT','RENOVAR_TOKEN','ORIGEM_INVALIDA','ACESSO_NEGADO',
                            'CRIAR','EDITAR','INATIVAR','REATIVAR','MUDAR_STATUS','CONSULTAR','EMITIR_RELATORIO')),
    resultado           VARCHAR(10)  NOT NULL DEFAULT 'SUCESSO' CHECK (resultado IN ('SUCESSO','FALHA','NEGADO')),
    entidade            VARCHAR(60),
    entidade_id         BIGINT,
    descricao           TEXT,
    ip_origem           VARCHAR(45),
    data_hora           TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX ix_auditoria_data     ON tb_auditoria (data_hora DESC);
CREATE INDEX ix_auditoria_usuario  ON tb_auditoria (login, data_hora DESC);
CREATE INDEX ix_auditoria_entidade ON tb_auditoria (entidade, entidade_id);

CREATE TRIGGER trg_auditoria_sem_update BEFORE UPDATE ON tb_auditoria
    FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao();

-- =====================================================================
-- Indices de apoio
-- =====================================================================
CREATE INDEX ix_setor_clinica         ON tb_setor (clinica_id);
CREATE INDEX ix_colaborador_setor     ON tb_colaborador (setor_id);
CREATE INDEX ix_paciente_nome         ON tb_paciente (lower(nome_completo));
CREATE INDEX ix_agendamento_data      ON tb_agendamento (data_prevista, hora_prevista);
CREATE INDEX ix_atendimento_pront     ON tb_atendimento (prontuario_id, data_hora_inicio DESC);
CREATE INDEX ix_atendimento_medico    ON tb_atendimento (medico_id, data_hora_inicio DESC);
CREATE INDEX ix_solicitacao_situacao  ON tb_solicitacao_medicamento (situacao);
CREATE INDEX ix_atendimento_etapa     ON tb_atendimento (etapa);

-- =====================================================================
-- Regras "nao excluir" (RN002) e "inativar com motivo" em todas as
-- tabelas de negocio
-- =====================================================================
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'tb_clinica','tb_setor','tb_convenio','tb_colaborador','tb_usuario','tb_paciente','tb_prontuario',
        'tb_agendamento','tb_atendimento','tb_medicamento','tb_solicitacao_medicamento','tb_orientacao_enfermagem']
    LOOP
        EXECUTE format('CREATE TRIGGER trg_%1$s_sem_delete BEFORE DELETE ON %1$I
                        FOR EACH ROW EXECUTE FUNCTION bloquear_exclusao()', t);
        EXECUTE format('CREATE TRIGGER trg_%1$s_inativacao BEFORE INSERT OR UPDATE OF status, motivo_inativacao ON %1$I
                        FOR EACH ROW EXECUTE FUNCTION validar_inativacao()', t);
    END LOOP;

    FOREACH t IN ARRAY ARRAY['tb_movimentacao_estoque','tb_auditoria','tb_medicacao_atendimento',
                             'tb_perfil','tb_especialidade']
    LOOP
        EXECUTE format('CREATE TRIGGER trg_%1$s_sem_delete BEFORE DELETE ON %1$I
                        FOR EACH ROW EXECUTE FUNCTION bloquear_exclusao()', t);
    END LOOP;
END $$;
-- tb_usuario_perfil, tb_clinica_telefone, tb_clinica_proprietario e tb_colaborador_turno
-- aceitam DELETE de proposito: sao listas editaveis. tb_token_revogado tambem,
-- para a limpeza dos tokens ja expirados.
