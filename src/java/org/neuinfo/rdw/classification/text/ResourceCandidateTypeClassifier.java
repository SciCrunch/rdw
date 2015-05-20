package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuinfo.rdw.data.model.ResourceCandidateInfo;

import bnlpkit.nlp.common.classification.ClassifierParams;
import bnlpkit.nlp.common.classification.IClassifier;
import bnlpkit.nlp.common.classification.ITrainer;
import bnlpkit.nlp.common.classification.feature.ExtFeatureManager;
import bnlpkit.nlp.common.classification.feature.InstanceFeatures;
import bnlpkit.nlp.common.classification.svm.SVMClassifierFactory;
import bnlpkit.util.Assertion;
import bnlpkit.util.LogMultiplexer;

/**
 * predicts a type for the resource candidate
 * 
 * @author bozyurt
 * 
 */
public class ResourceCandidateTypeClassifier {
	private RCTypeConfig config;
	private ClassifierParams params;
	private boolean verbose = true;
	private List<Freq<String>> typeList = new ArrayList<Freq<String>>(10);
	public final static LogMultiplexer _lm = LogMultiplexer
			.getLogger("ResourceCandidateTypeClassifier");

	public ResourceCandidateTypeClassifier(RCTypeConfig config) {
		this.config = config;
		params = new ClassifierParams();
	}

	public void prepTrainingFeatures(List<ResourceCandidateInfo> rciList)
			throws Exception {

		Map<String, Freq<String>> freqTable = new HashMap<String, Freq<String>>();
		for (ResourceCandidateInfo rci : rciList) {
			if (rci.getResourceType() == null) {
				// annotated bad candidates will not have any resource type
				continue;
			}
			Freq<String> freq = freqTable.get(rci.getResourceType());
			if (freq == null) {
				freq = new Freq<String>(rci.getResourceType());
				freqTable.put(rci.getResourceType(), freq);
			}
			freq.incr();
		}
		int threshold = 4;
		for (Freq<String> f : freqTable.values()) {
			if (f.getCount() >= threshold) {
				typeList.add(f);
			}
		}
		Set<ResourceCandidateInfo> annotatedBadCandidatesSet = new HashSet<ResourceCandidateInfo>();
		for (ResourceCandidateInfo rci : rciList) {
			if (rci.getLabel() != null && rci.getLabel().equals("bad")) {
				annotatedBadCandidatesSet.add(rci);
			}
		}

		Assertion.assertTrue(!typeList.isEmpty());
		// OVA classifiers
		boolean first = true;
		for (Freq<String> typeFreq : typeList) {
			ExtFeatureManager fm = new ExtFeatureManager(
					config.getFeatureIndicesFile(), null);
			String resourceType = typeFreq.getValue();
			FeatureGenerator fg = new FeatureGenerator(fm);
			for (ResourceCandidateInfo rci : rciList) {
				if (annotatedBadCandidatesSet.contains(rci)) {
					rci.setLabel("bad");
				} else {
					if (rci.getResourceType().equals(resourceType)) {
						rci.setLabel("good");
					} else {
						rci.setLabel("bad");
					}
				}
			}
			List<InstanceFeatures> ifList = fg.extractFeatures(rciList);

			fm.prepareFeatureTypeMap();
			if (first) {
				fm.saveFeatureIndicesFile();
				first = false;
			}
			fm.saveSVMFile(config.getOVASVMTrainFile(resourceType), ifList);
		}

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
		for (Freq<String> typeFreq : typeList) {
			String resourceType = typeFreq.getValue();
			String trFile = config.getOVASVMTrainFile(resourceType);
			params.putStr("training.file", trFile);
			params.putStr("kernel.type", "linear");
			String mdlFile = config.getOVASVMModelFile(resourceType);
			params.putStr("model.file", mdlFile);

			trainSVM();
		}
	}

	private void trainSVM() throws Exception {
		ITrainer trainer = new SVMClassifierFactory().createTrainer(
				config.getClassifierType(), params);
		trainer.train();
	}

	public List<Prediction> runClassifier(
			List<ResourceCandidateInfo> testRCIList) throws Exception {
		Map<Integer, Prediction> id2PredictionMap = new HashMap<Integer, Prediction>();
		String svmModelDir = config.getSvmModelDir();
		List<Prediction> predList = new ArrayList<Prediction>();
		boolean first = true;
		for (Freq<String> typeFreq : typeList) {
			String resourceType = typeFreq.getValue();
			String svmTestFile = config.getSvmTestFile();
			String mdlFile = config.getOVASVMModelFile(resourceType);
			params.putStr("model.file", mdlFile);
			params.putStr("testing.file", svmTestFile);
			params.putStr("training.file", config.getSvmTrainFile());
			_lm.info("svmModelDir:" + svmModelDir);
			_lm.info("svmTestFile:" + svmTestFile);

			IClassifier classifier = new SVMClassifierFactory()
					.createClassifier(config.getClassifierType(), params);

			_lm.info("running RegistryClassifier " + mdlFile + "\n on "
					+ svmTestFile);
			Map<Integer, Float> id2PredMap = classifier.classify();

			if (first) {
				first = false;
				for (Map.Entry<Integer, Float> entry : id2PredMap.entrySet()) {
					int id = entry.getKey();
					Prediction pred = new Prediction(id, entry.getValue());
					pred.rci = testRCIList.get(id);
					predList.add(pred);
					id2PredictionMap.put(id, pred);
					if (pred.score > 0) {
						pred.rci.setResourceType(resourceType);
					}
				}
			} else {
				for (Map.Entry<Integer, Float> entry : id2PredMap.entrySet()) {
					float score = entry.getValue();
					int id = entry.getKey();
					Prediction pred = id2PredictionMap.get(id);

					if (score > 0 && pred.score < score) {
						pred.score = score;
						pred.rci.setResourceType(resourceType);
					}
				}
			}
		}
		return predList;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public static class Freq<T> {
		T value;
		int count = 0;

		public Freq(T value) {
			this.value = value;
		}

		public void incr() {
			++count;
		}

		public T getValue() {
			return value;
		}

		public int getCount() {
			return count;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Freq other = (Freq) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
}
