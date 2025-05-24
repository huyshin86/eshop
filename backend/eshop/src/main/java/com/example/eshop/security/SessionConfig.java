package com.example.eshop.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 3600, // Session timeout: 1 hour
    tableName = "SPRING_SESSION"
)
public class SessionConfig  {
    // This class is intentionally empty. It only serves to enable JDBC session management.
    // The actual configuration is done in the @EnableJdbcHttpSession annotation.
}
