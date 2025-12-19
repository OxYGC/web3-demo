package com.web3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan(basePackages = "com.web3")
@Slf4j
public class Web3DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Web3DemoApplication.class, args);
    }
}


