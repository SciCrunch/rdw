package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neuinfo.rdw.data.model.NERResourceInfo;

import bnlpkit.nlp.common.classification.ClassifierParams;
import bnlpkit.nlp.common.classification.IClassifier;
import bnlpkit.nlp.common.classification.ITrainer;
import bnlpkit.nlp.common.classification.feature.ExtFeatureManager;
import bnlpkit.nlp.common.classification.feature.InstanceFeatures;
import bnlpkit.nlp.common.classification.svm.SVMClassifierFactory;
import bnlpkit.util.LogMultiplexer;

/**
 * 
 * @author bozyurt
 *
 */
public class NERResourceClassifier {
	private NERResourceConfig config;
	private ClassifierParams params;
	private boolean verbose = true;
	public final static LogMultiplexer _lm = LogMultiplexer
			.getLogger("NERResourceClassifier");

	public NERResourceClassifier(NERResourceConfig config) {
		this.config = config;
		params = new ClassifierParams();
	}

	public void prepTrainingFeatures(List<NERResourceInfo> nriList)
			throws Exception {
		ExtFeatureManager fm = new ExtFeatureManager(
				config.getFeatureIndicesFile(), null);
		NERResourceClassifierFeatureGenerator fg = new NERResourceClassifierFeatureGenerator(
				fm);
		List<InstanceFeatures> ifList = fg.extractFeatures(nriList);

		fm.prepareFeatureTypeMap();
		fm.saveFeatureIndicesFile();
		fm.saveSVMFile(config.getSvmTrainFile(), ifList);
	}

	public void prepTestingFeatures(List<NERResourceInfo> testNriList)
			throws Exception {
		ExtFeatureManager fm = new ExtFeatureManager(
				config.getFeatureIndicesFile(), null);
		fm.loadFeatureTypeMap();
		NERResourceClassifierFeatureGenerator fg = new NERResourceClassifierFeatureGenerator(
				fm);

		List<InstanceFeatures> ifList = fg.extractFeatures(testNriList);
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
		String mdlFile = svmModelDir + "/nr_filter.mdl";
		params.putStr("model.file", mdlFile);
		trainSVM();
	}

	private void trainSVM() throws Exception {
		ITrainer trainer = new SVMClassifierFactory().createTrainer(
				config.getClassifierType(), params);
		trainer.train();
	}

	public List<NERResourcePrediction> runClassifier(List<NERResourceInfo> testNriList,
			int returnSize, IALPolicy policy) throws Exception {
		String svmModelDir = config.getSvmModelDir();
		String mdlFile = svmModelDir + "/nr_filter.mdl";
		String svmTestFile = config.getSvmTestFile();
		params.putStr("model.file", mdlFile);
		params.putStr("testing.file", svmTestFile);
		params.putStr("training.file", config.getSvmTrainFile());
		_lm.info("svmModelDir:" + svmModelDir);
		_lm.info("svmTestFile:" + svmTestFile);

		IClassifier classifier = new SVMClassifierFactory().createClassifier(
				config.getClassifierType(), params);

		_lm.info("running NER Resource Filter Classifier " + mdlFile + "\n on "
				+ svmTestFile);
		Map<Integer, Float> id2PredMap = classifier.classify();
		List<NERResourcePrediction> predList = new ArrayList<NERResourcePrediction>();
		for (Map.Entry<Integer, Float> entry : id2PredMap.entrySet()) {
			int id = entry.getKey();
			NERResourcePrediction pred = new NERResourcePrediction(id,
					entry.getValue());
			pred.nri = testNriList.get(id);
			predList.add(pred);
		}
		if (policy != null) {
			Collections.sort(predList);
			List<Scoreable> selectedCandidates = policy.selectCandidates(
					predList, returnSize);
			List<NERResourcePrediction> returnList = new ArrayList<NERResourcePrediction>(
					selectedCandidates.size());
			for(Scoreable s : selectedCandidates) {
				NERResourcePrediction p = (NERResourcePrediction) s;
				returnList.add(p);
				if (verbose) {
					System.out.println(p.getNri().getContext() + " score:" + p.getScore());
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
