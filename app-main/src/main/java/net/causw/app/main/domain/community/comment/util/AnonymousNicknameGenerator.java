package net.causw.app.main.domain.community.comment.util;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 익명 댓글에 부여할 "능력치 + 숫자 + 명사" 형태의 랜덤 닉네임을 생성한다.
 *
 * <p>후보 생성 및 중복 회피는 순수 메모리 연산(in-memory {@link Set} 대조)으로만 처리되므로,
 * 이미 사용 중인 닉네임 목록을 DB에서 한 번만 읽어오면 추가 쿼리 없이 여러 번 재시도할 수 있다.</p>
 */
public class AnonymousNicknameGenerator {

	private static final Random RANDOM = new Random();

	private static final int NUMBER_MIN = 1;
	private static final int NUMBER_MAX = 100;

	/** 최악의 경우를 대비한 재시도 상한. 조합 공간(약 41만 개) 대비 매우 넉넉한 값이다. */
	private static final int MAX_ATTEMPTS = 30;

	private static final List<String> TRAITS = List.of(
		"명랑함", "쾌활함", "다정함", "온화함", "차분함", "신중함", "유쾌함", "듬직함", "성실함", "솔직함",
		"용감함", "겸손함", "당당함", "친근함", "친절함", "꼼꼼함", "정직함", "상냥함", "늠름함", "씩씩함",
		"활달함", "진중함", "관대함", "자상함", "느긋함", "대범함", "털털함", "무던함", "순둥함", "소탈함",
		"검소함", "수수함", "인자함", "포근함", "침착함", "우직함", "부지런함", "발랄함", "단호함", "순수함",
		"쿨함", "따뜻함", "든든함", "평온함", "넉넉함", "근면함", "강직함", "친밀함", "돈독함", "신실함",
		"정숙함", "온순함", "다감함", "정결함", "진실함", "반듯함", "착함", "단정함", "의젓함", "똑똑함",
		"유연함", "영리함", "유능함", "탁월함", "위대함", "영특함", "기발함", "명석함", "예리함", "철저함",
		"명확함", "정교함", "해박함", "치밀함", "민첩함", "날렵함", "능숙함", "비상함", "명철함", "총명함",
		"기민함", "막강함", "강력함", "다재다능함", "완벽함", "강인함", "단단함", "견고함", "깔끔함", "튼튼함",
		"과감함", "수려함", "화려함", "원만함", "견실함", "신속함", "정확함", "비범함", "출중함", "우수함");

	private static final List<String> NOUNS = List.of(
		"튜링", "폰노이만", "호퍼", "러브레이스", "섀넌", "다익스트라", "커누스", "배비지", "매카시", "민스키",
		"케이", "엥겔바트", "커닝햄", "카마크", "토르발스", "스톨만", "버너스리", "헤네시", "팻터슨", "무어",
		"노이스", "그로브", "잡스", "워즈니악", "게이츠", "앨런", "엘리슨", "슈미트", "베이조스", "머스크",
		"페이지", "브린", "피차이", "나델라", "저커버그", "도시", "헤이스팅스", "앤더슨", "나카모토", "손정의",
		"마윈", "이해진", "김범수");

	private AnonymousNicknameGenerator() {
	}

	/**
	 * {@code used}에 포함되지 않은 닉네임을 생성한다.
	 *
	 * <p>DB를 조회하지 않고 in-memory 대조만 수행하며, {@link #MAX_ATTEMPTS}번 안에 미사용 닉네임을
	 * 찾지 못하면 마지막으로 생성한 후보를 그대로 반환한다 (최종 유일성은 DB의 유니크 제약이 보장한다).</p>
	 */
	public static String generateUnused(Set<String> used) {
		String candidate = generate();
		for (int attempt = 1; attempt < MAX_ATTEMPTS && used.contains(candidate); attempt++) {
			candidate = generate();
		}
		return candidate;
	}

	private static String generate() {
		String trait = TRAITS.get(RANDOM.nextInt(TRAITS.size()));
		int number = NUMBER_MIN + RANDOM.nextInt(NUMBER_MAX - NUMBER_MIN + 1);
		String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
		return trait + " " + number + " " + noun;
	}
}
