-- =====================================================================
-- V3 - Dados de TESTE para desenvolvimento e apresentacao
-- Fica em db/testData para NÃO ir para produção. Ativada pela propriedade:
--   spring.flyway.locations=classpath:db/migration,classpath:db/testData
--
-- Todos os usuários de teste usam a senha: senha123
-- Dados fictícios (CNPJ/CPF apenas no formato, não são reais).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Clínica
-- ---------------------------------------------------------------------
-- Completa a clínica provisória criada na V2 com os dados de teste
UPDATE tb_clinica SET
    razao_social = 'Clínica Médica Acadêmica Ltda', nome_fantasia = 'Clínica Escola de Saúde',
    cnpj = '12345678000190', logradouro = 'Av. Exemplo', numero = '1000', bairro = 'Pituaçu',
    cidade = 'Salvador', estado = 'BA', cep = '41740090', email = 'contato@clinicaescola.exemplo',
    atividade_principal = '8630-5/03 - Atividade médica ambulatorial',
    missao = 'Oferecer atendimento de saúde de qualidade à comunidade, unindo ensino, pesquisa e assistência.',
    alvara_numero = 'ALV-2026-0001', alvara_data_emissao = '2026-01-10', alvara_data_validade = '2027-01-10',
    licenca_sanitaria_numero = 'LS-2026-0456', licenca_sanitaria_emissao = '2026-02-01',
    licenca_sanitaria_validade = '2027-02-01', registro_crm = 'CRM-BA-PJ-9876',
    diretor_tecnico_nome = 'Dr. Roberto Almeida', diretor_tecnico_crm = 'CRM-BA 11111'
 WHERE cnpj = '00000000000000';

INSERT INTO tb_clinica_telefone (clinica_id, telefone, descricao)
SELECT id, '7133330001', 'Recepção' FROM tb_clinica WHERE cnpj = '12345678000190'
UNION ALL
SELECT id, '7133330002', 'Diretoria' FROM tb_clinica WHERE cnpj = '12345678000190';

INSERT INTO tb_clinica_proprietario (clinica_id, nome)
SELECT id, 'Instituição de Ensino Superior (mantenedora)' FROM tb_clinica WHERE cnpj = '12345678000190';

-- ---------------------------------------------------------------------
-- Setores: só ALGUNS (a Diretoria já vem da V2). Os demais serão cadastrados ao vivo
-- na apresentação: Sala de Procedimentos, Setor de Diagnóstico,
-- Almoxarifado, Faturamento e Gestão Financeira, RH.
-- ---------------------------------------------------------------------
INSERT INTO tb_setor (clinica_id, nome, email, telefone, criado_por)
SELECT c.id, s.nome, s.email, s.tel, 'sistema'
  FROM tb_clinica c,
       (VALUES ('Recepção e Triagem',   'recepcao@clinicaescola.exemplo',    '7133330010'),
               ('Consultórios Médicos', 'consultorios@clinicaescola.exemplo','7133330020'),
               ('Farmácia',             'farmacia@clinicaescola.exemplo',    '7133330030')) AS s(nome, email, tel)
 WHERE c.cnpj = '12345678000190';

-- ---------------------------------------------------------------------
-- Convênios
-- ---------------------------------------------------------------------
INSERT INTO tb_convenio (razao_social, nome_fantasia, registro_ans, cnpj, inscricao_municipal,
                      codigo_prestador, email, telefone, criado_por) VALUES
    ('Saúde Bahia Operadora S.A.', 'Saúde Bahia', '412345', '11222333000144', '123456', 'PRES-0001',
     'credenciamento@saudebahia.exemplo', '7140001000', 'sistema'),
    ('Vida Plena Assistência Médica Ltda', 'Vida Plena', '498765', '55666777000188', '654321', 'VP-7788',
     'prestadores@vidaplena.exemplo', '7140002000', 'sistema');

-- ---------------------------------------------------------------------
-- Colaboradores
-- ---------------------------------------------------------------------
INSERT INTO tb_colaborador (nome_completo, cpf, email, telefone, funcao, setor_id, hora_inicio, hora_fim,
                         carga_horaria_semanal, conselho_sigla, conselho_uf, conselho_numero,
                         especialidade_id, criado_por)
