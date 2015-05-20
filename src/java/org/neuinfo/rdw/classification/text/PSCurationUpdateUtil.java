package org.neuinfo.rdw.classification.text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bnlpkit.util.Assertion;

import commonlib.CSVParser;

public class PSCurationUpdateUtil {

	public Connection getConnection() throws Exception {
		Connection con = null;
		String jdbcURL = "jdbc:postgresql://localhost:5432/rd_prod";
		con = DriverManager.getConnection(jdbcURL, "rd_prod", "");
		return con;
	}

	public void close(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	boolean isURLMention(Connection con, String pmid, long registryId)
			throws SQLException {
		PreparedStatement ps = null;
		ps = con.prepareStatement("select u.id from rd_paper p, rd_urls u where u.doc_id = p.id and u.registry_id = ? and p.pubmed_id = ?");
		ps.setLong(1, registryId);
		ps.setString(2, pmid);

		ResultSet rs = ps.executeQuery();
		boolean ok = false;
		if (rs.next()) {
			ok = true;
		}
		rs.close();
		ps.close();
		return ok;
	}

	boolean isAlreadyInPSAnnotInfo(Connection con, String pmid, long registryId)
			throws SQLException {
		PreparedStatement ps = null;
		ps = con.prepareStatement("select a.id from rd_ps_annot_info a, rd_paper_reference p "
				+ "where a.pr_id = p.id and a.registry_id = ? and p.pubmed_id = ?");
		ps.setLong(1, registryId);
		ps.setString(2, pmid);

		ResultSet rs = ps.executeQuery();
		boolean ok = false;
		if (rs.next()) {
			ok = true;
		}
		rs.close();
		ps.close();
		return ok;
	}

	Long getPaperRefId(Connection con, String pmid, long registryId)
			throws SQLException {
		PreparedStatement ps = null;
		ps = con.prepareStatement("select p.id from rd_paper_reference p where p.registry_id = ? and p.pubmed_id = ?");
		ps.setLong(1, registryId);
		ps.setString(2, pmid);

		ResultSet rs = ps.executeQuery();
		Long prId = null;
		if (rs.next()) {
			prId = rs.getLong(1);
		}
		return prId;
	}

	void insertPSAnnotInfo(Connection con, long prId, long registryId,
			String label) throws SQLException {
		PreparedStatement ps = null;
		ps = con.prepareStatement("insert into rd_ps_annot_info (label,mod_time,modified_by, "
				+ "op_type, pr_id, registry_id) values(?,now(),'admin','ps_filter',?,?)");
		ps.setString(1, label);
		ps.setLong(2, prId);
		ps.setLong(3, registryId);
		int count = ps.executeUpdate();
		System.out.println("inserted:" + count);
	}

	void updatePSAnnotInfo(Connection con, long prId, long registryId,
			String label) throws SQLException {
		PreparedStatement ps = null;
		ps = con.prepareStatement("update rd_ps_annot_info set mod_time= now(), label = ? where registry_id = ? and pr_id = ?");
		ps.setString(1, label);
		ps.setLong(2, registryId);
		ps.setLong(3, prId);

		int count = ps.executeUpdate();
		System.out.println("updated:" + count);
	}

	public List<PaperMention> loadFromCSV(String csvFile) throws Exception {
		List<PaperMention> pmList = new ArrayList<PaperMention>();
		CSVParser parser = new CSVParser();
		parser.extractData(csvFile);
		List<List<String>> rows = parser.getRows();
		for (List<String> row : rows) {
			String label = (row.size() > 4) ? row.get(3) : "true";
			String pmid = row.get(2);
			System.out.println(pmid);
			pmList.add(new PaperMention(pmid, label));
		}

		return pmList;
	}

	public void handle() throws Exception {
		List<PaperMention> commonList = loadFromCSV("/home/bozyurt/bin/modeldb/common_to_all.csv");
		List<PaperMention> onlyInRDList = loadFromCSV("/home/bozyurt/bin/modeldb/only_in_rd_curated.csv");
		int urlMentionCount = 0;
		int insertCount = 0;
		int updateCount = 0;

		Connection con = null;
		try {
			con = getConnection();

			System.out.printf("commonList:%d  onlyInRDList:%d%n",
					commonList.size(), onlyInRDList.size());
			Set<Long> prIdSet = new HashSet<Long>();
			for (PaperMention pm : commonList) {
				if (isURLMention(con, pm.pmid, 88)) {
					System.out.println(pm);
					urlMentionCount++;
				} else if (!isAlreadyInPSAnnotInfo(con, pm.pmid, 88)) {
					Long prId = getPaperRefId(con, pm.pmid, 88);
					Assertion.assertNotNull(prId);
					insertPSAnnotInfo(con, prId, 88, "good");
					insertCount++;

				} else {
					Long prId = getPaperRefId(con, pm.pmid, 88);
					Assertion.assertNotNull(prId);
					prIdSet.add(prId);
					updatePSAnnotInfo(con, prId, 88, "good");
					updateCount++;
				}
			}
			System.out.println("Handling onlyInRDList");
			System.out.println("=====================");
			for(PaperMention pm : onlyInRDList) {
				String label = pm.label.equals("true") ? "good" : "bad";
				if (isURLMention(con, pm.pmid, 88)) {
					System.out.println(pm);
					urlMentionCount++;
				} else if (!isAlreadyInPSAnnotInfo(con, pm.pmid, 88)) {
					Long prId = getPaperRefId(con, pm.pmid, 88);
					Assertion.assertNotNull(prId);
					insertPSAnnotInfo(con, prId, 88, label);
					insertCount++;

				} else {
					Long prId = getPaperRefId(con, pm.pmid, 88);
					Assertion.assertNotNull(prId);
					prIdSet.add(prId);
					updatePSAnnotInfo(con, prId, 88, label);
					updateCount++;
				}
			}
			System.out.printf(
					"urlMentionCount:%d insertCount:%d updateCount:%d%n",
					urlMentionCount, insertCount, updateCount);
			System.out.println("prIdSet:" + prIdSet.size());
		} finally {
			close(con);
		}

	}

	public static class PaperMention {
		final String pmid;
		final String label;

		public PaperMention(String pmid, String label) {
			this.pmid = pmid;
			this.label = label;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PaperMention [pmid=");
			builder.append(pmid);
			builder.append(", label=");
			builder.append(label);
			builder.append("]");
			return builder.toString();
		}

	}// ;

	public static void main(String[] args) throws Exception {

		PSCurationUpdateUtil util = new PSCurationUpdateUtil();
		// util.loadFromCSV("/home/bozyurt/bin/modeldb/common_to_all.csv");
		// util.loadFromCSV("/home/bozyurt/bin/modeldb/only_in_rd_curated.csv");

		util.handle();
	}
}
