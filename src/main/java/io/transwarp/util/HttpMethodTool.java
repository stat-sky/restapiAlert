package io.transwarp.util;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

public class HttpMethodTool {

	private static Logger logger = Logger.getLogger(HttpMethodTool.class);
	private static HttpMethodTool method = null;
	//登录信息
	private static String manager = null;
	private static String username = null;
	private static String password = null;
	
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private CloseableHttpResponse response = null;
	
	private HttpMethodTool() {
		super();
	}
	
	/**
	 * 根据给定的用户名、密码、manager节点连接串获取rest api的执行函数
	 * @param manager1 连接的manager节点连接串
	 * @param username1 用户名
	 * @param password1 密码
	 * @return rest api的执行函数
	 * @throws Exception
	 */
	public static HttpMethodTool getMethod(String manager1, String username1, String password1) throws Exception{
		if(method == null || !manager.equals(manager1) || !username.equals(username1) || !password.equals(password1)) {
			manager = manager1;
			username = username1;
			password = password1;
			method = new HttpMethodTool();
			try {
				boolean ok = method.login(username, password);
				if(!ok) {
					throw new RuntimeException("login failer");
				}
			}catch(RuntimeException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
				System.exit(1);				
			}

		}
		return method;
	}
	/**
	 * 关闭该rest api的执行函数
	 * @return 成功关闭返回true，否则返回false
	 */
	public boolean close() {
		try {
			boolean userLogout = logout();
			method = null;
			this.httpClient = null;
			return userLogout;
		}catch(Exception e) {
			logger.error("error at close method");
		}
		return false;
	}
	
	/**
	 * 执行指定的rest api获取结果的json字符串
	 * @param url 执行的rest api的url连接串
	 * @param httpMethod 执行的http方法
	 * @param paramJson 执行使用参数的json字符串
	 * @return 执行结果的json字符串
	 */
	public String execute(String url, String httpMethod, String paramJson) {
		String result = null;
		HttpEntity entity = null;
		if(url.indexOf("http") == -1) {
			url = manager + "/api" + url;
		}
		logger.debug("execute url is : \"" + url + "\", httpMethod is " + httpMethod);
		switch(httpMethod) {
		case "get" : entity = this.getHttpMethod(url); break;
		case "put" : entity = this.putHttpMethod(url, paramJson); break;
		case "post" : entity = this.postHttpMethod(url, paramJson); break;
		case "delete" : entity = this.deleteHttpMethod(url, paramJson); break;
		default : logger.error("execute httpMethod \"" + httpMethod + "\" is error"); break;
		}
		if(entity == null) {
			logger.error("result is null");
		}else {
			try {
				result = EntityUtils.toString(entity);
				response.close();
			} catch(Exception e) {
				logger.error("error at get result of execute : " + e.getMessage());
			}
		}
		return result;
	}
	
	//rest api执行函数进行登录
	private boolean login(String username, String password) throws Exception {
		boolean loginSuccess = true;
		Element loginConfig = Constant.prop_restapi.getElement("purpose", Constant.USER_LOGIN);
		String url = loginConfig.elementText("url");
		//构建参数
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userName", username);
		param.put("userPassword", password);
		String paramJson = UtilTool.changeMapToString(param);
		String resultOfLogin = execute(url, loginConfig.elementText("http-method"), paramJson);
//		logger.info(resultOfLogin);
		Map<String, Object> resultMap = UtilTool.changeJsonToMap(resultOfLogin);
		for(Iterator<String> keys = resultMap.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(key.equals("messageKey")) {
				loginSuccess = false;
				logger.error(resultOfLogin);
				throw new RuntimeException(resultMap.get("message").toString());
			}
		}
		return loginSuccess;
	}
	
	//登出
	private boolean logout() throws Exception{
		Element logoutConfig = Constant.prop_restapi.getElement("purpose", Constant.USER_LOGOUT);
		String url = logoutConfig.elementText("url");
		String httpMethod = logoutConfig.elementText("http-method");
		String logoutResult = execute(url, httpMethod, null);
		if(logoutResult.equals("success")) {
			return true;
		}
		return false;
	}
	

	//执行get 的http方法
	private HttpEntity getHttpMethod(String url) {
		HttpGet getRequest = new HttpGet(url);
		try {
			response = httpClient.execute(getRequest);
			return response.getEntity();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	//执行put的http方法
	private HttpEntity putHttpMethod(String url, String json) {
		HttpPut putRequest = new HttpPut(url);
		try {
			if(json != null) {
				StringEntity stringEntity = new StringEntity(json);
				putRequest.setEntity(stringEntity);
			}			
			response = httpClient.execute(putRequest);
			return response.getEntity();
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	//执行post的http方法
	private HttpEntity postHttpMethod(String url, String json) {
		HttpPost postRequest = new HttpPost(url);
		try {
			if(json != null) {
				StringEntity stringEntity = new StringEntity(json);
				postRequest.setEntity(stringEntity);
			}
			response = httpClient.execute(postRequest);
			return response.getEntity();
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	//执行delete的http方法
	private HttpEntity deleteHttpMethod(String url, String json) {
		HttpDeleteWithBody deleteRequest = new HttpDeleteWithBody(url);
		try {
			if(json != null) {
				StringEntity stringEntity = new StringEntity(json);
				deleteRequest.setEntity(stringEntity);
			}
			response = httpClient.execute(deleteRequest);
			return response.getEntity();
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	//自定义的delete的http方法
	private class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
		public static final String METHOD_NAME = "DELETE";
		public String getMethod() {
			return METHOD_NAME;
		}
		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}
		@SuppressWarnings("unused")
		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}
		@SuppressWarnings("unused")
		public HttpDeleteWithBody() {
			super();
		}
		
	}
}
