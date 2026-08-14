package com.dangdang.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [각주 A] "엔티티(Entity)"란?
 * DB 테이블 한 줄(row)을 자바 객체 하나로 대응시킨 클래스입니다.
 * 이렇게 매핑해두면 SQL문을 직접 안 쓰고도 자바 코드로 DB를 다룰 수 있는데,
 * 이 기술을 ORM(Object-Relational Mapping)이라 하고, 스프링에서는 JPA/Hibernate가 담당합니다.
 * 즉 이 클래스는 users 테이블과 1:1로 연결됩니다. (컬럼 정의는 V1__create_users_table.sql 참고)
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요하지만, 외부에서 함부로 new User()로
                                                     // 빈 객체를 만들지 못하도록 접근을 제한합니다.
public class User {

    @Id // 기본키(Primary Key) 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB의 SERIAL(자동증가)에 값 생성을 위임
    @Column(name = "user_no")
    private Integer userNo;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // [각주] 여기 저장되는 값은 원문 비밀번호가 아니라 BCrypt로 암호화된 "해시값"입니다.
    // AuthService에서 암호화 후 넘겨줍니다.
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(length = 10)
    private String gender;

    @Column(precision = 4, scale = 2)
    private BigDecimal hba1c; // 당화혈색소

    // 1=거의 안함 / 2=주 1~2회 / 3=주 3~5회 / 4=매일 — ActivityLevel enum 참고
    @Column(name = "activity_level")
    private Integer activityLevel;

    // "건강군" / "전당뇨" / "2형당뇨" — DiagnosisGroup enum 참고. FastAPI 호출 시 그대로 전달됨.
    @Column(name = "diagnosis_group", length = 10)
    private String diagnosisGroup;

    @Column(name = "target_glucose")
    private Integer targetGlucose; // 식후 2시간 목표 혈당 (mg/dL, 정수)

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;

    @Builder
    private User(String email, String password, String nickname, String gender,
                 LocalDate birthDate, BigDecimal height, BigDecimal weight,
                 BigDecimal hba1c, Integer activityLevel, String diagnosisGroup,
                 Integer targetGlucose) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.hba1c = hba1c;
        this.activityLevel = activityLevel;
        this.diagnosisGroup = diagnosisGroup;
        this.targetGlucose = targetGlucose;
        this.notificationEnabled = true; // 기획서 3.1: 알림 설정 기본값은 ON
    }

    // [각주] @PrePersist : "DB에 INSERT 되기 직전에 자동 실행"되는 JPA 콜백입니다.
    // joined_at 컬럼은 DB에도 DEFAULT now()가 있지만, JPA로 저장하면 값이 없는 채로 INSERT를
    // 시도하므로 자바 쪽에서도 명시적으로 채워줍니다.
    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}
