-- =====================================================================
-- V2 - Dados obrigatorios para o sistema funcionar (todos os ambientes)
-- =====================================================================

-- Perfis de acesso (no Spring Security: ROLE_ADMINISTRADOR, ROLE_COLABORADOR, ROLE_USUARIO)
INSERT INTO tb_perfil (nome, descricao) VALUES
    ('ADMINISTRADOR', 'Gerencia clinica, setores, convenios, colaboradores, usuarios, auditoria e relatorios'),
    ('COLABORADOR',   'Profissional da clinica: atendimento, prontuario, solicitacoes e orientacoes'),
    ('USUARIO',       'Atendente: agendamentos, recepcao, medicamentos, estoque e solicitacoes');

-- Especialidades medicas
INSERT INTO tb_especialidade (nome) VALUES
    ('Clínica Geral'), ('Pediatria'), ('Cardiologia'), ('Ortopedia'),
    ('Ginecologia e Obstetrícia'), ('Dermatologia'), ('Neurologia'),
    ('Psiquiatria'), ('Oftalmologia'), ('Otorrinolaringologia');

-- ---------------------------------------------------------------------
-- Cadastro minimo para o primeiro acesso.
-- RN037: todo usuario precisa estar vinculado a um colaborador, e todo
-- colaborador trabalha em um setor de uma clinica. Por isso o administrador
-- inicial ja nasce com uma clinica, um setor e um colaborador provisorios,
-- que devem ser completados pelo proprio administrador no primeiro acesso.
-- ---------------------------------------------------------------------
INSERT INTO tb_clinica (razao_social, nome_fantasia, cnpj, logradouro, bairro, cidade, estado, cep,
                        email, atividade_principal, alvara_numero, licenca_sanitaria_numero,
                        registro_crm, diretor_tecnico_nome, criado_por)
VALUES ('Clinica Medica Academica (completar cadastro)', 'Clinica Medica Academica', '00000000000000',
        'A completar', 'A completar', 'Salvador', 'BA', '00000000',
        'contato@clinica.exemplo', 'Atividade medica ambulatorial', 'A completar', 'A completar',
        'A completar', 'A completar', 'sistema');

INSERT INTO tb_setor (clinica_id, nome, criado_por)
SELECT id, 'Diretoria', 'sistema' FROM tb_clinica WHERE cnpj = '00000000000000';

INSERT INTO tb_colaborador (nome_completo, cpf, email, funcao, setor_id, hora_inicio, hora_fim,
                            carga_horaria_semanal, criado_por)
SELECT 'Administrador do Sistema', '00000000000', 'admin@clinica.exemplo', 'ADMINISTRADOR', s.id,
       '08:00', '17:00', 40, 'sistema'
  FROM tb_setor s WHERE s.nome = 'Diretoria';

UPDATE tb_setor SET responsavel_id = (SELECT id FROM tb_colaborador WHERE cpf = '00000000000')
 WHERE nome = 'Diretoria';

-- Administrador inicial
-- login: admin | senha: admin123  (hash BCrypt - TROCAR no primeiro acesso)
INSERT INTO tb_usuario (login, senha_hash, colaborador_id, criado_por)
SELECT 'admin', '$2a$10$OIZXk2QkjJRjkV2bHgsMReA7C4takkPXAFY3g.Ur/GsWozJhNhlFO', id, 'sistema'
  FROM tb_colaborador WHERE cpf = '00000000000';

INSERT INTO tb_usuario_perfil (usuario_id, perfil_id)
SELECT u.id, p.id FROM tb_usuario u, tb_perfil p
 WHERE u.login = 'admin' AND p.nome = 'ADMINISTRADOR';
