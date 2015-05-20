package org.neuinfo.rdw.classification.text;

import java.io.BufferedWriter;
import java.util.List;

import bnlpkit.nlp.common.CharSetEncoding;
import bnlpkit.util.FileUtils;

public class Utils {

	public static double mean(List<Double> data) {
		double sum = 0;
		for (Double num : data) {
			sum += num.doubleValue();
		}
		return (!data.isEmpty()) ? sum / data.size() : 0;
	}

	public static double sd(List<Double> data) {
		int n = data.size();
		double avg = mean(data), sum = 0;
		double var = 0;
		for (double num : data) {
			sum += (num - avg) * (num - avg);
		}
		var = sum / (n - 1);
		return Math.sqrt(var);
	}

	public static void saveAvgResults(List<ResultSummary> rsList,
			String outTextFile) throws Exception {
		BufferedWriter out = null;
		try {
			out = FileUtils
					.getBufferedWriter(outTextFile, CharSetEncoding.UTF8);
			out.write("X,P,R,F1,accuracy");
			out.newLine();
			int i = 2;
			for (ResultSummary rs : rsList) {
				out.write(String.format("%d,%.2f,%.2f,%.2f,%.2f", i, rs.P,
						rs.R, rs.F1, rs.accuracy));
				out.newLine();
				i++;
			}
		} finally {
			FileUtils.close(out);
		}
	}

}
