package cn.lams.service.impl;

import ch.qos.logback.classic.Logger;
import cn.lams.service.BaseService;
import cn.lams.service.i.OaDataRcvService;
import cn.lams.util.DateUtil;
import org.apache.commons.io.IOUtils;
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
			Integer volDid =insertDVOL4Map(volMap, getDvolMappingArc(), "D_VOL"+wsCode, qzh);
			String oaVolDid = volMap.get("did").toString();
			String fileSql = "select * from "+zjkFile+" where pid = '"+oaVolDid+"'";
			List<Map<String, Object>> fileListMap =  jdbcTemplate_zjk.queryForList(fileSql);
			for(Map<String, Object> fileMap : fileListMap){
				Integer fileDid = insertDfile4Map(fileMap, getDfileMappingArc(), wsCode, qzh, volDid);
				String oaFileDid = fileMap.get("did").toString();
				String efileSql = "select * from "+zjkEfile+" where pid = '"+oaFileDid+"'";
				List<Map<String, Object>> efileListMap =  jdbcTemplate_zjk.queryForList(efileSql);
				for(Map<String, Object> efileMap : efileListMap){
					String oaEfileDid = efileMap.get("did").toString();
					String etitle = efileMap.get("title").toString();
					String eFileTableName = "E_FILE" + wsCode;
					String efilepath = File.separator + eFileTableName + File.separator
							+ DateUtil.getCurrentDateStr() + File.separator;
					String absolutePath = lamsBasePath + efilepath + etitle;
					Integer result = -1;
					try {
						result = getImgByte(oaEfileDid, absolutePath);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					File efile = new File(absolutePath);
					if(result == 0 && efile.exists()){
						insertEfile(efile, efilepath, etitle, wsCode, fileDid , oaEfileDid, "");
					}
				}
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
							System.out.println(bytes.length);
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
