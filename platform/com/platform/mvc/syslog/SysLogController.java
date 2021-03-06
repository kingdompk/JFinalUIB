package com.platform.mvc.syslog;

import com.jfinal.log.Log;
import com.platform.annotation.Controller;
import com.platform.mvc.base.BaseController;

/**
 * 系统日志管理
 * @author 董华健
 */
@Controller("/platform/sysLog")
public class SysLogController extends BaseController {

	@SuppressWarnings("unused")
	private static final Log log = Log.getLog(SysLogController.class);
	
	private SysLogService sysLogService;
	
	/**
	 * 日志管理列表
	 */
	public void index() {
		defaultOrder(Syslog.column_startdate, "desc"); // 默认排序方式，指定列名和排序方式
		paging(splitPage, Syslog.sqlId_splitPageSelect, Syslog.sqlId_splitPageFrom);
		render("/platform/sysLog/list.html");
	}

	/**
	 * 查看日志
	 */
	public void view() {
		Syslog log =  Syslog.dao.findFirst(getSql(Syslog.sqlId_view), getPara());
		setAttr("sysLog", log);
		render("/platform/sysLog/view.html");
	}

	/**
	 * 删除日志
	 */
	public void delete() {
		sysLogService.baseDelete(Syslog.table_name, getPara() == null ? ids : getPara());
		redirect("/platform/sysLog");
	}

}


