package spring.board.member.dto;

import spring.board.member.domain.Member;

public record MemberResponse(
        Long id,
        String loginId,
        String nickname,
        String role,
        String status
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getRole().name(),
                member.getStatus().name()
        );
    }
}
