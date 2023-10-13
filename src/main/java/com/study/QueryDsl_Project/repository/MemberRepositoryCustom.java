package com.study.QueryDsl_Project.repository;

import com.study.QueryDsl_Project.dto.MemberSearchCondition;
import com.study.QueryDsl_Project.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);
}
