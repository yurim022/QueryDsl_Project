package com.study.QueryDsl_Project;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.QueryDsl_Project.entity.Member;
import com.study.QueryDsl_Project.entity.QMember;
import com.study.QueryDsl_Project.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.study.QueryDsl_Project.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em); //multi-thread 환경에서 동시성 문제없이 동작. 알아서 각 transaction에 맞게 분배해줌.

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);

        Member member3 = new Member("member3", 10, teamB);
        Member member4 = new Member("member4", 10, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }


    @Test
    public void startJQPL() {

        //member1을 찾기
        Member findMember = em.createQuery("select m from Member m " +
                        "where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        //런타임 오류 발생 가능
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
//        QMember m = new QMember("m");
//        QMember m = QMember.member;  //static import

        Member findMember = queryFactory.select(member)
                .from(member)
                .where(member.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        // 1. 컴파일 시에 오류 잡을 수 있음
        // 2. 파라미터 바인딩 자동으로 해결해줌 (성능 상의 이점 존재)
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }


    @Test
    public void search() {

        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.between(10, 30),
                        null
                ) //and의 경우 , 로 chain 연결 할 수 있음.  + null은 무시해서 동적쿼리할때 유리함
                .fetchOne();


        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    @Test
    public void resultFetch() {

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne(); // 결과값이 1개 이상이면 NonUniqueResultException


        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        List<Member> content = fetchResults.getResults(); //성능이 중요한 쿼리에서는 비추
        long total = fetchResults.getTotal();

        long count = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

}
