package com.example.eshop.config;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;

import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class PayPalConfig {
    @Value("${PAYPAL_CLIENT_ID}")
    private String PAYPAL_CLIENT_ID;

    @Value("${PAYPAL_CLIENT_SECRET}")
    private String PAYPAL_CLIENT_SECRET;

    @Value("${PAYPAL_MODE}")
    private String PAYPAL_MODE;

    @Bean
    public PaypalServerSdkClient paypalClient() {
        Environment environment;
        if ("production".equalsIgnoreCase(PAYPAL_MODE)) {
            environment = Environment.PRODUCTION;
        } else {
            environment = Environment.SANDBOX;
        }

        return new PaypalServerSdkClient.Builder()
                .loggingConfig(builder -> builder
                        .level(Level.DEBUG)
                        .requestConfig(logConfigBuilder -> logConfigBuilder.body(true))
                        .responseConfig(logConfigBuilder -> logConfigBuilder.headers(true)))
                .httpClientConfig(configBuilder -> configBuilder
                        .timeout(0))
                .environment(environment)
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(
                                PAYPAL_CLIENT_ID,
                                PAYPAL_CLIENT_SECRET
                        ).build()
                )
                .build();
    }
}