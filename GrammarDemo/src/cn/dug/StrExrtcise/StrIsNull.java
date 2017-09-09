package cn.dug.StrExrtcise;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StrIsNull {
	public static void main(String[] args) throws IOException {
		File f = new File("src/cn/dug/StrExrtcise/tem");
		if (!f.exists()) {
			System.out.println("文件不存在");
		} else {
			FileReader input = new FileReader(f);
			BufferedReader br = new BufferedReader(input);
			String line = "";
			while ((line = br.readLine()) != null) {
				if(line.isEmpty()||line.equals(""))
				{
					System.out.println("==================");
				}
				else {
					System.out.println(line);
				}
			}
		}
	}
}
