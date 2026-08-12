package com.dangdang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// [각주] @SpringBootApplication : "이 클래스가 스프링부트 앱의 시작점"이라는 표시입니다.
// main() 메서드를 실행하면 내장 웹서버(Tomcat)가 켜지면서 8080 포트에서 요청을 기다립니다.
@SpringBootApplication
public class DangdangServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DangdangServerApplication.class, args);
    }
}
