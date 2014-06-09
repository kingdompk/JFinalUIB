package little.ant.pingtai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import little.ant.pingtai.common.EhcacheFactory;
import little.ant.pingtai.common.ParamInit;
import little.ant.pingtai.common.SplitPage;
import little.ant.pingtai.model.Module;
import little.ant.pingtai.model.Operator;
import little.ant.pingtai.run.JfinalConfig;
import little.ant.pingtai.tools.ToolUtils;

import org.apache.log4j.Logger;

public class OperatorService extends BaseService {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(OperatorService.class);
	
	/**
	 * 保存
	 * @param operator
	 * @return
	 */
	public String save(Operator operator){
		String ids = ToolUtils.getUuidByJdk(true);
		operator.set("ids", ids).save();
		
		// 缓存
		EhcacheFactory cacheFactory = EhcacheFactory.getInstance();
		cacheFactory.add(EhcacheFactory.cache_name_system, ParamInit.cacheStart_operator + operator.getStr("ids"), operator);
		cacheFactory.add(EhcacheFactory.cache_name_system, ParamInit.cacheStart_operator + operator.getStr("url"), operator);
		
		return ids;
	}

	/**
	 * 更新
	 * @param operator
	 */
	public void update(Operator operator){
		operator.update();
		
		// 缓存
		EhcacheFactory cacheFactory = EhcacheFactory.getInstance();
		cacheFactory.update(EhcacheFactory.cache_name_system, ParamInit.cacheStart_operator + operator.getStr("ids"), operator);
		cacheFactory.update(EhcacheFactory.cache_name_system, ParamInit.cacheStart_operator + operator.getStr("url"), operator);
	}

	/**
	 * 更新
	 * @param ids
	 */
	public void delete(String ids){
		Operator operator = Operator.dao.findById(ids);;
		// 缓存
		EhcacheFactory cacheFactory = EhcacheFactory.getInstance();
		cacheFactory.delete(EhcacheFactory.cache_name_system, ParamInit.cacheStart_operator + operator.getStr("ids"));
		cacheFactory.delete(EhcacheFactory.cache_name_system, ParamInit.cacheStart_operator + operator.getStr("url"));
		
		// 删除
		operator.delete();
	}
	
	/**
	 * 获取子节点数据
	 * 
	 * @param station
	 * @param module
	 * @return
	 * @throws Exception
	 */
	public String childNodeData(String moduleIds){
		StringBuffer sqlModule = new StringBuffer();
		List<Module> listModule = null;

		StringBuffer sqlOperator = new StringBuffer();
		List<Operator> listOperator = new ArrayList<Operator>(0);
		
		String dbType = (String) JfinalConfig.getParamMapValue(JfinalConfig.db_type_key);

		if (null == moduleIds) {
			// 1.模块功能初始化调用
			sqlModule.append(" select ");
			if(dbType.equals(JfinalConfig.db_type_postgresql)){// pg
				sqlModule.append(" 'module_' || pm.ids as ids, ");
			}else if(dbType.equals(JfinalConfig.db_type_mysql)){// mysql
				sqlModule.append(" concat('module_' , pm.ids) as ids, ");
			}
			sqlModule.append(" (select ps.names from pt_systems ps where ps.ids = pm.systemsIds) as names, ");
			sqlModule.append(" pm.isParent, pm.images from pt_module pm where pm.parentModuleIds is null order by pm.orderIds asc ");
			listModule = Module.dao.find(sqlModule.toString());
			
		} else if (null != moduleIds) {
			moduleIds = moduleIds.replace("module_", "");
			// 2.通用子节点查询
			sqlModule.append(" select ");
			if(dbType.equals(JfinalConfig.db_type_postgresql)){// pg
				sqlModule.append(" 'module_' || ids as ids, ");
			}else if(dbType.equals(JfinalConfig.db_type_mysql)){// mysql
				sqlModule.append(" concat('module_' , ids) as ids, ");
			}
			sqlModule.append(" names, isParent , images from pt_module where parentModuleIds = ? order by orderIds asc ");
			listModule = Module.dao.find(sqlModule.toString(), moduleIds);

			sqlOperator.append(" select ");
			if(dbType.equals(JfinalConfig.db_type_postgresql)){// pg
				sqlOperator.append(" 'operator_' || ids as ids, ");
			}else if(dbType.equals(JfinalConfig.db_type_mysql)){// mysql
				sqlOperator.append(" concat('operator_' , ids) as ids, ");
			}
			sqlOperator.append(" names from pt_operator where moduleIds = ? order by url asc ");
			listOperator = Operator.dao.find(sqlOperator.toString(), moduleIds);
		}

		StringBuffer sb = new StringBuffer();
		sb.append("[");

		int operatorSize = listOperator.size();
		int operatorIndexSize = operatorSize - 1;
		for (Operator operator : listOperator) {
			sb.append(" { ");
			sb.append(" id : '").append(operator.getStr("ids")).append("', ");
			sb.append(" name : '").append(operator.getStr("names")).append("', ");
			sb.append(" isParent : false, ");

			sb.append(" checked : false, ");

			sb.append(" font : {'font-weight':'bold'}, ");
			sb.append(" icon : '/jsFile/zTree/css/zTreeStyle/img/diy/5.png' ");
			sb.append(" }");
			if (listOperator.indexOf(operator) < operatorIndexSize) {
				sb.append(", ");
			}
		}

		int moduleSize = listModule.size();
		int moduleIndexSize = moduleSize - 1;
		if (operatorSize > 0 && moduleSize > 0) {
			sb.append(", ");
		}
		for (Module module : listModule) {
			sb.append(" { ");
			sb.append(" id : '").append(module.getStr("ids")).append("', ");
			sb.append(" name : '").append(module.getStr("names")).append("', ");
			sb.append(" isParent : ").append(module.getStr("isparent")).append(", ");
			sb.append(" font : {'font-weight':'bold'}, ");
			sb.append(" icon : '/jsFile/zTree/css/zTreeStyle/img/diy/").append(module.getStr("images")).append("' ");
			sb.append(" }");
			if (listModule.indexOf(module) < moduleIndexSize) {
				sb.append(", ");
			}
		}

		sb.append("]");

		return sb.toString();
	}
	
	/**
	 * 分页
	 * @param splitPage
	 */
	public void list(SplitPage splitPage){
		String select = " select o.*, m.names as modulenames, s.names as systemsnames ";
		splitPageBase(splitPage, select);
	}
	
	protected void makeFilter(Map<String, String> queryParam, StringBuffer formSqlSb, List<Object> paramValue) {
		formSqlSb.append(" from pt_operator o ");
		formSqlSb.append(" left join pt_module m on o.moduleids=m.ids ");
		formSqlSb.append(" left join pt_systems s on s.ids=m.systemsids ");
		formSqlSb.append(" where 1=1 ");
		
		if(null == queryParam){
			return;
		}
		
		String names = queryParam.get("names");//功能名称
		String url = queryParam.get("url");//功能名称
		
		if(null!=names && !names.equals("")){
			formSqlSb.append(" and o.names like ? ");
			paramValue.add("%" + names.trim() + "%");
		}

		if(null!=url && !url.equals("")){
			formSqlSb.append(" and o.url like ? ");
			paramValue.add("%" + url.trim() + "%");
		}
	}
	
}
