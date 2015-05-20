package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuinfo.rdw.data.model.NERResourceInfo;

import bnlpkit.nlp.common.classification.ExtFeatureType;
import bnlpkit.nlp.common.classification.feature.ExtFeatureManager;
import bnlpkit.nlp.common.classification.feature.InstanceFeatures;
import bnlpkit.nlp.tools.sentence.TokenInfo;
import bnlpkit.nlp.util.SimpleSequentialIDGenerator;

/**
 * 
 * @author bozyurt
 * 
 */
public class NERResourceClassifierFeatureGenerator {
	protected ExtFeatureManager featureMan;

	public NERResourceClassifierFeatureGenerator(ExtFeatureManager featureMan) {
		this.featureMan = featureMan;
	}

	public List<InstanceFeatures> extractFeatures(List<NERResourceInfo> nriList)
			throws Exception {
		List<InstanceFeatures> ifList = new ArrayList<InstanceFeatures>(
				nriList.size());
		Set<String> vocabularySet = new HashSet<String>();
		for (NERResourceInfo nri : nriList) {
			List<TokenInfo> tiList = FeatureGenerator
					.toTokens(nri.getContext());
			for (TokenInfo ti : tiList) {
				String tok = ti.getTokValue().toLowerCase().trim();
				vocabularySet.add(tok);
			}
		}
		List<String> vocabularyList = new ArrayList<String>(vocabularySet);
		vocabularySet = null;
		Collections.sort(vocabularyList);

		featureMan.addVocabulary(
				NERClassifierFeatureNames.lemma_bow.toString(), vocabularyList);
		SimpleSequentialIDGenerator idGen = new SimpleSequentialIDGenerator();
		for (NERResourceInfo nri : nriList) {
			int instanceId = idGen.nextID();
			InstanceFeatures instance = new InstanceFeatures(instanceId);

			String entity = nri.getEntity();
			String resourceName = nri.getResourceName();
			featureMan.addFeature(NERClassifierFeatureNames.entity.toString(),
					ExtFeatureType.ENUM, entity, null, instance);

			featureMan.addFeature(
					NERClassifierFeatureNames.resourceName.toString(),
					ExtFeatureType.ENUM, resourceName, null, instance);
			List<TokenInfo> tiList = FeatureGenerator
					.toTokens(nri.getContext());
			Map<String, TokenInfo> tiMap = new HashMap<String, TokenInfo>();
			for (TokenInfo ti : tiList) {
				String tok = ti.getTokValue().toLowerCase().trim();
				tiMap.put(tok, ti);
			}
			for (TokenInfo ti : tiMap.values()) {
				String tok = ti.getTokValue().toLowerCase().trim();
				featureMan.addFeature(
						NERClassifierFeatureNames.lemma_bow.toString(),
						ExtFeatureType.BOW, "1", tok, instance);
			}
			instance.setLabelIdx(0);
			if(nri.getLabel() != null) {
				instance.setLabel(nri.getLabel());
				if (nri.getLabel().equals("good")) {
					instance.setLabelIdx(1);
				} else {
					instance.setLabelIdx(-1);
				}
			}
			ifList.add(instance);
			featureMan.addInstance(instance);
		}
		return ifList;
	}

}
