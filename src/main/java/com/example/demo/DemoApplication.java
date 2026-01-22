package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.demo.service.EmailSenderService;
import com.example.demo.service.UserService;

@SpringBootApplication
@EnableAsync
public class DemoApplication extends SpringBootServletInitializer{
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DemoApplication.class);
	}
	
	@Autowired
	private EmailSenderService senderService;
	
	@Autowired
	private UserService userService;


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
	}
		
}
	