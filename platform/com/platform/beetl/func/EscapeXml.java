package com.platform.beetl.func;

import org.apache.commons.lang3.StringEscapeUtils;
import org.beetl.core.Context;
import org.beetl.core.Function;

import com.jfinal.log.Log;

/**
 * 过滤xml文档函数
 * @author 董华健
 */
public class EscapeXml implements Function {

	private static final Log log = Log.getLog(EscapeXml.class);
	
	/**
	 * 过滤xml文档函数实现
	 */
	@Override
	public Object call(Object[] arg, Context context) {
		if(arg.length != 1 || null == arg[0] || !(arg[0] instanceof String)){
			return arg[0];
		}
		String content = null;// 
		try {
			content = (String) arg[0];
		} catch (Exception e) {
			log.error("EscapeXml参数转String异常:arg = " + arg[0]);
			return arg[0];
		}

		return StringEscapeUtils.escapeXml11(content);
	}

}
