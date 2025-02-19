package burp;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;

public class FormDataUtilities {
    /**
     * Helper method
     * */
    public static String getSaltString(int size) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < size) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }

    public static String buildFormData(String body, String salt) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        String randomBoundary = "----WebKitFormBoundary" + salt;
        result.append("--").append(randomBoundary);
        Map<String, String> keyVals = Utilities.splitQuery(body);
        for (String key : keyVals.keySet()) {
            result.
                    append("\r\nContent-Disposition: form-data; name=\"_KEY\"".replace("_KEY", FormURLUtilities.encode(key))).
                    append("\r\n\r\n").
                    append(FormURLUtilities.decode(keyVals.get(key))).
                    append("\r\n").
                    append("--").
                    append(randomBoundary);
        }

        result.append("--\r\n");
        return result.toString();
    }
}
