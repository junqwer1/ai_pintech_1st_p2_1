package org.koreait.member.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.koreait.global.libs.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final Utils utils;

    @GetMapping("/login")
    public String login(@ModelAttribute RequestLogin form) {

        return utils.tpl("member/login");
    }

    @PostMapping("/login")
    public String loginPs(@Valid RequestLogin form, Errors errors) {

        if (errors.hasErrors()) {
            return utils.tpl("member/login");
        }

        // 로그인 처리

        /*
        * 로그인 완료 후 페이지 이동
        * 1) redirectUrl 값이 전달된 경우는 해당 경로로 이동
        * 2) 없는 경우는 메이 페이지로 이동
        * */
        String redirectUrl = form.getRedirectUrl();
        redirectUrl = StringUtils.hasText(redirectUrl) ? redirectUrl : "/";


        return "redirect: " + redirectUrl;
    }

    /*
    * 회원가입 약관 동의
    *
    * */
    @GetMapping("/agree")
    public String joinAgree(@ModelAttribute RequestLogin form) {

        return utils.tpl("member/agree");
    }

    /*
    * 회원가입 양식 페이지
    * - 필수 약관 동의 여부 검증
    *
    * */
    @PostMapping("/join")
    public String join(@Valid RequestJoin form, Errors errors){

        if (errors.hasErrors()) { // 약관 동의를 하지 않았다면 약관 동의 화면을 출력
            return utils.tpl("member/agree");
        }

        return utils.tpl("member/join");
    }

    /*
    * 회원가입 처리
    *
    * */
    @PostMapping("/join_ps")
    public String joinPs(@Valid RequestJoin form, Errors errors) {

        if (errors.hasErrors()) {
            return utils.tpl("member/join");
        }

//        회원가입 처리 완료 후 - 로그인 페이지로 이동
        return "redirect:/member/login";
    }
}
