package spring.board.dto;

import spring.board.domain.Role;

public class MemberSearchDto {
    private Long id;
    private String loginId;
    private String nickname;
    private Role role;

    public MemberSearchDto(Long id, String loginId, String nickname, Role role) {
        this.id = id;
        this.loginId = loginId;
        this.nickname = nickname;
        this.role = role;
    }

    public MemberSearchDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
