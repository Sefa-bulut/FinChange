package com.example.finchange.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Ana sınıftan kopyaladığımız kodun aynısı buraya taşındı.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);    // Bağlantı zaman aşımı: 5 saniye
        factory.setReadTimeout(15000);      // Okuma zaman aşımı: 15 saniye
        factory.setProxy(Proxy.NO_PROXY);   // Proxy kullanımını devre dışı bırak

        return new RestTemplate(factory);
    }
}