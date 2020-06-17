package it.polito.tdp.seriea.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.model.Season;
import it.polito.tdp.seriea.model.Team;

public class SerieADAO {

	public List<Season> listAllSeasons() {
		String sql = "SELECT season, description FROM seasons";
		List<Season> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Season(res.getInt("season"), res.getString("description")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	//avrei anche potuto usare una mappa, ma non c'era nessun identificativo per i Team, ma solo il nome e quindi era inutile
	public List<Team> listTeams() {
		String sql = "select team " + 
				"from teams ";
		List<Team> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Team(res.getString("team")));
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//ottengo i gruppi stagione, risultato (vittoria(trasferta o casa)/pareggio) per la squadra selezionata
	/*
	 	select m.season,m.ftr, count(*) as numero
		from matches as m, seasons as s
		where m.season=s.season and ((m.homeTeam='Milan' and (m.FTR='H' || m.FTR='D')) || (m.awayTeam='Milan' and (m.FTR='A' || m.FTR='D'))) 
		group by m.season, m.ftr
	 */
	public Map<Integer,Season> listSeasonTeam(Team t) {
		String sql = "select m.season,s.description,m.ftr, count(*) as numero " + 
				"		from matches as m, seasons as s\n" + 
				"		where m.season=s.season and ((m.homeTeam=? and (m.FTR='H' || m.FTR='D')) || (m.awayTeam=? and (m.FTR='A' || m.FTR='D'))) " + 
				"		group by m.season, m.ftr ";
		
		Map<Integer,Season> result = new HashMap<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, t.getTeam());
			st.setString(2, t.getTeam());
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Season s;
				if(!result.containsKey(res.getInt("season"))) {
					s=new Season(res.getInt("season"),res.getString("description"));
					if(res.getString("ftr").equals("D")) {
						//i pareggi
						s.setPuntiSquadra(res.getInt("numero"));
					}
					else {
						//vittoria
						s.setPuntiSquadra(res.getInt("numero")*3);
					}
				}
				else {
					s= result.get(res.getInt("season"));
					if(res.getString("ftr").equals("D")) {
						//i pareggi
						s.setPuntiSquadra(s.getPuntiSquadra()+res.getInt("numero"));
					}
					else {
						//vittoria
						s.setPuntiSquadra(s.getPuntiSquadra()+res.getInt("numero")*3);
					}
					
				}
				result.put(s.getSeason(), s);
				
				
				
				//result.add(new Team(res.getString("team")));
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

}

