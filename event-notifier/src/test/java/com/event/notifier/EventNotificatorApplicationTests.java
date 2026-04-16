package com.event.notifier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
public class EventNotificatorApplicationTests {

	@Autowired
	private Environment env;

	@Test
	void contextLoads() {
		System.out.println("📍 Current dir: " + System.getProperty("user.dir"));
		System.out.println("🔐 JWT_SECRET_KEY = " + env.getProperty("JWT_SECRET_KEY"));
		System.out.println("🕒 NOTIFY_PORT = " + env.getProperty("NOTIFY_PORT"));
		System.out.println("🐘 NOTIFY_DATASOURCE_URL = " + env.getProperty("NOTIFY_DATASOURCE_URL"));
	}
}
