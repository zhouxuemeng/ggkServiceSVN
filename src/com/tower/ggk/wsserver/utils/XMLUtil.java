package com.tower.ggk.wsserver.utils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

public class XMLUtil {

	public static Map xml2map(String xmlString) throws DocumentException {
		Document doc = DocumentHelper.parseText(xmlString);
		Element rootElement = doc.getRootElement();
		Map<String, Object> map = new HashMap<String, Object>();
		ele2map(map, rootElement);
		//System.out.println(map);
		// ����xml2map��ɣ�����Ĵ����ǽ�mapת����json�����۲����ǵ�xml2mapת�����Ƿ�ok
		//String string = JSONObject.fromObject(map).toString();
		//System.out.println(string);
		return map;
	}
	
	/***
	 * ���ķ����������еݹ����
	 * 
	 * @param map
	 * @param ele
	 */
	public static void ele2map(Map map, Element ele) {
		System.out.println(ele);
		// ��õ�ǰ�ڵ���ӽڵ�
		List<Element> elements = ele.elements();
		if (elements.size() == 0) {
			// û���ӽڵ�˵����ǰ�ڵ���Ҷ�ӽڵ㣬ֱ��ȡֵ����
			map.put(ele.getName(), ele.getText());
		} else if (elements.size() == 1) {
			// ֻ��һ���ӽڵ�˵�����ÿ���list�������ֱ�Ӽ����ݹ鼴��
			Map<String, Object> tempMap = new HashMap<String, Object>();
			ele2map(tempMap, elements.get(0));
			map.put(ele.getName(), tempMap);
		} else {
			// ����ӽڵ�Ļ��͵ÿ���list������ˣ��������ӽڵ��нڵ�������ͬ��
			// ����һ��map����ȥ��
			Map<String, Object> tempMap = new HashMap<String, Object>();
			for (Element element : elements) {
				tempMap.put(element.getName(), null);
			}
			Set<String> keySet = tempMap.keySet();
			for (String string : keySet) {
				Namespace namespace = elements.get(0).getNamespace();
				List<Element> elements2 = ele.elements(new QName(string,
						namespace));
				// ���ͬ������Ŀ����1���ʾҪ����list
				if (elements2.size() > 1) {
					List<Map> list = new ArrayList<Map>();
					for (Element element : elements2) {
						Map<String, Object> tempMap1 = new HashMap<String, Object>();
						ele2map(tempMap1, element);
						list.add(tempMap1);
					}
					map.put(string, list);
				} else {
					// ͬ��������������1��ֱ�ӵݹ�ȥ
					Map<String, Object> tempMap1 = new HashMap<String, Object>();
					ele2map(tempMap1, elements2.get(0));
					map.put(string, tempMap1);
				}
			}
		}
	}
	
	public static String formatXml(Document document, String charset, boolean istrans) { 
	     OutputFormat format = OutputFormat.createPrettyPrint(); 
	     format.setEncoding(charset); 
	     StringWriter sw = new StringWriter(); 
	     XMLWriter xw = new XMLWriter(sw, format); 
	     xw.setEscapeText(istrans); 
	     try { 
	             xw.write(document); 
	             xw.flush(); 
	             xw.close(); 
	     } catch (IOException e) { 
	             System.out.println("��ʽ��XML�ĵ������쳣�����飡"); 
	             e.printStackTrace(); 
	     } 
	     return sw.toString(); 
	}
	
	public static Document getDocumentByString(String inxml){
		String getxml=inxml;
		org.dom4j.io.SAXReader saxReader = new SAXReader();
		getxml=getxml.replace("\n", "").replace("\r", "").replace(" ", "").replace("<![CDATA[", "").replace("]]>", "").replace("xmlversion=\"1.0\"encoding", "xml version=\"1.0\" encoding");
		Document doc = null;
		try {
			doc = saxReader.read(new   ByteArrayInputStream(getxml.getBytes("UTF-8")));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
	
	public static String getbzXML(String inxml){
		String getxml=inxml;
		getxml=getxml.replace("\n", "").replace("\r", "").replace(" ", "").replace("<![CDATA[", "").replace("]]>", "").replace("xmlversion=\"1.0\"encoding", "xml version=\"1.0\" encoding");
		return getxml;
	}
	
	
	
}
