// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   KMeans2.java

package clustream;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osu.ogsa.stream.util.Utilities;

// Referenced classes of package clustream:
//			CF, CFList, SecurityDataFilter

public class KMeans2
	implements Serializable
{

	private int k;
	private Vector points;
	private static float means[][];
	private static int total_N[];
	private int nTotalN;
	private static Random rand = new Random((new Date()).getTime());
	private static Log log;
	private static int nDimension;

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		means = new float[nTotalN][];
		total_N = new int[nTotalN];
	}

	public KMeans2(int max_k)
	{
		points = new Vector();
		KMeans2  = this;
		means = new float[max_k][];
		KMeans2 1 = this;
		total_N = new int[max_k];
		nTotalN = max_k;
	}

	public void addPoint(FloatBuffer floatTempBuf, String strAttackType)
	{
		float tempF[] = new float[nDimension];
		for (int i = 0; i < nDimension; i++)
			tempF[i] = floatTempBuf.get(i);

		points.add(tempF);
	}

	public void init()
	{
		Hashtable hashIndex = new Hashtable();
		int size = points.size();
		int i = 0;
		log.info("k is: " + k);
		while (i < k) 
		{
			int nTempIndex = Utilities.randnum(rand, size - 1);
			Integer intTemp = new Integer(nTempIndex);
			log.debug("here " + nTempIndex);
			if (!hashIndex.containsKey(intTemp))
			{
				hashIndex.put(intTemp, intTemp);
				float tempF[] = (float[])points.get(nTempIndex);
				if (tempF == null)
				{
					log.error("can't be null");
				} else
				{
					if (means[i] == null)
						means[i] = new float[nDimension];
					setMean(tempF, means[i]);
					i++;
				}
			}
		}
	}

	public void clearAll()
	{
		points.clear();
	}

	private float point_dis(float f1[], float f2[])
	{
		int intSum = 0;
		for (int i = 0; i < nDimension; i++)
		{
			int temp1 = (int)f1[i];
			int temp2 = (int)f2[i];
			intSum += (temp1 - temp2) * (temp1 - temp2);
		}

		return (float)intSum;
	}

	public void setMean(float srcMeans[], float means[])
	{
		for (int i = 0; i < nDimension; i++)
			means[i] = srcMeans[i];

	}

	public void runKMeans(CFList clusters, int k)
	{
		int nIteration = 0;
		float tempF[] = null;
		CF tempCF = null;
		Vector vecMeans[] = new Vector[k];
		float temp_means[][] = new float[k][];
		this.k = k;
		init();
		log.debug("hereher");
		do
		{
			nIteration++;
			for (int g = 0; g < k; g++)
			{
				total_N[g] = 0;
				if (vecMeans[g] == null)
					vecMeans[g] = new Vector();
				vecMeans[g].clear();
			}

			int j = 0;
			for (Enumeration e = points.elements(); e.hasMoreElements();)
			{
				tempF = (float[])e.nextElement();
				float minDis = 3.402823E+038F;
				int tempG = -1;
				for (int g = 0; g < k; g++)
				{
					for (int f = 0; f < nDimension; f++)
						log.debug(tempF[f] + " : " + means[g][f]);

					float tempDis = point_dis(tempF, means[g]);
					log.debug(g + " misDis:" + minDis + " tempDis:" + tempDis);
					if (tempDis < minDis)
					{
						minDis = tempDis;
						tempG = g;
					}
				}

				log.debug(j + " belongs to " + tempG);
				vecMeans[tempG].add(new Integer(j));
				total_N[tempG]++;
				j++;
			}

			for (int g = 0; g < k; g++)
			{
				if (temp_means[g] == null)
					temp_means[g] = new float[nDimension];
				for (j = 0; j < nDimension; j++)
				{
					temp_means[g][j] = 0.0F;
					for (Enumeration e = vecMeans[g].elements(); e.hasMoreElements();)
					{
						int indexPoint = ((Integer)e.nextElement()).intValue();
						tempF = (float[])points.get(indexPoint);
						temp_means[g][j] += tempF[j];
					}

					if (total_N[g] != 0)
					{
						temp_means[g][j] = temp_means[g][j] / (float)total_N[g];
						log.info(temp_means[g][j] + ":" + means[g][j] + " total_N[g]:" + total_N[g]);
						means[g][j] = temp_means[g][j];
					} else
					{
						log.error("total_N is 0");
					}
				}

				log.info("**********************************");
			}

			log.info("##################################################");
			log.info("iteranation" + nIteration);
		} while (nIteration < 4);
		log.debug("get a kmean");
		log.debug("iteranation" + nIteration);
		for (int g = 0; g < k; g++)
		{
			tempCF = new CF(nDimension);
			Enumeration e = vecMeans[g].elements();
			if (!e.hasMoreElements())
				log.error("can't be none");
			for (; e.hasMoreElements(); tempCF.add(tempF, "normal"))
			{
				int indexPoint = ((Integer)e.nextElement()).intValue();
				tempF = (float[])points.get(indexPoint);
			}

			clusters.add(tempCF);
		}

		log.debug("here");
		points.clear();
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
		log = LogFactory.getLog((clustream.KMeans2.class).getName());
		nDimension = SecurityDataFilter.NUM_CONTINUOUS_ATTRS - 1;
	}
}
