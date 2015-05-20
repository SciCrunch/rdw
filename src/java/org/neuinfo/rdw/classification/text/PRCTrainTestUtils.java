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
import org.neuinfo.rdw.data.model.PublisherResourceInfo;

import bnlpkit.nlp.common.CharSetEncoding;
import bnlpkit.nlp.common.classification.ClassificationStats;
import bnlpkit.util.Assertion;
import bnlpkit.util.FileUtils;

public class PRCTrainTestUtils {
	PublisherResourceClassifier prc;

	public PRCTrainTestUtils(PublisherResourceClassifier prc) {
		this.prc = prc;
	}

	public List<PublisherResourceInfo> loadDataSet() throws Exception {
		Connection con = null;
		String jdbcURL = "jdbc:postgresql://localhost:5432/rd_prod";
		Statement st = null;
		List<PublisherResourceInfo> priList = new ArrayList<PublisherResourceInfo>();
		try {
			con = DriverManager.getConnection(jdbcURL, "rd_prod", "");
			st = con.createStatement();
			ResultSet rs = st
					.executeQuery("select a.id, a.label, p.title, p.publication_name, p.description, p.mesh_headings "
							+ "from rd_ps_annot_info a, rd_paper_reference p "
							+ "where a.pr_id = p.id and  a.registry_id = 88");
			while (rs.next()) {
				int id = rs.getInt(1);
				String label = rs.getString(2);
				PublisherResourceInfo pri = new PublisherResourceInfo(id);
				pri.setLabel(label);
				pri.setTitle(rs.getString(3));
				pri.setPublicationName(rs.getString(4));
				pri.setDescription(rs.getString(5));
				pri.setMeshHeadings(rs.getString(6));
				pri.setResourceName("ModelDB");
				priList.add(pri);
			}
			rs.close();
			st.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}
		return priList;
	}

	DatasetWrapper<PublisherResourceInfo> prepTrainTestDataset(
			List<PublisherResourceInfo> priList, double testFrac, long seed) {

		DatasetSplitter<PublisherResourceInfo> splitter = new DatasetSplitter<PublisherResourceInfo>(
				priList, testFrac, seed);

		return splitter.filter(new PRCDataSetMetadata(priList));
	}

	public List<ClassificationStats> testLearning(
			DatasetWrapper<PublisherResourceInfo> dsw, long seed,
			IALPolicy policy, String resultFile) throws Exception {
		List<PublisherResourceInfo> testList = dsw.getTestSet();

		DatasetWrapper<PublisherResourceInfo> trDSW = selectStartingTrainingSet(
				dsw.getTrainSet(), seed);
		List<PublisherResourceInfo> poolList = trDSW.getTestSet();
		Assertion.assertTrue(poolList.size() > 100);
		List<PublisherResourceInfo> seedList = trDSW.getTrainSet();

		this.prc.prepTrainingFeatures(seedList);
		this.prc.train();

		List<ClassificationStats> csList = new ArrayList<ClassificationStats>(
				100);
		for (int i = 0; i < 100; i++) {
			PublisherResourceInfo selPRI = selectNextInstanceFromPool(poolList,
					policy);
			ClassificationStats cs = classifyTestSet(testList);
			csList.add(cs);
			poolList.remove(selPRI);
			seedList.add(selPRI);
			this.prc.prepTrainingFeatures(seedList);
			this.prc.train();
		}
		saveClassificationResult(csList, resultFile);
		System.out.println("saved results to " + resultFile);

		return csList;
	}

	DatasetWrapper<PublisherResourceInfo> selectStartingTrainingSet(
			List<PublisherResourceInfo> fullTrSet, long seed) {
		// randomly select a single positive and a single negative example from
		// the training set
		List<PublisherResourceInfo> fullTrSetCopy = new ArrayList<PublisherResourceInfo>(
				fullTrSet);
		Collections.shuffle(fullTrSetCopy, new Random(seed));
		List<PublisherResourceInfo> seedList = new ArrayList<PublisherResourceInfo>(
				2);
		Set<PublisherResourceInfo> seenSet = new HashSet<PublisherResourceInfo>();
		for (PublisherResourceInfo pri : fullTrSetCopy) {
			if (pri.getLabel().equals("good")) {
				seedList.add(pri);
				seenSet.add(pri);
				break;
			}
		}
		for (PublisherResourceInfo pri : fullTrSetCopy) {
			if (!pri.getLabel().equals("good")) {
				seedList.add(pri);
				seenSet.add(pri);
				break;
			}
		}
		List<PublisherResourceInfo> poolList = new ArrayList<PublisherResourceInfo>(
				fullTrSet.size() - 2);
		for (PublisherResourceInfo pri : fullTrSet) {
			if (!seenSet.contains(pri)) {
				poolList.add(pri);
			}
		}
		return new DatasetWrapper<PublisherResourceInfo>(seedList, poolList);
	}

