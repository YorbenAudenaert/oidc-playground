package com.audenyo.oidcplayground.config;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.http.HttpClient;

@Configuration
public class SslConfig {

    @Bean
    public SSLContext adfsSSLContext(SslBundles sslBundles) throws Exception {
        SslBundle bundle = sslBundles.getBundle("adfs");
        TrustManager[] trustManagers = bundle.getManagers().getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);

        SSLContext.setDefault(sslContext);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        return sslContext;
    }

    @Bean
    public RestClient adfsRestClient(SSLContext adfsSSLContext) {
        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(adfsSSLContext)
                .build();
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
