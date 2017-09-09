// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Clustering.java

package clustream;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.*;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osu.ogsa.stream.services.StreamProcessor;
import org.osu.ogsa.stream.services.StreamServiceProvider;
import org.osu.ogsa.stream.util.*;

// Referenced classes of package clustream:
//			CFList, CF, Global, SecurityDataFilter, 
//			CFListIterator

public class Clustering
	implements StreamProcessor
{

	private int maxBufSize;
	private int maxSampleSize;
	private double SAMPLING_RATIO;
	private StreamServiceProvider srvProvider;
	private ByteBuffer final_output_buf;
	private Random rand;
	private boolean bFileLog;
	private FileLog fileLog;
	private int MAX_NUM_CLUSTERS;
	private int MIN_NUM_CLUSTERS;
	private int NUM_CLUSTERS;
	private CFList clusters;
	private static Log log;
	private boolean test;
	private int countLSS;

	public Clustering()
	{
		test = false;
		countLSS = 0;
		MAX_NUM_CLUSTERS = Global.MAX_NUM_CLUSTERS;
		MIN_NUM_CLUSTERS = Global.MIN_NUM_CLUSTERS;
		NUM_CLUSTERS = Global.NUM_CLUSTERS;
		int outputset_size = (SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1) * Global.FLOATSIZE + 1032 + 4 + 4 + 4 + 4 + 100;
		final_output_buf = ByteBuffer.allocateDirect(outputset_size);
	}

	public void pre_work()
	{
		try
		{
			clusters = (CFList)srvProvider.newLSSInstance(this, "clustream.CFList");
			if (!clusters.bInit)
			{
				clusters.initialize(NUM_CLUSTERS);
			} else
			{
				clusters.printMyself();
				NUM_CLUSTERS = clusters.getNumClusters();
				log.fatal("NUM_CLUSTERS: " + NUM_CLUSTERS);
			}
		}
		catch (ClassCastException e)
		{
			e.printStackTrace();
			log.error(e.getCause());
			return;
		}
	}

	public void init(StreamServiceProvider srvProvider)
	{
		this.srvProvider = srvProvider;
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
		if (Global.IFADJUSTPARA)
			srvProvider.specifyAccuracyPara(NUM_CLUSTERS, MIN_NUM_CLUSTERS, MAX_NUM_CLUSTERS, 0.0D);
	}

	public void work(AutoFillInputBufferArray inBufArray, AutoFillOutputBufferArray outBufArray)
	{
		Date tStart = new Date();
		bFileLog = Global.FILELOG;
		try
		{
			if (bFileLog)
			{
				String myLogFileName = "Clustering_" + tStart.getTime();
				fileLog = new FileLog(myLogFileName);
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
		pre_work();
		log.error("done pre_work");
		if (inBufArray.howmanyInputBuffers() == 0)
		{
			log.fatal("[" + srvProvider.myHandle + "]:" + "[work]: No input buffer");
			System.exit(-1);
		}
		AutoFillInputBuffer inBuffer = null;
		AutoFillOutputBuffer outBuffer = null;
		IntBuffer intFinalOutBuf = final_output_buf.asIntBuffer();
		ByteBuffer tempBuf[] = new ByteBuffer[30];
		FloatBuffer floatTempBuf[] = new FloatBuffer[30];
		ByteBuffer strByteBuf = ByteBuffer.allocate(40);
		char tempChar = '1';
		boolean bExit[] = new boolean[30];
		int nBytesToRead[] = new int[30];
		int set_size = SecurityDataFilter.NUM_CONTINUOUS_ATTRS * Global.FLOATSIZE;
		for (int i = 0; i < 30; i++)
		{
			tempBuf[i] = null;
			floatTempBuf[i] = null;
			bExit[i] = false;
			nBytesToRead[i] = set_size;
		}

		int temp = 0;
		int temp1 = 0;
		CF newCF;
		CF tempCF = newCF = null;
		String strAttackType = "";
		int nExit = 0;
		log.debug("set_size" + set_size);
		do
		{
			int numBuf = inBufArray.howmanyInputBuffers();
			if (numBuf != nExit)
			{
				for (int i = 0; i < numBuf; i++)
				{
					if (bExit[i])
						continue;
					inBuffer = inBufArray.getValidInputBufferChangeStatus(i);
					if (inBuffer == null)
					{
						nExit = numBuf;
						if (test)
							break;
						log.info("processing STOP due to fault");
						test = true;
						break;
					}
					if (tempBuf[i] == null)
					{
						tempBuf[i] = ByteBuffer.allocate(set_size);
						floatTempBuf[i] = tempBuf[i].asFloatBuffer();
					}
					log.debug("1:the number of bytes the array[" + i + "] :" + nBytesToRead[i]);
					if ((temp1 = inBuffer.readToByteBuffer(tempBuf[i], nBytesToRead[i], nBytesToRead[i], false)) <= 0)
					{
						log.debug("no value" + temp1);
					} else
					{
						nBytesToRead[i] -= temp1;
						log.debug("after reading:the number of bytes read in  the array[" + i + "]:" + nBytesToRead[i] + ":" + tempBuf[i].position());
						if (nBytesToRead[i] != 0)
							log.error("something wrong with it");
						else
						if (floatTempBuf[i].get(0) < -1F)
						{
							bExit[i] = true;
							nExit++;
						} else
						{
							for (int k = 0; k < SecurityDataFilter.NUM_CONTINUOUS_ATTRS; k++)
								log.debug(k + ":" + floatTempBuf[i].get(k));

							strByteBuf.clear();
							strAttackType = "";
							do
							{
								log.debug("come to here");
								if (inBuffer.readToByteBuffer(strByteBuf, 1, true) <= 0)
								{
									log.error("can't be 0");
									strAttackType = "others";
									break;
								}
								tempChar = (char)strByteBuf.get(strByteBuf.position() - 1);
								log.debug("char read: " + tempChar);
								if (tempChar == '!')
									break;
								strAttackType = strAttackType + tempChar;
							} while (true);
							log.info(strAttackType);
							if (clusters.nReadPoints < Global.NUM_TRAINING_DATA)
							{
								clusters.addTrainingPoint(floatTempBuf[i], strAttackType);
								tempBuf[i].clear();
								nBytesToRead[i] = set_size;
								clusters.nPoints++;
								clusters.nReadPoints++;
							} else
							{
								if (clusters.isEmpty())
								{
									clusters.setCapacity(NUM_CLUSTERS);
									clusters.runKMeans(NUM_CLUSTERS);
									log.debug("how many microclusters: " + clusters.getNumClusters());
								}
								clusters.nPoints++;
								clusters.nReadPoints++;
								countLSS++;
								tempCF = findClosetClusterForPoint(floatTempBuf[i], clusters);
								if (tempCF == null)
								{
									log.error("can't be null");
								} else
								{
									if (tempCF.isPointBelongToHere(floatTempBuf[i]))
									{
										tempCF.add(floatTempBuf[i], strAttackType);
										log.debug("insert to CF");
									} else
									{
										log.debug("create a new CF");
										if (clusters.isFull())
											clusters.mergeClusters();
										newCF = new CF(SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1, floatTempBuf[i], strAttackType);
										newCF.initRadius((float)((double)tempCF.point_dis(floatTempBuf[i]) / 2D));
										clusters.add(newCF);
									}
									if (nBytesToRead[i] == 0)
									{
										tempBuf[i].clear();
										nBytesToRead[i] = set_size;
									}
									log.debug("the sending flag is " + getSendingFlag());
									log.debug("clusters are " + clusters.isFull());
									if (countLSS == Global.NUM_LSS)
									{
										StreamServiceProvider  = srvProvider;
										synchronized (StreamServiceProvider.synLSSUpdated)
										{
											StreamServiceProvider 1 = srvProvider;
											StreamServiceProvider.bLSSUpdated = true;
											StreamServiceProvider 2 = srvProvider;
											StreamServiceProvider.synLSSUpdated.notify();
										}
										countLSS = 0;
									}
									if (clusters.nReadPoints == Global.NUM_REAL_DATA)
									{
										log.info("come to send a package");
										sendPackage(outBufArray, clusters);
										clusters.clearAll();
										clusters.nReadPoints = 0;
										if (Global.IFADJUSTPARA)
										{
											NUM_CLUSTERS = (int)(srvProvider.getSuggestedAccuracyPara() + 0.5D);
											Date temTime = new Date();
											log.error(temTime.getTime() + " new number clusters: " + NUM_CLUSTERS);
										}
										setSendingFlag(false);
									}
								}
							}
						}
					}
				}

			} else
			{
				intFinalOutBuf.put(0, -1);
				final_output_buf.clear();
				outBuffer = outBufArray.getOutputBuffer();
				outBuffer.put(final_output_buf, 4, true);
				Date tEnd = new Date();
				System.out.println("[" + srvProvider.myHandle + "]:" + "Clustering finsihed work, time consumed is " + Global.timeval_diff(tStart, tEnd));
				System.out.println("nPoints is " + clusters.nPoints);
				return;
			}
		} while (true);
	}

	public CF findClosetClusterForPoint(FloatBuffer floatTempBuf, CFList clusters)
	{
		float minDistance = 3.402823E+038F;
		float tempDist = -1F;
		CF minCF;
		CF currentCF = minCF = null;
		for (CFListIterator iterator = clusters.iterator(); iterator.hasNext();)
		{
			currentCF = iterator.next();
			if (currentCF == null)
			{
				log.error("can't be null");
				return null;
			}
			tempDist = currentCF.point_dis(floatTempBuf);
			if (tempDist < minDistance)
			{
				minDistance = tempDist;
				minCF = currentCF;
			}
		}

		return minCF;
	}

	public synchronized void setSendingFlag(boolean flag)
	{
		clusters.bSendingFlag = flag;
	}

	public synchronized boolean getSendingFlag()
	{
		return clusters.bSendingFlag;
	}

	public void sendPackage(AutoFillOutputBufferArray outBufArray, CFList clusters)
	{
		CFListIterator iterator = null;
		CF currentCF = null;
		AutoFillOutputBuffer outBuffer = outBufArray.getOutputBuffer();
		int len = -1;
		int count = clusters.getNumClusters();
		if (count <= 0)
			return;
		IntBuffer intFinalOutBuf = final_output_buf.asIntBuffer();
		intFinalOutBuf.put(0, count);
		intFinalOutBuf.rewind();
		outBuffer.put(final_output_buf, 4, true);
		for (iterator = clusters.iterator(); iterator.hasNext(); outBuffer.put(final_output_buf, len, true))
		{
			currentCF = iterator.next();
			final_output_buf.clear();
			len = packingOneCluster(final_output_buf, currentCF);
			final_output_buf.rewind();
		}

	}

	public int packingOneCluster(ByteBuffer final_output_buf, CF currentCF)
	{
		IntBuffer intFinalOutBuf = final_output_buf.asIntBuffer();
		FloatBuffer floatFinalOutBuf = final_output_buf.asFloatBuffer();
		String strAttackType = null;
		String strAttackTypes = null;
		byte byteStr[] = null;
		int attack_types[] = new int[currentCF.hashAttackTypes.size()];
		int posSS = (1 + SecurityDataFilter.NUM_CONTINUOUS_ATTRS) - 1;
		int posNumPoints = 1 + posSS;
		int posAttackTypesLen = posNumPoints + 1;
		int posAttackTypes = 4 * Global.INTSIZE + (SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1) * Global.FLOATSIZE;
		int nIndex = 0;
		int nTotalLen = -1;
		int numAttackTypes = 0;
		int numPoints = currentCF.N;
		int nAttackTypesLen = -1;
		for (int i = 0; i < SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1; i++)
			floatFinalOutBuf.put(i + 1, currentCF.LS[i]);

		log.debug("SS is " + currentCF.SS);
		floatFinalOutBuf.put(posSS, currentCF.SS);
		intFinalOutBuf.put(posNumPoints, numPoints);
		log.debug("the numPoints is " + numPoints);
		final_output_buf.position(posAttackTypes);
		for (Enumeration e = currentCF.hashAttackTypes.keys(); e.hasMoreElements();)
		{
			strAttackType = (String)e.nextElement();
			if (strAttackType == null)
			{
				log.error("can't be null");
			} else
			{
				if (strAttackTypes == null)
					strAttackTypes = strAttackType;
				else
					strAttackTypes = strAttackTypes + ";" + strAttackType;
				nIndex = ((Integer)currentCF.hashAttackTypes.get(strAttackType)).intValue();
				if (nIndex < 0)
					log.error("can't be less than 0");
				else
				if (currentCF.attack_types[nIndex] != 0)
				{
					attack_types[numAttackTypes] = currentCF.attack_types[nIndex];
					numAttackTypes++;
				}
			}
		}

		byteStr = strAttackTypes.getBytes();
		final_output_buf.put(byteStr, 0, byteStr.length);
		nAttackTypesLen = strAttackTypes.length();
		intFinalOutBuf.put(posAttackTypesLen, nAttackTypesLen);
		IntBuffer intFinalOutBufForNumAttackTypes = final_output_buf.asIntBuffer();
		for (int i = 0; i < numAttackTypes; i++)
			intFinalOutBufForNumAttackTypes.put(attack_types[i]);

		nTotalLen = 4 * Global.INTSIZE + (SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1) * Global.FLOATSIZE + nAttackTypesLen + numAttackTypes * Global.INTSIZE;
		intFinalOutBuf.put(0, nTotalLen);
		return nTotalLen;
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
		log = LogFactory.getLog((clustream.Clustering.class).getName());
	}
}
