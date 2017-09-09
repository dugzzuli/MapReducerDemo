// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Producer.java

package clustream;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Date;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osu.ogsa.stream.services.StreamProcessor;
import org.osu.ogsa.stream.services.StreamServiceProvider;
import org.osu.ogsa.stream.util.*;

// Referenced classes of package clustream:
//			RandomDataGenerator, Global, SecurityDataFilter

public class Producer
	implements StreamProcessor
{

	private StreamServiceProvider srvProvider;
	private Random rand;
	private FileLog fileLog;
	private static Log log;
	private boolean bFileLog;
	private RandomDataGenerator randData;

	public Producer()
	{
	}

	public void work(AutoFillInputBufferArray inBufArray, AutoFillOutputBufferArray outBufArray)
	{
		bFileLog = Global.FILELOG;
		try
		{
			if (bFileLog)
			{
				Date tempTime = new Date();
				String myLogFileName = "Producer_" + tempTime.getTime();
				fileLog = new FileLog(myLogFileName);
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
		if (outBufArray.howmanyOutputBuffers() == 0)
		{
			log.fatal("[" + srvProvider.myHandle + "]:" + "[work]: output buffer");
			System.exit(-1);
		}
		Date tStart = new Date();
		rand = new Random(tStart.getTime());
		double rate = Global.maxProducingRate;
		int sign = -1;
		double delay_para = 0.0D;
		int set_size = SecurityDataFilter.NUM_CONTINUOUS_ATTRS * Global.FLOATSIZE;
		log.debug("set_size = " + set_size);
		int times = 0;
		int bufSize = 0;
		String strAttackType = null;
		ByteBuffer resultsByteBuf = ByteBuffer.allocate(set_size);
		FloatBuffer resultsFloatBuf = resultsByteBuf.asFloatBuffer();
		ByteBuffer strByteBuf = null;
		int counter = 0;
		int rnt = 0;
		boolean test = false;
		AutoFillOutputBuffer outBuf;
		do
		{
			if (++counter % 1000 == 0)
				log.debug(":" + counter);
			if (counter > Global.NUM_DATA)
				break;
			outBuf = outBufArray.getOutputBuffer();
			if (outBuf.getConnectionContext().neighStreamHandle.indexOf("node13") > 0 && !test)
			{
				log.warn("connected to node13 now");
				test = true;
			}
			resultsFloatBuf.clear();
			strAttackType = randData.filterLineToFloat(false, resultsFloatBuf);
			if (strAttackType == null)
				break;
			if (!strAttackType.equals("error"))
			{
				resultsByteBuf.rewind();
				rnt = outBuf.put(resultsByteBuf, set_size, true);
				log.debug("rnt = " + rnt);
				if (rnt < 0)
					log.error("something wrong with the output buffer");
				byte tempStr[] = strAttackType.getBytes();
				strByteBuf = ByteBuffer.wrap(tempStr);
				strByteBuf.rewind();
				for (int g = 0; g < tempStr.length; g++)
					log.debug(g + ":" + strByteBuf.get(g));

				if (outBuf.put(strByteBuf, tempStr.length, true) < 0)
					log.error("something wrong with the output buffer");
				times++;
				if (Global.maxProducingRate > 0)
				{
					if (counter % 40000 == 0)
					{
						Date temTime = new Date();
						if (Global.maxProducingRate > Global.minProducingRate && Global.maxProducingRate > 0 && Global.minProducingRate >= 0)
						{
							rate += ((double)sign * (double)(Global.maxProducingRate - Global.minProducingRate)) / (double)Global.nDivide;
							log.debug(temTime.getTime() + " the rate is " + rate);
							if (Double.compare(rate, Global.maxProducingRate) > 0)
							{
								rate = Global.maxProducingRate;
								sign = -1;
								rate += ((double)sign * (double)(Global.maxProducingRate - Global.minProducingRate)) / (double)Global.nDivide;
								log.debug(temTime.getTime() + " the rate is " + rate);
							} else
							if (Double.compare(rate, Global.minProducingRate) < 0)
							{
								rate = Global.minProducingRate;
								sign = 1;
								rate += ((double)sign * (double)(Global.maxProducingRate - Global.minProducingRate)) / (double)Global.nDivide;
								log.debug(temTime.getTime() + " the rate is " + rate);
							}
						} else
						if (Global.maxProducingRate == Global.minProducingRate && Global.minProducingRate != 0)
							rate = Global.maxProducingRate;
					}
					if (bFileLog)
					{
						Date temTime = new Date();
						fileLog.write(temTime.getTime() + "   " + rate);
					}
					bufSize = tempStr.length + set_size;
					delay_para += (1000000000D * (double)bufSize * 8D) / rate;
					long millis_delay = (long)(delay_para / 1000000D);
					int nanos_delay = (int)(delay_para - (double)millis_delay * 1000000D);
					try
					{
						if (millis_delay >= (long)DefConstants.DELAY_THRESHOLD)
						{
							delay_para = 0.0D;
							Thread.currentThread();
							Thread.sleep(millis_delay, nanos_delay);
						}
					}
					catch (Exception e)
					{
						log.error(e);
					}
				}
			}
		} while (true);
		for (int i = 0; i < SecurityDataFilter.NUM_CONTINUOUS_ATTRS; i++)
			resultsFloatBuf.put(i, -2F);

		resultsByteBuf.rewind();
		outBuf = outBufArray.getOutputBuffer();
		outBuf.put(resultsByteBuf, set_size, true);
		randData.endFiltering();
		Date tEnd = new Date();
		System.out.println("[" + srvProvider.myHandle + "]:" + "Date source finsihed work: time consumed is " + Global.timeval_diff(tStart, tEnd));
	}

	public void init(StreamServiceProvider srvProvider)
	{
		this.srvProvider = srvProvider;
		if (Global.DATAFILE == null)
		{
			log.error("the data file is null, can't initilize the producer");
			System.exit(-1);
		}
		randData = new RandomDataGenerator();
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
		log = LogFactory.getLog((clustream.Producer.class).getName());
	}
}
