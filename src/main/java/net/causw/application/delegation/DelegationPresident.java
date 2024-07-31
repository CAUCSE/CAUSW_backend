//package net.causw.application.delegation;
//
//import net.causw.adapter.persistence.repository.UserRepository;
//import net.causw.adapter.persistence.user.User;
//import net.causw.application.spi.UserPort;
//import net.causw.domain.exceptions.ErrorCode;
//import net.causw.domain.exceptions.InternalServerException;
//import net.causw.domain.model.enums.Role;
//import net.causw.domain.model.enums.UserState;
//import net.causw.domain.model.user.UserDomainModel;
//import net.causw.domain.model.util.MessageUtil;
//
//import java.util.List;
//
///**
// * The delegation process for the student president.
// * The users who have council role become COMMON state in this process.
// * The user who is student president become COMMON state in this process.
// */
//public class DelegationPresident implements Delegation {
//
//    private final UserRepository userRepository;
//
//    public DelegationPresident(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public void delegate(String currentId, String targetId) {
//        List<User> councilList = this.userRepository.findByRoleAndState(Role.COUNCIL, UserState.ACTIVE);
//        if (!councilList.isEmpty()) {
//            councilList.forEach(
//                    user -> this.userRepository.removeRole(user.getId(), Role.COUNCIL)
//            );
//        }
//
//        List<UserDomainModel> vicePresident = this.userPort.findByRole("VICE_PRESIDENT");
//        if (!vicePresident.isEmpty()) {
//            vicePresident.forEach(
//                    user -> this.userPort.removeRole(user.getId(), Role.VICE_PRESIDENT)
//            );
//        }
//
//        this.userPort.removeRole(currentId, Role.PRESIDENT).orElseThrow(
//                () -> new InternalServerException(
//                        ErrorCode.INTERNAL_SERVER,
//                        MessageUtil.INTERNAL_SERVER_ERROR
//                )
//        );
//    }
//}
