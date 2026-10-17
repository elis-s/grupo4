package br.ucsal.clinica.compartilhado.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Os casos de uso (pacotes "aplicacao") sao Java puro, sem anotacoes do Spring
 * (secao 3.1.3 - "Frameworks sao Detalhes"). Esta configuracao, que fica na
 * infraestrutura, registra cada um deles como bean pelo nome do pacote, e o Spring
 * injeta pelo construtor as implementacoes das portas (adaptadores).
 *
 * Convencao: no pacote "aplicacao" so ficam as classes de caso de uso; os
 * comandos e resultados de cada caso de uso sao records internos a ele.
 */
@Configuration
@ComponentScan(basePackages = "br.ucsal.clinica", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "br\\.ucsal\\.clinica\\.\\w+\\.aplicacao\\.\\w+"))
public class CasosDeUsoConfig {
}
