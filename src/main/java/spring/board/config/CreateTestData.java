package spring.board.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import spring.board.domain.Post;
import spring.board.repository.PostRepository;

import java.time.LocalDateTime;

@Component
public class CreateTestData implements CommandLineRunner {
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    CreateTestData(PostRepository postRepository, PasswordEncoder passwordEncoder){
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (postRepository.count() > 300) {
            return;
        }

        String[] name = {"민준", "서준", "도윤", "예준", "시우", "하준", "지호", "주원"
                ,"지민", "서연", "하윤", "지아", "수아", "유나", "채원"};
        for (int i=0; i<300; i++){
            Post post = new Post();
            post.setGuestPassword(passwordEncoder.encode("qwer1234"));
            post.setContent("테스트 게시물 " + (i + 1) + " 번 입니다.");
            post.setPoster(name[i%15]);
            post.setTitle("테스트 게시물 " + (i + 1) + " 번 입니다.");
            post.setViewCount(0);
            post.setCreatedAt(LocalDateTime.now().minusMinutes(i));
            postRepository.save(post);
        }
    }
}
