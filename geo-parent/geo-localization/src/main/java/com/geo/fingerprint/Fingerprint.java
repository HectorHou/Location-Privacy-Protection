package com.geo.fingerprint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.geo.proto.LocationRespProto.Location;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：指纹库，用于定位
 */
public class Fingerprint {

	private final Map<Location, HashMap<String, Float>> fingerprint;
	private int minMatch;

	private int size;

	private Fingerprint() {
		this.fingerprint = new HashMap<Location, HashMap<String, Float>>();
		size = 0;
	}

	public Fingerprint(String filePath, int minMatch) throws FileNotFoundException {
		this();
		File file = new File(filePath);
		createFingerprint(file);
		this.size = this.fingerprint.size();
	}

	public Fingerprint(File file, int minMatch) throws FileNotFoundException {
		this();
		this.minMatch = minMatch;
		createFingerprint(file);
		this.size = this.fingerprint.size();
	}

	public Map<Location, HashMap<String, Float>> getFingerprint() {
		return fingerprint;
	}

	public int getSize() {
		return size;
	}

	public int getMinMatch() {
		return minMatch;
	}

	public void setMinMatch(int minMatch) {
		this.minMatch = minMatch;
	}

	@Override
	public String toString() {
		return "Fingerprint [fingerprint=" + fingerprint + ", size=" + size + "]";
	}
/**
 * 
     * 
     * @param File
     * @return null
     * 从文件中按照指定格式获取指纹库
 */
	private void createFingerprint(File file) throws FileNotFoundException {
		if (!file.isFile()) {
			throw new FileNotFoundException("这不是一个文件");
		}
		InputStream is = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		Map<String, Float> currentMap = null;
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("(")) {
					String[] coordinates = line.substring(1, line.length() - 1).split(",");
					Location.Builder builder = Location.newBuilder();
					builder.setLongitude(Double.parseDouble(coordinates[0]));
					builder.setLatitude(Double.parseDouble(coordinates[1]));
					currentMap = new HashMap<>();
					this.fingerprint.put(builder.build(), (HashMap<String, Float>) currentMap);
				} else if (!line.isEmpty()) {
					String[] AP = line.split(" ");
					currentMap.put(AP[0], Float.valueOf(AP[1]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.size = this.fingerprint.size();
	}

	public void addFingerprint(Map<Location, HashMap<String, Float>> fingerprintMap) {
		this.fingerprint.putAll(fingerprintMap);
		this.size = this.fingerprint.size();
	}

	public void addFingerprint(File file) throws FileNotFoundException {
		createFingerprint(file);
		this.size = this.fingerprint.size();
	}

	public void addFingerprint(String filePath) throws FileNotFoundException {
		createFingerprint(new File(filePath));
		this.size = this.fingerprint.size();
	}

	public void addFingerprint(Fingerprint newFingerprint) throws FileNotFoundException {
		addFingerprint(newFingerprint.fingerprint);
		this.size = this.fingerprint.size();
	}
}
