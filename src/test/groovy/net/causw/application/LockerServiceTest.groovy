package net.causw.application

import net.causw.application.dto.locker.LockerCreateRequestDto
import net.causw.application.dto.locker.LockerLocationCreateRequestDto
import net.causw.application.dto.locker.LockerLocationResponseDto
import net.causw.application.dto.locker.LockerLocationUpdateRequestDto
import net.causw.application.dto.locker.LockerMoveRequestDto
import net.causw.application.dto.locker.LockerResponseDto
import net.causw.application.dto.locker.LockerUpdateRequestDto
import net.causw.application.spi.LockerLocationPort
import net.causw.application.spi.LockerLogPort
import net.causw.application.spi.LockerPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
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

        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                mockLockerCreateRequestDto.getLockerNumber(),
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn((LockerDomainModel) this.mockLockerDomainModel)

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerPort.findByLockerNumber(mockLockerCreateRequestDto.getLockerNumber()) >> Optional.ofNullable(null)

        this.lockerPort.create(((LockerDomainModel) this.mockLockerDomainModel)) >> this.mockLockerDomainModel

        when: "create locker"
        def lockerCreate = this.lockerService.create("test user id", mockLockerCreateRequestDto)

        then:
        lockerCreate instanceof LockerResponseDto
        with(lockerCreate) {
            getLockerNumber() == 1
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
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerPort.findByLockerNumber(mockLockerCreateRequestDto.getLockerNumber()) >> Optional.ofNullable(null)

        when: "creator user is COMMON when create role is PRESIDENT"
        this.lockerService.create("test user id", mockLockerCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Locker create invalid data case"() {
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
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerPort.findByLockerNumber(null) >> Optional.ofNullable(null)

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
    @Test
    def "Locker update normal case"() {
        given:
        def lockerEnableId = "test enable locker id"
        def lockerDisableId = "test disable locker id"
        def lockerRegisterId = "test register locker id"
        def lockerReturnId = "test return locker id"

        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def presidentUserDomainModel = UserDomainModel.of(
                "president test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def ownerUserDomainModel = UserDomainModel.of(
                "owner test user id",
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
                lockerEnableId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                ((LockerDomainModel) this.mockLockerDomainModel).getUpdatedAt(),
                null,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel),
        )

        def mockDisabledLockerDomainModel = LockerDomainModel.of(
                lockerDisableId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                false,
                ((LockerDomainModel) this.mockLockerDomainModel).getUpdatedAt(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        def mockRegisteredLockerDomainModel = LockerDomainModel.of(
                lockerRegisterId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                ((LockerDomainModel) this.mockLockerDomainModel).getUpdatedAt(),
                (UserDomainModel) ownerUserDomainModel,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        def mockReturnedLockerDomainModel = LockerDomainModel.of(
                lockerReturnId,
                ((LockerDomainModel) this.mockLockerDomainModel).getLockerNumber(),
                true,
                ((LockerDomainModel) this.mockLockerDomainModel).getUpdatedAt(),
                null,
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel,
        )

        this.userPort.findById(presidentUserDomainModel.getId()) >> Optional.of(presidentUserDomainModel)
        this.userPort.findById(ownerUserDomainModel.getId()) >> Optional.of(ownerUserDomainModel)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        this.lockerPort.findById(lockerEnableId) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)
        this.lockerPort.findById(lockerDisableId) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)
        this.lockerPort.findById(lockerRegisterId) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)
        this.lockerPort.findById(lockerReturnId) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        this.lockerPort.update(lockerEnableId, (LockerDomainModel)this.mockLockerDomainModel) >> Optional.of(mockEnabledLockerDomainModel)
        this.lockerPort.update(lockerDisableId, (LockerDomainModel)this.mockLockerDomainModel) >> Optional.of(mockDisabledLockerDomainModel)
        this.lockerPort.update(lockerRegisterId, (LockerDomainModel)this.mockLockerDomainModel) >> Optional.of(mockRegisteredLockerDomainModel)
        this.lockerPort.update(lockerReturnId, (LockerDomainModel)this.mockLockerDomainModel) >> Optional.of(mockReturnedLockerDomainModel)

        when: "Locker enable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setId(lockerEnableId)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(false)
        def lockerResponseDto1 = this.lockerService.update("president test user id", lockerEnableId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto1 instanceof LockerResponseDto
        with(lockerResponseDto1) {
            getLockerNumber() == 1
            getIsActive()
        }

        when: "Locker disable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setId(lockerDisableId)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        def lockerResponseDto2 = this.lockerService.update("president test user id", lockerDisableId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto2 instanceof LockerResponseDto
        with(lockerResponseDto2) {
            getLockerNumber() == 1
            !getIsActive()
        }

        when: "Locker return"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setId(lockerReturnId)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(ownerUserDomainModel)
        def lockerResponseDto4 = this.lockerService.update("owner test user id", lockerReturnId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto4 instanceof LockerResponseDto
        with(lockerResponseDto4) {
            getLockerNumber() == 1
            getIsActive()
        }

        when: "Locker register"
        this.lockerPort.findByUserId("owner test user id") >> Optional.ofNullable(null)
        this.lockerLogPort.whenRegister(ownerUserDomainModel) >> Optional.ofNullable(null)

        mockLockerUpdateRequestDto.setAction(LockerLogAction.REGISTER.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setId(lockerRegisterId)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(null)
        def lockerResponseDto3 = this.lockerService.update("owner test user id", lockerRegisterId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto3 instanceof LockerResponseDto
        with(lockerResponseDto3) {
            getLockerNumber() == 1
            getIsActive()
        }

        when: "Locker force return by president"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setId(lockerReturnId)
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(ownerUserDomainModel)
        def lockerResponseDto5 = this.lockerService.update("president test user id", lockerReturnId, mockLockerUpdateRequestDto)

        then:
        lockerResponseDto5 instanceof LockerResponseDto
        with(lockerResponseDto5) {
            getLockerNumber() == 1
            getIsActive()
        }
    }

    @Test
    def "Locker update invalid data case"() {
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

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        when: "Locker action is invalid"
        mockLockerUpdateRequestDto.setAction("ERROR_TYPE")
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        this.lockerService.update("test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Locker update unauthorized case"() {
        given:
        def lockerId = "test locker id"

        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def ownerUserDomainModel = UserDomainModel.of(
                "owner test user id",
                "test2@cau.ac.kr",
                "test user name2",
                "test1234!",
                "20210001",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def otherUserDomainModel = UserDomainModel.of(
                "other test user id",
                "test3@cau.ac.kr",
                "test user name3",
                "test1234!",
                "20210002",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById(ownerUserDomainModel.getId()) >> Optional.of(ownerUserDomainModel)
        this.userPort.findById(otherUserDomainModel.getId()) >> Optional.of(otherUserDomainModel)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        when: "Locker enable by common user"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(false)
        this.lockerService.update("owner test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Locker disable by common user"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        this.lockerService.update("owner test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Locker return by other user"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN.toString())
        ((LockerDomainModel) this.mockLockerDomainModel).setIsActive(true)
        ((LockerDomainModel) this.mockLockerDomainModel).setUser(ownerUserDomainModel)
        this.lockerService.update("other test user id", lockerId, mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }


    /**
     * Test cases for locker move
     */
    @Test
    def "Locker move normal case"() {
        given:
        def lockerId = "test locker id"

        def mockLockerMoveRequestDto = new LockerMoveRequestDto("test locker location id2")

        def mockMovedLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test locker location id2",
                "test name2",
                "test description2"
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

        def mockMovedLockerDomainModel = LockerDomainModel.of(
                "test locker id",
                1,
                true,
                LocalDateTime.now(),
                null,
                mockMovedLockerLocationDomainModel
        )

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerLocationPort.findById(((LockerLocationDomainModel) mockMovedLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) mockMovedLockerLocationDomainModel)
        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        this.lockerPort.updateLocation(lockerId, (LockerDomainModel)this.mockLockerDomainModel) >> Optional.of(mockMovedLockerDomainModel)

        when:
        def lockerResponseDto = this.lockerService.move("test user id", lockerId, mockLockerMoveRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == 1
        }
    }

    @Test
    def "Locker move unauthorized case"() {
        given:
        def lockerId = "test locker id"

        def mockLockerMoveRequestDto = new LockerMoveRequestDto("test locker location id2")

        def mockMovedLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test locker location id2",
                "test name2",
                "test description2"
        )

        def updaterUserDomainModel = UserDomainModel.of(
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

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)

        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerLocationPort.findById(((LockerLocationDomainModel) mockMovedLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) mockMovedLockerLocationDomainModel)

        this.lockerPort.findById(((LockerDomainModel) this.mockLockerDomainModel).getId()) >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        when:
        this.lockerService.move("test user id", lockerId, mockLockerMoveRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for create locker location
     */
    @Test
    def "Locker location create normal case"() {
        given:
        def lockerLocationCreateRequestDto = new LockerLocationCreateRequestDto(
                "test locker location name",
                "test locker location description",
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
        this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()) >> Optional.ofNullable(null)
        this.lockerLocationPort.create((LockerLocationDomainModel) this.mockLockerLocationDomainModel) >> this.mockLockerLocationDomainModel

        when:
        PowerMockito.mockStatic(LockerLocationDomainModel.class)
        PowerMockito.when(LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName(),
                lockerLocationCreateRequestDto.getDescription()
        )).thenReturn((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        def lockerLocationResponseDto = this.lockerService.createLocation("test user id", lockerLocationCreateRequestDto)

        then:
        lockerLocationResponseDto instanceof LockerLocationResponseDto
        with(lockerLocationResponseDto) {
            getName() == "test locker location name"
            getDescription() == "test locker location description"
        }
    }

    @Test
    def "Locker location create invalid data case"() {
        given:
        def lockerLocationCreateRequestDto = new LockerLocationCreateRequestDto(
                "",
                "",
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
        this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()) >> Optional.ofNullable(null)

        when:
        ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).setName("")
        ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).setDescription("")
        PowerMockito.mockStatic(LockerLocationDomainModel.class)
        PowerMockito.when(LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName(),
                lockerLocationCreateRequestDto.getDescription()
        )).thenReturn((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerService.createLocation("test user id", lockerLocationCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Locker location create unauthorized case"() {
        given:
        def lockerLocationCreateRequestDto = new LockerLocationCreateRequestDto(
                "test locker location name",
                "test locker location description",
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
        this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()) >> Optional.ofNullable(null)

        when:
        PowerMockito.mockStatic(LockerLocationDomainModel.class)
        PowerMockito.when(LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName(),
                lockerLocationCreateRequestDto.getDescription()
        )).thenReturn((LockerLocationDomainModel) this.mockLockerLocationDomainModel)
        this.lockerService.createLocation("test user id", lockerLocationCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for update locker location
     */
    @Test
    def "Locker location update normal case"() {
        given:
        def lockerLocationId = "test locker location id"

        def lockerLocationUpdateRequestDto = new LockerLocationUpdateRequestDto(
                "test locker location updated name",
                "test locker location updated description"
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

        def updatedLockerLocationDomainModel = LockerLocationDomainModel.of(
                lockerLocationId,
                "test locker location updated name",
                "test locker location updated description"
        )

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.lockerLocationPort.findByName(lockerLocationUpdateRequestDto.getName()) >> Optional.ofNullable(null)
        this.lockerLocationPort.findById(lockerLocationId) >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerLocationPort.update(lockerLocationId, (LockerLocationDomainModel)this.mockLockerLocationDomainModel) >> Optional.of(updatedLockerLocationDomainModel)

        when:
        def lockerLocationResponseDto = this.lockerService.updateLocation("test user id", lockerLocationId, lockerLocationUpdateRequestDto)

        then:
        lockerLocationResponseDto instanceof LockerLocationResponseDto
        with(lockerLocationResponseDto) {
            getName() == "test locker location updated name"
            getDescription() == "test locker location updated description"
        }
    }

    @Test
    def "Locker location update invalid data case"() {
        given:
        def lockerLocationId = "test locker location id"

        def lockerLocationUpdateRequestDto = new LockerLocationUpdateRequestDto(
                "",
                ""
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

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.lockerLocationPort.findByName(lockerLocationUpdateRequestDto.getName()) >> Optional.ofNullable(null)
        this.lockerLocationPort.findById(lockerLocationId) >> Optional.of(this.mockLockerLocationDomainModel)

        when:
        this.lockerService.updateLocation("test user id", lockerLocationId, lockerLocationUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Locker location update unauthorized case"() {
        given:
        def lockerLocationId = "test locker location id"

        def lockerLocationUpdateRequestDto = new LockerLocationUpdateRequestDto(
                "test locker location updated name",
                "test locker location updated description"
        )

        def updaterUserDomainModel = UserDomainModel.of(
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

        this.userPort.findById(updaterUserDomainModel.getId()) >> Optional.of(updaterUserDomainModel)
        this.lockerLocationPort.findByName(lockerLocationUpdateRequestDto.getName()) >> Optional.ofNullable(null)
        this.lockerLocationPort.findById(lockerLocationId) >> Optional.of(this.mockLockerLocationDomainModel)

        when:
        this.lockerService.updateLocation("test user id", lockerLocationId, lockerLocationUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }
}
