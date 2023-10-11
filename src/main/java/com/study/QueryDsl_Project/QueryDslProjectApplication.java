package com.study.QueryDsl_Project;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class QueryDslProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryDslProjectApplication.class, args);
	}

	@Bean
	JPAQueryFactory jpaQueryFactory(EntityManager em){
		return new JPAQueryFactory(em);
	}  //jpa 동시성 문제 없음!!! EntityManager에 의존한는데, 이는 transaction 단위로 바인딩되서 라우팅됨.

}
