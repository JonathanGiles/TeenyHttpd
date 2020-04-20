package net.jonathangiles.teenyhttpd;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QueryParams {
    public static final QueryParams EMPTY = new QueryParams("");

    private final String allParams;
    private Map<String, String> map;

    public QueryParams(final String allParams) {
        this.allParams = allParams;
    }

    public Map<String, String> getQueryParams() {
        if (map == null) {
            if (allParams == null || allParams.isEmpty()) {
                map = Collections.emptyMap();
            } else {
                Map<String, String> tempMap = new HashMap<>();

                for (String param : allParams.split("&")) {
                    String[] keyValue = param.split("=", 2);

                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
                        if (!key.isEmpty()) {
                            tempMap.put(key, value);
                        }
                    } catch (UnsupportedEncodingException e) {
                        // ignore
                    }
                }

                this.map = Collections.unmodifiableMap(tempMap);
            }
        }
        return map;
    }

    public String toString() {
        return getQueryParams().toString();
    }
}
