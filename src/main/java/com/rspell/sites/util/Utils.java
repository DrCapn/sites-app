package com.rspell.sites.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String setToJson(Set<String> setOfStrings) {
        if (setOfStrings != null) {
            try {
                return mapper.writeValueAsString(setOfStrings);
            } catch (JsonProcessingException jpe) {
                log.error("Unable to convrt set to json string (return empty string)", jpe);
            }
        }
        return "";
    }
    public static Set<String> jsonToSet(String jsonSet) {
        if (jsonSet != null && !jsonSet.isBlank()) {
            try {
                return mapper.readValue(jsonSet, new TypeReference<>() {});
            } catch (JsonProcessingException jpe) {
                log.error("Unable to convert json string to set (return empty set)", jpe);
            }
        }
        return new LinkedHashSet<>();
    }
}
