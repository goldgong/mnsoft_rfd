package com.mnsoft.mac.rfd.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json Util
 * Created by goldgong on 2018-08-02.
 */
public class JsonUtil {


    /**
     * Java Object(collection 포함) -> Json String
     * @param object
     * @return
     * @throws IOException
     */
    public static <T> String objectToJson(T object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Json String -> Java Object (collection 포함)
     * @param jsonStr
     * @return
     * @throws IOException
     */
    public static <T> T jsonToObject(String jsonStr, Class<T> classz) {
        T object = null;
        try {
            object = new ObjectMapper().readValue(jsonStr, classz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return object;
    }
}