SELECT x.nome, x.cpf, x.email, x.tel, x.funcao,
       (SELECT id FROM tb_setor WHERE nome = x.setor),
       x.ini::time, x.fim::time, x.carga, x.cs, x.cuf, x.cnum,
       (SELECT id FROM tb_especialidade WHERE nome = x.esp), 'sistema'
  FROM (VALUES
    ('Ana Beatriz Souza',   '11111111111', 'ana.souza@clinicaescola.exemplo',   '71990000001', 'MEDICO',
     'Consultórios Médicos', '08:00', '17:00', 40, 'CRM',   'BA', '22222', 'Clínica Geral'),
    ('Marcos Vinícius Lima','22222222222', 'marcos.lima@clinicaescola.exemplo', '71990000002', 'MEDICO',
     'Consultórios Médicos', '13:00', '19:00', 30, 'CRM',   'BA', '33333', 'Pediatria'),
    ('Juliana Ferreira',    '33333333333', 'juliana.f@clinicaescola.exemplo',   '71990000003', 'ENFERMEIRO',
     'Consultórios Médicos', '07:00', '19:00', 36, 'COREN', 'BA', '44444', NULL),
    ('Paula Ribeiro',       '44444444444', 'paula.r@clinicaescola.exemplo',     '71990000004', 'FARMACEUTICO',
     'Farmácia',             '08:00', '17:00', 40, 'CRF',   'BA', '55555', NULL),
    ('Carlos Henrique Dias','55555555555', 'carlos.dias@clinicaescola.exemplo', '71990000005', 'ATENDENTE',
     'Recepção e Triagem',   '07:00', '16:00', 44, NULL, NULL, NULL, NULL),
    ('João Pedro Santos',   '66666666666', 'joao.santos@clinicaescola.exemplo', '71990000006', 'ADMINISTRADOR',
     'Diretoria',            '08:00', '17:00', 40, NULL, NULL, NULL, NULL)
  ) AS x(nome, cpf, email, tel, funcao, setor, ini, fim, carga, cs, cuf, cnum, esp);

-- Dias e turnos dos médicos
INSERT INTO tb_colaborador_turno (colaborador_id, dia_semana, turno)
SELECT c.id, t.dia, t.turno FROM tb_colaborador c
  JOIN (VALUES ('11111111111','SEG','MANHA'), ('11111111111','SEG','TARDE'),
               ('11111111111','QUA','MANHA'), ('11111111111','SEX','MANHA'),
               ('22222222222','TER','TARDE'), ('22222222222','QUI','TARDE')) AS t(cpf, dia, turno)
    ON c.cpf = t.cpf;

-- Responsáveis pelos setores (1 por setor)
UPDATE tb_setor SET responsavel_id = (SELECT id FROM tb_colaborador WHERE cpf = '55555555555') WHERE nome = 'Recepção e Triagem';
UPDATE tb_setor SET responsavel_id = (SELECT id FROM tb_colaborador WHERE cpf = '11111111111') WHERE nome = 'Consultórios Médicos';
UPDATE tb_setor SET responsavel_id = (SELECT id FROM tb_colaborador WHERE cpf = '44444444444') WHERE nome = 'Farmácia';
UPDATE tb_setor SET email = 'diretoria@clinicaescola.exemplo', telefone = '7133330002',
                    responsavel_id = (SELECT id FROM tb_colaborador WHERE cpf = '66666666666') WHERE nome = 'Diretoria';

-- ---------------------------------------------------------------------
-- Usuários de teste (senha: senha123)
--   ana.souza    -> COLABORADOR (médica)
--   marcos.lima  -> COLABORADOR (médico)
--   carlos.dias  -> USUARIO (atendente)
--   paula.ribeiro-> USUARIO (farmácia/estoque)
--   joao.santos  -> ADMINISTRADOR + USUARIO  (demonstra acúmulo de perfis)
-- ---------------------------------------------------------------------
INSERT INTO tb_usuario (login, senha_hash, colaborador_id, criado_por)
SELECT u.login, '$2a$10$w4gaY2pD8Gkkp9f7ReiQle2FUb4zE/PVvkpIf1AsPmJg/Cq69WU1.', c.id, 'sistema'
  FROM (VALUES ('ana.souza','11111111111'), ('marcos.lima','22222222222'),
               ('carlos.dias','55555555555'), ('paula.ribeiro','44444444444'),
               ('joao.santos','66666666666')) AS u(login, cpf)
  JOIN tb_colaborador c ON c.cpf = u.cpf;

INSERT INTO tb_usuario_perfil (usuario_id, perfil_id)
SELECT u.id, p.id
  FROM (VALUES ('ana.souza','COLABORADOR'), ('marcos.lima','COLABORADOR'),
               ('carlos.dias','USUARIO'), ('paula.ribeiro','USUARIO'),
               ('joao.santos','ADMINISTRADOR'), ('joao.santos','USUARIO')) AS x(login, perfil)
  JOIN tb_usuario u ON u.login = x.login
  JOIN tb_perfil  p ON p.nome  = x.perfil;

