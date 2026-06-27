package spring.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.board.domain.Post;
import spring.board.repository.PostRepository;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostViewCountConcurrencyTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Test
    @DisplayName("동시 요청에서도 조회수가 정확히 증가한다")
    void viewCountIncreasesExactlyUnderConcurrentRequests() throws InterruptedException {
        int requestCount = 100_000;
        int threadCount = 100;

        Post post = new Post();
        post.setTitle("view count concurrency test");
        post.setContent("content");
        post.setPoster("tester");
        Post savedPost = postRepository.saveAndFlush(post);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);
        Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    postService.getPostAndIncreaseViewCount(savedPost.getId());
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(2, TimeUnit.MINUTES);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(completed).isTrue();
        assertThat(failures).isEmpty();
        assertThat(foundPost.getViewCount()).isEqualTo(requestCount);
    }
}
