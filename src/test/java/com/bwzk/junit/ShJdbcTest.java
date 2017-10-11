package com.bwzk.junit;

import cn.lams.service.i.OaDataRcvService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring/test*.xml" })
public class ShJdbcTest {

	@Test
	public void test() throws Exception {
//		List<String> aa = jdbcTemplate_zjk.queryForList("select pid from d_file1 where did= '33455455'", String.class);
//		System.out.println(aa.size());
//		File doc = new File("D:/300150.pdf");
//		setImgByte(FileUtils.readFileToByteArray(doc),"123");
		Object fileInputStream =  getImgByte("123","css.pdf");
//		System.out.println(fileInputStream);
//		inputOutputStreamTest(fileInputStream, "image.pdf");
	}
	public Object  getImgByte(final String id, final String fileName) throws FileNotFoundException {

		return jdbcTemplate_zjk.execute(
				"select file_data from NY_E_FILECODE where did=?",
				new PreparedStatementCallback() {
					public Object doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						File testdoc = new File("E:/"+fileName);
						BufferedOutputStream bufferedOutput = null;
						try {
							OutputStream output = new FileOutputStream(testdoc);
							bufferedOutput = new BufferedOutputStream(output);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						ps.setString(1, id);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							InputStream inputStream = rs.getBinaryStream("file_data");
							try {
//								System.out.println(IOUtils.toByteArray(inputStream).length);
//								InputStream input = new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
//								fileOutputStream.write(IOUtils.toByteArray(inputStream));
//								byte[] b = new byte[1024*3];
//								int length = 0;
//								while ((length = input.read(b)) != -1){
//									System.out.println(new String(b,0,length));
//									bufferedOutput.write(b);
//								}
								byte[] bytes = IOUtils.toByteArray(inputStream);
								System.out.println(bytes.length);
								bufferedOutput.write(bytes);
								bufferedOutput.flush();
//								return IOUtils.toByteArray(inputStream);
							} catch (IOException e) {
							}finally {
								try {
									bufferedOutput.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						return null;
					}
				});
	}
	public void setImgByte(final byte[] bytes, final String id) {
		jdbcTemplate_zjk.execute(
				"update NY_E_FILECODE set file_data=? where did=?",
				new PreparedStatementCallback() {

					public Object doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						ps.setBytes(1, bytes);
						ps.setString(2, id);
						ps.execute();
						return null;
					}
				});
	}
	public  void inputOutputStreamTest(FileInputStream fileInputStream, String fileName) throws Exception {
		File doc = new File("D:/"+fileName);
		File testdoc = new File("E:/"+fileName);
//		FileInputStream fileInputStream = new FileInputStream(doc);
		FileOutputStream fileOutputStream = new FileOutputStream(testdoc);
//        int content = 0;
//        while ((content = fileInputStream.read()) != -1){
//            fileOutputStream.write(content);
//        }
		int length = 0;
		byte[] b = new byte[1024*3];
		while ((length = fileInputStream.read(b)) != -1){
			System.out.println(new String(b,0,length));
			fileOutputStream.write(b);
		}
		fileOutputStream.close();
		fileInputStream.close();
	}
	@Autowired
	private JdbcTemplate jdbcTemplate_zjk;
	@Autowired
	private OaDataRcvService oaDataRcvService;
}
