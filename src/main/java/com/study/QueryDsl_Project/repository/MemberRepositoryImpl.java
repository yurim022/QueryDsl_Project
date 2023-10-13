package com.study.QueryDsl_Project.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.QueryDsl_Project.dto.MemberSearchCondition;
import com.study.QueryDsl_Project.dto.MemberTeamDto;
import com.study.QueryDsl_Project.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static com.study.QueryDsl_Project.entity.QMember.member;
import static com.study.QueryDsl_Project.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("tamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null ;    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null ;
    }
}
