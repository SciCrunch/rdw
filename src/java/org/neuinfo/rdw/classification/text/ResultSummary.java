package org.neuinfo.rdw.classification.text;

public class ResultSummary {
	double P;
	double R;
	double F1;
	int count;
	double accuracy;
	int posCount;
	int negCount;

	public ResultSummary(double p, double r, double f1, int posCount,
			int negCount) {
		P = p;
		R = r;
		F1 = f1;
		count = 1;
		this.posCount = posCount;
		this.negCount = negCount;
		accuracy = calcAccuracy(p, r, posCount, negCount);
	}

	public void add(double p, double r, double f1) {
		P += Double.isNaN(p) ? 0 : p;
		R += Double.isNaN(r) ? 0 : r;
		F1 += Double.isNaN(f1) ? 0 : f1;
		accuracy += calcAccuracy(p, r, posCount, negCount);
		count++;
	}

	public static double calcAccuracy(double p, double r, int posCount,
			int negCount) {
		double accuracy = 0;
		p /= 100.0;
		r /= 100.0;
		double rprime = Double.isNaN(r) ? 0 : (1.0 - r) / r;
		double pprime = Double.isNaN(p) ? 0 : (1.0 - p) / p;
		double TP = posCount / (1.0 + rprime);
		double TN = negCount - pprime / (1.0 + rprime) * posCount;
		accuracy = ((TP + TN) / (posCount + negCount)) * 100;

		return accuracy;
	}

	public void calcAvg() {
		P /= count;
		R /= count;
		F1 /= count;
		accuracy /= count;
	}

}