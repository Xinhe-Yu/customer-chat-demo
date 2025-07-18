package com.ycyw.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ChatApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();
    System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
    System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
    SpringApplication.run(ChatApplication.class, args);
  }

  // @Bean
  // CommandLineRunner runner(BCryptPasswordEncoder encoder) {
  // return args -> {
  // System.out.println("Encoded password: " + encoder.encode("12345678"));
  // };
  // }
}
