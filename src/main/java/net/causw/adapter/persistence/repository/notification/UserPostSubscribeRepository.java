//package net.causw.adapter.persistence.repository.notification;
//
//import net.causw.adapter.persistence.notification.UserPostSubscribe;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserPostSubscribeRepository extends JpaRepository<UserPostSubscribe ,String> {
//    Optional<UserPostSubscribe> findByUser_IdAndBoard_Id(String userId, String boardId);
//
//    List<UserPostSubscribe> findByBoard_Id(String boardId);
//}
