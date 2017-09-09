// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SecurityDataFilter.java

package clustream;

import java.io.*;
import java.net.*;
import java.nio.FloatBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package clustream:
//			Global

public class SecurityDataFilter
{

	public static int flags[] = {
		1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 
		1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 
		0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 
		0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 
		0, 1
	};
	public static String names[] = {
		"duration", "protocol_type", "service", "flag", "src_bytes", "dst_bytes", "land", "wrong_fragment", "urgent", "hot", 
		"num_failed_logins", "logged_in", "num_compromised", "root_shell", "su_attempted", "num_root", "num_file_creations", "num_shells", "num_access_files", "num_outbound_cmds", 
		"is_host_login", "is_guest_login", "count", "srv_count", "serror_rate", "srv_serror_rate", "rerror_rate", "srv_rerror_rate", "same_srv_rate", "diff_srv_rate", 
		"srv_diff_host_rate", "dst_host_count", "dst_host_srv_count", "dst_host_same_srv_rate", "dst_host_diff_srv_rate", "dst_host_same_src_port_rate", "dst_host_srv_diff_host_rate", "dst_host_serror_rate", "dst_host_srv_serror_rate", "dst_host_rerror_rate", 
		"dst_host_srv_rerror_rate", "attack_type"
	};
	public static int NUMATTRS = 42;
	public static int NUM_CONTINUOUS_ATTRS;
	private static Log log;
	InputStream is;
	InputStreamReader reader;
	LineNumberReader lineReader;
	String strLine;
	String attributes[];

	public SecurityDataFilter(String dataFile)
	{
		is = null;
		reader = null;
		lineReader = null;
		strLine = null;
		attributes = new String[NUMATTRS];
		URI dataURI = new URI(dataFile);
		if (dataURI.isAbsolute())
			is = dataURI.toURL().openStream();
		else
			is = new FileInputStream(dataFile);
		if (is == null)
		{
			log.error("the input stream is null");
			return;
		}
		try
		{
			reader = new InputStreamReader(is);
			lineReader = new LineNumberReader(reader);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("File not found.");
			System.exit(-1);
		}
		catch (IOException e)
		{
			System.err.println("Cannot access file.");
			System.exit(-1);
		}
		catch (URISyntaxException e)
		{
			System.err.println("the incorrect URI");
			System.exit(-1);
		}
		return;
	}

	public String filterLineToFloat(boolean bTraining, FloatBuffer resultsFloatBuf)
	{
		int nLengthNewAttri = 0;
		String strNewLine = "";
		try
		{
			strLine = lineReader.readLine();
		}
		catch (IOException e)
		{
			log.error(e);
			return "error";
		}
		if (strLine == null)
			return null;
		int i;
		try
		{
			attributes = strLine.split(",");
			for (i = 0; i < NUMATTRS - 1; i++)
				if (attributes[i] == null)
					log.debug("the " + i + " attribute should not be null");
				else
				if (flags[i] == 1)
					resultsFloatBuf.put(Double.valueOf(attributes[i]).floatValue());

			if (bTraining)
				resultsFloatBuf.put(-1F);
			else
				resultsFloatBuf.put(1.0F);
		}
		catch (Exception e)
		{
			log.error(e);
			return "error";
		}
		return attributes[i].substring(0, attributes[i].length() - 1) + "!";
	}

	public String filterLineToString(boolean bTraining)
	{
		String strNewLine = "";
		try
		{
			strLine = lineReader.readLine();
		}
		catch (IOException e)
		{
			log.error(e);
			return null;
		}
		if (strLine == null)
			return null;
		attributes = strLine.split(",");
		int i;
		for (i = 0; i < NUMATTRS - 1; i++)
			if (attributes[i] == null)
				log.debug("the " + i + " attribute should not be null");
			else
			if (flags[i] == 1)
				strNewLine = strNewLine + attributes[i] + ",";

		if (bTraining)
			strNewLine = strNewLine + "$";
		strNewLine = strNewLine + attributes[i].substring(0, attributes[i].length() - 1) + "!";
		return strNewLine;
	}

	public void endFiltering()
	{
		try
		{
			if (is != null)
				is.close();
		}
		catch (Exception e)
		{
			log.fatal("can't close the config file");
		}
	}

	public static void main(String args[])
	{
		if (args.length < 1)
		{
			log.error("usaga: SecurityDataFilter filename");
			return;
		}
		SecurityDataFilter fr = new SecurityDataFilter(args[0]);
		String newLine = null;
		String strAttackType = null;
		float rangeFMax[] = new float[NUM_CONTINUOUS_ATTRS];
		float rangeFMin[] = new float[NUM_CONTINUOUS_ATTRS];
		for (int i = 0; i < NUM_CONTINUOUS_ATTRS; i++)
		{
			rangeFMax[i] = 1.401298E-045F;
			rangeFMin[i] = 3.402823E+038F;
		}

		FloatBuffer tempBuf = FloatBuffer.allocate(1000);
		float tempF[] = tempBuf.array();
		do
		{
			tempBuf.clear();
			strAttackType = fr.filterLineToFloat(true, tempBuf);
			if (strAttackType == null)
				break;
			for (int i = 0; i < NUM_CONTINUOUS_ATTRS; i++)
			{
				if (tempF[i] < rangeFMin[i])
					rangeFMin[i] = tempF[i];
				if (tempF[i] > rangeFMax[i])
					rangeFMax[i] = tempF[i];
			}

		} while (true);
		fr.endFiltering();
		for (int i = 0; i < NUM_CONTINUOUS_ATTRS - 1; i++)
		{
			int j = i + 1;
			log.info(j + ":" + rangeFMin[i] + ":: " + rangeFMax[i]);
			System.out.println(j + ":" + rangeFMin[i] + ":: " + rangeFMax[i]);
		}

	}

	static Class class$(String x0)
	{
		return Class.forName(x0);
		ClassNotFoundException x1;
		x1;
		throw new NoClassDefFoundError(x1.getMessage());
	}

	static 
	{
		NUM_CONTINUOUS_ATTRS = Global.NUM_DIMENSION;
		log = LogFactory.getLog((clustream.SecurityDataFilter.class).getName());
	}
}
