package org.neuinfo.rdw.classification.text;

import java.io.BufferedWriter;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.neuinfo.rdw.classification.text.DatasetSplitter.DatasetWrapper;
import org.neuinfo.rdw.data.model.ResourceCandidateInfo;

import bnlpkit.nlp.common.CharSetEncoding;
import bnlpkit.nlp.common.classification.ClassificationStats;
import bnlpkit.util.Assertion;
import bnlpkit.util.FileUtils;

/**
 * for testing different filtering policies for resource candidate
 * classification
 * 
 * @author bozyurt
 *
 */
public class RCCTrainTestUtils {
	ResourceCandidateClassifier rcc;

	public RCCTrainTestUtils(ResourceCandidateClassifier rcc) {
		this.rcc = rcc;
	}

	public List<ResourceCandidateInfo> loadDataSet() throws Exception {
		Connection con = null;
		String jdbcURL = "jdbc:postgresql://localhost:5432/rd_prod";
		Statement st = null;
		List<ResourceCandidateInfo> rciList = new ArrayList<ResourceCandidateInfo>();
		try {
			con = DriverManager.getConnection(jdbcURL, "rd_prod", "");
			st = con.createStatement();
			ResultSet rs = st
					.executeQuery("select u.id, a.label, u.description, u.url, u.score from rd_url_annot_info a, rd_urls u "
							+ "where a.url_id = u.id and a.op_type = 'candidate_filter' and u.batch_id = '201403'");
			while (rs.next()) {
				int urlId = rs.getInt(1);
				String label = rs.getString(2);
				ResourceCandidateInfo rci = new ResourceCandidateInfo(urlId);
				rci.setLabel(label);
				rci.setDescription(rs.getString(3));
				rci.setUrl(rs.getString(4));
				rci.setScore((int) rs.getFloat(5));
				rciList.add(rci);
			}
			rs.close();
			st.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		return rciList;
	}

	DatasetWrapper<ResourceCandidateInfo> prepTrainTestDataset(
			List<ResourceCandidateInfo> rciList, double testFrac, long seed) {

		DatasetSplitter<ResourceCandidateInfo> splitter = new DatasetSplitter<ResourceCandidateInfo>(
				rciList, testFrac, seed);
		return splitter.filter(new RCCDataSetMetadata(rciList));
	}

	public List<ClassificationStats> testLearning(
			DatasetWrapper<ResourceCandidateInfo> dsw, long seed,
			IALPolicy policy, String resultFile) throws Exception {
		List<ResourceCandidateInfo> testList = dsw.getTestSet();
		DatasetWrapper<ResourceCandidateInfo> trDSW = selectStartingTrainingSet(
				dsw.getTrainSet(), seed);
		List<ResourceCandidateInfo> poolList = trDSW.getTestSet();
		Assertion.assertTrue(poolList.size() > 100);
		List<ResourceCandidateInfo> seedList = trDSW.getTrainSet();
		this.rcc.prepTrainingFeatures(seedList);
		this.rcc.train();

		List<ClassificationStats> csList = new ArrayList<ClassificationStats>(
				100);
		for (int i = 0; i < 100; i++) {
			ResourceCandidateInfo selRCI = selectNextInstanceFromPool(poolList,
					policy);
			ClassificationStats cs = classifyTestSet(testList);
			csList.add(cs);

			poolList.remove(selRCI);
			seedList.add(selRCI);
			this.rcc.prepTrainingFeatures(seedList);
			this.rcc.train();
		}

		saveClassificationResult(csList, resultFile);
		System.out.println("saved results to " + resultFile);

		return csList;
	}

	void saveClassificationResult(List<ClassificationStats> csList,
			String outTextFile) throws Exception {
		BufferedWriter out = null;
		try {
			out = FileUtils
					.getBufferedWriter(outTextFile, CharSetEncoding.UTF8);
			out.write("X,P,R,F1");
			out.newLine();
			int i = 2;
			for (ClassificationStats cs : csList) {
				out.write(String.format("%d,%.2f,%.2f,%.2f", i, cs.getP(),
						cs.getR(), cs.getF1()));
				out.newLine();
				i++;
			}

		} finally {
			FileUtils.close(out);
		}
	}

	DatasetWrapper<ResourceCandidateInfo> selectStartingTrainingSet(
			List<ResourceCandidateInfo> fullTrSet, long seed) {
		// randomly select a single positive and a single negative example from
		// the training set
		List<ResourceCandidateInfo> fullTrSetCopy = new ArrayList<ResourceCandidateInfo>(
				fullTrSet);
		Collections.shuffle(fullTrSetCopy, new Random(seed));
		List<ResourceCandidateInfo> seedList = new ArrayList<ResourceCandidateInfo>(
				2);
		Set<ResourceCandidateInfo> seenSet = new HashSet<ResourceCandidateInfo>();
		for (ResourceCandidateInfo rci : fullTrSetCopy) {
			if (rci.getLabel().equals("good")) {
				seedList.add(rci);
				seenSet.add(rci);
				break;
			}
		}
		for (ResourceCandidateInfo rci : fullTrSetCopy) {
			if (!rci.getLabel().equals("good")) {
				seedList.add(rci);
				seenSet.add(rci);
				break;
			}
		}
		List<ResourceCandidateInfo> poolList = new ArrayList<ResourceCandidateInfo>(
				fullTrSet.size() - 2);
		for (ResourceCandidateInfo rci : fullTrSet) {
			if (!seenSet.contains(rci)) {
				poolList.add(rci);
			}
		}

		return new DatasetWrapper<ResourceCandidateInfo>(seedList, poolList);
	}

	public ResourceCandidateInfo selectNextInstanceFromPool(
			List<ResourceCandidateInfo> poolList, IALPolicy policy)
			throws Exception {
		this.rcc.prepTestingFeatures(poolList);
		List<Prediction> predictions = this.rcc.runClassifier(poolList, 1,
				policy);
		return predictions.get(0).getRci();
	}

	public ClassificationStats classifyTestSet(
			List<ResourceCandidateInfo> testList) throws Exception {
		this.rcc.prepTestingFeatures(testList);
		List<Prediction> predictions = this.rcc.runClassifier(testList, -1,
				null);
		ClassificationStats cs = new ClassificationStats();
		for (Prediction pred : predictions) {
			Assertion.assertNotNull(pred.getRci().getLabel());
			if (pred.score > 0) {
				if (pred.getRci().getLabel().equals("good")) {
					cs.incrCorrectCount();
				} else {
					cs.incrFpCount();
					cs.incrIncorrectCount();
				}
			} else {
				if (pred.getRci().getLabel().equals("good")) {
					cs.incrFnCount();
					cs.incrIncorrectCount();
				}
			}
		}
		System.out.printf("Precision:%.2f Recall:%.2f F1:%.2f%n", cs.getP(),
				cs.getR(), cs.getF1());
		return cs;
	}

	public static class BestPredictionPolicy implements IALPolicy {

		@Override
		public List<Scoreable> selectCandidates(
				List<? extends Scoreable> predictions, int maxCandPoolSize) {
			float minScore = Float.POSITIVE_INFINITY;
			float maxScore = Float.NEGATIVE_INFINITY;
			int maxScoreIdx = -1;
			int i = 0;
			for (Scoreable sc : predictions) {
				float score = sc.getScore();
				if (score > maxScore) {
					maxScore = score;
					maxScoreIdx = i;
				}
				if (score < minScore) {
					minScore = score;
				}
				i++;
			}
			Assertion.assertTrue(maxScoreIdx != -1);
			List<Scoreable> candidates = new ArrayList<Scoreable>(1);
			candidates.add(predictions.get(maxScoreIdx));
			return candidates;
		}
	}

	public static class RCCDataSetMetadata implements
			ILabelable<ResourceCandidateInfo> {
		int goodClassSize = 0;
		int badClassSize = 0;

		public RCCDataSetMetadata(List<ResourceCandidateInfo> dataSet) {
			for (ResourceCandidateInfo rci : dataSet) {
				if (rci.getLabel().equals("good")) {
					goodClassSize++;
				} else {
					badClassSize++;
				}
			}
		}

		@Override
		public int getNumOfClasses() {
			return 2;
		}

		@Override
		public int[] getClassIDs() {
			return new int[] { 0, 1 };
		}

		@Override
		public int getClassID(ResourceCandidateInfo instance) {
			if (instance.getLabel().equals("good")) {
				return 1;
			} else if (instance.getLabel().equals("bad")) {
				return 0;
			}
			return -1;
		}

		@Override
		public int getSizeForClass(int classId) {
			return classId == 1 ? goodClassSize : badClassSize;
		}

		@Override
		public List<ResourceCandidateInfo> getInstances4Class(int classId,
				List<ResourceCandidateInfo> dataSet) {
			List<ResourceCandidateInfo> instances = new ArrayList<ResourceCandidateInfo>(
					getSizeForClass(classId));
			for (ResourceCandidateInfo rci : dataSet) {
				if (classId == 1 && rci.getLabel().equals("good")) {
					instances.add(rci);
				} else if (classId == 0 && !rci.getLabel().equals("good")) {
					instances.add(rci);
				}
			}
			return instances;
		}
	}

	

	public static void handle(long[] seeds, ResourceCandidateClassifier rcc,
			IALPolicy policy, String resultFilePrefix, int posCount,
			int negCount) throws Exception {
		RCCTrainTestUtils util = new RCCTrainTestUtils(rcc);
		List<ResourceCandidateInfo> rciList = util.loadDataSet();
		System.out.println("# of instances:" + rciList.size());
		int i = 1;
		List<ResultSummary> rsList = new ArrayList<ResultSummary>(
				110);

		for (long seed : seeds) {
			DatasetWrapper<ResourceCandidateInfo> dsw = util
					.prepTrainTestDataset(rciList, 0.3, seed);
			String resultFile = "/tmp/" + resultFilePrefix + "_" + i + ".csv";
			List<ClassificationStats> csList = util.testLearning(dsw, seed,
					policy, resultFile);
			if (i == 1) {
				for (ClassificationStats cs : csList) {
					rsList.add(new ResultSummary(cs.getP(), cs.getR(), cs
							.getF1(), posCount, negCount));
				}
			} else {
				for (int j = 0; j < csList.size(); j++) {
					ClassificationStats cs = csList.get(j);
					rsList.get(j).add(cs.getP(), cs.getR(), cs.getF1());
				}
			}
			i++;
		}
		for (ResultSummary rs : rsList) {
			rs.calcAvg();
		}
		Utils.saveAvgResults(rsList, "/tmp/" + resultFilePrefix + "_avg.csv");
	}

	public static void main(String[] args) throws Exception {
		File workDir = new File("/tmp/rcc");
		workDir.mkdir();
		Config config = new Config(workDir.getAbsolutePath());
		ResourceCandidateClassifier rcc = new ResourceCandidateClassifier(
				config);
		RCCTrainTestUtils util = new RCCTrainTestUtils(rcc);

		List<ResourceCandidateInfo> rciList = util.loadDataSet();
		System.out.println("# of instances:" + rciList.size());
		int goodCount = 0;
		int badCount = 0;
		int noLabel = 0;
		for (ResourceCandidateInfo rci : rciList) {
			if (rci.getLabel().equals("good")) {
				goodCount++;
			} else if (rci.getLabel().equals("bad")) {
				badCount++;
			} else {
				noLabel++;
			}
		}
		System.out.println("goodCount:" + goodCount + " badCount:" + badCount
				+ " noLabel:" + noLabel);

		long[] seeds = new long[] { 175440811, 82854371, 147558854, 149760888,
				132377134, 124735586, 168553522, 159116695, 118780595,
				109241064 };
		// long seed = 26287365347363L;

		// handle(seeds, rcc, new ALPolicy(), "active_201403", goodCount, badCount);
		// handle(seeds, rcc, new BestPredictionPolicy(), "best_201403", goodCount, badCount);

		// DatasetWrapper<ResourceCandidateInfo> dsw =
		// util.prepTrainTestDataset(
		// rciList, 0.3, seed);

		// IALPolicy policy = new ALPolicy();
		// String resultFile = "/tmp/active_2014_03_1.csv";
		// policy = new BestPredictionPolicy();
		// resultFile = "/tmp/active_2014_03_best.csv";

		// util.testLearning(dsw, seed, policy, resultFile);

	}
}
