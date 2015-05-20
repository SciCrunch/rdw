package org.neuinfo.rdw.classification.text;

import java.io.File;

import bnlpkit.nlp.common.classification.svm.SVMClassifierFactory;
import bnlpkit.nlp.common.services.config.AbstractConfig;

public class NERResourceConfig extends AbstractConfig {
	private static final long serialVersionUID = 1L;
	protected String svmModelDir;
	protected String svmTestFile;
	protected String svmTrainFile;
	protected String classifierType = SVMClassifierFactory.SVMLIGHT;
	protected String featureIndicesFile;
	protected String trainingXmlFile;
	protected String testingXmlFile;

	public final static String DEFAULT_svmModelDir = "svm_ner_resource";
	public final static String DEFAULT_svmTestFile = "svm/ner_resource_classify.dat";
	public final static String DEFAULT_svmTrainFile = "svm/ner_resource_train.dat";
	public final static String DEFAULT_featureIndicesFile = "ner_resource_feature_indices.xml";
	
	public NERResourceConfig(String workDir) throws Exception {
		super(workDir);
		init();
		File f = new File(svmModelDir);
		if (!f.exists()) {
			f.mkdirs();
		}
		f = new File(workDir,"svm");
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	public String getClassifierType() {
		return classifierType;
	}
	
	public String getSvmModelDir() {
		return svmModelDir;
	}

	public void setSvmModelDir(String svmModelDir) {
		this.svmModelDir = svmModelDir;
	}

	public String getSvmTestFile() {
		return svmTestFile;
	}

	public void setSvmTestFile(String svmTestFile) {
		this.svmTestFile = svmTestFile;
	}

	public String getFeatureIndicesFile() {
		return featureIndicesFile;
	}

	public void setFeatureIndicesFile(String featureIndicesFile) {
		this.featureIndicesFile = featureIndicesFile;
	}

	public String getTrainingXmlFile() {
		return trainingXmlFile;
	}

	public void setTrainingXmlFile(String trainingXmlFile) {
		this.trainingXmlFile = trainingXmlFile;
	}

	public String getTestingXmlFile() {
		return testingXmlFile;
	}

	public void setTestingXmlFile(String testingXmlFile) {
		this.testingXmlFile = testingXmlFile;
	}

	public String getSvmTrainFile() {
		return svmTrainFile;
	}

	public void setSvmTrainFile(String svmTrainFile) {
		this.svmTrainFile = svmTrainFile;
	}
}
