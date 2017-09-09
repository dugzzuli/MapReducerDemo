// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   RandomDataGenerator.java

package clustream;

import java.nio.FloatBuffer;
import java.util.Date;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package clustream:
//			SecurityDataFilter, Global

public class RandomDataGenerator
{

	public static int NUM_CONTINUOUS_ATTRS;
	public static int FLOAT_RANGE = 10000;
	public static int NUM_DATA;
	private static Log log;
	private Random rand;
	private int count;

	public RandomDataGenerator()
	{
		Date tStart = new Date();
		rand = new Random(tStart.getTime());
		count = 0;
	}

	public String filterLineToFloat(boolean bTraining, FloatBuffer resultsFloatBuf)
	{
		if (count == NUM_DATA)
			return null;
		count++;
		for (int i = 0; i < NUM_CONTINUOUS_ATTRS - 1; i++)
		{
			float fTemp = rand.nextFloat() * (float)FLOAT_RANGE;
			resultsFloatBuf.put(fTemp);
		}

		if (bTraining)
			resultsFloatBuf.put(-1F);
		else
			resultsFloatBuf.put(1.0F);
		return "normal!";
	}

	public void endFiltering()
	{
	}

	public static void main(String args[])
	{
		RandomDataGenerator fr = new RandomDataGenerator();
		String newLine = null;
		String strAttackType = null;
		NUM_DATA = 200;
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
			if (strAttackType != null)
			{
				for (int i = 0; i < NUM_CONTINUOUS_ATTRS; i++)
				{
					log.info("[" + i + "]" + ":" + tempF[i]);
					if (tempF[i] < rangeFMin[i])
						rangeFMin[i] = tempF[i];
					if (tempF[i] > rangeFMax[i])
						rangeFMax[i] = tempF[i];
				}

			} else
			{
				fr.endFiltering();
				return;
			}
		} while (true);
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
		NUM_CONTINUOUS_ATTRS = SecurityDataFilter.NUM_CONTINUOUS_ATTRS;
		NUM_DATA = Global.NUM_DATA;
		log = LogFactory.getLog((clustream.RandomDataGenerator.class).getName());
	}
}
