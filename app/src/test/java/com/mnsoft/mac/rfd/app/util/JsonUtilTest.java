package com.mnsoft.mac.rfd.app.util;

import com.mnsoft.mac.rfd.app.redis.PathInfo;
import com.mnsoft.mac.rfd.common.util.JsonUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by goldgong on 2018-08-03.
 */
public class JsonUtilTest {

    @Test
    public void objToJsonTest() {
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSeq(1);
        pathInfo.setPathId("1_1234565");
        pathInfo.setLatestGpsTime(1234565);

        System.out.println(JsonUtil.objectToJson(pathInfo));
    }

    @Test
    public void jsonToObjectTest() {
        String jsonStr = "{\"pathSeq\":1,\"pathId\":1,\"latestGpsTime\":1234565}";
        PathInfo pathInfo = JsonUtil.jsonToObject(jsonStr, PathInfo.class);
        System.out.println(pathInfo.toString());
    }

    @Test
    public void mapToJsonTest() {
        Map<String, Object> item = new HashMap();
        item.put("pathSeq", 1);
        item.put("pathId", "pathId");

        System.out.println(JsonUtil.objectToJson(item));
    }

    @Test
    public void jsonToMapTest() {
        String json = "{\"pathSeq\":1,\"pathId\":\"pathId\"}";
        //{pathSeq=1, pathId=pathId}
        Map map = JsonUtil.jsonToObject(json, Map.class);
        System.out.println(map.toString());
    }



}
