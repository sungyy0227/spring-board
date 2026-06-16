package spring.board.dto;

import spring.board.domain.Role;

public class MemberDto {
    //정규식 써야됨 어떻게쓰는진 몰?루
    private String loginId;
    private String password;
    private String nickname;
    private Role role;

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
