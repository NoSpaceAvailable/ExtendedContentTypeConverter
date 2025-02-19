package burp;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * External helper class. Developed by NoSpaceAvailable
 */

public class FormDataParser {
    public static Map<String, String> parse(String rawBody) {

        Map<String, String> parsedForm = new Hashtable<String, String>();

        String boundary = rawBody.split("\r\n")[0];

        String[] parts = rawBody.split(boundary);

        for (String part : parts) {
            if (part.trim().isEmpty() || part.contains("--")) continue;

            String[] sections = part.split("\r\n\r\n", 2);
            if (sections.length < 2) continue;

            String headers = sections[0];
            String content = sections[1].trim();

            Matcher matcher = Pattern.compile("name=\"(.*?)\"").matcher(headers);
            String fieldName = matcher.find() ? matcher.group(1) : "unknown";

            if (!headers.contains("filename=")) {
                parsedForm.put(fieldName, content);
            }
        }

        return parsedForm;
    }
}
