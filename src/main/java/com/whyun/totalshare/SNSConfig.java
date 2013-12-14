package com.whyun.totalshare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.whyun.totalshare.bean.SNSConfigBean;
import com.whyun.util.ConfigFileUtil;
import com.whyun.util.JarUtil;

public class SNSConfig {
	private static final Map<String, SNSConfigBean> CONFIG_MAP = new HashMap<String, SNSConfigBean>();
	private static final String DEFAULT_CONFIG_FILENAME = "snsconfig.xml";
	private static final Logger LOGGER = Logger.getLogger(SNSConfig.class);
	private static volatile  SNSConfig instance = null;
	
	private SNSConfig(String configFile) throws FileNotFoundException {
		if (configFile == null) {
			configFile = ConfigFileUtil.getPath(SNSConfig.class, DEFAULT_CONFIG_FILENAME);
		}
		File file = new File(configFile);
		if (file == null || !file.exists()) {
			configFile = new JarUtil(SNSConfig.class).getJarPath() + "/" + DEFAULT_CONFIG_FILENAME;
			file = new File(configFile);
			if (file == null || !file.exists()) {
				throw new FileNotFoundException("the sns config file is not found.");
			}
		}
		try {
			init(configFile);
		} catch (SAXException e) {
			LOGGER.warn("error occured when parse config file", e);
		} catch (IOException e) {
			LOGGER.warn("error occured when parse config file", e);
		} catch (ParserConfigurationException e) {
			LOGGER.warn("error occured when parse config file", e);
		}
	}
	
	private void init(String configFile) throws SAXException, IOException, ParserConfigurationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("the config file is " + configFile);
		}
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//通过文档构建器工厂获取一个文档构建器
        DocumentBuilder db = dbf.newDocumentBuilder();
            //通过文档通过文档构建器构建一个文档实例
        Document doc = db.parse(configFile);
        
        NodeList list = doc.getElementsByTagName("sns");
        if (list != null) {
        	for(int i=0,len=list.getLength();i<len;i++) {
        		Node snsNode = list.item(i);
        		NodeList snsAttrs = snsNode.getChildNodes();
        		if (snsAttrs != null) {
        			SNSConfigBean bean = new SNSConfigBean();
        			for(int j=0,len2=snsAttrs.getLength();j<len2;j++) {
        				Node attrNow = snsAttrs.item(j);
        				if (attrNow.getNodeType() == Node.ELEMENT_NODE) {
        					String name = attrNow.getNodeName();
            				String value = attrNow.getTextContent();
            				if ("type".equals(name)) {
            					bean.setType(value);
            				} else if ("clientId".equals(name)) {
            					bean.setClientId(value);
            				} else if ("clientKey".equals(name)) {
            					bean.setClientKey(value);
            				} else if("scope".equals(name)) {
            					bean.setScope(value);
            				}
        				}
        				
        			}
        			String typeNow = bean.getType();
        			if (typeNow != null) {
        				CONFIG_MAP.put(typeNow, bean);
        			}
        		}
        	}
        }
	}
	
	public static SNSConfig getInstance(String configFile)
			throws FileNotFoundException {
		if (instance == null) {
			synchronized(SNSConfig.class) {
				if (instance == null) {
					instance = new SNSConfig(configFile);
				}
			}
		}
		return instance;
	}
	
	public static SNSConfig getInstance()
			throws FileNotFoundException {
		return getInstance(null);
	}
	
	public SNSConfigBean getConfig(String type) {
		return CONFIG_MAP.get(type);
	}
}
