package egovframework.com.cmm.models;


import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;

public class CamelKeyMap<K, V> extends ListOrderedMap<K, V> {

	private static final long serialVersionUID = -6771193864151918088L;

	/**
	 * <pre>key 에 대하여 camelCase로 변환하여 super.put
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
		return super.put((K) toCamelCase((String) key), value);
	}

	/**
	 * Converts a string to camelCase
	 * @param input
	 *        - The string to be converted
	 * @return The camelCase version of the input string
	 */
	private String toCamelCase(String input) {
		if (input == null) {
			return null;
		}
		String[] parts = input.split("_");
		StringBuilder camelCaseString = new StringBuilder(parts[0].toLowerCase());
		for (int i = 1; i < parts.length; i++) {
			camelCaseString.append(StringUtils.capitalize(parts[i].toLowerCase()));
		}
		return camelCaseString.toString();
	}

}