package org.neuinfo.rdw.classification.text;

import org.neuinfo.rdw.data.model.ResourceCandidateInfo;
/**
 * 
 * @author bozyurt
 *
 */
public class Prediction implements Scoreable, Comparable<Prediction> {
	int instanceId;
	float score;
	ResourceCandidateInfo rci;

	public Prediction(int instanceId, float score) {
		this.instanceId = instanceId;
		this.score = score;
	}

	@Override
	public float getScore() {
		return score;
	}

	@Override
	public int compareTo(Prediction o) {
		return Float.compare(score, o.score);
	}

	public int getInstanceId() {
		return instanceId;
	}

	public ResourceCandidateInfo getRci() {
		return rci;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Prediction [instanceId=");
		builder.append(instanceId);
		builder.append(", score=");
		builder.append(score);
		builder.append(", ");
		if (rci != null) {
			builder.append("rci=");
			builder.append(rci);
		}
		builder.append("]");
		return builder.toString();
	}

}
