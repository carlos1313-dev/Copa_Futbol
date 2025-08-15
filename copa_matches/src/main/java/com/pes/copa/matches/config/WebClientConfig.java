package com.pes.copa.matches.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Configuración de WebClients para comunicación con otros microservicios
 * @author sangr
 */
@Configuration
public class WebClientConfig {
    
    @Value("${services.teams.url:http://localhost:8081}")
    private String teamsServiceUrl;
    
    @Value("${services.auth.url:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${services.stats.url:http://localhost:8084}")
    private String statsServiceUrl;
    
    /**
     * WebClient para Teams Service
     * @return cliente configurado para Teams Service
     */
    @Bean
    public WebClient teamsWebClient() {
        return createWebClient(teamsServiceUrl);
    }
    
    /**
     * WebClient para Auth Service
     * @return cliente configurado para Auth Service
     */
    @Bean
    public WebClient authWebClient() {
        return createWebClient(authServiceUrl);
    }
    
    /**
     * WebClient para Stats Service
     * @return cliente configurado para Stats Service
     */
    @Bean
    public WebClient statsWebClient() {
        return createWebClient(statsServiceUrl);
    }
    
    /**
     * Crea un WebClient con configuración base común
     * @param baseUrl URL base del servicio
     * @return WebClient configurado
     */
    private WebClient createWebClient(String baseUrl) {
        // Configurar HttpClient con timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));
        
        // Configurar estrategias de intercambio para manejar respuestas grandes
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().maxInMemorySize(1 * 1024 * 1024); // 1MB
                })
                .build();
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}