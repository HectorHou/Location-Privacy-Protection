package com.geo.localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.geo.fingerprint.Fingerprint;

import junit.framework.TestCase;

public class LocalizationTest extends TestCase {
	@Test
	public static void testLocalization() throws FileNotFoundException {
		Properties prop = new Properties();
		try {
			prop.load(LocalizationTest.class.getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Fingerprint fp = new Fingerprint(prop.getProperty("fpFile"), Integer.parseInt(prop.getProperty("minMatch")));
		assertEquals(fp.getFingerprint().size(), 90);
		File f = new File(prop.getProperty("apFile"));
		Localization ll = new LocalizationImpl(fp);
		assertNotNull(ll.getLocation(f));
	}
}
