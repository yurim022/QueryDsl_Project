package com.study.QueryDsl_Project;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.QueryDsl_Project.entity.Hello;
import com.study.QueryDsl_Project.entity.QHello;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QueryDslProjectApplicationTests {

	@Autowired
	EntityManager em;
	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");

		Hello result = query
				.selectFrom(qHello)
				.fetchOne();

		assertThat(result).isEqualTo(hello);
	}

}

