package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neuinfo.rdw.data.model.PublisherResourceInfo;

import bnlpkit.nlp.common.classification.ClassifierParams;
import bnlpkit.nlp.common.classification.IClassifier;
import bnlpkit.nlp.common.classification.ITrainer;
import bnlpkit.nlp.common.classification.feature.ExtFeatureManager;
import bnlpkit.nlp.common.classification.feature.InstanceFeatures;
import bnlpkit.nlp.common.classification.svm.SVMClassifierFactory;
import bnlpkit.util.LogMultiplexer;

public class PublisherResourceClassifier {
	PublisherResourceConfig config;
	private ClassifierParams params;
	private boolean verbose = true;
	public final static LogMultiplexer _lm = LogMultiplexer
			.getLogger("PublisherResourceClassifier");

	public PublisherResourceClassifier(PublisherResourceConfig config) {
		this.config = config;
		params = new ClassifierParams();
	}

	public void prepTrainingFeatures(List<PublisherResourceInfo> priList)
			throws Exception {
		ExtFeatureManager fm = new ExtFeatureManager(
				config.getFeatureIndicesFile(), null);
		PublisherResourceClassifierFeatureGenerator fg = new PublisherResourceClassifierFeatureGenerator(
				fm);
		List<InstanceFeatures> ifList = fg.extractFeatures(priList);

		fm.prepareFeatureTypeMap();
		fm.saveFeatureIndicesFile();
		System.out.println("svmTrainFile:" + config.getSvmTrainFile());
		fm.saveSVMFile(config.getSvmTrainFile(), ifList);
	}

	public void prepTestingFeatures(List<PublisherResourceInfo> testPriList)
			throws Exception {
		ExtFeatureManager fm = new ExtFeatureManager(
				config.getFeatureIndicesFile(), null);
		fm.loadFeatureTypeMap();
		PublisherResourceClassifierFeatureGenerator fg = new PublisherResourceClassifierFeatureGenerator(
				fm);
		List<InstanceFeatures> ifList = fg.extractFeatures(testPriList);
		fm.prepareFeatureTypeMap();
		fm.saveSVMFile(config.getSvmTestFile(), ifList);
	}

	public void train() throws Exception {
		String outDir = config.getSvmModelDir();
		_lm.info("outDir:" + outDir);
		String trFile = config.getSvmTrainFile();
		params.putStr("training.file", trFile);
		params.putStr("kernel.type", "linear");
		String svmModelDir = config.getSvmModelDir();
		String mdlFile = svmModelDir + "/pr_filter.mdl";
		params.putStr("model.file", mdlFile);
		trainSVM();
	}

	private void trainSVM() throws Exception {
		ITrainer trainer = new SVMClassifierFactory().createTrainer(
				config.getClassifierType(), params);
		trainer.train();
	}


	public List<PublisherResourcePrediction> runClassifier(
			List<PublisherResourceInfo> testPriList, int returnSize,
			IALPolicy policy) throws Exception {
		String svmModelDir = config.getSvmModelDir();
		String mdlFile = svmModelDir + "/pr_filter.mdl";
		String svmTestFile = config.getSvmTestFile();
		params.putStr("model.file", mdlFile);
		params.putStr("testing.file", svmTestFile);
		params.putStr("training.file", config.getSvmTrainFile());
		_lm.info("svmModelDir:" + svmModelDir);
		_lm.info("svmTestFile:" + svmTestFile);

		IClassifier classifier = new SVMClassifierFactory().createClassifier(
				config.getClassifierType(), params);

		_lm.info("running Publisher Resource Filter Classifier on " + mdlFile
				+ "\n on " + svmTestFile);
		Map<Integer, Float> id2PredMap = classifier.classify();
		List<PublisherResourcePrediction> predList = new ArrayList<PublisherResourcePrediction>(
				id2PredMap.size());
		for (Map.Entry<Integer, Float> entry : id2PredMap.entrySet()) {
			int id = entry.getKey();
			PublisherResourcePrediction pred = new PublisherResourcePrediction(
					id, entry.getValue());
			pred.pri = testPriList.get(id);
			predList.add(pred);
		}
		if (policy != null) {
			Collections.sort(predList);
			List<Scoreable> selectedCandidates = policy.selectCandidates(
					predList, returnSize);
			List<PublisherResourcePrediction> returnList = new ArrayList<PublisherResourcePrediction>(
					selectedCandidates.size());
			for (Scoreable s : selectedCandidates) {
				PublisherResourcePrediction p = (PublisherResourcePrediction) s;
				returnList.add(p);
				if (verbose) {
					System.out.println(p.getPri().getTitle() + " score:"
							+ p.getScore());
				}
			}
			return returnList;
		}
		return predList;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
