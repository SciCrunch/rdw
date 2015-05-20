package org.neuinfo.rdw.classification.text;

import java.util.List;

/**
 * 
 * @author bozyurt
 * 
 */
public interface IALPolicy {
	/**
	 * Assumption: predictions are sorted
	 * 
	 * @param predictions
	 * @param maxCandPoolSize
	 * @return
	 */
	public abstract List<Scoreable> selectCandidates(
			List<? extends Scoreable> predictions, int maxCandPoolSize);

}
