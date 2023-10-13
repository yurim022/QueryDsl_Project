package com.study.QueryDsl_Project.repository;

import com.study.QueryDsl_Project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> , MemberRepositoryCustom , QuerydslPredicateExecutor<Member> {

    List<Member> findByUsername(String username);

}
