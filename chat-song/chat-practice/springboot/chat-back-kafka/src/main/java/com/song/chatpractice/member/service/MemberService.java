package com.song.chatpractice.member.service;

import com.song.chatpractice.member.dto.MemberDto;

import java.util.List;

public interface MemberService {

    String getMemberNameById(String memberId);

    List<MemberDto> getAllMembers();
}
