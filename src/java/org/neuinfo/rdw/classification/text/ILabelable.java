package org.neuinfo.rdw.classification.text;

import java.util.List;

public interface ILabelable<T> {
	public int getNumOfClasses();

	public int[] getClassIDs();

	public int getClassID(T instance);

	public int getSizeForClass(int classId);
	
	public List<T> getInstances4Class(int classId, List<T> dataSet);

}
