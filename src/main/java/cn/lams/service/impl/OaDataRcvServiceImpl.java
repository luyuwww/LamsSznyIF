package cn.lams.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import sun.misc.BASE64Decoder;
import ch.qos.logback.classic.Logger;
import cn.lams.service.BaseService;
import cn.lams.service.i.OaDataRcvService;
import cn.lams.util.DateUtil;
import cn.lams.util.XmlObjUtil;

@Service("oaDataRcvService")
public class OaDataRcvServiceImpl extends BaseService implements
		OaDataRcvService {

	@Override
	public void dataReceive() {

		Map<String, String> oaBodyMap = null;
		String dfileTableName = "D_FILE" + wsCode;
		String wjlx = null;
		try {
			// 获取指定目录下所有xml文件
			File[] listFiles = new File(catalogue).listFiles();
			for (File listFile : listFiles) {
				File[] files = listFile.listFiles();
				for (File file : files) {
					String xmlFullName = file.getAbsolutePath();
					Document document = XmlObjUtil
							.xmlFile2Document(xmlFullName);
					Element root = document.getRootElement();
					String File_type = root.getChildText("File_type");
					Integer maxDid = null;
					if (File_type.equals("0") || File_type.equals("1") || File_type.equals("2")
							|| File_type.equals("3")) {
						if (File_type.equals("1")) {
							wjlx = "发文";
						}
						if (File_type.equals("2")) {
							wjlx = "收文";
						}
						if (File_type.equals("3")) {
							wjlx = "签报";
						}
						if(File_type.equals("0")){
							wjlx = "协同";
						}
						String oaGwFieldsSql = "select F1 from "
								+ oaGwMappingTable;
						List<String> oaGwFields = jdbcDao
								.quert4List(oaGwFieldsSql);
						oaBodyMap = new HashMap<String, String>();
						String oaGwValue = null;
						// 遍历代码表
						// **注意若存在Element元素,代码表中Oa配置格式为：Element.字段名称,例：Element.文件标题**
						Element element = root.getChild("Elements");
						for (String oaGwField : oaGwFields) {
							if (oaGwField.contains("Element")
									&& element != null) {
								String wjbs = oaGwField.substring(
										oaGwField.indexOf(".") + 1,
										oaGwField.length());
								int elementSize = root.getChildren("Elements")
										.size();
								if (elementSize != 0) {
									List<Element> Elements = root.getChild(
											"Elements").getChildren("Element");
									for (Element ele : Elements) {
										String name = ele
												.getAttributeValue("name");
										if (wjbs.equals(name)) {
											oaGwValue = ele.getText();
											if (StringUtils
													.isNotBlank(oaGwValue)) {
												oaBodyMap.put(oaGwField,
														oaGwValue);
											}
										}
									}
								}
							} else {
								oaGwValue = root.getChildText(oaGwField);
								if (StringUtils.isNotBlank(oaGwValue)) {
									oaBodyMap.put(oaGwField, oaGwValue);
								}
							}
						}
						// 文件
						maxDid = insertDfile4Map(oaBodyMap,
								getOaGwMappingArc(), dfileTableName, wjlx);
						// 正文部分
						Element fileEle = root.getChild("Body");
						if(fileEle != null){
							String file_type =	fileEle.getAttributeValue("File_type");
							if (StringUtils.isNotBlank(file_type)) {
								String Subject = root.getChildText("Subject");
								String ert = null;
								if (file_type.contains("Word")) {
									ert = ".doc";
								} else if (file_type.contains("Excel")) {
									ert = ".xls";
								} else {
									ert = ".html";
								}
								String fileName = Subject + ert;
								String bodyBase64 = root.getChildText("Body");
								if (StringUtils.isNotBlank(bodyBase64)) {
									convertBase64ToFile(bodyBase64, fileName);
									String efileName = tempArcFilePath + fileName;
									File efile = new File(efileName);
									insertEfile(efile, wsCode, maxDid, "");
								}
							}
						}
						// 附件部分
						List<Element> Attachments = root
								.getChildren("Attachments");
						for (Element Attachment : Attachments) {
							int size = Attachment.getChildren().size();
							if (size != 0) {
								String bdName = Attachment.getChild(
										"Attachment").getAttributeValue("name");
								String formBase64 = Attachment
										.getChildText("Attachment");
								convertBase64ToFile(formBase64, bdName);
								String efileName = tempArcFilePath + bdName;
								File efile = new File(efileName);
								insertEfile(efile, wsCode, maxDid, "");
							}
						}
						//表单部分
						Element formEle = root.getChild("Form");
						if(formEle != null){
							String formName = formEle.getAttributeValue("name");
							if (StringUtils.isNotBlank(formName)) {
								String formBase64 = root.getChildText("Form");
								if (StringUtils.isNotBlank(formBase64)) {
									convertBase64ToFile(formBase64, formName);
									String efileName = tempArcFilePath + formName;
									File efile = new File(efileName);
									insertEfile(efile, wsCode, maxDid, "");
								}
							}
						}
					} else {
						log.error("不支持的文件类型！");
						break;
					}
					// 移动oa xml文件到指定目录下
					String movePath = oaXmlLocalPath + wjlx + File.separator
							+ DateUtil.getCurrentDateStr() + File.separator
							+ System.currentTimeMillis();
					// new File(movePath).mkdir();
					FileUtils.moveFileToDirectory(new File(xmlFullName),
							new File(movePath), true);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		try {
			// 删除生成Oa文件的临时目录
			FileUtils.deleteDirectory(new File(tempArcFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * base64编码2File
	 * 
	 * @param base64
	 * @param fileName
	 * @throws Exception
	 */
	private void convertBase64ToFile(String base64, String fileName)
			throws Exception {
		BASE64Decoder decoder = new BASE64Decoder();
		FileUtils.touch(new File(tempArcFilePath + fileName));
		FileOutputStream writes = new FileOutputStream(new File(tempArcFilePath
				+ fileName));
		byte[] decoderBytes = decoder.decodeBuffer(base64);
		writes.write(decoderBytes);
		writes.close();
	}

	private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
