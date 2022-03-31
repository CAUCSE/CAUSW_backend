package net.causw.application

import net.causw.application.dto.locker.*
import net.causw.application.spi.*
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
    private FlagPort flagPort = Mock(FlagPort.class)
    private LockerActionFactory lockerActionFactory = new LockerActionFactory()
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private LockerService lockerService = new LockerService(
            this.lockerPort,
            this.lockerLocationPort,
            this.lockerLogPort,
            this.userPort,
            this.flagPort,
            this.lockerActionFactory,
            this.validator
    )

    def mockUserDomainModel
    def mockAdminDomainModel
    def mockLockerDomainModel
    def mockLockerLocationDomainModel

    def setup() {
        this.mockAdminDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.ADMIN,
                null,
                UserState.ACTIVE
        )

        this.mockUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.mockLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test",
                "test locker location name"
        )

        this.mockLockerDomainModel = LockerDomainModel.of(
                "test",
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

        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                mockLockerCreateRequestDto.getLockerNumber(),
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn((LockerDomainModel) this.mockLockerDomainModel)

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerPort.findByLockerNumber(mockLockerCreateRequestDto.getLockerNumber()) >> Optional.ofNullable(null)

        this.lockerPort.create(((LockerDomainModel) this.mockLockerDomainModel)) >> this.mockLockerDomainModel

        when:
        def lockerResponseDto = this.lockerService.create("test", mockLockerCreateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }
    }

    @Test
    def "Locker create unauthorized case"() {
        given:
        def mockLockerCreateRequestDto = new LockerCreateRequestDto(
                1,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()
        )

        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerPort.findByLockerNumber(mockLockerCreateRequestDto.getLockerNumber()) >> Optional.ofNullable(null)

        when:
        this.lockerService.create("test", mockLockerCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Locker create invalid parameter"() {
        given:
        def mockLockerCreateRequestDto = new LockerCreateRequestDto(
                1,
                ((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerPort.findByLockerNumber(null) >> Optional.ofNullable(null)

        mockLockerCreateRequestDto.setLockerNumber(null)
        this.mockLockerDomainModel.setLockerNumber(null)

        PowerMockito.mockStatic(LockerDomainModel.class)
        PowerMockito.when(LockerDomainModel.of(
                mockLockerCreateRequestDto.getLockerNumber(),
                (LockerLocationDomainModel) this.mockLockerLocationDomainModel
        )).thenReturn((LockerDomainModel) this.mockLockerDomainModel)

        when:
        this.lockerService.create("test", mockLockerCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test cases for locker update
     */
    @Test
    def "Locker update normal case"() {
        given:
        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def ownerUserDomainModel = UserDomainModel.of(
                "owner test",
                "test2@cau.ac.kr",
                "test user name2",
                "test1234!",
                "20210001",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test1") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("owner test") >> Optional.of(ownerUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        this.lockerPort.update("test", (LockerDomainModel)this.mockLockerDomainModel) >> Optional.of(this.mockLockerDomainModel)

        when: "Locker enable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE.toString())
        this.mockLockerDomainModel.setIsActive(false)
        def lockerResponseDto = this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }

        when: "Locker disable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE.toString())
        this.mockLockerDomainModel.setIsActive(true)
        lockerResponseDto = this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }

        when: "Locker return"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN.toString())
        this.mockLockerDomainModel.setIsActive(true)
        this.mockLockerDomainModel.setUser(ownerUserDomainModel)
        lockerResponseDto = this.lockerService.update("owner test", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }

        when: "Locker register for admin"
        this.userPort.findById("test") >> Optional.of(this.mockAdminDomainModel)

        mockLockerUpdateRequestDto.setAction(LockerLogAction.REGISTER.toString())
        this.mockLockerDomainModel.setIsActive(true)
        this.mockLockerDomainModel.setUser(null)
        lockerResponseDto = this.lockerService.update("test", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }

        when: "Locker register"
        this.lockerPort.findByUserId("owner test") >> Optional.ofNullable(null)
        this.lockerLogPort.whenRegister(ownerUserDomainModel) >> Optional.ofNullable(null)
        this.flagPort.findByKey("LOCKER_ACCESS") >> Optional.of(true)

        this.mockLockerDomainModel.setIsActive(true)
        this.mockLockerDomainModel.setUser(null)
        lockerResponseDto = this.lockerService.update("owner test", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }

        when: "Locker return and register"
        this.lockerPort.findByUserId("owner test") >> Optional.of(this.mockLockerDomainModel)

        this.mockLockerDomainModel.setIsActive(true)
        this.mockLockerDomainModel.setUser(null)
        lockerResponseDto = this.lockerService.update("owner test", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }

        when: "Locker force return by president"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN.toString())
        this.mockLockerDomainModel.setIsActive(true)
        this.mockLockerDomainModel.setUser(ownerUserDomainModel)
        lockerResponseDto = this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }
    }

    @Test
    def "Locker update invalid parameter"() {
        given:
        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of(this.mockLockerDomainModel)

        when: "Locker action is invalid"
        mockLockerUpdateRequestDto.setAction("ERROR_TYPE")
        this.mockLockerDomainModel.setIsActive(true)
        this.lockerService.update("test", "test", mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Locker update invalid locker status"() {
        given:
        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def ownerUserDomainModel = UserDomainModel.of(
                "owner test",
                "test2@cau.ac.kr",
                "test user name2",
                "test1234!",
                "20210001",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test1") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("owner test") >> Optional.of(ownerUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        when: "Locker enable with inactive locker"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE.toString())
        this.mockLockerDomainModel.setIsActive(true)
        this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Locker disable with inactive locker"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE.toString())
        this.mockLockerDomainModel.setIsActive(false)
        this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Locker register with inactive locker"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.REGISTER.toString())
        this.mockLockerDomainModel.setIsActive(false)
        this.mockLockerDomainModel.setUser(null)
        this.lockerService.update("owner test", "test", mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Locker register locker in use"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.REGISTER.toString())
        this.mockLockerDomainModel.setIsActive(false)
        this.mockLockerDomainModel.setUser(ownerUserDomainModel)
        this.lockerService.update("owner test", "test", mockLockerUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Locker update unauthorized case"() {
        given:
        def mockLockerUpdateRequestDto = new LockerUpdateRequestDto(
                null,
                ''
        )

        def ownerUserDomainModel = UserDomainModel.of(
                "owner test",
                "test2@cau.ac.kr",
                "test user name2",
                "test1234!",
                "20210001",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test1") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("owner test") >> Optional.of(ownerUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of((LockerDomainModel) this.mockLockerDomainModel)

        when: "Locker enable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.ENABLE.toString())
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.mockLockerDomainModel.setIsActive(false)
        this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Locker disable"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.DISABLE.toString())
        this.mockLockerDomainModel.setIsActive(true)
        this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Locker return"
        mockLockerUpdateRequestDto.setAction(LockerLogAction.RETURN.toString())
        this.mockLockerDomainModel.setIsActive(true)
        this.mockLockerDomainModel.setUser(ownerUserDomainModel)
        this.lockerService.update("test1", "test", mockLockerUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }


    /**
     * Test cases for locker move
     */
    @Test
    def "Locker move normal case"() {
        given:
        def mockLockerMoveRequestDto = new LockerMoveRequestDto("test1")

        def mockMovedLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test1",
                "test name1"
        )

        def mockMovedLockerDomainModel = LockerDomainModel.of(
                "test",
                1,
                true,
                LocalDateTime.now(),
                null,
                mockMovedLockerLocationDomainModel
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test1") >> Optional.of(mockMovedLockerLocationDomainModel)
        this.lockerPort.findById("test") >> Optional.of(this.mockLockerDomainModel)
        this.lockerPort.updateLocation("test", (LockerDomainModel) this.mockLockerDomainModel) >> Optional.of(mockMovedLockerDomainModel)

        when:
        def lockerResponseDto = this.lockerService.move("test", "test", mockLockerMoveRequestDto)

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getLockerNumber() == "1"
        }
    }

    @Test
    def "Locker move unauthorized case"() {
        given:
        def mockLockerMoveRequestDto = new LockerMoveRequestDto("test1")

        def mockMovedLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test1",
                "test name1"
        )

        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test1") >> Optional.of(mockMovedLockerLocationDomainModel)
        this.lockerPort.findById("test") >> Optional.of(this.mockLockerDomainModel)

        when:
        this.lockerService.move("test", "test", mockLockerMoveRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for locker delete
     */
    @Test
    def "Locker delete normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of(this.mockLockerDomainModel)

        this.lockerPort.delete((LockerDomainModel) this.mockLockerDomainModel) >> null

        when:
        def lockerResponseDto = this.lockerService.delete("test", "test")

        then:
        lockerResponseDto instanceof LockerResponseDto
        with(lockerResponseDto) {
            getId() == "test"
        }
    }

    @Test
    def "Locker delete unauthorized case"() {
        given:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of(this.mockLockerDomainModel)

        when:
        this.lockerService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Locker delete locker in use"() {
        given:
        this.mockLockerDomainModel.setUser(this.mockUserDomainModel)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerPort.findById("test") >> Optional.of(this.mockLockerDomainModel)

        when:
        this.lockerService.delete("test", "test")

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for locker location create
     */
    @Test
    def "Locker location create normal case"() {
        given:
        def lockerLocationCreateRequestDto = new LockerLocationCreateRequestDto(
                "test locker location name"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()) >> Optional.ofNullable(null)
        this.lockerLocationPort.create((LockerLocationDomainModel) this.mockLockerLocationDomainModel) >> this.mockLockerLocationDomainModel

        PowerMockito.mockStatic(LockerLocationDomainModel.class)
        PowerMockito.when(LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName()
        )).thenReturn((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        when:
        def lockerLocationResponseDto = this.lockerService.createLocation("test", lockerLocationCreateRequestDto)

        then:
        lockerLocationResponseDto instanceof LockerLocationResponseDto
        with(lockerLocationResponseDto) {
            getName() == "test locker location name"
        }
    }

    @Test
    def "Locker location create invalid data case"() {
        given:
        def lockerLocationCreateRequestDto = new LockerLocationCreateRequestDto(
                "",
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()) >> Optional.ofNullable(null)

        this.mockLockerLocationDomainModel.setName("")
        PowerMockito.mockStatic(LockerLocationDomainModel.class)
        PowerMockito.when(LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName()
        )).thenReturn((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

        when:
        this.lockerService.createLocation("test", lockerLocationCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Locker location create unauthorized case"() {
        given:
        def lockerLocationCreateRequestDto = new LockerLocationCreateRequestDto(
                "test locker location name"
        )

        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()) >> Optional.ofNullable(null)

        when:
        this.lockerService.createLocation("test", lockerLocationCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for locker location update
     */
    @Test
    def "Locker location update normal case"() {
        given:
        def lockerLocationUpdateRequestDto = new LockerLocationUpdateRequestDto(
                "test locker location updated name"
        )

        def updatedLockerLocationDomainModel = LockerLocationDomainModel.of(
                "test",
                "test locker location updated name"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerLocationPort.findByName(lockerLocationUpdateRequestDto.getName()) >> Optional.ofNullable(null)
        this.lockerLocationPort.update("test", (LockerLocationDomainModel) this.mockLockerLocationDomainModel) >> Optional.of(updatedLockerLocationDomainModel)
        this.lockerPort.countEnableLockerByLocation("test") >> 0L
        this.lockerPort.countByLocation("test") >> 0L

        when:
        def lockerLocationResponseDto = this.lockerService.updateLocation("test", "test", lockerLocationUpdateRequestDto)

        then:
        lockerLocationResponseDto instanceof LockerLocationResponseDto
        with(lockerLocationResponseDto) {
            getName() == "test locker location updated name"
            getEnableLockerCount() == 0L
            getTotalLockerCount() == 0L
        }
    }

    @Test
    def "Locker location update invalid data case"() {
        given:
        def lockerLocationUpdateRequestDto = new LockerLocationUpdateRequestDto(
                ""
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerLocationPort.findByName(lockerLocationUpdateRequestDto.getName()) >> Optional.ofNullable(null)

        when:
        this.lockerService.updateLocation("test", "test", lockerLocationUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Locker location update unauthorized case"() {
        given:
        def lockerLocationUpdateRequestDto = new LockerLocationUpdateRequestDto(
                "test locker location updated name"
        )

        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerLocationPort.findByName(lockerLocationUpdateRequestDto.getName()) >> Optional.ofNullable(null)

        when:
        this.lockerService.updateLocation("test", "test", lockerLocationUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for locker location update
     */
    @Test
    def "Locker location delete normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerPort.countByLocation("test") >> 0L

        this.lockerLocationPort.delete((LockerLocationDomainModel) this.mockLockerLocationDomainModel) >> null

        when:
        def lockerLocationResponseDto = this.lockerService.deleteLocation("test", "test")

        then:
        lockerLocationResponseDto instanceof LockerLocationResponseDto
        with(lockerLocationResponseDto) {
            getId() == "test"
        }
    }

    @Test
    def "Locker location unauthorized case"() {
        given:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerLocationPort.findById("test") >> Optional.of(this.mockLockerLocationDomainModel)
        this.lockerPort.countByLocation("test") >> 0L

        when:
        this.lockerService.deleteLocation("test", "test")

        then:
        thrown(UnauthorizedException)
    }
}
