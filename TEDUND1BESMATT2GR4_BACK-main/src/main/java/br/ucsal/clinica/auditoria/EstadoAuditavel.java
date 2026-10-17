package br.ucsal.clinica.auditoria;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.Table;

/**
 * Fotografa o estado de uma entidade JPA para a auditoria registrar o "delta de
 * modificacao do estado" (secao 3.4.3).
 *
 * Cuidado com dados sensiveis (secao 6.6 e LGPD): os valores so aparecem para os
 * campos da lista VALORES_VISIVEIS (status, situacao, nomes...). Para os demais
 * (dados clinicos, CPF, contatos, senha) o registro informa apenas QUAL campo mudou.
 */
public final class EstadoAuditavel {

    /** Campos que nao entram na comparacao (controle tecnico). */
    private static final Set<String> IGNORADOS = Set.of("id", "versao", "dataCadastro", "criadoPor",
            "atualizadoPor", "dataAtualizacao", "estadoAnterior", "ultimoLogin");

    /** Campos cujo valor pode aparecer no log. */
    private static final Set<String> VALORES_VISIVEIS = Set.of("status", "motivoInativacao", "situacao", "etapa",
            "internado", "quantidade", "motivoReprovacao", "nome", "nomeFantasia", "razaoSocial", "nomeCompleto",
            "login", "funcao", "perfis", "dataPrevista", "horaPrevista", "estoqueMinimo", "tipo", "clinica", "setor",
            "responsavel", "medico", "colaborador", "especialidade", "convenio", "paciente", "medicamento",
            "solicitante", "analisadoPor", "dataRecepcao", "dataHoraEncerramento", "turnos", "unidade");

    private EstadoAuditavel() {
    }

    /** Nome da entidade na auditoria: o nome da tabela sem o prefixo tb_. */
    public static String nomeEntidade(Object entidade) {
        Class<?> classe = classeReal(entidade);
        Table tabela = classe.getAnnotation(Table.class);
        String nome = tabela != null && !tabela.name().isBlank() ? tabela.name() : classe.getSimpleName();
        return nome.startsWith("tb_") ? nome.substring(3) : nome;
    }

    public static Long id(Object entidade) {
        Object valor = ler(entidade, "id");
        return valor instanceof Long l ? l : null;
    }

    public static String rotulo(Object entidade) {
        return entidade instanceof Rotulavel r ? r.rotuloAuditoria() : null;
    }

    /** Estado atual da entidade: nome do campo -> valor em texto. */
    public static Map<String, String> de(Object entidade) {
        Map<String, String> estado = new LinkedHashMap<>();
        for (Field campo : campos(classeReal(entidade))) {
            if (IGNORADOS.contains(campo.getName())) {
                continue;
            }
            Object valor;
            try {
                campo.setAccessible(true);
                valor = campo.get(entidade);
            } catch (ReflectiveOperationException | RuntimeException e) {
                continue;
            }
            if (valor instanceof Collection<?> && !Hibernate.isInitialized(valor)) {
                continue; // colecao nao carregada: nao foi alterada nesta operacao
            }
            estado.put(campo.getName(), texto(valor));
        }
        return estado;
    }

    /** Lista do que mudou, ja respeitando quais valores podem aparecer no log. */
    public static List<String> diferencas(Map<String, String> antes, Map<String, String> depois) {
        List<String> mudancas = new ArrayList<>();
        Set<String> chaves = new LinkedHashSet<>(antes.keySet());
        chaves.addAll(depois.keySet());
        for (String chave : chaves) {
            if (!antes.containsKey(chave) || !depois.containsKey(chave)) {
                continue;
            }
            String a = antes.get(chave);
            String d = depois.get(chave);
            if (Objects.equals(a, d)) {
                continue;
            }
            mudancas.add(VALORES_VISIVEIS.contains(chave)
                    ? chave + ": " + vazio(a) + " -> " + vazio(d)
                    : chave);
        }
        return mudancas;
    }

    private static String vazio(String valor) {
        return valor == null || valor.isEmpty() ? "(vazio)" : valor;
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Collection<?> colecao) {
            return colecao.stream().map(EstadoAuditavel::textoItem).sorted().toList().toString();
        }
        return textoItem(valor);
    }

    private static String textoItem(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof HibernateProxy proxy) {
            if (!Hibernate.isInitialized(proxy)) {
                return "#" + proxy.getHibernateLazyInitializer().getIdentifier();
            }
            valor = proxy.getHibernateLazyInitializer().getImplementation();
        }
        if (valor instanceof Rotulavel r) {
            Long id = id(valor);
            return id != null ? r.rotuloAuditoria() + " (#" + id + ")" : r.rotuloAuditoria();
        }
        if (valor.getClass().getAnnotation(jakarta.persistence.Entity.class) != null) {
            return "#" + id(valor);
        }
        return String.valueOf(valor);
    }

    private static Object ler(Object entidade, String nomeCampo) {
        for (Field campo : campos(classeReal(entidade))) {
            if (campo.getName().equals(nomeCampo)) {
                try {
                    campo.setAccessible(true);
                    return campo.get(entidade);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /** Classe da entidade, mesmo quando o objeto e um proxy do Hibernate. */
    private static Class<?> classeReal(Object entidade) {
        if (entidade instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return entidade.getClass();
    }

    private static List<Field> campos(Class<?> classe) {
        List<Field> campos = new ArrayList<>();
        for (Class<?> c = classe; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
                    campos.add(f);
                }
            }
        }
        return campos;
    }
}
