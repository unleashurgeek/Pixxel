package io.lgs.starbound.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class BanList extends File {
	
	/**
	 * UUID
	 */
	private static final long serialVersionUID = 6982610685775336567L;

	private Set<String> bans;
	
	private static final String IPADDRESS_PATTERN =
					"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	public BanList(String pathname) {
		super(pathname);
		this.load();
	}
	
	public Set<String> getBans() {
		return bans;
	}
	
	public void load() {
		if (!this.exists())
			try {
				this.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		try(BufferedReader br = new BufferedReader(new FileReader(this))) {
			for (String line; (line = br.readLine()) != null;) {
				if (!(line.matches("") || line.isEmpty() || line == null) && line.matches(IPADDRESS_PATTERN)){
					line.trim();
					bans.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean addBan(String ip) {
		ip = ip.trim();
		
		if (!ip.matches(IPADDRESS_PATTERN))
			return false;
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(this))) {
			bw.append(ip + "\n");
			bans.add(ip);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean removeBan(String ip) {
		ip = ip.trim();
		boolean isRemoved = false;
		
		if (!ip.matches(IPADDRESS_PATTERN))
			return false;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(this));
			bw = new BufferedWriter(new FileWriter(this));
			
			for (String line; (line = br.readLine()) != null;) {
				line = line.trim();
				if (line.equals(ip)) {
					bw.write(line);
					bans.remove(ip);
					isRemoved = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isRemoved;
	}
}
