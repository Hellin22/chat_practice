package com.song.chatpractice.member.service;

import com.song.chatpractice.member.dto.MemberDto;
import com.song.chatpractice.member.entity.Member;
import com.song.chatpractice.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public String getMemberNameById(String memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();

        return member.getName();
    }

    @Override
    public List<MemberDto> getAllMembers() {

        return memberRepository.findAll().stream()
                                         .map(this::convertEntityToDto)
                                         .collect(Collectors.toList());
    }

    // ModelMapper 대신 Entity를 Dto로 변환해주는 메소드
    private MemberDto convertEntityToDto(Member member){
        MemberDto memberDto = new MemberDto();
        memberDto.setId(member.getId());
        memberDto.setName(member.getName());

        return memberDto;
    }
}
