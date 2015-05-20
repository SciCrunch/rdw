package org.neuinfo.rdw.classification.text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuinfo.rdw.data.model.ResourceCandidateInfo;

import bnlpkit.nlp.common.classification.ExtFeatureType;
import bnlpkit.nlp.common.classification.feature.ExtFeatureManager;
import bnlpkit.nlp.common.classification.feature.InstanceFeatures;
import bnlpkit.nlp.tools.sentence.SentenceLexer2;
import bnlpkit.nlp.tools.sentence.TokenInfo;
import bnlpkit.nlp.util.SimpleSequentialIDGenerator;

public class FeatureGenerator {
	protected ExtFeatureManager featureMan;

	public FeatureGenerator(ExtFeatureManager featureMan) {
		super();
		this.featureMan = featureMan;
	}

	public List<InstanceFeatures> extractFeatures(
			List<ResourceCandidateInfo> rciList) throws Exception {
		List<InstanceFeatures> ifList = new ArrayList<InstanceFeatures>(
				rciList.size());
		Set<String> vocabularySet = new HashSet<String>();
		for (ResourceCandidateInfo rci : rciList) {
			List<TokenInfo> tiList = FeatureGenerator.toTokens(rci
					.getDescription());
			for (TokenInfo ti : tiList) {
				String tok = ti.getTokValue().toLowerCase().trim();
				vocabularySet.add(tok);
			}
		}
		List<String> vocabularyList = new ArrayList<String>(vocabularySet);
		vocabularySet = null;
		Collections.sort(vocabularyList);

		featureMan.addVocabulary(FeatureNames.lemma_bow.toString(),
				vocabularyList);
		SimpleSequentialIDGenerator idGen = new SimpleSequentialIDGenerator();
		for (ResourceCandidateInfo rci : rciList) {
			int instanceId = idGen.nextID();
			InstanceFeatures instance = new InstanceFeatures(instanceId);

		//	featureMan.addFeature(FeatureNames.score.toString(),
		//			ExtFeatureType.NUMERIC, String.valueOf(rci.getScore()),
		//			null, instance);
			String topLevelDomain = getTopLevelDomain(rci.getUrl());
			featureMan.addFeature(FeatureNames.hostSuffix.toString(),
					ExtFeatureType.ENUM, topLevelDomain, null, instance);
			
			List<TokenInfo> tiList = FeatureGenerator.toTokens(rci
					.getDescription());
			Map<String, TokenInfo> tiMap = new HashMap<String, TokenInfo>();
			for (TokenInfo ti : tiList) {
				String tok = ti.getTokValue().toLowerCase().trim();
				tiMap.put(tok, ti);
			}
			for (TokenInfo ti : tiMap.values()) {
				String tok = ti.getTokValue().toLowerCase().trim();
				featureMan.addFeature(FeatureNames.lemma_bow.toString(),
						ExtFeatureType.BOW, "1", tok, instance);
			}

			instance.setLabelIdx(0);
			if (rci.getLabel() != null) {
				instance.setLabel(rci.getLabel());
				if (rci.getLabel().equals("good")) {
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

	public static List<TokenInfo> toTokens(String text) throws IOException {
		List<TokenInfo> tiList = new ArrayList<TokenInfo>();
		SentenceLexer2 sl = new SentenceLexer2(text);
		TokenInfo ti = null;
		while ((ti = sl.getNextTI()) != null) {
			String tok = ti.getTokValue().toLowerCase();
			if (tok.length() > 1 && !stopWordSet.contains(tok)) {
				if (tok.endsWith(".")) {
					if (countChar(tok, '.') == 1) {
						int len = ti.getTokValue().length();
						TokenInfo ti1 = new TokenInfo(ti.getTokValue()
								.substring(0, len - 1), ti.getStart(),
								ti.getEnd() - 1);
						tiList.add(ti1);
						TokenInfo ti2 = new TokenInfo(".", ti.getEnd() - 1,
								ti.getEnd());
						tiList.add(ti2);
					}
				} else {
					tiList.add(ti);
				}
			}
		}
		return tiList;
	}

	public static String getTopLevelDomain(String urlStr) {
		try {
			URL url = new URL(urlStr);
			String host = url.getHost();
			int idx = host.lastIndexOf('.');
			if (idx != -1) {
				return host.substring(idx + 1);
			}
			return "";

		} catch (MalformedURLException e) {
			return "";
		}
	}

	static int countChar(String s, char c) {
		int len = s.length(), count = 0;
		for (int i = 0; i < len; i++) {
			if (s.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}

	public static final Set<String> stopWordSet = new HashSet<String>();

	public static final String[] stopWords = new String[] { "a", "able",
			"about", "across", "after", "all", "almost", "also", "am", "among",
			"an", "and", "any", "are", "as", "at", "be", "because", "been",
			"but", "by", "can", "cannot", "could", "dear", "did", "do", "does",
			"either", "else", "ever", "every", "for", "from", "get", "got",
			"had", "has", "have", "he", "her", "hers", "him", "his", "how",
			"however", "i", "if", "in", "into", "is", "it", "its", "just",
			"least", "let", "like", "likely", "may", "me", "might", "most",
			"must", "my", "neither", "no", "nor", "not", "of", "off", "often",
			"on", "only", "or", "other", "our", "own", "rather", "said", "say",
			"says", "she", "should", "since", "so", "some", "than", "that",
			"the", "their", "them", "then", "there", "these", "they", "this",
			"tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
			"what", "when", "where", "which", "while", "who", "whom", "why",
			"will", "with", "would", "yet", "you", "your" };

	static {
		for (String sw : stopWords) {
			stopWordSet.add(sw);
		}
	}
}
