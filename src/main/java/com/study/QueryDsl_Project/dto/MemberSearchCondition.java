package com.study.QueryDsl_Project.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {

    private String username;
    private String teamName;
    private Integer ageGoe; //greater or equal
    private Integer ageLoe; //large or equal

}
