package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neuinfo.rdw.data.model.ResourceCandidateInfo;

import bnlpkit.nlp.common.classification.ClassifierParams;
import bnlpkit.nlp.common.classification.IClassifier;
import bnlpkit.nlp.common.classification.ITrainer;
import bnlpkit.nlp.common.classification.feature.ExtFeatureManager;
import bnlpkit.nlp.common.classification.feature.InstanceFeatures;
import bnlpkit.nlp.common.classification.svm.SVMClassifierFactory;
import bnlpkit.util.LogMultiplexer;

public class ResourceCandidateClassifier {
	private Config config;
	private ClassifierParams params;
	private boolean verbose = true;
	public final static LogMultiplexer _lm = LogMultiplexer
			.getLogger("ResourceCandidateClassifier");

	public ResourceCandidateClassifier(Config config) {
		this.config = config;
		params = new ClassifierParams();
	}

	public void prepTrainingFeatures(List<ResourceCandidateInfo> rciList)
			throws Exception {
		ExtFeatureManager fm = new ExtFeatureManager(
				config.getFeatureIndicesFile(), null);
		FeatureGenerator fg = new FeatureGenerator(fm);

		List<InstanceFeatures> ifList = fg.extractFeatures(rciList);

		fm.prepareFeatureTypeMap();
		fm.saveFeatureIndicesFile();

		fm.saveSVMFile(config.getSvmTrainFile(), ifList);
	}

	public void prepTestingFeatures(List<ResourceCandidateInfo> testRciList)
			throws Exception {
		ExtFeatureManager fm = new ExtFeatureManager(
				config.getFeatureIndicesFile(), null);
		fm.loadFeatureTypeMap();
		FeatureGenerator fg = new FeatureGenerator(fm);

		List<InstanceFeatures> ifList = fg.extractFeatures(testRciList);
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
		String mdlFile = svmModelDir + "/rc_filter.mdl";
		params.putStr("model.file", mdlFile);

		trainSVM();
	}

	private void trainSVM() throws Exception {
		ITrainer trainer = new SVMClassifierFactory().createTrainer(
				config.getClassifierType(), params);

		trainer.train();
	}

	public List<Prediction> runClassifier(
			List<ResourceCandidateInfo> testRCIList, int returnSize,
			IALPolicy policy) throws Exception {
		String svmModelDir = config.getSvmModelDir();
		String mdlFile = svmModelDir + "/rc_filter.mdl";
		String svmTestFile = config.getSvmTestFile();
		params.putStr("model.file", mdlFile);
		params.putStr("testing.file", svmTestFile);
		params.putStr("training.file", config.getSvmTrainFile());
		_lm.info("svmModelDir:" + svmModelDir);
		_lm.info("svmTestFile:" + svmTestFile);

		IClassifier classifier = new SVMClassifierFactory().createClassifier(
				config.getClassifierType(), params);

		_lm.info("running RegistryClassifier " + mdlFile + "\n on "
				+ svmTestFile);
		Map<Integer, Float> id2PredMap = classifier.classify();
		List<Prediction> predList = new ArrayList<Prediction>();
		for (Map.Entry<Integer, Float> entry : id2PredMap.entrySet()) {
			int id = entry.getKey();
			Prediction pred = new Prediction(id, entry.getValue());
			pred.rci = testRCIList.get(id);
			predList.add(pred);
		}
		if (policy != null) {
			Collections.sort(predList);
			List<Scoreable> selectedCandidates = policy.selectCandidates(
					predList, returnSize);
			List<Prediction> returnList = new ArrayList<Prediction>(
					selectedCandidates.size());

			for (Scoreable s : selectedCandidates) {
				returnList.add((Prediction) s);
				Prediction p = (Prediction) s;
				if (verbose) {
					System.out.println(p.getRci().getUrl() + " score:"
							+ p.getScore());
				}
			}
			return returnList;
		} else {
			return predList;
		}
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
