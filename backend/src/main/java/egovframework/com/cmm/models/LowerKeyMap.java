package egovframework.com.cmm.models;


import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;

public class LowerKeyMap <K, V> extends ListOrderedMap<K, V> {

	private static final long serialVersionUID = 1629908826215938208L;

	/**
     * <pre>key 에 대하여 소문자로 변환하여 super.put
     * (ListOrderedMap) 을 호출한다.</pre>
     * @param key
     *        - '_' 가 포함된 변수명
     * @param value
     *        - 명시된 key 에 대한 값 (변경 없음)
     * @return previous value associated with specified
     *         key, or null if there was no mapping for
     *         key
     * @see
     */
	@SuppressWarnings("unchecked")
	public V put(final K key, final V value) {
		return super.put((K)StringUtils.lowerCase((String) key), value);
	}
}
