package com.RentKaro.RentKaro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class RentKaroApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentKaroApplication.class, args);
	}

}

