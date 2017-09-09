// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   CF.java

package clustream;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osu.ogsa.stream.util.DefConstants;

// Referenced classes of package clustream:
//			Global

public class CF
	implements Serializable
{

	public transient CF prev;
	public transient CF next;
	public int N;
	public float LS[];
	public float SS;
	public float X0[];
	public float radius;
	private static Log log;
	public int nDimension;
	private transient double probability;
	public transient Hashtable hashAttackTypes;
	public int nAttackTypes;
	public int attack_types[];
	public int nRealN;
	private static Random rand;

	public CF()
	{
		prev = null;
		next = null;
		SS = 0.0F;
		radius = 0.0F;
		hashAttackTypes = new Hashtable();
		N = nRealN = 0;
	}

	public CF(int nDimension)
	{
		prev = null;
		next = null;
		SS = 0.0F;
		radius = 0.0F;
		hashAttackTypes = new Hashtable();
		this.nDimension = nDimension;
		N = nRealN = 0;
		LS = new float[nDimension];
		X0 = new float[nDimension];
		SS = 0.0F;
		for (int i = 0; i < nDimension; i++)
			LS[i] = X0[i] = 0.0F;

		SS = 0.0F;
		radius = 0.0F;
		nAttackTypes = 0;
		attack_types = new int[Global.ATTACKS.length + 100];
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
	}

	public void initRadius(float init_r)
	{
		radius = init_r;
	}

	public CF(int nDimension, float init_cluster[], String strAttackType)
	{
		prev = null;
		next = null;
		SS = 0.0F;
		radius = 0.0F;
		hashAttackTypes = new Hashtable();
		new CF(nDimension, FloatBuffer.wrap(init_cluster), strAttackType);
	}

	public CF(int nDimension, FloatBuffer init_cluster, String strAttackType)
	{
		prev = null;
		next = null;
		SS = 0.0F;
		radius = 0.0F;
		hashAttackTypes = new Hashtable();
		this.nDimension = nDimension;
		N = 1;
		LS = new float[nDimension];
		X0 = new float[nDimension];
		SS = 0.0F;
		for (int i = 0; i < nDimension; i++)
		{
			LS[i] = X0[i] = init_cluster.get(i);
			SS += init_cluster.get(i) * init_cluster.get(i);
		}

		radius = 0.0F;
		nAttackTypes = 0;
		attack_types = new int[Global.ATTACKS.length + 100];
		nRealN = 1;
		if (strAttackType != null)
		{
			int nType = findAttack(strAttackType);
			if (nType >= 0)
				attack_types[nType]++;
		}
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
	}

	public CF(CF srcCF)
	{
		prev = null;
		next = null;
		SS = 0.0F;
		radius = 0.0F;
		hashAttackTypes = new Hashtable();
		nDimension = srcCF.nDimension;
		N = srcCF.N;
		LS = new float[nDimension];
		X0 = new float[nDimension];
		SS = srcCF.SS;
		for (int i = 0; i < nDimension; i++)
		{
			LS[i] = srcCF.LS[i];
			X0[i] = srcCF.X0[i];
		}

		radius = srcCF.radius;
		attack_types = new int[nAttackTypes + 100];
		String strAttackType = null;
		for (Enumeration e = srcCF.hashAttackTypes.keys(); e.hasMoreElements();)
		{
			strAttackType = (String)e.nextElement();
			if (strAttackType == null)
			{
				log.error("can't be null");
			} else
			{
				int nIndex = ((Integer)srcCF.hashAttackTypes.get(strAttackType)).intValue();
				if (nIndex < 0)
				{
					log.error("can't be less than 0");
				} else
				{
					int nMyIndex = findAttack(strAttackType);
					if (nMyIndex < 0)
						log.error("can't be less than 0");
					else
						attack_types[nMyIndex] = srcCF.attack_types[nIndex];
				}
			}
		}

		nAttackTypes = srcCF.nAttackTypes;
		nRealN = srcCF.nRealN;
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
	}

	public int getDimension()
	{
		return nDimension;
	}

	public void add(CF tempCF)
	{
		int i = 0;
		N += tempCF.N;
		nRealN += tempCF.nRealN;
		for (i = 0; i < nDimension; i++)
		{
			LS[i] += tempCF.LS[i];
			X0[i] = LS[i] / (float)N;
		}

		SS += tempCF.SS;
		String strAttackType = null;
		int nIndex = -1;
		int nMyIndex = -1;
		for (Enumeration e = tempCF.hashAttackTypes.keys(); e.hasMoreElements();)
		{
			strAttackType = (String)e.nextElement();
			if (strAttackType == null)
			{
				log.error("can't be null");
				return;
			}
			nIndex = ((Integer)tempCF.hashAttackTypes.get(strAttackType)).intValue();
			if (nIndex < 0)
			{
				log.error("can't be less than 0");
				return;
			}
			nMyIndex = findAttack(strAttackType);
			if (nMyIndex < 0)
			{
				log.error("can't be less than 0");
				return;
			}
			attack_types[nMyIndex] += tempCF.attack_types[nIndex];
		}

	}

	public void add(float point[], String strAttackType)
	{
		add(FloatBuffer.wrap(point), strAttackType);
	}

	public void add(FloatBuffer point, String strAttackType)
	{
		int flag = 0;
		if (flag >= 1)
		{
			float coeff = 0.5F;
			float temp_SS = SS * coeff * coeff;
			N = (int)((double)((float)N * coeff) + 0.5D) + 1;
			for (int i = 0; i < nDimension; i++)
			{
				LS[i] = LS[i] * coeff + point.get(i);
				SS = temp_SS + point.get(i) * point.get(i);
				X0[i] = LS[i] / (float)N;
			}

		} else
		{
			N++;
			for (int i = 0; i < nDimension; i++)
			{
				LS[i] += point.get(i);
				SS += point.get(i) * point.get(i);
				X0[i] = LS[i] / (float)N;
			}

		}
		log.debug("N is " + N);
		nRealN++;
		int nType = findAttack(strAttackType);
		if (nType >= 0)
			attack_types[nType]++;
		else
			log.error("can't be less than 0");
	}

	public boolean isPointBelongToHere(float floatTempBuf[])
	{
		return isPointBelongToHere(FloatBuffer.wrap(floatTempBuf));
	}

	public boolean isPointBelongToHere(FloatBuffer floatTempBuf)
	{
		float X0_square = 0.0F;
		if (nRealN != 1)
		{
			for (int i = 0; i < nDimension; i++)
				X0_square += X0[i] * X0[i];

			radius = (float)Math.sqrt(SS / (float)N - X0_square);
		}
		float dis = point_dis(floatTempBuf);
		log.debug("dis: " + dis + " radius: " + radius);
		if (Global.IFDETERMINISTIC)
			return dis <= radius;
		if (dis <= radius)
			return true;
		probability = (radius * radius) / (dis * dis);
		return DefConstants.Biased_Coin(rand, probability) == 1;
	}

	public boolean isPointBelongToHere(float distance)
	{
		float X0_square = 0.0F;
		if (nRealN != 1)
		{
			for (int i = 0; i < nDimension; i++)
				X0_square += X0[i] * X0[i];

			radius = (float)Math.sqrt(SS / (float)N - X0_square);
		}
		if (Global.IFDETERMINISTIC)
			return distance <= radius;
		if (distance <= radius)
			return true;
		probability = (radius * radius) / (distance * distance);
		return DefConstants.Biased_Coin(rand, probability) == 1;
	}

	public float point_dis(float point[])
	{
		float sum = 0.0F;
		for (int i = 0; i < nDimension; i++)
		{
			sum += (X0[i] - point[i]) * (X0[i] - point[i]);
			log.info(X0[i] + "  " + point[i]);
		}

		sum = (float)Math.sqrt(sum);
		return sum;
	}

	public float point_dis(FloatBuffer point)
	{
		float sum = 0.0F;
		float X0_p = 0.0F;
		float p_square = 0.0F;
		float X0_square = 0.0F;
		for (int i = 0; i < nDimension; i++)
		{
			X0_p += X0[i] * point.get(i);
			p_square += point.get(i) * point.get(i);
			X0_square += X0[i] * X0[i];
		}

		sum = (float)Math.sqrt((X0_square - 2.0F * X0_p) + p_square);
		return sum;
	}

	public float inter_cluster_dis(CF cf_orig)
	{
		float temp = 0.0F;
		for (int i = 0; i < nDimension; i++)
			temp += (cf_orig.X0[i] - X0[i]) * (cf_orig.X0[i] - X0[i]);

		temp = (float)Math.sqrt(temp);
		return temp;
	}

	public int findAttack(String strAttack)
	{
		if (strAttack == null)
		{
			log.error("the string of the attack is null");
			return -1;
		}
		Integer intIndex = (Integer)hashAttackTypes.get(strAttack);
		if (intIndex != null)
		{
			return intIndex.intValue();
		} else
		{
			hashAttackTypes.put(strAttack, new Integer(nAttackTypes));
			nAttackTypes = nAttackTypes + 1;
			return nAttackTypes - 1;
		}
	}

	public void setN(int N)
	{
		this.N = N;
		nRealN = N;
	}

	public void setSS(float SS)
	{
		this.SS = SS;
	}

	public void addAttackType(String strAttackType, int frequent)
	{
		int nType = findAttack(strAttackType);
		if (nType >= 0)
			attack_types[nType] = frequent;
		else
			log.error("can't be less than 0");
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
		out.writeObject(new Integer(hashAttackTypes.size()));
		String strAttackType;
		for (Enumeration e = hashAttackTypes.keys(); e.hasMoreElements(); out.writeObject(hashAttackTypes.get(strAttackType)))
		{
			strAttackType = (String)e.nextElement();
			if (strAttackType == null)
			{
				log.error("can't be null");
				return;
			}
			out.writeObject(strAttackType);
		}

	}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		hashAttackTypes = new Hashtable();
		int size = ((Integer)in.readObject()).intValue();
		for (int i = 0; i < size; i++)
		{
			String strAttackType = (String)in.readObject();
			Integer intTemp = (Integer)in.readObject();
			hashAttackTypes.put(strAttackType, intTemp);
		}

		prev = next = null;
		Date forRand = new Date();
		rand = new Random(forRand.getTime());
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
		log = LogFactory.getLog((clustream.CF.class).getName());
	}
}