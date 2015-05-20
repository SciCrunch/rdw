package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuinfo.rdw.data.model.PublisherResourceInfo;

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
public class PublisherResourceClassifierFeatureGenerator {
	protected ExtFeatureManager featureMan;

	public PublisherResourceClassifierFeatureGenerator(
			ExtFeatureManager featureMan) {
		this.featureMan = featureMan;
	}

	public List<InstanceFeatures> extractFeatures(
			List<PublisherResourceInfo> priList) throws Exception {
		List<InstanceFeatures> ifList = new ArrayList<InstanceFeatures>(
				priList.size());
		Set<String> vocabularySet = new HashSet<String>();
		for (PublisherResourceInfo pri : priList) {
			String context = prepContext(pri);

			List<TokenInfo> tiList = FeatureGenerator.toTokens(context);
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

		Map<String, TokenInfo> tiMap = new HashMap<String, TokenInfo>();
		for (PublisherResourceInfo pri : priList) {
			int instanceId = idGen.nextID();
			InstanceFeatures instance = new InstanceFeatures(instanceId);
			String genre = pri.getGenre();
			String resourceName = pri.getResourceName();
			if (genre == null) {
				genre = "unknown";
			}

			featureMan.addFeature(
					PublisherResClassifierFeatureNames.genre.toString(),
					ExtFeatureType.ENUM, genre, null, instance);

			featureMan.addFeature(
					PublisherResClassifierFeatureNames.resourceName.toString(),
					ExtFeatureType.ENUM, resourceName, null, instance);

			String context = prepContext(pri);
			List<TokenInfo> tiList = FeatureGenerator.toTokens(context);
			tiMap.clear();
			for (TokenInfo ti : tiList) {
				String tok = ti.getTokValue().toLowerCase().trim();
				tiMap.put(tok, ti);
			}
			for (TokenInfo ti : tiMap.values()) {
				String tok = ti.getTokValue().toLowerCase().trim();
				featureMan
						.addFeature(
								PublisherResClassifierFeatureNames.lemma_bow
										.toString(), ExtFeatureType.BOW, "1",
								tok, instance);
			}
			instance.setLabelIdx(0);
			if (pri.getLabel() != null) {
				instance.setLabel(pri.getLabel());
				if (pri.getLabel().equals("good")) {
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

	String prepContext(PublisherResourceInfo pri) {
		StringBuilder sb = new StringBuilder(1024);
		if (pri.getTitle() != null) {
			sb.append(pri.getTitle()).append(' ');
		}

		if (pri.getPublicationName() != null) {
			sb.append(pri.getPublicationName());
		}
		if (pri.getDescription() != null) {
			sb.append(' ').append(pri.getDescription());
		}
		if (pri.getMeshHeadings() != null) {
			sb.append(' ').append(pri.getMeshHeadings());
		}
		String context = sb.toString().trim();
		return context;
	}
}
