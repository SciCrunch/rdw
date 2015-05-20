package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import bnlpkit.util.Assertion;

public class DatasetSplitter<T> {
	List<T> dataSet;
	/** fraction of the test data set */
	protected double fraction;
	protected Random rnd;

	public DatasetSplitter(List<T> dataSet, double testFraction, long seed) {
		this.dataSet = dataSet;
		this.fraction = testFraction;
		rnd = new Random(seed);
	}

	public DatasetWrapper<T> filter(ILabelable<T> classMetaData) {
		int noClasses = classMetaData.getNumOfClasses();
		System.out.println("#classes:" + noClasses + " #instances:"
				+ dataSet.size());
		int[] testCaseSizes = new int[noClasses];
		int testSize = (int) (dataSet.size() * fraction);
		Assertion.assertTrue(testSize > 0);
		int[] classIDs = classMetaData.getClassIDs();
		int sum = 0;
		for (int i = 0; i < noClasses; i++) {
			testCaseSizes[i] = (int) (classMetaData.getSizeForClass(classIDs[i]) * fraction);
			sum += testCaseSizes[i];
		}
		System.out.println("testSize difference due to roundoff error "
				+ (testSize - sum));
		testSize = sum;
		List<T> testSet = new ArrayList<T>();
		for (int i = 0; i < noClasses; i++) {
			List<T> instances = classMetaData.getInstances4Class(classIDs[i],
					dataSet);
			int count = 0;
			Set<T> selectedInstances = new HashSet<T>();
			while (count < testCaseSizes[i]) {
				for (T instance : instances) {
					if (selectedInstances.contains(instance)) {
						continue;
					}
					if (count < testCaseSizes[i]
							&& rnd.nextDouble() <= fraction) {
						testSet.add(instance);
						selectedInstances.add(instance);
						count++;
					}
				}
			}
		}
		// training part
		List<T> trainSet = new ArrayList<T>();
		Set<T> selectedInstances = new HashSet<T>(testSet);
		for (T instance : dataSet) {
			if (!selectedInstances.contains(instance)) {
				trainSet.add(instance);
			}
		}

		System.out.println("finished stratification");

		System.out.println("# training instances:" + trainSet.size());
		System.out.println("# testing instances:" + testSet.size());
		return new DatasetWrapper<T>(trainSet, testSet);
	}

	public static class DatasetWrapper<T> {
		final List<T> trainSet;
		final List<T> testSet;

		public DatasetWrapper(List<T> trainSet, List<T> testSet) {
			this.trainSet = trainSet;
			this.testSet = testSet;
		}

		public List<T> getTrainSet() {
			return trainSet;
		}

		public List<T> getTestSet() {
			return testSet;
		}

	}
}
