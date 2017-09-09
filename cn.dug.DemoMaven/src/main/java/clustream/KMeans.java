// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   KMeans.java

package clustream;

import java.io.PrintStream;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osu.ogsa.stream.util.Utilities;

// Referenced classes of package clustream:
//			CF, SecurityDataFilter

public class KMeans
{

	private int k;
	private CF final_clusters[];
	private CF microClusters[];
	private float means[][];
	private float temp_means[][];
	private int total_N[];
	private int nTotalN;
	private int nNumMicroClusters;
	private static boolean bInit = true;
	private boolean bDifferent;
	private Random rand;
	private static Log log;
	private Vector vecMeans[];
	private int nDimension;
	private float sumSS;
	private float sumSquareMeans;
	private float sumLS;
	private float SSD;
	private float tempSquareMeans;

	public KMeans(int k)
	{
		bDifferent = false;
		nDimension = SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1;
		this.k = k;
		means = new float[k][];
		temp_means = new float[k][];
		total_N = new int[k];
		vecMeans = new Vector[k];
	}

	public void init(CF clusters[], int numMicroClusters)
	{
		microClusters = clusters;
		nNumMicroClusters = numMicroClusters;
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
		int indexMeans[] = new int[k];
		nTotalN = 0;
		for (int j = 0; j < numMicroClusters; j++)
			nTotalN += microClusters[j].nRealN;

		final_clusters = new CF[k];
		int i = 0;
		for (int g = 0; g < k; g++)
			indexMeans[g] = -1;

		while (i < k) 
		{
			for (int j = 0; j < numMicroClusters; j++)
			{
				int g;
				for (g = 0; g < k && j != indexMeans[g]; g++);
				if (g < k || Utilities.Biased_Coin(rand, (double)microClusters[j].nRealN / (double)nTotalN) != 1)
					continue;
				means[i] = new float[nDimension];
				temp_means[i] = new float[nDimension];
				final_clusters[i] = new CF(nDimension);
				vecMeans[i] = new Vector();
				setMean(microClusters[j], means[i]);
				log.debug(j + " belongs to " + i);
				if (++i >= k)
					break;
			}

		}
	}

	public void setMean(CF cluster, float means[])
	{
		for (int i = 0; i < nDimension; i++)
			means[i] = cluster.X0[i];

	}

	public float runKMeans()
	{
		int nIteration = 0;
		do
		{
			nIteration++;
			for (int g = 0; g < k; g++)
			{
				total_N[g] = 0;
				vecMeans[g].clear();
			}

			for (int j = 0; j < nNumMicroClusters; j++)
			{
				float minDis = 3.402823E+038F;
				int tempG = -1;
				for (int g = 0; g < k; g++)
				{
					float tempDis = microClusters[j].point_dis(means[g]);
					log.debug(g + " misDis:" + minDis + " tempDis:" + tempDis);
					if (tempDis < minDis)
					{
						minDis = tempDis;
						tempG = g;
					}
				}

				log.debug(j + " belongs to " + tempG);
				vecMeans[tempG].add(new Integer(j));
				total_N[tempG] += microClusters[j].nRealN;
			}

			bDifferent = false;
			for (int g = 0; g < k; g++)
			{
				for (int j = 0; j < nDimension; j++)
				{
					temp_means[g][j] = 0.0F;
					for (Enumeration e = vecMeans[g].elements(); e.hasMoreElements();)
					{
						int indexCF = ((Integer)e.nextElement()).intValue();
						temp_means[g][j] += microClusters[indexCF].LS[j];
					}

					if (total_N[g] != 0)
					{
						temp_means[g][j] = temp_means[g][j] / (float)total_N[g];
						log.debug(temp_means[g][j] + ":" + means[g][j] + " total_N[g]:" + total_N[g]);
						if ((double)temp_means[g][j] >= 1.0D || (double)means[g][j] >= 1.0D)
							if ((double)means[g][j] == 0.0D)
							{
								if ((double)temp_means[g][j] != 0.0D)
									bDifferent = true;
							} else
							if ((double)Math.abs(((temp_means[g][j] - means[g][j]) / means[g][j]) * 100F) > 1.0D)
								bDifferent = true;
						means[g][j] = temp_means[g][j];
					}
				}

				log.debug("**********************************");
			}

			log.debug("##################################################");
		} while (bDifferent);
		log.debug("get a kmean");
		sumSS = 0.0F;
		sumSquareMeans = 0.0F;
		sumLS = 0.0F;
		for (int g = 0; g < k; g++)
		{
			log.debug(g + " : " + total_N[g]);
			tempSquareMeans = 0.0F;
			for (int j = 0; j < nDimension; j++)
				tempSquareMeans += means[g][j] * means[g][j];

			sumSquareMeans += tempSquareMeans * (float)total_N[g];
			for (Enumeration e = vecMeans[g].elements(); e.hasMoreElements();)
			{
				int indexCF = ((Integer)e.nextElement()).intValue();
				sumSS += microClusters[indexCF].SS;
				for (int j = 0; j < nDimension; j++)
					sumLS += microClusters[indexCF].LS[j] * means[g][j];

			}

		}

		SSD = (sumSS - 2.0F * sumLS) + sumSquareMeans;
		return SSD;
	}

	public void printClusters()
	{
		String strAttackType = null;
		for (int i = 0; i < k; i++)
		{
			CF currentCF = final_clusters[i];
			for (int j = 0; j < nDimension; j++)
				System.out.println(j + ":" + currentCF.X0[j]);

			System.out.println(i + "th mean... has " + currentCF.nRealN + " :");
			for (Enumeration e = currentCF.hashAttackTypes.keys(); e.hasMoreElements();)
			{
				strAttackType = (String)e.nextElement();
				if (strAttackType == null)
				{
					log.error("can't be null");
				} else
				{
					int nIndex = ((Integer)currentCF.hashAttackTypes.get(strAttackType)).intValue();
					if (nIndex < 0)
						log.error("can't be less than 0");
					else
					if (currentCF.attack_types[nIndex] != 0)
						System.out.println("index:" + nIndex + strAttackType + ":" + currentCF.attack_types[nIndex]);
				}
			}

			System.out.println("******************************************");
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
		log = LogFactory.getLog((clustream.KMeans.class).getName());
	}
}