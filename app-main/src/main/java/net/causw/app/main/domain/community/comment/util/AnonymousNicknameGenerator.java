package net.causw.app.main.domain.community.comment.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

/**
 * 익명 댓글에 부여할 "수식어 + 명사 + 숫자" 형태의 랜덤 닉네임을 생성한다.
 *
 * <p>후보 하나를 무작위로 생성만 할 뿐 중복 여부는 판단하지 않는다. 실제 유일성은
 * {@code CommentAnonymousNicknameResolver}가 DB 유니크 제약과 재시도로 보장한다.</p>
 */
@Component
public class AnonymousNicknameGenerator {

	private static final int NUMBER_MIN = 1;
	private static final int NUMBER_MAX = 100;

	private static final List<String> TRAITS = List.of(
		"명랑한", "쾌활한", "다정한", "온화한", "차분한", "신중한", "유쾌한", "듬직한", "성실한", "솔직한",
		"용감한", "겸손한", "당당한", "친근한", "친절한", "꼼꼼한", "정직한", "상냥한", "늠름한", "씩씩한",
		"활달한", "진중한", "관대한", "자상한", "느긋한", "대범한", "털털한", "무던한", "순둥한", "소탈한",
		"검소한", "수수한", "인자한", "포근한", "침착한", "우직한", "부지런한", "발랄한", "단호한", "순수한",
		"쿨한", "따뜻한", "든든한", "평온한", "넉넉한", "근면한", "강직한", "친밀한", "돈독한", "신실한",
		"정숙한", "온순한", "다감한", "정결한", "진실한", "반듯한", "착한", "단정한", "의젓한", "똑똑한",
		"유연한", "영리한", "유능한", "탁월한", "위대한", "영특한", "기발한", "명석한", "예리한", "철저한",
		"명확한", "정교한", "해박한", "치밀한", "민첩한", "날렵한", "능숙한", "비상한", "명철한", "총명한",
		"기민한", "막강한", "강력한", "다재다능한", "완벽한", "강인한", "단단한", "견고한", "깔끔한", "튼튼한",
		"과감한", "수려한", "화려한", "원만한", "견실한", "신속한", "정확한", "비범한", "출중한", "우수한");

	private static final List<String> NOUNS = List.of(
		"튜링", "노이만", "호퍼", "러브레이스", "섀넌", "다익스트라", "커누스", "배비지",
		"케이", "엥겔바트", "카마크", "토르발스", "버너스리", "헤네시", "팻터슨", "무어", "민스키",
		"매카시", "노이스", "잡스", "워즈니악", "게이츠", "엘리슨", "베이조스", "머스크", "페이지",
		"브린", "피차이", "나델라", "저커버그", "나카모토", "마윈");

	public String generate() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		String trait = TRAITS.get(random.nextInt(TRAITS.size()));
		String noun = NOUNS.get(random.nextInt(NOUNS.size()));
		int number = NUMBER_MIN + random.nextInt(NUMBER_MAX - NUMBER_MIN + 1);
		return trait + " " + noun + " " + number;
	}
}
