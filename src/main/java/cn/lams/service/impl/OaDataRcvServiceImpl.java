package cn.lams.service.impl;

import ch.qos.logback.classic.Logger;
import cn.lams.service.BaseService;
import cn.lams.service.i.OaDataRcvService;
import cn.lams.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service("oaDataRcvService")
public class OaDataRcvServiceImpl extends BaseService implements
		OaDataRcvService {

	@Override
	public void dataReceive() {
		String volSql = "select * from "+zjkVol+" where GDBZ = 0";
		List<Map<String, Object>> volListMap =  jdbcTemplate_zjk.queryForList(volSql);
		for(Map<String, Object> volMap : volListMap){
			String qzh = volMap.get("qzh") == null ? lamsDefaultQzh : volMap.get("qzh").toString();
			insertDVOL4Map(volMap, getDvolMappingArc(), "D_VOL"+wsCode, qzh);
		}

		String fileSql = "select * from "+zjkFile+" where GDBZ = 0";
		List<Map<String, Object>> fileListMap =  jdbcTemplate_zjk.queryForList(fileSql);
		for(Map<String, Object> fileMap : fileListMap){
			String oaFilePid = fileMap.get("pid").toString();
			List<String> stringDids = jdbcDao.quert4List("select did from d_vol" + wsCode + " where " + lamsWisVolFiled + "= '"+oaFilePid+"'");
			if(stringDids.size() > 0){
				Integer dvolDid = Integer.parseInt(stringDids.get(0).toString());
				String dfileQzh = jdbcDao.query4String("select qzh from d_vol" + wsCode + " where did = '"+dvolDid+"'");
				insertDfile4Map(fileMap, getDfileMappingArc(), wsCode, dfileQzh, dvolDid);
			}
		}

		String efileSql = "select * from "+zjkEfile+" where GDBZ = 0";
		List<Map<String, Object>> efileListMap =  jdbcTemplate_zjk.queryForList(efileSql);
		for(Map<String, Object> efileMap : efileListMap){
			String oaEFilePid = efileMap.get("pid").toString();
			List<String> stringDids = jdbcDao.quert4List("select did from d_file" + wsCode + " where " + lamsWisFileFiled + "= '"+oaEFilePid+"'");
			if(stringDids.size() > 0) {
				Integer dfileDid = Integer.parseInt(stringDids.get(0).toString());
				String oaEfileDid = efileMap.get("did").toString();
				String etitle = efileMap.get("title").toString();
				String eFileTableName = "E_FILE" + wsCode;
				String efilepath = File.separator + "wis" + File.separator + eFileTableName + File.separator
						+ DateUtil.getCurrentDateStr() +  File.separator + System.currentTimeMillis() + File.separator;
				String absolutePath = lamsBasePath + efilepath + etitle;
				Integer result = -1;
				try {
					result = getImgByte(oaEfileDid, absolutePath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				File efile = new File(absolutePath);
				if (result == 0) {
					insertEfile(efile, efilepath, etitle, wsCode, dfileDid, oaEfileDid, "");
				}
				if(efile.length() == 0){
					try {
						FileUtils.deleteDirectory(new File(lamsBasePath + efilepath));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {
			updataData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void updataData(){
		String upVolSql = "select * from "+zjkVol+" where GDBZ = 2";
		List<Map<String, Object>> upVolListMap =  jdbcTemplate_zjk.queryForList(upVolSql);
		for(Map<String, Object> upVolMap : upVolListMap){
			String voldid = upVolMap.get("did") == null ? "" : upVolMap.get("did").toString();
			if(StringUtils.isNotBlank(voldid)){
				updateDVOL4Map(upVolMap,getDvolMappingArc(),"D_VOL"+wsCode,"");
			}
		}
		String upFileSql = "select * from "+zjkFile+" where GDBZ = 2";
		List<Map<String, Object>> upFileListMap =  jdbcTemplate_zjk.queryForList(upFileSql);
		for(Map<String, Object> upFileMap : upFileListMap){
			String filedid = upFileMap.get("did") == null ? "" : upFileMap.get("did").toString();
			if(StringUtils.isNotBlank(filedid)){
				updateDFILE4Map(upFileMap,getDfileMappingArc(),"D_FILE"+wsCode,"");
			}
		}
	}
	public Integer getImgByte(final String id, final String absolutePath) throws FileNotFoundException {
		jdbcTemplate_zjk.execute(
			"select file_data from "+zjkEfile+" where did=?",
			new PreparedStatementCallback() {
				public Object doInPreparedStatement(PreparedStatement ps)
						throws SQLException, DataAccessException {
					File efile = new File(absolutePath);
					BufferedOutputStream bufferedOutput = null;
					try {
						if(!efile.exists()){
							try {
								FileUtils.touch(efile);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						OutputStream output = new FileOutputStream(efile);
						bufferedOutput = new BufferedOutputStream(output);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					ps.setString(1, id);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						InputStream inputStream = rs.getBinaryStream("file_data");
						try {
							byte[] bytes = IOUtils.toByteArray(inputStream);
							System.out.print(bytes.length+",");
							bufferedOutput.write(bytes);
							bufferedOutput.flush();
						} catch (IOException e) {
							return -1;
						}finally {
							try {
								bufferedOutput.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					return 0;
				}
			});
		return 0;
	}
	@Autowired
	private JdbcTemplate jdbcTemplate_zjk;
	private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

}
