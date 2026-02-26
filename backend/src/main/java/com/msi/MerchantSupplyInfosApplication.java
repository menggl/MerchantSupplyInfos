package com.msi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MerchantSupplyInfosApplication {
  public static void main(String[] args) {
    SpringApplication.run(MerchantSupplyInfosApplication.class, args);
  }
}

