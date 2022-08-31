package com.ovit.agriLoan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ovit.agriLoan")
public class AgriLoanApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriLoanApplication.class, args);
    }

}
