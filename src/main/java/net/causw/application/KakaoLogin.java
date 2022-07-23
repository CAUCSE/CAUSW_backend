package net.causw.application;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.user.KakaoProfileDto;
import net.causw.application.dto.user.OauthTokenDto;
import net.causw.application.dto.user.SocialSignInRequestDto;
import net.causw.application.dto.user.SocialSignInResponseDto;
import net.causw.application.spi.UserPort;
import net.causw.config.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserDomainModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class KakaoLogin implements SocialLogin{

    @Override
    public SocialSignInResponseDto returnJwtToken(UserPort userPort, JwtTokenProvider jwtTokenProvider, SocialSignInRequestDto socialSignInRequestDto) {

        String accessToken = socialSignInRequestDto.getAccessToken();
        System.out.println("accessToken = " + accessToken);

        RestTemplate rt = new RestTemplate();
        //HttpHEader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer "+accessToken);

        //HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);

        //Http Post방식으로 요청 후 response 변수에 응답 받음.
        ResponseEntity<String> response;
        try{
            response = rt.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    kakaoProfileRequest,
                    String.class
            );
        }
        catch(Exception e)
        {
            throw new BadRequestException(ErrorCode.SOCIAL_LOGIN_FAIL, "소셜로그인 토큰 요청이 실패했습니다.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfileDto kakaoProfileDto = null;
        try {
            kakaoProfileDto = objectMapper.readValue(response.getBody(), KakaoProfileDto.class);
        } catch(Exception e) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN, "유효하지 않은 Access Token 입니다.");
        }

        UserDomainModel userDomainModel = userPort.findByEmail("KAKAO_" + kakaoProfileDto.getKakao_account().getEmail())
                .orElse(null);

        if(userDomainModel == null){
            userDomainModel = userPort.create(UserDomainModel.of(
                    "KAKAO_"+kakaoProfileDto.getKakao_account().getEmail(),
                    "temporary",
                    "temporary",
                    null,
                    0,
                    null
            ));
        }

        return new SocialSignInResponseDto(jwtTokenProvider.createToken(
                userDomainModel.getId(),
                userDomainModel.getRole(),
                userDomainModel.getState()
        ), "KAKAO", kakaoProfileDto.getKakao_account().getEmail());
    }
}
