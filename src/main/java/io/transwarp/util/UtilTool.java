package io.transwarp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UtilTool {
	
	private static Logger logger = Logger.getLogger(UtilTool.class);
	/**
	 * 根据带占位符的url和url参数来构建可用的url字符串
	 * @param original 带占位符的url
	 * @param urlParam url中使用的参数
	 * @return 可用的url
	 * @throws Exception 构建失败
	 */
	public static String buildURL(String original, Map<String, Object> urlParam) throws Exception {
		if(urlParam == null) urlParam = new HashMap<String, Object>();
		String url = null;
		if(original.indexOf("{") == -1) {
			logger.debug("this url has not parameter");
			url = original;
		}else if(original.indexOf("[") == -1) {
			logger.debug("this url has required parameter but not optional parameter");
			url = buildURLWithRequired(original, urlParam);
		}else {
			logger.debug("this url has optional parameter");
			url = buildURLWithOptional(original, urlParam);
		}
		return url;
	}
	//存在且仅存在必选参数的url构建
	private static String buildURLWithRequired(String original, Map<String, Object> urlParam) throws Exception{
		StringBuffer urlBuild = new StringBuffer();
		String[] urlSplits = original.split("\\{");
		int numberOfSplit = urlSplits.length;
		if(numberOfSplit < 1) {
			throw new RuntimeException("原始url切分错误");
		}
		urlBuild.append(urlSplits[0]);
		for(int i = 1; i < numberOfSplit; i++) {
			String[] items = urlSplits[i].split("\\}");
			Object value = urlParam.get(items[0]);
			if(value == null || value.equals("")) {
				throw new RuntimeException("there is not this param : " + items[0]);
			}
			urlBuild.append(value);
			if(items.length == 2) urlBuild.append(items[1]);  
			
		}
		return urlBuild.toString();
	}
	/* 存在可选参数的url构建 */
	private static String buildURLWithOptional(String original, Map<String, Object> urlParam) throws Exception {
		StringBuffer urlBuild = new StringBuffer();
		String[] urlSplitByOptionals = original.split("\\[");
		int numberOfSplit = urlSplitByOptionals.length;
		if(numberOfSplit < 1) {
			throw new RuntimeException("原始url切分错误");
		}		
		urlBuild.append(buildURL(urlSplitByOptionals[0], urlParam));
		boolean hasParam = (urlBuild.toString().indexOf("?") == -1) ? false : true;
		for(int i = 0; i < numberOfSplit; i++) {
			urlSplitByOptionals[i] = urlSplitByOptionals[i].substring(1, urlSplitByOptionals[i].length() - 1);
			logger.debug("urlSplitByOptional is : " + urlSplitByOptionals[i]);
			String[] items = urlSplitByOptionals[i].split("\\&");
			for(int j = 0; j < items.length; j++) {
				try {
					String urlSplit = buildURLWithRequired(items[j], urlParam);
					logger.debug("read : " + items[j] + "  " + urlSplit);
					if(hasParam) {
						urlBuild.append("&").append(urlSplit);
					}else {
						urlBuild.append("?").append(urlSplit);
						hasParam = true;
					}
				}catch(RuntimeException e) {}
			}
		}
		return urlBuild.toString();
	}
	
	/**
	 * 将json字符串转换为map类型返回
	 * @param jsonString 要进行转换的json字符串
	 * @return 转换后返回的map类型参数
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> changeJsonToMap(String jsonString) throws Exception {
		Map<String, Object> answer = new HashMap<String, Object>();
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		
		for(Iterator<String> keys = jsonObject.keys(); keys.hasNext(); ) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			if(value.getClass().equals(JSONObject.class)) {
				String json = value.toString();
				answer.put(key, changeJsonToMap(json));
			}else if(value.getClass().equals(JSONArray.class)) {
				List<Object> list = new ArrayList<Object>();
				JSONArray array = JSONArray.fromObject(value.toString());
				int length = array.size();
				for(int i = 0; i < length; i++) {
					Object item = array.get(i);
					if(item.getClass().equals(JSONObject.class)) {
						String json = item.toString();
						list.add(changeJsonToMap(json));
					}else {
						list.add(item);
					}
				}
				answer.put(key, list);
			}else {
				answer.put(key, value);
			}
		}
		return answer;
	}
	
	/**
	 * 将map类型参数转换成json字符串
	 * @param param 要进行转换的map类型参数
	 * @return 转换后的json字符串
	 */
	public static String changeMapToString(Map<String, Object> param) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(param);
		return jsonObject.toString();
	}
}
