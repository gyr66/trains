package com.gyr.trains.crawler.utils;

import java.util.HashMap;

public class ParseUrlUtil {
    public static HashMap<String, String> parse(String url){
        HashMap<String, String> strUrlParas = new HashMap<>();
        String strUrl = "";
        String strUrlParams = "";
        if(url.contains("?")) {
            String[] strUrlPatten = url.split("\\?");
            strUrl = strUrlPatten[0];
            strUrlParams = strUrlPatten[1];
        } else {
            strUrl = url;
            strUrlParams = "";
        }
        strUrlParas.put("URL", strUrl);
        String[] params;
        if (strUrlParams.contains("&")) {
            params = strUrlParams.split("&");
        } else {
            params = new String[] { strUrlParams };
        }
        for (String p : params) {
            if(p.contains("=")) {
                String[] param = p.split("=");
                if(param.length == 1){
                    strUrlParas.put(param[0],"");
                } else {
                    String key = param[0];
                    String value = param[1];
                    strUrlParas.put(key, value);
                }
            } else {
                strUrlParas.put("errorParam", p);
            }
        }
        return strUrlParas;
    }
}