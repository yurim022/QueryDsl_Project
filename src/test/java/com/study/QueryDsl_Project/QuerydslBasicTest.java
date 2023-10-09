package com.study.QueryDsl_Project;

import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.QueryDsl_Project.entity.Member;
import com.study.QueryDsl_Project.entity.QMember;
import com.study.QueryDsl_Project.entity.QTeam;
import com.study.QueryDsl_Project.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static com.study.QueryDsl_Project.entity.QMember.member;
import static com.study.QueryDsl_Project.entity.QTeam.team;
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
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

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


    /**
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력 (null last)
     * **/
    @Test
    public void sort() {

        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }


    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);

    }


    @Test
    public void paging2() {
        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();  //count query는 단순한데 (join안해도 되고~~), 조회는 복잡할때 (join등) fetchResults() 사용 안하고 분리해서 사용

        assertThat(fetchResults.getTotal()).isEqualTo(4);
        assertThat(fetchResults.getLimit()).isEqualTo(2);
        assertThat(fetchResults.getOffset()).isEqualTo(1);
        assertThat(fetchResults.getResults().size()).isEqualTo(2);

    }


    @Test
    public void aggregate() {

        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

        //use dto rather than tuple
    }


    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {

        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 팀A에 소속된 모든 회원
     */
    @Test
    public void join(){
        List<Member> result = queryFactory.selectFrom(member)
                .join(member.team, team) //inner join, left join, right join,,,, acceptable
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1","member2");

    }


    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {

      em.persist(new Member("teamA"));
      em.persist(new Member("teamB"));

        List<Member> result = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch(); //actually, db optimize itself

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");

    }

    /**
     * join on relation
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
//                .where(team.name.eq("teamA"))  //join() = inner join 의 경우 where과 on의 결과가 같음
                .fetch();


        //if it is inner join, rather use 'where' (because it's more general. for communication)
        //but if it is outer join, only solve problem by 'on' clause

        result.stream()
                .forEach(
                        tuple -> System.out.println("tuple = " + tuple)
                );

    }


    /**
     * join on no-relation
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL :
     */
    @Test
    public void join_on_no_relation() throws Exception {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member,team)
                .from(member)
                .leftJoin(team) // cf) relation 있는 테이블  leftjoin(member.team, team)  -> fk로 join (join in member0_.team_id=team1_.id 가 들어감)
                .on(member.username.eq(team.name))
                .fetch(); //actually, db optimize itself

        result.stream()
                .forEach(
                        tuple -> System.out.println("tuple = " + tuple)
                );
    }


    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {

        //fetch join test 시 영속성 컨텍스트 반영하고 날리고 시작
      em.flush();
      em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("fetch join not applied").isFalse();

    }



    @Test
    public void fetchJoinUse() throws Exception {

        //fetch join test 시 영속성 컨텍스트 반영하고 날리고 시작
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("fetch join not applied").isTrue();

    }


    /**
         * select oldest member
         */

    @Test
    public void subQuery() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(40);

    }


    /**
     * select members who are older than average
     */
    @Test
    public void subQueryGoe() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(30,40);

    }



    /**
     * select members who are older than 10 ...just for practice (not good eg)
     */
    @Test
    public void subQueryIn() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(20,30,40);

    }


    @Test
    public void selectSubQuery() {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = "+tuple);
        }

        //JPA 서브쿼리의 한계점으로 from 절의 서브쿼리는 지원하지 않음. Querydsl도 당연히 지원x
        //해결방안
        // 0. 빠르게 동작해야 하는 상황이 아니라면, (관리자 페이지 등) DB는 나눠서 조회하고 애플리케이션 단에서 처리
        // 1. 서브쿼리를 join으로 변경 (가능한 상황도 있고, 불가능한 상황도 있음)
        // 2. 애플리케이션에서 쿼리를 2번 분리해서 실행
        // 3. nativeSQL 사용

    }

    @Test
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = "+s);
        }

    }

    @Test
    public void complexCase() {

        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        //왠만하면 case절에 쓰지말고 application에서 처리 or 화면에서
        for (String s : result) {
            System.out.println("s = "+s);
        }

    }


    @Test
    public void constant() {

        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = "+tuple);
        }
    }

    @Test
    public void concat() {

        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue())) //stringValue() 특히 ENUM의 경우 많이 사용
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = "+s);
        }
    }

}
