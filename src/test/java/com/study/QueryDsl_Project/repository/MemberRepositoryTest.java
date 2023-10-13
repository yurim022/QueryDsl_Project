package com.study.QueryDsl_Project.repository;

import com.study.QueryDsl_Project.dto.MemberSearchCondition;
import com.study.QueryDsl_Project.dto.MemberTeamDto;
import com.study.QueryDsl_Project.entity.Member;
import com.study.QueryDsl_Project.entity.QMember;
import com.study.QueryDsl_Project.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.study.QueryDsl_Project.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1",10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }



    @Test
    public void searchTest() {
        prepareData();

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member4");

    }




    @Test
    public void searchPageSimple() {
        prepareData();

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0,3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition , pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1","member2","member3");
    }

    private void prepareData() {
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
    public void querydslPredicateExecutorTest() {

        prepareData();

        Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member3")));
        for (Member member1 : result) {
            System.out.println("member = " + member1);
        }

        // QuerydslPredicateExecutor<Member> 단점
        // 1. join 불가능
        // 2. 서비스 계층에서 QueryDsl 에 의존하게 됨
        // 3. 복잡한 실무환경에서 사용하기에는 한계가 명확
        // cf. Pageable , Sort 지원
    }

}