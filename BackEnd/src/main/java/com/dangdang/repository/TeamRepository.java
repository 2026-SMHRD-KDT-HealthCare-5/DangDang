package com.dangdang.repository;

import com.dangdang.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * [각주 EC] team 테이블 CRUD.
 *
 * findByTeamNameContaining : "팀 검색/목록 조회"(GET /api/teams?keyword=)에서 씁니다.
 * keyword가 없으면(빈 문자열) 전체 팀이 다 걸리도록 서비스에서 keyword를 ""로 기본 처리합니다.
 *
 * @lastModified 2026-08-20
 */
public interface TeamRepository extends JpaRepository<Team, Integer> {
    List<Team> findByTeamNameContaining(String keyword);

    boolean existsByTeamName(String teamName);
}
