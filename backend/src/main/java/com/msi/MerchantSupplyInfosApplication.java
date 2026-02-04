package com.msi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.msi", "com.anji.captcha"})
public class MerchantSupplyInfosApplication {
  public static void main(String[] args) {
    SpringApplication.run(MerchantSupplyInfosApplication.class, args);
  }
}

