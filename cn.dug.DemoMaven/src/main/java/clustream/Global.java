// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Global.java

package clustream;

import java.io.PrintStream;
import java.util.Date;
import java.util.Hashtable;
import org.osu.ogsa.stream.util.ConfigFileReader;

public class Global
{

	public static boolean IFDETERMINISTIC;
	public static boolean FILELOG = false;
	public static int FLOATSIZE = 4;
	public static int INTSIZE = 4;
	public static int WIN_SIZE;
	public static int NUM_TRAINING_DATA;
	public static int NUM_REAL_DATA;
	public static int NUM_DATA;
	public static int NUM_LSS;
	public static int NUM_DIMENSION;
	public static int INTERVAL;
	public static int minProducingRate;
	public static int maxProducingRate;
	public static int nDivide;
	public static int K;
	public static boolean IFADJUSTPARA;
	public static String ATTACKS[] = {
		"normal", "back", "land", "neptune", "pod", "smurf", "teardrop", "buffer_overflow", "loadmodule", "perl", 
		"rootkit", "ftp_write", "guess_passwd", "imap", "multihop", "phf", "spy", "warezclient", "warezmaster", "ipsweep", 
		"satan", "portsweep", "nmap"
	};
	public static Hashtable hashAttackTypes;
	public static int MAX_NUM_CLUSTERS;
	public static int MIN_NUM_CLUSTERS;
	public static int NUM_CLUSTERS;
	public static String DATAFILE;

	public Global()
	{
	}

	public static double timeval_diff(Date b, Date a)
	{
		double msec = a.getTime() - b.getTime();
		return msec;
	}

	static 
	{
		IFDETERMINISTIC = false;
		WIN_SIZE = 1000;
		NUM_TRAINING_DATA = 1000;
		NUM_REAL_DATA = 1000;
		NUM_DATA = 1000;
		NUM_LSS = 20;
		NUM_DIMENSION = 5;
		INTERVAL = 1000;
		minProducingRate = 10000;
		maxProducingRate = 10000;
		nDivide = 0;
		K = 6;
		IFADJUSTPARA = false;
		hashAttackTypes = new Hashtable();
		MAX_NUM_CLUSTERS = 100;
		MIN_NUM_CLUSTERS = ATTACKS.length;
		NUM_CLUSTERS = 60;
		DATAFILE = "null";
		try
		{
			ConfigFileReader c = new ConfigFileReader();
			c.init("clustream.cnf");
			WIN_SIZE = c.getInt("WIN_SIZE");
			NUM_TRAINING_DATA = c.getInt("NUM_TRAINING_DATA");
			NUM_REAL_DATA = c.getInt("NUM_REAL_DATA");
			NUM_DATA = c.getInt("NUM_DATA");
			MAX_NUM_CLUSTERS = c.getInt("MAX_NUM_CLUSTERS");
			MIN_NUM_CLUSTERS = c.getInt("MIN_NUM_CLUSTERS");
			NUM_CLUSTERS = c.getInt("NUM_CLUSTERS");
			NUM_LSS = c.getInt("NUM_LSS");
			NUM_DIMENSION = c.getInt("NUM_DIMENSION");
			INTERVAL = c.getInt("INTERVAL");
			K = c.getInt("K");
			IFDETERMINISTIC = c.getBoolean("IFDETERMINISTIC");
			IFADJUSTPARA = c.getBoolean("IFADJUSTPARA");
			DATAFILE = c.getString("DATAFILE");
			maxProducingRate = c.getInt("maxProducingRate");
			minProducingRate = c.getInt("minProducingRate");
			nDivide = c.getInt("nDivide");
			hashAttackTypes = new Hashtable();
			for (int i = 0; i < ATTACKS.length; i++)
				hashAttackTypes.put(ATTACKS[i], new Integer(i));

			System.out.println("WIN_SIZE" + WIN_SIZE);
			System.out.println("NUM_TRAINING_DATA" + NUM_TRAINING_DATA);
			System.out.println("NUM_DATA" + NUM_DATA);
			System.out.println("NUM_LSS" + NUM_LSS);
			System.out.println("NUM_REAL_DATA" + NUM_REAL_DATA);
			System.out.println("MAX_NUM_CLUSTERS" + MAX_NUM_CLUSTERS);
			System.out.println("MIN_NUM_CLUSTERS" + MIN_NUM_CLUSTERS);
			System.out.println("NUM_CLUSTERS" + NUM_CLUSTERS);
			System.out.println("maxProducingRate:" + maxProducingRate);
			System.out.println("minProducingRate:" + minProducingRate);
			System.out.println("nDivide:" + nDivide);
			System.out.println("IFDETERMINISTIC" + IFDETERMINISTIC);
			System.out.println("IFADJUSTPARA" + IFADJUSTPARA);
			System.out.println("DATAFILE" + DATAFILE);
			System.out.println("INTERVAL" + INTERVAL);
			System.out.println("NUM_DIMENSION" + NUM_DIMENSION);
			System.out.println("K" + K);
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
	}
}