-- ---------------------------------------------------------------------
-- Pacientes + prontuários (sempre juntos, na mesma transação)
-- ---------------------------------------------------------------------
INSERT INTO tb_paciente (nome_completo, cpf, data_nascimento, sexo, telefone, email,
                      medico_id, especialidade_id, convenio_id, numero_carteirinha, criado_por)
SELECT x.nome, x.cpf, x.nasc::date, x.sexo, x.tel, x.email,
       (SELECT id FROM tb_colaborador WHERE cpf = x.medico),
       (SELECT id FROM tb_especialidade WHERE nome = x.esp),
       (SELECT id FROM tb_convenio WHERE nome_fantasia = x.conv),
       x.cart, 'carlos.dias'
  FROM (VALUES
    ('Maria das Graças Oliveira', '70000000001', '1965-03-12', 'F', '71980000001', NULL,
     '11111111111', 'Clínica Geral', 'Saúde Bahia', 'SB-000123'),
    ('Lucas Gabriel Costa',       '70000000002', '2018-07-25', 'M', '71980000002', NULL,
     '22222222222', 'Pediatria', NULL, NULL),
    ('Fernanda Lima Rocha',       '70000000003', '1990-11-02', 'F', '71980000003', 'fernanda@exemplo.com',
     '11111111111', 'Clínica Geral', 'Vida Plena', 'VP-998877')
  ) AS x(nome, cpf, nasc, sexo, tel, email, medico, esp, conv, cart);

INSERT INTO tb_prontuario (paciente_id, numero, criado_por)
SELECT id, 'PR-' || lpad(id::text, 6, '0'), 'carlos.dias' FROM tb_paciente;

-- ---------------------------------------------------------------------
-- Agendamentos
-- ---------------------------------------------------------------------
INSERT INTO tb_agendamento (medico_id, paciente_id, data_prevista, hora_prevista, criado_por)
SELECT (SELECT id FROM tb_colaborador WHERE cpf = x.medico),
       (SELECT id FROM tb_paciente WHERE cpf = x.pac),
       x.d::date, x.h::time, 'carlos.dias'
  FROM (VALUES ('11111111111','70000000001','2026-10-19','08:00'),
               ('22222222222','70000000002','2026-10-20','14:00'),
               ('11111111111','70000000003','2026-10-21','09:30')) AS x(medico, pac, d, h);

-- ---------------------------------------------------------------------
-- Medicamentos e estoque
-- ---------------------------------------------------------------------
INSERT INTO tb_medicamento (nome, descricao, fabricante, armazenamento, unidade, estoque_minimo, criado_por) VALUES
    ('Dipirona Sódica 500 mg',  'Analgésico e antitérmico. Comprimido.',                  'EMS',        'TEMPERATURA_AMBIENTE', 'COMPRIMIDO', 10, 'paula.ribeiro'),
    ('Amoxicilina 500 mg',      'Antibiótico betalactâmico. Cápsula.',                    'Medley',     'TEMPERATURA_AMBIENTE', 'CAPSULA',    10, 'paula.ribeiro'),
    ('Insulina NPH 100 UI/mL',  'Insulina humana de ação intermediária. Frasco 10 mL.',   'Novo Nordisk','REFRIGERADO',         'FRASCO',     10, 'paula.ribeiro'),
    ('Soro Fisiológico 0,9%',   'Cloreto de sódio 0,9%, bolsa 500 mL.',                   'Fresenius',  'TEMPERATURA_AMBIENTE', 'BOLSA',      10, 'paula.ribeiro');

-- Entradas (aquisições)
INSERT INTO tb_movimentacao_estoque (medicamento_id, tipo, quantidade, lote, data_validade, data_aquisicao, criado_por)
SELECT m.id, 'ENTRADA', x.qtd, x.lote, x.val::date, x.aq::date, 'paula.ribeiro'
  FROM (VALUES ('Dipirona Sódica 500 mg', 50, 'DP2610A', '2028-06-30', '2026-09-01'),
               ('Amoxicilina 500 mg',     30, 'AM2609B', '2027-12-31', '2026-09-01'),
               ('Insulina NPH 100 UI/mL', 12, 'IN2610C', '2027-03-31', '2026-10-01'),
               ('Soro Fisiológico 0,9%', 100, 'SF2608D', '2028-01-31', '2026-08-15')) AS x(nome, qtd, lote, val, aq)
  JOIN tb_medicamento m ON m.nome = x.nome;

