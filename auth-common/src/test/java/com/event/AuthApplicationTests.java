package com.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthApplicationTests {

	@Autowired
	private Environment env;

	@Test
	void contextLoads() {
		// Проверяем ключевые свойства, которые должны быть доступны
		assertThat(env.getProperty("AUTH_NAME")).isEqualTo("auth-common");
		assertThat(env.getProperty("AUTH_PORT")).isEqualTo("8082");

		// БД
		assertThat(env.getProperty("AUTH_DATASOURCE_URL")).contains("authDB");
		assertThat(env.getProperty("AUTH_POSTGRES_USER")).isEqualTo("root");
		assertThat(env.getProperty("AUTH_POSTGRES_PASSWORD")).isEqualTo("root");

		// Redis
		assertThat(env.getProperty("AUTH_REDIS_URL")).startsWith("redis://");

		// JWT
		assertThat(env.getProperty("JWT_SECRET_KEY")).isNotNull().hasSize(43); // base64 строка длиной 43
		assertThat(env.getProperty("JWT_LIFETIME")).isEqualTo("720");
		assertThat(env.getProperty("JWT_REFRESH_EXPIRATION_MIN")).isEqualTo("1440");
	}
}
