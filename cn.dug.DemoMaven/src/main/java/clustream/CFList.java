// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   CFList.java

package clustream;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osu.ogsa.stream.util.LSS;

// Referenced classes of package clustream:
//			CF, KMeans2, CFListIterator

public class CFList extends LSS
{

	public transient CF head;
	protected int count;
	private int capacity;
	public int nPoints;
	public int nReadPoints;
	boolean bSendingFlag;
	public boolean bInit;
	private static Log log;
	private KMeans2 kmean;

	public CFList()
	{
		count = 0;
		capacity = 0;
		head = new CF();
		head.next = head;
		head.prev = head;
		nPoints = nReadPoints = 0;
		bSendingFlag = false;
		capacity = -1;
		kmean = null;
		bInit = false;
	}

	public void initialize(int capacity)
	{
		this.capacity = capacity;
		kmean = new KMeans2(capacity);
		bInit = true;
	}

	public int getNumClusters()
	{
		return count;
	}

	public KMeans2 getKMeans()
	{
		return kmean;
	}

	public void setCapacity(int newCapacity)
	{
		capacity = newCapacity;
	}

	public boolean isFull()
	{
		return count >= capacity;
	}

	public void add(CF item)
	{
		if (item == null)
		{
			return;
		} else
		{
			CF first = head.next;
			item.next = first;
			item.prev = head;
			first.prev = item;
			head.next = item;
			count++;
			log.debug("count:" + count);
			return;
		}
	}

	public void remove(CF item)
	{
		if (item == head)
			return;
		item.prev.next = item.next;
		item.next.prev = item.prev;
		count--;
		if (count < 0)
			count = 0;
	}

	public void replicateTo(CFList destClusters)
	{
		CFListIterator iterator = null;
		CF currentCF = null;
		for (iterator = iterator(); iterator.hasNext(); destClusters.add(new CF(currentCF)))
		{
			currentCF = iterator.next();
			if (currentCF == null)
			{
				log.error("can't be null");
				return;
			}
		}

	}

	public void clearAll()
	{
		CFListIterator iterator = null;
		CF currentCF = null;
		for (iterator = iterator(); iterator.hasNext(); remove(currentCF))
		{
			currentCF = iterator.next();
			if (currentCF == null)
			{
				log.error("can't be null");
				return;
			}
		}

		if (!isEmpty())
			log.error("can not be non-empty");
		count = 0;
		kmean.clearAll();
	}

	public boolean isEmpty()
	{
		return head.next == head;
	}

	public CF first()
	{
		if (isEmpty())
			return null;
		else
			return head.next;
	}

	public CFListIterator iterator()
	{
		CFListIterator iter = new CFListIterator(this);
		return iter;
	}

	public CFListIterator iterator(CF cursor)
	{
		CFListIterator iter = new CFListIterator(this, cursor);
		return iter;
	}

	public void mergeClusters()
	{
		CFListIterator compIterator;
		CFListIterator iterator = compIterator = null;
		CF compCF;
		CF minCF1;
		CF minCF2;
		CF currentCF = compCF = minCF1 = minCF2 = null;
		float distance = -1F;
		float minDistance = 3.402823E+038F;
		for (iterator = iterator(); iterator.hasNext();)
		{
			currentCF = iterator.next();
			if (currentCF == null)
			{
				log.error("can't be null");
				return;
			}
			for (compIterator = iterator(currentCF); compIterator.hasNext();)
			{
				compCF = compIterator.next();
				distance = currentCF.inter_cluster_dis(compCF);
				if (distance < minDistance)
				{
					minDistance = distance;
					minCF1 = currentCF;
					minCF2 = compCF;
				}
			}

		}

		if (minCF1.N > minCF2.N)
		{
			minCF1.add(minCF2);
			remove(minCF2);
		} else
		{
			minCF2.add(minCF1);
			remove(minCF1);
		}
	}

	public void printMyself()
	{
		System.out.println("count = " + count);
		System.out.println("capacity = " + capacity);
		System.out.println("nReadPoints = " + nReadPoints);
		System.out.println("nPoints = " + nPoints);
		System.out.println("bInit = " + bInit);
	}

	public void printClusters()
	{
		CFListIterator iterator = null;
		String strAttackType = null;
		int nIndex = 0;
		for (iterator = iterator(); iterator.hasNext();)
		{
			CF currentCF = iterator.next();
			log.info("cluster realn : " + currentCF.nRealN);
			if (currentCF == null)
			{
				log.error("can't be nulll");
				return;
			}
			for (Enumeration e = currentCF.hashAttackTypes.keys(); e.hasMoreElements();)
			{
				strAttackType = (String)e.nextElement();
				if (strAttackType == null)
				{
					log.error("can't be null");
				} else
				{
					nIndex = ((Integer)currentCF.hashAttackTypes.get(strAttackType)).intValue();
					if (nIndex < 0)
						log.error("can't be less than 0");
					else
					if (currentCF.attack_types[nIndex] != 0)
						log.info("index:" + nIndex + strAttackType + ":" + currentCF.attack_types[nIndex]);
				}
			}

		}

	}

	public void addTrainingPoint(FloatBuffer floatTempBuf, String strAttackType)
	{
		kmean.addPoint(floatTempBuf, strAttackType);
	}

	public void runKMeans(int k)
	{
		kmean.runKMeans(this, k);
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
		log.warn("starting to write an Object");
		CF currentCF;
		for (CFListIterator iterator = iterator(); iterator.hasNext(); out.writeObject(currentCF))
			currentCF = iterator.next();

	}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		try
		{
			in.defaultReadObject();
			log.warn("starting to read objects");
			head = new CF();
			head.next = head;
			head.prev = head;
			int temp = count;
			count = 0;
			for (int i = 0; i < temp; i++)
			{
				CF currentCF = (CF)in.readObject();
				add(currentCF);
			}

		}
		catch (OptionalDataException e)
		{
			log.error("eof: " + e.eof);
			log.error("length: " + e.length);
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
		log = LogFactory.getLog((clustream.CFList.class).getName());
	}
}
