package com.event.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class EventManagerApplicationTests {

	@Autowired
	private Environment env;

	@Test
	void contextLoads() {
		System.out.println("📍 Current dir: " + System.getProperty("user.dir"));
		System.out.println("🔐 JWT_SECRET_KEY = " + env.getProperty("JWT_SECRET_KEY"));
		System.out.println("🕒 HUB_PORT = " + env.getProperty("8080"));
		System.out.println("📦 HUB_NAME = " + env.getProperty("HUB_NAME"));
		System.out.println("🐘 HUB_DATASOURCE_URL = " + env.getProperty("HUB_DATASOURCE_URL"));
	}

}
