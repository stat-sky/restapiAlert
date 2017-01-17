package io.transwarp.util;

public class Constant {

	public static ConfigRead prop_restapi = null;
	
	public static final String USER_LOGIN = "用户登录";
	public static final String USER_LOGOUT = "用户登出";
	public static final String ALERT = "告警信息";
	
	static {
		try {
			prop_restapi = new ConfigRead("config/restapiURL.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
