package net.causw.application

import net.causw.application.dto.LockerCreateRequestDto
import net.causw.application.dto.LockerResponseDto
import net.causw.application.spi.LockerLocationPort
import net.causw.application.spi.LockerLogPort
import net.causw.application.spi.LockerPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.LockerDomainModel
import net.causw.domain.model.LockerLocationDomainModel
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
        this.lockerPort.create(((LockerDomainModel) this.mockLockerDomainModel)) >> this.mockLockerDomainModel
        this.lockerLocationPort.findById(((LockerLocationDomainModel) this.mockLockerLocationDomainModel).getId()) >> Optional.of((LockerLocationDomainModel) this.mockLockerLocationDomainModel)

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
}
