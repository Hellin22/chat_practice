package practice.chatstomp.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.chatstomp.member.entity.Member;


@Repository("memberRepository")
public interface MemberRepository extends JpaRepository<Member, String> {
}
