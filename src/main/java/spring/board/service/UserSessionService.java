package spring.board.service;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import spring.board.security.CustomUserDetails;

@Service
public class UserSessionService {
    private final SessionRegistry sessionRegistry;

    public UserSessionService(SessionRegistry sessionRegistry){
        this.sessionRegistry = sessionRegistry;
    }

    public void expireUserSessions(Long memberId){
        for (Object principal : sessionRegistry.getAllPrincipals()){
            if(principal instanceof CustomUserDetails userDetails
                && memberId.equals(userDetails.getId())){
                for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(principal,false)){
                    sessionInformation.expireNow();
                }
            }
        }
    }
}
