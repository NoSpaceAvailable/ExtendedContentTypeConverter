package burp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class FormURLParser {

    private static FormURLUtilities formURLUtilities = new FormURLUtilities();

    /**
     * Helper method for insertIntoMap()
     * */
    private static void deepMerge(JSONObject source, JSONObject target) throws JSONException {
        for (String key : JSONObject.getNames(source)) {
            Object sourceValue = source.get(key);

            if (!target.has(key)) {
                target.put(key, sourceValue);
            } else {
                Object targetValue = target.get(key);

                if (sourceValue instanceof JSONObject && targetValue instanceof JSONObject) {
                    deepMerge((JSONObject) sourceValue, (JSONObject) targetValue);
                } else if (sourceValue instanceof JSONArray || targetValue instanceof JSONArray) {
                    JSONArray mergedArray = new JSONArray();

                    if (!(targetValue instanceof JSONArray)) {
                        mergedArray.put(targetValue);
                    } else {
                        for (int i = 0; i < ((JSONArray) targetValue).length(); i++) {
                            mergedArray.put(((JSONArray) targetValue).get(i));
                        }
                    }

                    if (sourceValue instanceof JSONArray) {
                        for (int i = 0; i < ((JSONArray) sourceValue).length(); i++) {
                            mergedArray.put(((JSONArray) sourceValue).get(i));
                        }
                    } else {
                        mergedArray.put(sourceValue);
                    }

                    target.put(key, mergedArray);
                } else {
                    JSONArray mergedArray = new JSONArray();
                    mergedArray.put(targetValue);
                    mergedArray.put(sourceValue);
                    target.put(key, mergedArray);
                }
            }
        }
    }

    private static boolean isArray(String symbol) {
        return symbol.equals("[]");
    }

    /**
     * Helper method for parse()
     */
    private static List<String> extractKeys(String key) {
        List<String> keys = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (char c : key.toCharArray()) {
            if (c == '[') {
                if (sb.length() > 0) {
                    keys.add(sb.toString());
                    sb.setLength(0);
                }
            } else if (c == ']') {
                if (sb.length() > 0) {
                    keys.add(sb.toString());
                } else {
                    keys.add("[]"); // Special marker for empty brackets
                }
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            keys.add(sb.toString());
        }

        return keys;
    }

    /**
     * Helper method for parse()
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> insertIntoMap(List<String> keys, String value) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (keys.isEmpty()) return null;

        if (keys.get(0).equals("[]")) {
            map.put("", new ArrayList<>());
        } else {
            map.put(keys.get(0), new LinkedHashMap<>());
        }

        if (keys.size() == 1) {
            Object object = map.get(keys.get(0));
            if (object instanceof Map) map.put(keys.get(0), value);
            else ((List) map.get("")).add(value);
            return map;
        }

        Object currentObject = map;
        boolean arrayNext;
        String currentKey = "";
        for (int i = 1; i < keys.size(); ++i) {
            currentKey = keys.get(i);
            arrayNext = isArray(currentKey);

            if (arrayNext) {
                ArrayList<?> tmp = new ArrayList<>();
                if (currentObject instanceof Map) {
                    ((Map) currentObject).put(keys.get(i - 1), tmp);
                } else {
                    ((List) currentObject).add(tmp);
                }
                currentObject = tmp;
            } else {
                Map<?, ?> tmp = new LinkedHashMap<>();
                if (currentObject instanceof Map) {
                    ((Map) currentObject).put(keys.get(i - 1), tmp);
                } else {
                    ((List) currentObject).add(tmp);
                }
                currentObject = tmp;
            }
        }

        if (currentObject instanceof List) {
            ((List) currentObject).add(value);
        } else {
            ((Map) currentObject).put(keys.get(keys.size() - 1), value);
        }

        return map;
    }

    /**
     * Main method, parse form data and save it into a map
     */
    public static JSONObject parse(String formQuery) throws Exception {
        JSONObject parsed = new JSONObject();
        String[] pairs = formQuery.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length < 2) continue;

            String key = FormURLUtilities.decode(keyValue[0]);
            String value = FormURLUtilities.decode(keyValue[1]);

            List<String> keys = extractKeys(key);
            JSONObject json = new JSONObject(insertIntoMap(keys, value));

            deepMerge(json, parsed);
        }

        return parsed;
    }
}