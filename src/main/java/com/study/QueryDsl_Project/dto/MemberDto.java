package com.study.QueryDsl_Project.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection //dto to Qfile
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

}
