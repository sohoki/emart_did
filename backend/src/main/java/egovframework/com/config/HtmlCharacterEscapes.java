package egovframework.com.config;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HtmlCharacterEscapes extends CharacterEscapes {
	
	private static final long serialVersionUID = -6353236148390563705L;
	
	private final int[] asciiEscapes;
	
	private final CharSequenceTranslator translator;

	public HtmlCharacterEscapes() {
		Map<CharSequence, CharSequence> customMap = new HashMap<CharSequence, CharSequence>();
        customMap.put("\'", "&apos;");
        //customMap.put("(", "&#40;");
        //customMap.put(")", "&#41;");
        //customMap.put("#", "&#35;");
        Map<CharSequence, CharSequence> CUSTOM_ESCAPE = Collections.unmodifiableMap(customMap);
        
        // 1. XSS 방지 처리할 특수 문자 지정
        // ※ REST API JSON 응답에서 HTML 이스케이프는 불필요하며 오히려 에디터 콘텐츠를 파괴함.
        //   - <, >, & : React JSX가 자동 처리 + Content-Type:application/json으로 XSS 방어
        //   - "  : JSON 표준 이스케이프(\")를 Jackson이 자동 처리 — &quot;로 치환 시 JSON 파싱 오류 발생
        //   - (, ), #, ': REST API에서 XSS 위협 없음
        // → 모두 비활성화하여 JSON 콘텐츠 무결성 유지
        asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();

        // 2. XSS 방지 처리 특수 문자 인코딩 값 지정
        translator = new AggregateTranslator(
            new LookupTranslator(EntityArrays.BASIC_ESCAPE),  // <, >, &, " 는 여기에 포함됨
            new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE),
            new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE),
            // 여기에서 커스터마이징 가능
            new LookupTranslator(CUSTOM_ESCAPE)
        );
	}
	
	@Override
	public int[] getEscapeCodesForAscii() {
		return asciiEscapes;
	}
	
	@Override
	public SerializableString getEscapeSequence(int ch) {
		//20250601 한글 안되는 이유 찾기
		return new SerializedString(translator.translate(Character.toString((char) ch)));
		//return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char) ch)));
	}
}
