package com.whyun.totalshare;

import java.io.FileNotFoundException;

import junit.framework.TestCase;

public class SNSConfigTest extends TestCase {

	public void testGetInstance() throws FileNotFoundException {
		//fail("Not yet implemented");
		SNSConfig config = SNSConfig.getInstance();
		String id = config.getConfig("tx").getClientId();
		System.out.println(id);
		id = config.getConfig("sina").getClientId();
		System.out.println(id);
	}

}
