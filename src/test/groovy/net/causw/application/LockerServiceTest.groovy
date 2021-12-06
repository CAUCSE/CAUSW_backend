package net.causw.application

import net.causw.application.dto.LockerCreateRequestDto
import net.causw.application.dto.LockerMoveRequestDto
import net.causw.application.dto.LockerResponseDto
import net.causw.application.dto.LockerUpdateRequestDto
import net.causw.application.spi.LockerLocationPort
import net.causw.application.spi.LockerLogPort
import net.causw.application.spi.LockerPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.LockerDomainModel
import net.causw.domain.model.LockerLocationDomainModel
import net.causw.domain.model.LockerLogAction
import net.causw.domain.model.Role
import net.causw.domain.model.UserDomainModel
import net.causw.domain.model.UserState
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator
import java.time.LocalDateTime

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([LockerDomainModel.class, LockerLocationDomainModel.class])
class LockerServiceTest extends Specification {
    private LockerPort lockerPort = Mock(LockerPort.class)
    private LockerLocationPort lockerLocationPort = Mock(LockerLocationPort.class)
    private LockerLogPort lockerLogPort = Mock(LockerLogPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private LockerActionFactory lockerActionFactory = new LockerActionFactory()
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private LockerService lockerService = new LockerService(
            this.lockerPort,
            this.lockerLocationPort,
            this.lockerLogPort,
            this.userPort,
            this.lockerActionFactory,
            this.validator
    )

    def mockLockerDomainModel
    def mockLockerLocationDomainModel

    def setup() {
        this.mockLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test locker location id",
                "test locker location name",
                "test locker location description"
        )
        this.mockLockerDomainModel = LockerDomainModel.of(
                "test locker id",
                1,
                true,
                LocalDateTime.now(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )
    }

    /**
     * Test cases for locker create
     */
    @Test
    def "Locker create normal case"() {
        given:
        def mockLockerCreateRequestDto = new LockerCreateRequestDto(
                1,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.lockerPort.create(((LockerDomainModel) this.mockLockerDomainModel)) >> this.mockLockerDomainModel
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        when: "create locker"
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                mockLockerCreateRequestDto.getLockerNumber(),
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn((LockerDomainModel) this.mockLockerDomainModel)
        def lockerCreate = this.lockerService.create("test user id", mockLockerCreateRequestDto)

        then:
        lockerCreate instanceof LockerResponseDto
        with(lockerCreate) {
            getLockerNumber() == 1
            getLockerLocationName() == "test locker location name"
        }
    }

    @Test
    def "Locker create unauthorized case"() {
        given:
        def mockLockerCreateRequestDto = new LockerCreateRequestDto(
                1,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.lockerPort.create(((LockerDomainModel) this.mockLockerDomainModel)) >> this.mockLockerDomainModel
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        when: "creator user is COMMON when create role is PRESIDENT"
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                mockLockerCreateRequestDto.getLockerNumber(),
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn((LockerDomainModel) this.mockLockerDomainModel)
        this.lockerService.create("test user id", mockLockerCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test def "Locker create invalid data case"() {
        given:
        def mockLockerCreateRequestDto = new LockerCreateRequestDto(
                1,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        this.lockerPort.create(((LockerDomainModel) this.mockLockerDomainModel)) >> this.mockLockerDomainModel

        when: "Locker number is null"
        mockLockerCreateRequestDto.setLockerNumber(null)
        ((LockerDomainModel) this.mockLockerDomainModel).setLockerNumber(null)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                mockLockerCreateRequestDto.getLockerNumber(),
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn((LockerDomainModel) this.mockLockerDomainModel)
        this.lockerService.create("test user id", mockLockerCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test cases for locker update
     */
    @Test "Locker update normal case"() {
        given:
        def lockerId = "test locker id";

        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def updaterUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def updaterUserDomainModel2 = UserDomainModel.of(
                "test user id2",
                "test2@cau.ac.kr",
                "test user name2",
                "test1234!",
                "20210001",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def mockEnabledLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                LocalDateTime.now(),
                null,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel),
        )

        def mockDisabledLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                LocalDateTime.now(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        def mockRegisteredLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                LocalDateTime.now(),
                (UserDomainModel) updaterUserDomainModel2,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        def mockReturnedLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                LocalDateTime.now(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.userPort.findById(updaterUserDomainModel2.getId()) >> Optional.of(updaterUserDomainModel2)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        this.lockerPort.update(lockerId, mockEnabledLockerDomainModel) >> Optional.of(mockEnabledLockerDomainModel)
        this.lockerPort.update(lockerId, mockDisabledLockerDomainModel) >> Optional.of(mockDisabledLockerDomainModel)
        this.lockerPort.update(lockerId, mockRegisteredLockerDomainModel) >> Optional.of(mockRegisteredLockerDomainModel)
        this.lockerPort.update(lockerId, mockReturnedLockerDomainModel) >> Optional.of(mockReturnedLockerDomainModel)

        when: "Locker enable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(false)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockEnabledLockerDomainModel)
        def lockerResponseDto1 = this.lockerService.update("test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto1 instanceof LockerResponseDto
        with(lockerResponseDto1) {
            getLockerNumber() == 1
            getIsActive()
        }

        when: "Locker disable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockDisabledLockerDomainModel)
        def lockerResponseDto2 = this.lockerService.update("test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto2 instanceof LockerResponseDto
        with(lockerResponseDto2) {
            getLockerNumber() == 1
            !getIsActive()
        }

        when: "Locker register"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.REGISTER)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(null)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                null,
                updaterUserDomainModel2,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockRegisteredLockerDomainModel)
        def lockerResponseDto3 = this.lockerService.update("test user id2", lockerId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto3 instanceof LockerResponseDto
        with(lockerResponseDto3) {
            getLockerNumber() == 1
            getIsActive()
            getUserId() == "test user id2"
        }

        when: "Locker return"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(updaterUserDomainModel2)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockReturnedLockerDomainModel)
        def lockerResponseDto4 = this.lockerService.update("test user id2", lockerId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto4 instanceof LockerResponseDto
        with(lockerResponseDto4) {
            getLockerNumber() == 1
            getIsActive()
            getUserId() == null
        }

        when: "Locker force return by president"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(updaterUserDomainModel2)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockReturnedLockerDomainModel)
        def lockerResponseDto5 = this.lockerService.update("test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto5 instanceof LockerResponseDto
        with(lockerResponseDto5) {
            getLockerNumber() == 1
            getIsActive()
            getUserId() == null
        }
    }

    @Test "Locker update invalid data case"() {
        given:
        def lockerId = "test locker id";

        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def updaterUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def mockDisabledLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                LocalDateTime.now(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        this.lockerPort.update(lockerId, mockDisabledLockerDomainModel) >> Optional.of(mockDisabledLockerDomainModel)

        when: "Locker action is invalid"
        mockLockerUpdateRequestDto.setAction("ERROR_TYPE" as LockerLogAction)
        ((LockerDomainModel) this.mockLockerDomainModel).setLockerNumber(null)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(false)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockDisabledLockerDomainModel)
        this.lockerService.update("test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test "Locker update unauthorized case"() {
        given:
        def lockerId = "test locker id"

        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def updaterUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def updaterUserDomainModel2 = UserDomainModel.of(
                "test user id2",
                "test2@cau.ac.kr",
                "test user name2",
                "test1234!",
                "20210001",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def updaterUserDomainModel3 = UserDomainModel.of(
                "test user id3",
                "test2@cau.ac.kr",
                "test user name3",
                "test1234!",
                "20210002",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def mockEnabledLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                LocalDateTime.now(),
                null,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel),
        )

        def mockDisabledLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                LocalDateTime.now(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        def mockReturnedLockerDomainModel = LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                LocalDateTime.now(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.userPort.findById(updaterUserDomainModel2.getId()) >> Optional.of(updaterUserDomainModel2)
        this.userPort.findById(updaterUserDomainModel3.getId()) >> Optional.of(updaterUserDomainModel3)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        this.lockerPort.update(lockerId, mockEnabledLockerDomainModel) >> Optional.of(mockEnabledLockerDomainModel)
        this.lockerPort.update(lockerId, mockDisabledLockerDomainModel) >> Optional.of(mockDisabledLockerDomainModel)
        this.lockerPort.update(lockerId, mockReturnedLockerDomainModel) >> Optional.of(mockReturnedLockerDomainModel)

        when: "Locker enable by common user"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(false)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockEnabledLockerDomainModel)
        this.lockerService.update("test user id2", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Locker disable by common user"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockDisabledLockerDomainModel)
        this.lockerService.update("test user id2", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Locker return by other user"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(updaterUserDomainModel2)
        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                lockerId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                null,
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn(mockReturnedLockerDomainModel)
        this.lockerService.update("test user id3", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }
}
