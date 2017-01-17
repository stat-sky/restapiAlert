package io.transwarp.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ConfigRead {

	private static Logger logger = Logger.getLogger(ConfigRead.class);
	private List<Element> values;
	
	@SuppressWarnings("unchecked")
	public ConfigRead(String path) throws Exception{
		super();
		File file = new File(path);
		if(!file.exists()) {
			throw new IOException("xml file not found : " + path);
		}
		logger.debug("load xml configuration is " + path);
		Document document = new SAXReader().read(file);
		Element rootElement = document.getRootElement();
		this.values = rootElement.elements();
	}
	
	/**
	 * 根据子节点key-value对获取配置
	 * @param key 子节点的key值
	 * @param value 子节点的value值
	 * @return 该配置的org.dom4j.Element数据
	 */
	public Element getElement(String key, String value){
		for(Element element : values) {
//			logger.info(element.elementText(key));
			String keyValue = element.elementText(key);
			if(keyValue != null && keyValue.equals(value)) {
				return element;
			}
		}
		return null;
	}
	
	/**
	 * 获取该配置的所有信息
	 * @return 该配置文件的所有信息的list列表
	 */
	public List<Element> getAll() {
		return this.values;
	}
}
