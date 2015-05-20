package org.neuinfo.rdw.classification.text;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author bozyurt
 *
 */
public class ALPolicy implements IALPolicy {

	@Override
	public List<Scoreable> selectCandidates(
			List<? extends Scoreable> predictions, int maxCandPoolSize) {
		List<Scoreable> candidates = new ArrayList<Scoreable>(maxCandPoolSize);
		float minScore = Float.POSITIVE_INFINITY;
		float maxScore = Float.NEGATIVE_INFINITY;
		float absMinScore = Float.POSITIVE_INFINITY;
		int absMinScoreIdx = -1;
		@SuppressWarnings("unused")
		int posCount = 0, negCount = 0;
		int i = 0;
		for (Scoreable sc : predictions) {
			float score = sc.getScore();
			float absScore = Math.abs(score);
			if (score > maxScore) {
				maxScore = score;
			}
			if (score < minScore) {
				minScore = score;
			}
			if (score >= 0) {
				posCount++;
			} else {
				negCount++;
			}
			if (absScore < absMinScore) {
				absMinScore = absScore;
				absMinScoreIdx = i;
			}
			i++;
		}
		int halfSize = maxCandPoolSize / 2;

		if (negCount == 0 || negCount < halfSize) {
			int len = Math.min(predictions.size(), maxCandPoolSize);
			for (i = 0; i < len; i++) {
				candidates.add(predictions.get(i));
			}
		} else {
			double posBound = (maxScore - absMinScore) * 0.05 + absMinScore;
			double negBound = minScore * 0.05;
			int idx = absMinScoreIdx;
			candidates.add(predictions.get(idx));
			idx--;
			int count = 1;
			while (idx >= 0) {
				float score = predictions.get(idx).getScore();
				if (count < halfSize && score >= negBound) {
					candidates.add(predictions.get(idx));
					count++;
				} else {
					break;
				}
				idx--;
			}
			idx = absMinScoreIdx + 1;
			while (idx < predictions.size()) {
				float score = predictions.get(idx).getScore();
				if (count < maxCandPoolSize && score <= posBound) {
					candidates.add(predictions.get(idx));
					count++;
				} else {
					break;
				}
				idx++;
			}
		}

		return candidates;
	}

}
