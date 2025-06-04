package com.song.chatpractice.member.controller;

import com.song.chatpractice.member.dto.MemberDto;
import com.song.chatpractice.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public String getMemberName(@PathVariable String memberId){
        return memberService.getMemberNameById(memberId);
    }

    @GetMapping("")
    public List<MemberDto> getAllMembers(){
        return memberService.getAllMembers();
    }
}
