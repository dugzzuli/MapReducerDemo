// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Merging.java

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
//			KMeans, CF, Global, SecurityDataFilter

public class Merging
	implements StreamProcessor
{

	private StreamServiceProvider srvProvider;
	private int pInt_Smp[];
	private int fin_pInt_Smp[];
	private Random rand;
	private boolean bFileLog;
	private FileLog fileLog;
	private int MAX_NUM_CLUSTERS;
	private static int STATUS_INIT = 0;
	private static int STATUS_MICRO_CLUSTERS = 1;
	private static int MAX_INPUT_BUFFERS = 20;
	private static Log log;
	private static int K;

	public Merging()
	{
		MAX_NUM_CLUSTERS = Global.MAX_NUM_CLUSTERS;
	}

	public void init(StreamServiceProvider srvProvider)
	{
		this.srvProvider = srvProvider;
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
	}

	public void work(AutoFillInputBufferArray inBufArray, AutoFillOutputBufferArray outBufArray)
	{
		Date tStart = new Date();
		bFileLog = Global.FILELOG;
		try
		{
			if (bFileLog)
			{
				String myLogFileName = "Merging_" + tStart.getTime();
				fileLog = new FileLog(myLogFileName);
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
		AutoFillInputBuffer inBuffer = null;
		ByteBuffer tempBuf[] = new ByteBuffer[MAX_INPUT_BUFFERS];
		FloatBuffer floatTempBuf[] = new FloatBuffer[MAX_INPUT_BUFFERS];
		IntBuffer intTempBuf[] = new IntBuffer[MAX_INPUT_BUFFERS];
		KMeans kmeans[] = new KMeans[MAX_INPUT_BUFFERS];
		ByteBuffer strByteBuf = ByteBuffer.allocate(40);
		char tempChar = '1';
		boolean bExit[] = new boolean[MAX_INPUT_BUFFERS];
		int nClusterIndex[] = new int[MAX_INPUT_BUFFERS];
		int nNumMicroClusters[] = new int[MAX_INPUT_BUFFERS];
		int status[] = new int[MAX_INPUT_BUFFERS];
		CF clusters[][] = new CF[MAX_INPUT_BUFFERS][];
		int nDimension = SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1;
		int set_size = SecurityDataFilter.NUM_CONTINUOUS_ATTRS * Global.FLOATSIZE + 1032 + 4 + 4 + 4 + 100;
		for (int i = 0; i < MAX_INPUT_BUFFERS; i++)
		{
			tempBuf[i] = null;
			floatTempBuf[i] = null;
			intTempBuf[i] = null;
			bExit[i] = false;
			nNumMicroClusters[i] = 0;
			status[i] = STATUS_INIT;
			kmeans[i] = null;
			clusters[i] = new CF[MAX_NUM_CLUSTERS];
			nClusterIndex[i] = 0;
		}

		int temp = 0;
		float fSS = 0.0F;
		float SSD = 0.0F;
		int temp1 = 0;
		Hashtable hashCF = new Hashtable();
		CF newCF;
		CF tempCF = newCF = null;
		String strAttackTypes = null;
		String strArrayAttackTypes[] = null;
		int posSS = SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1;
		int posNumPoints = posSS + 1;
		int posAttackTypesLen = posNumPoints + 1;
		int posAttackTypes = (posAttackTypesLen + 1) * Global.INTSIZE;
		int nExit = 0;
		int g = 0;
		byte byteArray[] = null;
		int frequent = 0;
		do
		{
			int numBuf = inBufArray.howmanyInputBuffers();
			if (numBuf != nExit)
			{
				for (int i = 0; i < numBuf; i++)
				{
					if (bExit[i])
						continue;
					if (tempBuf[i] == null)
					{
						tempBuf[i] = ByteBuffer.allocate(set_size);
						floatTempBuf[i] = tempBuf[i].asFloatBuffer();
						intTempBuf[i] = tempBuf[i].asIntBuffer();
					}
					inBuffer = inBufArray.getValidInputBufferChangeStatus(i);
					if (inBuffer == null)
					{
						int subIndex = inBufArray.getSubstitute(i);
						log.error("the new index is " + subIndex);
						if (subIndex < 0 || subIndex == i)
						{
							log.error("wait...");
							continue;
						}
						inBuffer = inBufArray.getInputBuffer(subIndex);
						tempBuf[subIndex] = tempBuf[i];
						floatTempBuf[subIndex] = floatTempBuf[i];
						intTempBuf[subIndex] = intTempBuf[i];
						status[subIndex] = status[i];
						nNumMicroClusters[subIndex] = nNumMicroClusters[i];
						nClusterIndex[subIndex] = nClusterIndex[i];
						clusters[subIndex] = clusters[i];
						nExit++;
						bExit[i] = true;
						i = subIndex;
					}
					if (status[i] == STATUS_INIT)
					{
						temp1 = inBuffer.readToByteBuffer(tempBuf[i], 4, 4, false);
						if (temp1 == 4)
							if (intTempBuf[i].get(0) == -1)
							{
								bExit[i] = true;
								nExit++;
							} else
							{
								nNumMicroClusters[i] = intTempBuf[i].get(0);
								log.debug("the number of microclusters is: " + nNumMicroClusters[i]);
								nClusterIndex[i] = 0;
								tempBuf[i].clear();
								status[i] = STATUS_MICRO_CLUSTERS;
							}
					} else
					if (status[i] == STATUS_MICRO_CLUSTERS)
					{
						tempBuf[i].clear();
						if (nClusterIndex[i] == nNumMicroClusters[i])
						{
							if (kmeans[i] == null)
								kmeans[i] = new KMeans(K);
							kmeans[i].init(clusters[i], nNumMicroClusters[i]);
							SSD += kmeans[i].runKMeans();
							status[i] = STATUS_INIT;
						} else
						{
							log.debug("readToByteBuffer 2");
							do
							{
								inBuffer = inBufArray.getValidInputBufferChangeStatus(i);
								if (inBuffer == null)
								{
									int subIndex = inBufArray.getSubstitute(i);
									log.error("the new index is " + subIndex);
									if (subIndex < 0 || subIndex == i)
									{
										log.error("wait...");
									} else
									{
										inBuffer = inBufArray.getInputBuffer(subIndex);
										tempBuf[subIndex] = tempBuf[i];
										floatTempBuf[subIndex] = floatTempBuf[i];
										intTempBuf[subIndex] = intTempBuf[i];
										status[subIndex] = status[i];
										nNumMicroClusters[subIndex] = nNumMicroClusters[i];
										nClusterIndex[subIndex] = nClusterIndex[i];
										clusters[subIndex] = clusters[i];
										nExit++;
										bExit[i] = true;
										i = subIndex;
									}
								}
							} while ((temp1 = inBuffer.readToByteBuffer(tempBuf[i], 4, 4, false)) != 4);
							int nTotalLen = intTempBuf[i].get(0);
							log.debug("nTotalLen:" + nTotalLen);
							if (nTotalLen <= 0)
							{
								log.error("can't be ");
							} else
							{
								tempBuf[i].clear();
								log.debug("readToByteBuffer 3");
								do
								{
									inBuffer = inBufArray.getValidInputBufferChangeStatus(i);
									if (inBuffer == null)
									{
										int subIndex = inBufArray.getSubstitute(i);
										log.debug("the new subIndex is " + subIndex);
										if (subIndex < 0 || subIndex == i)
										{
											log.error("wait...");
										} else
										{
											inBuffer = inBufArray.getInputBuffer(subIndex);
											tempBuf[subIndex] = tempBuf[i];
											floatTempBuf[subIndex] = floatTempBuf[i];
											intTempBuf[subIndex] = intTempBuf[i];
											status[subIndex] = status[i];
											nNumMicroClusters[subIndex] = nNumMicroClusters[i];
											nClusterIndex[subIndex] = nClusterIndex[i];
											clusters[subIndex] = clusters[i];
											nExit++;
											bExit[i] = true;
											i = subIndex;
										}
									}
								} while ((temp1 = inBuffer.readToByteBuffer(tempBuf[i], nTotalLen - 4, nTotalLen - 4, false)) != nTotalLen - 4);
								clusters[i][nClusterIndex[i]] = new CF(nDimension, floatTempBuf[i], null);
								fSS = floatTempBuf[i].get(posSS);
								clusters[i][nClusterIndex[i]].setSS(fSS);
								int numPoints = intTempBuf[i].get(posNumPoints);
								clusters[i][nClusterIndex[i]].setN(numPoints);
								for (int j = 0; j < nDimension; j++)
									clusters[i][nClusterIndex[i]].X0[j] = clusters[i][nClusterIndex[i]].LS[j] / (float)numPoints;

								int nAttackTypesLen = intTempBuf[i].get(posAttackTypesLen);
								log.debug("numPoints: " + numPoints);
								if (nAttackTypesLen <= 0)
								{
									log.error("can't be less than 0");
								} else
								{
									try
									{
										tempBuf[i].clear();
										byteArray = tempBuf[i].array();
									}
									catch (Exception e)
									{
										log.error("can't get the array from ByteBuffer");
									}
									strAttackTypes = new String(byteArray, posAttackTypes, nAttackTypesLen);
									log.debug(strAttackTypes);
									strArrayAttackTypes = strAttackTypes.split(";");
									if (strArrayAttackTypes.length <= 0)
									{
										log.error("can't be less than 0");
									} else
									{
										int posFrequentAttackTypes = posAttackTypes + nAttackTypesLen;
										tempBuf[i].position(posFrequentAttackTypes);
										IntBuffer intTempBuf_FrequentAttackTypes = tempBuf[i].asIntBuffer();
										for (g = 0; g < strArrayAttackTypes.length; g++)
										{
											frequent = intTempBuf_FrequentAttackTypes.get(g);
											clusters[i][nClusterIndex[i]].addAttackType(strArrayAttackTypes[g], frequent);
											log.debug(nClusterIndex[i] + ":" + strArrayAttackTypes[g] + "frequent:" + frequent);
										}

										nClusterIndex[i] = nClusterIndex[i] + 1;
									}
								}
							}
						}
					}
				}

			} else
			{
				Date tEnd = new Date();
				System.out.println("[" + srvProvider.myHandle + "]:" + "Merging finisihed work: time consumed is " + Global.timeval_diff(tStart, tEnd));
				System.out.println("the SSD is " + SSD);
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
		log = LogFactory.getLog((clustream.Merging.class).getName());
		K = Global.K;
	}
}
