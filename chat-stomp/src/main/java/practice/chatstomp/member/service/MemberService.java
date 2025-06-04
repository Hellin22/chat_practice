package practice.chatstomp.member.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import practice.chatstomp.member.dto.MemberDto;
import practice.chatstomp.member.entity.Member;
import practice.chatstomp.member.repository.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 멤버 로그인
    public MemberDto login(String memberId) {

        Optional<Member> member = memberRepository.findById(memberId);
        log.info("member: {}", member.get());
        return converToDto(member.orElse(null));
    }

    // 모든 멤버 조회
    public List<MemberDto> getMembers() {
        List<Member> all = memberRepository.findAll();
        return all.stream()
                .map(member -> converToDto(member))
                .collect(Collectors.toList());
    }

    public MemberDto converToDto(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setName(member.getName());

        return dto;
    }
}
