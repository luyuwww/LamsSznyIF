package com.bwzk.junit;

import DefaultNamespace.WS_EKP_SSO;
import DefaultNamespace.WS_EKP_SSOServiceLocator;
import cn.lams.service.i.OaDataRcvService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring/test*.xml" })
public class ShJdbcTest {

	@Test
	public void test() throws Exception {
//		URL url = new URL("http://10.185.16.165:9999/ThamsWebServicesJJ/services/DbUtilService?wsdl");
		WS_EKP_SSO sso = new WS_EKP_SSOServiceLocator().getDomino();
		//System.out.println(request.getServerName()+" #$%^&");
		//System.out.println("Ltpatoken------"+Ltpatoken);
		String usercode = sso.getUserName("oa.sec.com.cn","Ltpatoken");
		//System.out.println("usercode:"+usercode);
	}
	@Autowired
	private JdbcTemplate jdbcTemplate_zjk;
	@Autowired
	private OaDataRcvService oaDataRcvService;
}
