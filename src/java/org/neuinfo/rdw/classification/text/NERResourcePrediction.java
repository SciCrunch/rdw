package org.neuinfo.rdw.classification.text;

import org.neuinfo.rdw.data.model.NERResourceInfo;

/**
 * 
 * @author bozyurt
 * 
 */
public class NERResourcePrediction implements Scoreable,
		Comparable<NERResourcePrediction> {
	int instanceId;
	float score;
	NERResourceInfo nri;

	public NERResourcePrediction(int instanceId, float score) {
		this.instanceId = instanceId;
		this.score = score;
	}

	public NERResourceInfo getNri() {
		return nri;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public float getScore() {
		return score;
	}

	@Override
	public int compareTo(NERResourcePrediction o) {
		return Float.compare(score, o.score);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NERResourcePrediction [instanceId=");
		builder.append(instanceId);
		builder.append(", score=");
		builder.append(score);
		builder.append(", ");
		if (nri != null) {
			builder.append("nri=");
			builder.append(nri);
		}
		builder.append("]");
		return builder.toString();
	}

}
