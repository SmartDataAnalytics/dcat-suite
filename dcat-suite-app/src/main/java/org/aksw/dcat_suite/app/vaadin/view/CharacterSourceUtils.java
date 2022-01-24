package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class CharacterSourceUtils {

	/** Return the ratio of characters that are text (letters or numeric) */
	public static float getTextRatio(Path path, Charset charset) throws IOException {
		float result;
		try (Reader reader = new InputStreamReader(Files.newInputStream(path), charset)) {
			long[] stats = countMatchingChars(reader, Character::isDefined);
			result = stats[1] / (float)stats[0];
		}
		return result;
	}
	
	/**
	 * Read the provided reader wholly and count the number of characters and how many of them are letters.
	 * Useful to determine whether the content is text or binary.
	 * 
	 * @param reader
	 * @return An array with two elements: [total character count, letter character count]
	 * @throws IOException
	 */
	public static long[] countMatchingChars(Reader reader, Predicate<Character> predicate) throws IOException {
		char[] buf = new char[1024 * 8];
		int n;
		long total = 0;
		long letterCount = 0;
		while (true) {
			n = reader.read(buf);
			if (n == -1) {
				break;
			}
			total += n;
			letterCount += countMatchingChars(buf, 0, n, predicate);
		}
		return new long[] { total, letterCount };
	}
	
	public static int countMatchingChars(char[] buf, int offset, int length, Predicate<Character> predicate) {
		int matchCount = 0;
		int n = offset + length;
		for (int i = offset; i < n; ++i) {
			char c = buf[i];
			if (predicate.test(c)) {
				++matchCount;
			}
		}
		return matchCount;
	}
}
