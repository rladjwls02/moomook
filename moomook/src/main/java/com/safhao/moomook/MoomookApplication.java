package com.safhao.moomook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoomookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoomookApplication.class, args);
	}

}
