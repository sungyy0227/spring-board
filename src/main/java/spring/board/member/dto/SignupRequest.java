package spring.board.member.dto;

public record SignupRequest(
        String loginId,
        String password,
        String nickname
) {
    public SignupForm toSignupForm() {
        SignupForm signupForm = new SignupForm();
        signupForm.setLoginId(loginId);
        signupForm.setPassword(password);
        signupForm.setNickname(nickname);
        return signupForm;
    }
}
