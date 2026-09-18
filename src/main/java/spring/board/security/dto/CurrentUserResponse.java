package spring.board.security.dto;

import spring.board.security.CustomUserDetails;

public record CurrentUserResponse(
        boolean authenticated,
        Long id,
        String loginId,
        String nickname,
        String role,
        boolean admin
) {
    public static CurrentUserResponse anonymous() {
        return new CurrentUserResponse(false, null, null, null, null, false);
    }

    public static CurrentUserResponse from(CustomUserDetails userDetails) {
        return new CurrentUserResponse(
                true,
                userDetails.getId(),
                userDetails.getLoginId(),
                userDetails.getNickname(),
                userDetails.getRole().name(),
                userDetails.isAdmin()
        );
    }
}