	public PublisherResourceInfo selectNextInstanceFromPool(
			List<PublisherResourceInfo> poolList, IALPolicy policy)
			throws Exception {
		this.prc.prepTestingFeatures(poolList);
		List<PublisherResourcePrediction> predictions = this.prc.runClassifier(
				poolList, 1, policy);
		return predictions.get(0).getPri();
	}

	public ClassificationStats classifyTestSet(
			List<PublisherResourceInfo> testList) throws Exception {
		this.prc.prepTestingFeatures(testList);
		List<PublisherResourcePrediction> predictions = this.prc.runClassifier(
				testList, -1, null);
		ClassificationStats cs = new ClassificationStats();
		for (PublisherResourcePrediction pred : predictions) {
			Assertion.assertNotNull(pred.getPri().getLabel());
			if (pred.score > 0) {
				if (pred.getPri().getLabel().equals("good")) {
					cs.incrCorrectCount();
				} else {
					cs.incrFpCount();
					cs.incrIncorrectCount();
				}
			} else {
				if (pred.getPri().getLabel().equals("good")) {
					cs.incrFnCount();
					cs.incrIncorrectCount();
				}
			}
		}
		System.out.printf("Precision:%.2f Recall:%.2f F1:%.2f%n", cs.getP(),
				cs.getR(), cs.getF1());
		return cs;
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

	public static class PRCDataSetMetadata implements
			ILabelable<PublisherResourceInfo> {
		int goodClassSize = 0;
		int badClassSize = 0;

		public PRCDataSetMetadata(List<PublisherResourceInfo> dataSet) {
			for (PublisherResourceInfo pri : dataSet) {
				if (pri.getLabel().equals("good")) {
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
		public int getClassID(PublisherResourceInfo instance) {
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
		public List<PublisherResourceInfo> getInstances4Class(int classId,
				List<PublisherResourceInfo> dataSet) {
			List<PublisherResourceInfo> instances = new ArrayList<PublisherResourceInfo>(
					getSizeForClass(classId));
			for (PublisherResourceInfo pri : dataSet) {
				if (classId == 1 && pri.getLabel().equals("good")) {
					instances.add(pri);
				} else if (classId == 0 && !pri.getLabel().endsWith("good")) {
					instances.add(pri);
				}
			}
			return instances;
		}
	}// ;

	public static void handle(long[] seeds, PublisherResourceClassifier prc,
			IALPolicy policy, String resultFilePrefix, int posCount,
			int negCount) throws Exception {
		PRCTrainTestUtils utils = new PRCTrainTestUtils(prc);
		List<PublisherResourceInfo> priList = utils.loadDataSet();
		System.out.println("# of instances:" + priList.size());
		int i = 1;
		List<ResultSummary> rsList = new ArrayList<ResultSummary>(110);
		for (long seed : seeds) {
			DatasetWrapper<PublisherResourceInfo> dsw = utils
					.prepTrainTestDataset(priList, 0.3, seed);
			String resultFile = "/tmp/" + resultFilePrefix + "_" + i + ".csv";
			List<ClassificationStats> csList = utils.testLearning(dsw, seed,
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
		File workDir = new File("/tmp/prc");
		workDir.mkdir();
		PublisherResourceConfig config = new PublisherResourceConfig(
				workDir.getAbsolutePath());
		PublisherResourceClassifier prc = new PublisherResourceClassifier(
				config);

		PRCTrainTestUtils util = new PRCTrainTestUtils(prc);
		List<PublisherResourceInfo> priList = util.loadDataSet();
		System.out.println("# of instances:" + priList.size());
		int goodCount = 0;
		int badCount = 0;
		int noLabel = 0;
		for (PublisherResourceInfo pri : priList) {
			if (pri.getLabel().equals("good")) {
				goodCount++;
			} else if (pri.getLabel().equals("bad")) {
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

		handle(seeds, prc, new ALPolicy(), "prc_active", goodCount, badCount);
	}
}
