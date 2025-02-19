package burp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * External helper class. Developed by NoSpaceAvailable
 */

public class FormURLUtilities {

    /**
     * Helper method
     */
    public static String encode(String enc) {
        String result = "";
        try {
            result = enc.replaceAll("=", "%3D").
                         replaceAll("&", "%26").
                         replaceAll("\"", "%22").
                         replaceAll("\\+", "%2B").
                         replaceAll(" ", "%20");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String decode(String dec) {
        String result = "";
        try {
            result =  dec.replaceAll("(?i)%3D", "=").
                          replaceAll("%26", "&").
                          replaceAll("%22", "\\\\\"").
                          replaceAll("(?i)%2B", "+").
                          replaceAll("%20", " ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Helper method of jsonToURLEncoding
     */
    private String jsonToURLEncodingAux(Object json, String encodedPrefix) {
        StringBuilder output = new StringBuilder();
        if (json instanceof JSONObject obj) {
            String[] keys = JSONObject.getNames(obj);
            for (String currKey : keys) {
                String subPrefix = encodedPrefix + "[" + encode(currKey) + "]";
                output.append(jsonToURLEncodingAux(obj.get(currKey), subPrefix));
            }
        } else if (json instanceof JSONArray jsonArr) {
            int arrLen = jsonArr.length();

            for (int i = 0; i < arrLen; i++) {
                String subPrefix = encodedPrefix + "[]";
                Object child = jsonArr.get(i);
                output.append(jsonToURLEncodingAux(child, subPrefix));
            }
        } else {
            output = new StringBuilder(encodedPrefix + "=" + encode(json.toString()) + "&");
        }

        return output.toString();
    }

    /**
     * Convert some characters in JSON content to its url encoded form.
     * */
    public byte[] jsonToFormURL(JSONObject json) {
        StringBuilder output = new StringBuilder();
        String[] keys = JSONObject.getNames(json);
        for (String currKey : keys)
            output.append(jsonToURLEncodingAux(json.get(currKey), encode(currKey)));

        return output.substring(0, output.length()-1).getBytes();
    }

    /**
     * Convert form-data POST body to form urlencoded.
     * Note that this method will ignore files in the request body.
     * */
    public byte[] formDataToFormUrl(String body) {
        Map<String, String> keyVals = FormDataParser.parse(body);
        StringBuilder formURL = new StringBuilder();

        for (String key : keyVals.keySet()) {
            formURL.append(FormURLUtilities.encode(key)).append("=").append(FormURLUtilities.encode(keyVals.get(key))).append("&");
        }

        return formURL.deleteCharAt(formURL.length() - 1).toString().getBytes();
    }
}