-- Solicitações de medicamento (RF023, RF059):
--   1) Dipirona: aprovada pela paula.ribeiro e já retirada (ENTREGUE)
--   2) Amoxicilina: PENDENTE, para demonstrar a aprovação/reprovação
--   3) Soro: APROVADA, aguardando a retirada no estoque
--   4) Insulina: REPROVADA, com motivo
INSERT INTO tb_solicitacao_medicamento (medicamento_id, solicitante_id, quantidade, justificativa, situacao,
                                        analisado_por_id, data_analise, motivo_reprovacao, criado_por)
SELECT m.id, c.id, x.qtd, x.just, x.sit,
       CASE WHEN x.sit = 'PENDENTE' THEN NULL ELSE u.id END,
       CASE WHEN x.sit = 'PENDENTE' THEN NULL ELSE now() END,
       x.motivo, x.login_sol
  FROM (VALUES
    ('Dipirona Sódica 500 mg', '11111111111', 42, 'Reposição do consultório para atendimentos da semana', 'ENTREGUE', NULL, 'ana.souza'),
    ('Amoxicilina 500 mg',     '22222222222',  2, 'Paciente pediátrico com quadro infeccioso',            'PENDENTE', NULL, 'marcos.lima'),
    ('Soro Fisiológico 0,9%',  '11111111111',  5, 'Hidratação venosa na sala de procedimentos',           'APROVADA', NULL, 'ana.souza'),
    ('Insulina NPH 100 UI/mL', '22222222222',  6, 'Estoque do consultório pediátrico',                    'REPROVADA',
     'Insulina não é armazenada no consultório; solicitar por paciente.', 'marcos.lima')
  ) AS x(nome, cpf, qtd, just, sit, motivo, login_sol)
  JOIN tb_medicamento m ON m.nome = x.nome
  JOIN tb_colaborador c ON c.cpf = x.cpf
  JOIN tb_usuario u ON u.login = 'paula.ribeiro';

-- Retirada da solicitação entregue: deixa a Dipirona com saldo 8 (abaixo do mínimo -> alerta de reposição)
INSERT INTO tb_movimentacao_estoque (medicamento_id, tipo, quantidade, solicitacao_id, solicitante_id, entregue_por_id, criado_por)
SELECT s.medicamento_id, 'SAIDA', s.quantidade, s.id, s.solicitante_id, s.analisado_por_id, 'paula.ribeiro'
  FROM tb_solicitacao_medicamento s
 WHERE s.situacao = 'ENTREGUE';

-- ---------------------------------------------------------------------
-- Um atendimento já finalizado (consulta do dia 14/10), para os relatórios.
-- Segue o fluxo: aberto, recebe a prescrição e só então é finalizado; ao
-- finalizar, o agendamento vira FINALIZADO (RN010).
-- ---------------------------------------------------------------------
INSERT INTO tb_agendamento (medico_id, paciente_id, data_prevista, hora_prevista, data_recepcao, criado_por)
SELECT med.id, p.id, '2026-10-14', '10:00', '2026-10-14 09:50', 'carlos.dias'
  FROM tb_colaborador med, tb_paciente p
 WHERE med.cpf = '11111111111' AND p.cpf = '70000000003';

INSERT INTO tb_atendimento (prontuario_id, medico_id, agendamento_id, convenio_id, tipo, etapa, data_hora_inicio,
                            sintomas, diagnostico, tratamento, criado_por)
SELECT pr.id, a.medico_id, a.id, p.convenio_id, 'CONSULTA', 'EM_ATENDIMENTO', '2026-10-14 10:05',
       'Dor de cabeça e febre há 2 dias', 'Síndrome gripal', 'Repouso, hidratação e antitérmico', 'ana.souza'
  FROM tb_agendamento a
  JOIN tb_paciente p    ON p.id = a.paciente_id
  JOIN tb_prontuario pr ON pr.paciente_id = p.id
 WHERE a.data_prevista = '2026-10-14';

INSERT INTO tb_medicacao_atendimento (atendimento_id, medicamento_id, dosagem, posologia)
SELECT at.id, m.id, '500 mg', '1 comprimido de 6/6 h se houver febre'
  FROM tb_atendimento at, tb_medicamento m
 WHERE m.nome = 'Dipirona Sódica 500 mg';

UPDATE tb_atendimento SET etapa = 'FINALIZADO', data_hora_encerramento = '2026-10-14 10:30'
 WHERE data_hora_inicio = '2026-10-14 10:05';
