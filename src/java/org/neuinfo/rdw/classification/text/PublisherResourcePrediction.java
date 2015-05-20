package org.neuinfo.rdw.classification.text;

import org.neuinfo.rdw.data.model.PublisherResourceInfo;

public class PublisherResourcePrediction implements Scoreable,
		Comparable<PublisherResourcePrediction> {
	int instanceId;
	float score;
	PublisherResourceInfo pri;

	public PublisherResourcePrediction(int instanceId, float score) {
		this.instanceId = instanceId;
		this.score = score;
	}

	@Override
	public int compareTo(PublisherResourcePrediction o) {
		return Float.compare(score, o.score);
	}

	@Override
	public float getScore() {
		return score;
	}

	public PublisherResourceInfo getPri() {
		return pri;
	}

	public void setPri(PublisherResourceInfo pri) {
		this.pri = pri;
	}

	public int getInstanceId() {
		return instanceId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PublisherResourcePrediction [instanceId=");
		builder.append(instanceId);
		builder.append(", score=");
		builder.append(score);
		builder.append(", ");
		if (pri != null) {
			builder.append("pri=");
			builder.append(pri);
		}
		builder.append("]");
		return builder.toString();
	}
	
	

}
