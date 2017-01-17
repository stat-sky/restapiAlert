package io.transwarp.main;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.dom4j.Element;

import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.UtilTool;

public class Test {

	private String managerIP = "172.16.2.63";
	private String username = "xhy";
	private String password = "123456";
	
	public void getAlert() {
		try {
			/* 读取配置 */
			Element config = Constant.prop_restapi.getElement("purpose", Constant.ALERT);
			HttpMethodTool method = HttpMethodTool.getMethod("http://" + managerIP + ":8180", username, password);
			/* 构建url */
			Map<String, Object> urlParam = new HashMap<String, Object>();
//			urlParam.put("status", "ACTIVE");
			urlParam.put("status", "CLEARED");
			/* 获取当前时间戳 */
			long end = System.currentTimeMillis();
			long start = end - 24 * 60 * 60 * 1000;
			urlParam.put("start", start);
			urlParam.put("end", end);
			String url = UtilTool.buildURL(config.elementText("url"), urlParam);
			String result = method.execute(url, config.elementText("http-method"), null);
			JSONArray array = JSONArray.fromObject(result);
			int num = array.size();
			for(int i = 0; i < num; i++) {
				JSONObject json = array.getJSONObject(i);
				System.out.println(json.get("title"));
				System.out.println("  告警级别：" + json.get("severity"));
				System.out.println("  告警状态：" + json.get("status"));
				System.out.println("  告警描述：" + json.get("description"));
				System.out.println("  涉及资源：" + json.get("contextName"));
				System.out.println("  告警分类：" + json.get("category") + "\n");
			}
			method.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Test test = new Test();
		test.getAlert();
	}
}
