-- Testes das regras garantidas pelo banco. Rode num banco de TESTE, depois das migracoes,
-- conectado com o usuario da aplicacao (clinica_app), para testar tambem as permissoes (RNF018):
--   psql -U clinica_app -d clinica -f testes-banco.sql
-- Cada bloco mostra uma regra sendo aplicada; os que "devem falhar" mostram ERROR.
\pset pager off
\echo '--- 1. Saldo e alerta de reposicao (RF057, RN015: minimo de 10 unidades)'
SELECT nome, saldo, estoque_minimo FROM vw_estoque_saldo ORDER BY nome;
SELECT nome, saldo FROM vw_estoque_reposicao;
\echo '--- 2. Perfis acumulados (RN001) e usuario sempre com colaborador (RN037)'
SELECT u.login, c.nome_completo, string_agg(p.nome, ', ' ORDER BY p.nome) perfis
  FROM tb_usuario u JOIN tb_colaborador c ON c.id = u.colaborador_id
  JOIN tb_usuario_perfil up ON up.usuario_id = u.id JOIN tb_perfil p ON p.id = up.perfil_id
 GROUP BY u.login, c.nome_completo ORDER BY 1;
\echo '--- 3. Usuario sem colaborador deve falhar (RN037)'
INSERT INTO tb_usuario (login, senha_hash) VALUES ('sem.colaborador', '$2a$10$abc');
\echo '--- 4. DELETE em tabela de dominio deve falhar (RN002 / sem permissao)'
DELETE FROM tb_setor WHERE nome = 'Farmácia';
\echo '--- 5. Inativar sem motivo deve falhar (RN023)'
UPDATE tb_convenio SET status = 'INATIVO' WHERE registro_ans = '412345';
\echo '--- 6. Inativar com motivo deve funcionar'
UPDATE tb_convenio SET status = 'INATIVO', motivo_inativacao = 'Contrato encerrado' WHERE registro_ans = '412345';
SELECT nome_fantasia, status, motivo_inativacao, data_inativacao IS NOT NULL AS tem_data FROM tb_convenio;
\echo '--- 7. Paciente sem prontuario deve falhar (RN009)'
INSERT INTO tb_paciente (nome_completo, data_nascimento) VALUES ('Sem Prontuario', '2000-01-01');
\echo '--- 8. Segundo prontuario para o mesmo paciente deve falhar (RN007)'
INSERT INTO tb_prontuario (paciente_id, numero) SELECT id, 'PR-X' FROM tb_paciente LIMIT 1;
\echo '--- 9. Retirada maior que o saldo deve falhar (RN032)'
INSERT INTO tb_movimentacao_estoque (medicamento_id, tipo, quantidade, solicitante_id, entregue_por_id)
SELECT m.id, 'SAIDA', 100, (SELECT id FROM tb_colaborador LIMIT 1), (SELECT id FROM tb_usuario LIMIT 1)
  FROM tb_medicamento m WHERE nome LIKE 'Dipirona%';
\echo '--- 10. Segunda retirada para a mesma solicitacao deve falhar'
INSERT INTO tb_movimentacao_estoque (medicamento_id, tipo, quantidade, solicitacao_id, solicitante_id, entregue_por_id)
SELECT medicamento_id, 'SAIDA', 1, solicitacao_id, solicitante_id, entregue_por_id FROM tb_movimentacao_estoque WHERE solicitacao_id IS NOT NULL;
\echo '--- 11. Medico sem CRM deve falhar (RN024)'
INSERT INTO tb_colaborador (nome_completo, cpf, email, funcao, setor_id, hora_inicio, hora_fim, carga_horaria_semanal)
VALUES ('Dr Sem CRM', '99999999999', 'x@x', 'MEDICO', 1, '08:00', '12:00', 20);
\echo '--- 12. Horario duplicado do medico deve falhar'
INSERT INTO tb_agendamento (medico_id, paciente_id, data_prevista, hora_prevista)
SELECT medico_id, paciente_id, data_prevista, hora_prevista FROM tb_agendamento LIMIT 1;
\echo '--- 13. Finalizar o atendimento finaliza o agendamento (RN010)'
INSERT INTO tb_atendimento (prontuario_id, medico_id, agendamento_id, tipo, etapa, sintomas, diagnostico, tratamento, data_hora_encerramento)
SELECT pr.id, a.medico_id, a.id, 'CONSULTA', 'FINALIZADO', 'Dor de cabeca ha 3 dias', 'Cefaleia tensional', 'Repouso', now() + interval '30 min'
  FROM tb_agendamento a JOIN tb_prontuario pr ON pr.paciente_id = a.paciente_id WHERE a.hora_prevista = '08:00';
SELECT a.data_prevista, a.hora_prevista, a.situacao FROM tb_agendamento a ORDER BY 1, 2;
\echo '--- 14. Etapa FINALIZADO sem data de encerramento deve falhar'
UPDATE tb_atendimento SET etapa = 'FINALIZADO' WHERE data_hora_encerramento IS NULL;
INSERT INTO tb_atendimento (prontuario_id, medico_id, tipo, etapa, sintomas)
SELECT pr.id, (SELECT id FROM tb_colaborador WHERE cpf = '11111111111'), 'URGENCIA', 'FINALIZADO', 'Teste' FROM tb_prontuario pr LIMIT 1;
\echo '--- 15. Reprovar solicitacao sem motivo deve falhar (RN033)'
UPDATE tb_solicitacao_medicamento SET situacao = 'REPROVADA', analisado_por_id = 1, data_analise = now() WHERE situacao = 'PENDENTE';
\echo '--- 16. Alterar movimentacao deve falhar (sem permissao de UPDATE)'
UPDATE tb_movimentacao_estoque SET quantidade = 1 WHERE id = 1;
\echo '--- 17. Alterar ou excluir auditoria deve falhar (RN036)'
INSERT INTO tb_auditoria (login, acao, descricao) VALUES ('teste', 'CONSULTAR', 'teste de insercao');
UPDATE tb_auditoria SET descricao = 'adulterado';
DELETE FROM tb_auditoria;
\echo '--- 18. Usuario da aplicacao nao cria tabela (RNF018)'
CREATE TABLE tb_invasora (id int);
\echo '--- 19. Logout: token revogado pode ser incluido e os expirados excluidos'
INSERT INTO tb_token_revogado (jti, usuario_id, data_expiracao) VALUES ('jti-teste', 1, now() - interval '1 min');
DELETE FROM tb_token_revogado WHERE data_expiracao < now();
\echo '--- 20. Atendimento finalizado nao muda o registro clinico (historico imutavel)'
UPDATE tb_atendimento SET diagnostico = 'alterado' WHERE etapa = 'FINALIZADO';
\echo '--- 21. Nao inclui medicamento em atendimento finalizado'
INSERT INTO tb_medicacao_atendimento (atendimento_id, medicamento_id, dosagem)
SELECT a.id, (SELECT id FROM tb_medicamento WHERE nome LIKE 'Amoxicilina%'), '500 mg' FROM tb_atendimento a WHERE etapa = 'FINALIZADO' LIMIT 1;
\echo '--- 22. Inativar atendimento finalizado (so status) e permitido'
UPDATE tb_atendimento SET status = 'INATIVO', motivo_inativacao = 'Registrado no paciente errado' WHERE etapa = 'FINALIZADO' AND data_hora_inicio = '2026-10-14 10:05';
