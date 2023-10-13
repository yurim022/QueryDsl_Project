package com.study.QueryDsl_Project.repository;

import com.study.QueryDsl_Project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> , MemberRepositoryCustom {

    List<Member> findByUsername(String username);

}
