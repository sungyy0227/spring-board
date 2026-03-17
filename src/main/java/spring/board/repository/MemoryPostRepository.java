//package spring.board.repository;
//
//import org.springframework.stereotype.Repository;
//import spring.board.domain.Post;
//
//import java.util.*;
//
//@Repository
//public class MemoryPostRepository implements PostRepository{
//
//    private static long sequence=0L;
//    private static Map<Long,Post> store=new HashMap<>();
//
//    @Override
//    public Post save(Post post){
//        post.setId(++sequence);
//        store.put(post.getId(),post);
//        return post;
//    }
//    @Override
//    public void deleteById(long id){
//        store.remove(id);
//    }
//    @Override
//    public Optional<Post> findById(long id){
//        return Optional.ofNullable(store.get(id));
//    }
//
//    @Override
//    public List<Post> findAll() {
//        return new ArrayList<>(store.values());
//    }
//
//    @Override
//    public void clear() {
//        store.clear();
//        sequence=0L;
//    }
//
//    @Override
//    public void update(Post post) {
//        store.put(post.getId(),post);
//    }
//
//
//}
