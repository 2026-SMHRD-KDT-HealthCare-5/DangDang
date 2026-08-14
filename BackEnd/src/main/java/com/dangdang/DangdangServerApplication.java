package com.dangdang;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

// [각주] @SpringBootApplication : "이 클래스가 스프링부트 앱의 시작점"이라는 표시입니다.
// main() 메서드를 실행하면 내장 웹서버(Tomcat)가 켜지면서 8080 포트에서 요청을 기다립니다.
@SpringBootApplication
public class DangdangServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DangdangServerApplication.class, args);
    }

    /**
     * [각주 AJ] 백엔드 가이드 8.5절 — 서버(이 PC/컨테이너)의 기본 타임존이 KST가 아닐 수 있어서,
     * JVM이 "지금 몇 시인지" 계산하는 기준 자체를 앱 시작 시점에 명시적으로 Asia/Seoul로 고정합니다.
     * 이게 없으면 LocalDateTime.now()(예: AiChat.onCreate())가 서버 OS 타임존 기준으로 찍혀서,
     * 자정 근처 시각이 하루 단위 이력 조회(GET /api/chat/history)에서 엉뚱한 날짜로 잡힐 수 있습니다.
     * @PostConstruct : 스프링이 이 빈(클래스)을 다 만든 직후 딱 한 번 자동 실행해주는 콜백입니다.
     */
    @PostConstruct
    public void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
