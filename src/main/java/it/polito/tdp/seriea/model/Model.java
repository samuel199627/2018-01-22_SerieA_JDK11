package it.polito.tdp.seriea.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	List<Team> squadre;
	Map<Integer, Season> mappaStagioni;
	List<Season> stagioniOrdinate=null;
	
	SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge> grafo=null;
	
	SerieADAO dao;
	
	public Model() {
		dao= new SerieADAO();
		
	}
	
	public List<Team> getSquadre(){
		squadre=new ArrayList<>();
		squadre = dao.listTeams();
		return squadre;
		
		
	}
	
	public String ritornaStagioniSquadra(Team t) {
		String ritornare="ECCO L'ANDAMENTO NELLE STAGIONI DELLA SQUADRA SELEZIONATA: \n\n";
		mappaStagioni=new HashMap<>();
		mappaStagioni=dao.listSeasonTeam(t);
		stagioniOrdinate=new ArrayList<>();
		
		for(Season s: mappaStagioni.values()) {
			stagioniOrdinate.add(s);
		}
		
		stagioniOrdinate.sort(null);
		
		
		for(Season s: stagioniOrdinate) {
			ritornare=ritornare+"stagione: "+s.getDescription()+"-> Punti totalizzati: "+s.getPuntiSquadra()+"\n";
		}
		
		return ritornare;
	}
	
	public void creaGrafo() {
		//i vertici sono le stagioni della squadra
		if(stagioniOrdinate==null) {
			System.out.println("DEVI PRIMA FARE IL PUNTO PRECEDETE");
			return;
		}
				
		//si fa riferimento alla squadra e alle stagioni del punto precedente
		grafo= new SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		
		Graphs.addAllVertices(grafo, stagioniOrdinate);
		
		//analizzo ogni coppia di stagioni per valutare l'arco da mettere
		for(Season s1: stagioniOrdinate) {
			for(Season s2: stagioniOrdinate) {
				//evito di analizzare due volte le stesse coppie
				if(s2.getSeason()>s1.getSeason()) {
					int peso=0;
					if(s1.getPuntiSquadra()>s2.getPuntiSquadra()) {
						peso= s1.getPuntiSquadra()-s2.getPuntiSquadra();
						//da quella meno devo andare a quella piu' secondo le specifiche
						if(s1.getSeason()==2015) {
							System.out.println("TARGET");
						}
						if(s2.getSeason()==2015) {
							System.out.println("SORGENTE");
						}
						Graphs.addEdge(grafo, s2, s1, peso);
					}
					else if(s1.getPuntiSquadra()<s2.getPuntiSquadra()) {
						peso= s2.getPuntiSquadra()-s1.getPuntiSquadra();
						if(s1.getSeason()==2015) {
							System.out.println("SORGENTE");
						}
						if(s2.getSeason()==2015) {
							System.out.println("TARGET");
						}
						Graphs.addEdge(grafo, s1, s2, peso);
					}
				}
			}
		}
		
		System.out.println("GRAFO CREATO CON: \n\n"+grafo.vertexSet().size()+" vertici \n"+grafo.edgeSet().size()+" lati.");
		
		System.out.println("\n\n ARCHI: \n\n");
		for(DefaultWeightedEdge e: grafo.edgeSet()){
			//analizzo solo la stagione che mi viene fuori quella massima che e' la 2014/2015
			if(grafo.getEdgeSource(e).getSeason()==2015||grafo.getEdgeTarget(e).getSeason()==2015) {
				System.out.println("sorgente "+grafo.getEdgeSource(e).getDescription()+" destinazione "+grafo.getEdgeTarget(e).getDescription()+" peso "+grafo.getEdgeWeight(e));
			}
		}
		
		
	}
	
	public String trovaAnnataOro() {
		String ritorna="";
		
		if(grafo==null) {
		
			return "IL GRAFO NON E' ANCORA STATO CREATO";
		}
		//per ogni stagione vado ad analizzare tutti gli archi entranti ed uscenti
		int best=Integer.MIN_VALUE;
		Season stagioneBest=null;
		
		for(Season s: stagioniOrdinate) {
			int pesoEntrante=0;
			for(DefaultWeightedEdge e: grafo.incomingEdgesOf(s)){
				pesoEntrante=pesoEntrante+(int) grafo.getEdgeWeight(e);
			}
			int pesoUscente=0;
			for(DefaultWeightedEdge e: grafo.outgoingEdgesOf(s)){
				pesoUscente=pesoUscente+(int) grafo.getEdgeWeight(e);
			}
			
			int pesoDiff=pesoEntrante-pesoUscente;
			System.out.println("STAGIONE "+s.getDescription()+" pesoDiff "+pesoDiff+" best "+best);
			if(pesoDiff>best) {
				best=pesoDiff;
				stagioneBest=s;
			}
		}
		
		ritorna="LA STAGIONE MIGLIORE PER LA SQUADRA TRATTATA E': \n\n"+stagioneBest.getDescription()+" con differenza tra entranti ed uscenti "+best;
		return ritorna;
	}
	
	List<Season> soluzione;
	private int maxLunghezza;
	
	public String trovaCamminoVirtuoso() {
		String ritorna= "CAMMINO VIRTUOSO: \n";
		if(grafo==null) {
			
			return "IL GRAFO NON E' ANCORA STATO CREATO";
		}
		soluzione=new ArrayList<>(); 
		maxLunghezza=0;
		
		ricorsione(0,new ArrayList<Season>());
		
		System.out.println("\nFINITA LA RICORSIONE.\n");
		
		for(Season s: soluzione) {
			ritorna=ritorna+"Stagione "+s.getDescription()+" con punti realizzati "+s.getPuntiSquadra()+"\n";
		}
		
		
		return ritorna;
	}
	
	public void ricorsione(int livello, List<Season> parziale) {
		
		if(parziale.size()>0) {
			//devo cercare la stagione successiva di quella che avevo aggiunto
			//potrebbe anche essere di due anni successiva se ho saltato quella in mezzo, ma
			//sfrutto la lista di stagioni ordinate per la squadra che avevo
			Season ultima=parziale.get(parziale.size()-1);
			//indice che devo analizzare
			int indiceDaAnalizzare=stagioniOrdinate.indexOf(ultima)+1;
			//controllo di non uscire fuori dalle dimensioni
			Season prossima;
			if(stagioniOrdinate.size()>indiceDaAnalizzare) {
				prossima=stagioniOrdinate.get(indiceDaAnalizzare);
				//devo solo trovare un cammino in crescendo di punti
				if(prossima.getPuntiSquadra()>ultima.getPuntiSquadra()) {
					parziale.add(prossima);
					ricorsione(livello+1,parziale);
					parziale.remove(parziale.size()-1);
				}
				else {
					//non ho piu' nulla da aggiungere perche' ho finito la sequenza crescente
					if(livello>maxLunghezza) {
						//ho un percorso di lunghezza massima
						maxLunghezza=livello;
						soluzione=new ArrayList<>(parziale);
					}
				}
			}
			else {
				//non ho piu' nulla da aggiungere perche' ho finito la lista di stagioni e quindi controllo
				if(livello>maxLunghezza) {
					//ho un percorso di lunghezza massima
					maxLunghezza=livello;
					soluzione=new ArrayList<>(parziale);
				}
			}
			
		}
		else {
			for(Season s: stagioniOrdinate) {
				parziale.add(s);
				ricorsione(livello+1,parziale);
				parziale.remove(parziale.size()-1);
			}
		}
		
	}
	

}
