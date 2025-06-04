package practice.chatstomp.member.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import practice.chatstomp.member.dto.MemberDto;
import practice.chatstomp.member.service.MemberService;

import java.util.List;

@RestController("memberController")
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{memberId}")
    public String login(@PathVariable String memberId) {
        return memberService.login(memberId).getName();
    }

    @GetMapping("")
    public List<MemberDto> getMembers() {
        return memberService.getMembers();
    }
}
