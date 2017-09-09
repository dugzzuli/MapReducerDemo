// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   CFListIterator.java

package clustream;


// Referenced classes of package clustream:
//			CFList, CF

public class CFListIterator
{

	private CFList list;
	private CF cursor;
	private CF head;

	public CFListIterator(CFList l)
	{
		list = l;
		head = list.head;
		cursor = list.head;
	}

	public CFListIterator(CFList l, CF cursor)
	{
		list = l;
		head = list.head;
		this.cursor = cursor;
	}

	public boolean hasNext()
	{
		return cursor.next != list.head;
	}

	public CF next()
	{
		cursor = cursor.next;
		return cursor;
	}
}
