import java.io.*;
import java.awt.*;
import java.applet.*;
import java.net.*;   
import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;


public class Evolve {
	int numLinks;
	int numNodes;
	int numAuto;
	public int od[][];
	public Node node[];
	public Auto auto[];
	Demo sd;
	
	public int to[][];	//downstream nodes;
	//First bracket: nodes;	(from 0)
	//Second:
	//0: number of destinations from current node;	
	//1-n: next node id;
	
	public int from[][];	//upstream nodes
	//First bracket:  nodes;	(from 0)
	//Second:
	//0: number of origin nodes leading to the current node;
	//1-n: last node id;
	
	public double link[][][];
	//Store link information;
	//first bracket: Origin (from 1)
	//Second bracket: Destination	(from 1)
	//Third:
	//0: link id;
	//1: length
	//2: capacity
	//3: free flow travel time (Min)
	//4: traffic flow; (veh/hour)
	//5: BPR travel time	(Min)
	//6: tollrate
	
	double turningMatrix[][];	//Turning probability from node i to jth node;
	//Store probability to turn from currentNode i to jth possible destination;
	//first bracket: Current Node (from 1);
	//second bracket: probability to next destination (from 0);
	
	int estimatedOD[][];
	//first bracket represents origin; (from1)
	//second bracket represents destination; (from1)
	
	
	final static int link_attributes = 7;
	final static int max_step = 500;
	final static int max_auto_nodestination = 0;
	final static int warmupsteps = 50;
	final static int max_iteration = 30;
	final static int max_error = 5;	//Flow difference between two successive iterations;
	final static int large = 500;
	final static double alpha=0.15;
	final static int beta = 4;
	//Variables for route choice strategy;
	final private long seed = 9327;
	Random rand = new Random(seed);
	float r;
	final static double gamma=-1;
	final static double D=1;
	final static int routechangestrategy = 4;
	int iteration;
	double steepness=3.0;
	//Variables for Network Origin-Destination Estimator;
	final static double timevalue_mean = 10;	//Mean of Auto's value of time; unit in $/hour, should divided by 60 when assigned to Autos;
	final static double timevalue_sd = 5;		//Standard deviation of Auto's value of time;
	final private long seed_timevalue = 9327;	//Random seed for generating random value of time;
	Random rVofT = new Random(seed_timevalue);		//random varible for generating random value of time;
	final static double mu=-1;	//sensitivity for travel cost when choose destination;
	//Variables for Travel budget construction and maintenance
	final static double travelbudget_mean = 25.39;	//unit in minute, this is the value for Sioux Falls network;
	final static double travelbudget_sd = 13.35;	//unit in minute, for Sioux Falls network;
	final private long seed_travelbudget = 2000;
	Random rTrB = new Random(seed_travelbudget);
	final static int max_moving = 50;
	
	float A[][];
	float theta;
	float lbeta;
	
	public Evolve(){		
	}
	
	public Evolve(DirectedGraph dg, Demo sd, float theta, float lbeta) throws IOException{
		System.out.println("Initialization Evolve");
		this.sd = sd;
		this.theta = theta;		//Influence of time;
//		this.lbeta = lbeta;		//Travel length coefficient; Larger -> shorter;
		this.lbeta = (float)(0.15*0.15)/lbeta;
		numLinks = dg.numLink;
		numNodes = dg.numNodes;
		
		link =  new double[numNodes+1][numNodes+1][link_attributes];
		for (int i=0;i<numNodes+1;i++){
			for (int j=0;j<numNodes+1;j++){
				for (int k=0;k<link_attributes;k++){
					link[i][j][k]=0;
				}
			}
		}
		int nextNodesNum[];
		int lastNodesNum[];
		nextNodesNum = new int[numNodes];
		lastNodesNum = new int[numNodes];
		for (int i=0;i< numNodes;i++){
			nextNodesNum[i] = 0;
			lastNodesNum[i] = 0;
		}
		//Read information from DG;
		for (int i=0;i<numLinks;i++){
			int oNode;
			int dNode;
			oNode = dg.link[i].oNode;
			dNode = dg.link[i].dNode;
			link[oNode][dNode][0] = dg.link[i].linkID;
			link[oNode][dNode][1] = dg.link[i].length;
			link[oNode][dNode][2] = dg.link[i].capacity;
			link[oNode][dNode][3] = dg.link[i].fft;
//			link[oNode][dNode][4] = dg.link[i].flow;
			link[oNode][dNode][4] = 0;
//			link[oNode][dNode][5] = dg.link[i].currentT;
			link[oNode][dNode][5] = dg.link[i].fft;
			link[oNode][dNode][6] = 0;
//			Statistic for nextNodes and lastNodes;
			nextNodesNum[oNode-1]++;
			lastNodesNum[dNode-1]++;
		}
				
		to = new int [numNodes][];
		from = new int [numNodes][];
		for (int i=0;i<numNodes;i++){
			to[i] = new int[nextNodesNum[i]+1];
			from[i] = new int[lastNodesNum[i]+1];
			to[i][0] = nextNodesNum[i];
			from[i][0] = lastNodesNum[i];
			nextNodesNum[i] = 1;	//used as index number for next step;
			lastNodesNum[i] = 1;
		}
		for (int i=0;i<numLinks;i++){
			int oNode;
			int dNode;
			oNode = dg.link[i].oNode;
			dNode = dg.link[i].dNode;
//			System.out.println("\t"+(oNode-1)+"\t"+(nextNodesNum[oNode-1])+"\t"+dNode);
			to[oNode-1][nextNodesNum[oNode-1]] =dNode;
			from[dNode-1][lastNodesNum[dNode-1]] = oNode;
			nextNodesNum[oNode-1]++;
			lastNodesNum[dNode-1]++;
		}
		//End of generatint index file to[][] and from[][];
		
		//Construct Nodes
		node = new Node[numNodes];
		int counter;
		counter = 0;
		for (int i=0;i<numNodes;i++){
			int id = i+1;
			int nodeauto = dg.node[id].numAuto;
			counter += nodeauto;
			int nodeoppo = dg.node[id].numOppo;
			node[i] = new Node(id,numNodes,nodeauto,nodeoppo,nodeoppo);
		}
		numAuto = counter;
		//Construct Autos
		rVofT = new Random(seed_timevalue);
		rTrB = new Random(seed_travelbudget);
		auto = new Auto[numAuto];
		counter = 0;
		for (int i=0;i<numNodes;i++){
			for (int j=0;j<node[i].numAuto;j++){
					double vot;
					double travelbudget;
					vot = (timevalue_mean+timevalue_sd*rVofT.nextGaussian())/60;	//Random value of time in $/min;
					travelbudget = travelbudget_mean+travelbudget_sd*rTrB.nextGaussian();
					if (vot<0) vot=0;		//vot should be non-negative;
					if (travelbudget<3) travelbudget=3;	//travelbudget should be at least 3 according to the histogram of distribution;
					travelbudget = travelbudget*vot;	//change to value in dollar;
//					System.out.println("travelbudget:"+travelbudget);
//					System.out.println("vot:"+vot);
					new Auto(counter+1,i+1,-1,vot,travelbudget);
					auto[counter] = new Auto(counter+1,i+1,-1,vot,travelbudget);	//Unspecified destination is marked as -1;
					counter++;
					
			}
		}
		printlink();
	//End of Constructor;
	}
	
	public void initialization(){
		rand = new Random(seed);
		iteration = 0;
		//choose the orginial route for each auto;
		int step, freeauto;
		step = 1;
		freeauto = numAuto;
		//Because of the OD intialization, we have to reset the Auto;
		resetAuto();
		while (step<max_step && freeauto > max_auto_nodestination){
			for (int i=0;i<numAuto;i++){
				if (auto[i].status == 0){
					int currentNode, previousNode;
					currentNode = auto[i].currentNode;
					previousNode = auto[i].previousNode;
					int nextNode;
					nextNode = chooseNextNode(currentNode,previousNode);
//						if (i==207){
//							System.out.println("Auto207 previous:"+auto[i].previousNode+"\tcurrent"+auto[i].currentNode+"\tnext"+nextNode);
//						}
					double linkcost;
					linkcost = link[currentNode][nextNode][5];
					boolean existCircle;
					existCircle = auto[i].addNode(nextNode, linkcost);
					if (existCircle == true){
						//If a circle exists in the path, remove it and rebuild the route;
						//the circle is removed from pathchain during addNode; the cost will be updated;
						rebuildpathcost(i);
					}
					//Check if this auto found its destination;
					if (auto[i].status == 1){
						freeauto--;
					}
				}else{
					//Auto stopped searching;
				}
			}
			step++;
		}
		//***************************************
		//After initialization, some travelers may fail to find the initial path
		//Set up initial knowledge set for Nodes;
		for (int i=0;i<numAuto;i++){
			if (auto[i].status==1){
				int nodeindex;
				nodeindex = auto[i].destination-1;
				exchangeinfo(i,nodeindex);
			}else{
				//not include information from travelers without destination
//				System.out.print("\nAuto"+i+"\tO"+auto[i].origin+"D"+auto[i].destination+"\tpath:");
//				int pathlength;
//				pathlength = (int)auto[i].path_info[0];
//				for(int j=0;j<pathlength;j++){
//					System.out.print(""+auto[i].path[j]);
//				}
			}
		}
		//****************************************
		//Set up intial path for travelers without destination;
		//Solution1: assign the shortest to them;
		for (int i=0;i<numAuto;i++){
			if (auto[i].status == 0){
				//Autos without inital path;
				int origin, destination;
				origin = auto[i].origin;
				destination = auto[i].destination;
				if (node[destination-1].path_info[origin-1][0][1] < Node.infinite){
					//at one path exist between OD;
					int pathlength;
					pathlength = (int)node[destination-1].path_info[origin-1][0][0];
					for (int j=0;j<pathlength;j++){
						auto[i].path[j] = node[destination-1].path[origin-1][0][j];
					}
					for (int j=pathlength;j<Auto.large;j++){
						auto[i].path[j] = Auto.nullindicator;
					}
					auto[i].path_info[0] = node[destination-1].path_info[origin-1][0][0];
					auto[i].path_info[1] = node[destination-1].path_info[origin-1][0][1];
				}else{
					System.out.println("****The OD("+origin+")("+destination+") fails to set up the initial path!");
				}
			}
		}
		//Initalroute is the same as the inital path
		for (int i=0;i<numAuto;i++){
			auto[i].copypathToroute();
		}
		//Build travel budget for autos;
		buildTravelbudget();
		System.out.println("End of initialization and Autos without destination:"+freeauto);
		//End of initialization, original route built;
//		printroute();
	}
	
	public void iteration(){
		iteration = 1;
		steepness = 3;
		double error = large;
		while (iteration<max_iteration && error>max_error){
			//reset the information of network: Auto and Nodes;
			resetAuto();
			resetNode();
//			printnode();
			int freeauto = numAuto;
			//Set the steepness for routechoice function;
//			if (iteration == 10) steepness=2.0;
//			if (iteration == 20) steepness=1.0;
//			if (iteration == 30) steepness=0.5;
//			if (iteration == 40) steepness=0.4;
			
			//start inner cycle;
			int step=1;
			
			while (step<max_step && freeauto > max_auto_nodestination){
				for (int i=0;i<numAuto;i++){
					if (auto[i].status == 0){
						int routeindex;
						routeindex = auto[i].currentposition;
						int currentNodeId;
						currentNodeId = auto[i].currentNode;
						int nextNodeId;
						nextNodeId = auto[i].route[routeindex+1];
						auto[i].currentposition = routeindex+1;
						double linkcost;
						linkcost = link[currentNodeId][nextNodeId][5];
						auto[i].pathIncrement(nextNodeId, linkcost);		//Move the Auto one node forward;
						auto[i].previousNode = currentNodeId;
						auto[i].currentNode = nextNodeId;
						if (nextNodeId == auto[i].destination){
							//Auto arrives at its destination;
							auto[i].status = 1;
							freeauto--;
						}else{
							//Auto arrives at an intermedia node;
						}
						exchangeinfo(i,nextNodeId-1);
					}else{
						//Auto stopped searching;
					}
				}
				
				step++;
			}
			//********************************
			//Auto update their route choice
			routechoice();
			//Decide error term;
			double temperror = 0;
			double oldflow[][];
			oldflow = new double[numNodes+1][numNodes+1];
			for (int i=1;i<numNodes+1;i++){
				for (int j=1;j<numNodes+1;j++){
					oldflow[i][j] = link[i][j][4];
				}
			}
			statistics();
			for (int i=1;i<numNodes+1;i++){
				for (int j=1;j<numNodes+1;j++){
					double err = Math.abs(oldflow[i][j]-link[i][j][4]);
					if (err > temperror){
						temperror = err;
					}
				}
			}
			error = temperror;
			System.out.println("End of Iteration"+iteration+"\tError:"+error);
			String temp=".";
			for (int i=0;i<iteration;i++){
				temp = temp+".";
			}
			sd.dp.showStatus.setText("End of Iteration"+iteration+"\tError:"+error+"\tProgress"+temp);
			iteration++;
		}
	}
	
	public int chooseNextNode(int currentNode, int previousNode){
		int nextNode = -1;
		int numcandidate;
		int index;
		index = currentNode-1;
		int counter = 0;
		boolean backwayexist = false;
		if (to[index][0]>0){
			for (int i=1;i<=to[index][0];i++){
				if (to[index][i] != previousNode){
					counter++;
				}else{
					backwayexist = true;
				}
			}
		}else{
			System.out.println("Dead end!!!");
		}
		boolean found = false;
		if (counter>0){
			//at lease one nextnode different from previous node;
			double interval;
			double upbound;
			interval = 1.0/counter;
			upbound = 0;
			r=rand.nextFloat();
			for (int i=1;i<=to[index][0];i++){
				if (to[index][i] != previousNode && found==false){
					upbound += interval;
					if (r<=upbound){
						nextNode = to[index][i];
						found = true;
					}
				}
			}
		}else if (counter==0 && backwayexist==true){
			nextNode = previousNode;
			found = true;
		}else{
			nextNode = previousNode;
			found = false;
		}
		
		if (found){
			
		}else{
			System.out.println("Fail to choose next Node !!!!");
		}
		return (nextNode);
	}
	
	public void rebuildpathcost(int autoindex){
		int pathlength;
		pathlength = (int)auto[autoindex].path_info[0];
		double pathcost = 0;
		for (int i=0;i<pathlength-1;i++){
			int oNode, dNode;
			oNode = auto[autoindex].path[i];
			dNode = auto[autoindex].path[i+1];
			pathcost += link[oNode][dNode][5];
		}
		auto[autoindex].path_info[1] = pathcost;
	}
	
	public void exchangeinfo(int autoindex, int nodeindex){
		int initialpathindex;
		initialpathindex = (int)auto[autoindex].path_info[0]-1-1;	//Check from the second last digit for comaprison;
		double autocost;
		autocost = 0;
		for (int i=initialpathindex;i>=0;i--){
			//Cut out one section from the end for comparison;
			int originindex;
			originindex = auto[autoindex].path[i]-1;
			autocost += link[originindex+1][auto[autoindex].path[i+1]][5];	//calculate cost info by auto;
			//************Step1, auto->node
			//In case autocost < longest_nodepathcost, insert the former in the latter;
			boolean autoshorter;
			autoshorter = false;
			//*****************
			int sectionlength;	//For the judgement of same path section;
			sectionlength = (int)auto[autoindex].path_info[0]-i;
			int[] section;
			section = new int[sectionlength];
			for (int j=0;j<sectionlength;j++){
				section[j]=auto[autoindex].path[i+j];
			}
			boolean sectionexisted;
			sectionexisted = false;
			//*****************
			int nodepathcounter;
			nodepathcounter = 0;
			int insertposition = -1;
			while ((autoshorter==false) && (nodepathcounter<Node.option_number) && (sectionexisted == false)){
				if (autocost<node[nodeindex].path_info[originindex][nodepathcounter][1]){
					autoshorter = true;
					insertposition = nodepathcounter;
				}
				sectionexisted = samepath(section,node[nodeindex].path[originindex][nodepathcounter],sectionlength);	//check if this path exists in storage;
				nodepathcounter++;
			}
			if ((autoshorter==true) && (sectionexisted == false)){
				//Insert autopath to insertposition in the node path information stack;
				for (int j=Node.option_number-1;j>insertposition;j--){
					for (int k=0;k<Node.large;k++){
						node[nodeindex].path[originindex][j][k] = node[nodeindex].path[originindex][j-1][k];
					}
					node[nodeindex].path_info[originindex][j][0] = node[nodeindex].path_info[originindex][j-1][0];
					node[nodeindex].path_info[originindex][j][1] = node[nodeindex].path_info[originindex][j-1][1];
				}
				//Copy the information of comparison section of auto path info to the insertposition of node path info stack;
//				System.out.print("copy auto"+autoindex+"to node"+(nodeindex+1)+"from"+(originindex+1)+"option"+insertposition+":");
				int counter =0;
				int pathlength = (int)auto[autoindex].path_info[0];
				for (int k=i;k<pathlength;k++){
					node[nodeindex].path[originindex][insertposition][counter] = auto[autoindex].path[k];
//					System.out.print(""+node[nodeindex].path[originindex][insertposition][counter]);
					counter++;
				}//we need copy -9 to the rest position;
				while (counter<Node.large && node[nodeindex].path[originindex][insertposition][counter]!=Node.nullindicator){
					node[nodeindex].path[originindex][insertposition][counter] = Node.nullindicator;
					counter++;
				}
//				System.out.print("\n");
				node[nodeindex].path_info[originindex][insertposition][0] = auto[autoindex].path_info[0]-i;
				node[nodeindex].path_info[originindex][insertposition][1] = autocost;
			}else{
				//auto path is longer than any node path, do nothing;
				//Or this path has existed in the node information, do nothing;
			}
			//***************Step2:node->auto
			//In case autocost>shortest_nodepathcost, copy the latter to the former;
			if (autocost>node[nodeindex].path_info[originindex][0][1]){
				//Auto's path is not the shortest one, update Auto's path;
//				System.out.print("copy from node"+(nodeindex+1)+"to auto"+autoindex+":");
				int counter = 1;
				for (int j=1;j<(int)node[nodeindex].path_info[originindex][0][0];j++){
					auto[autoindex].path[i+j] = node[nodeindex].path[originindex][0][j];
//					System.out.print(""+auto[autoindex].path[i+j]);
					counter++;
				}
				while ((i+counter)<Auto.large && auto[autoindex].path[i+counter]!=Auto.nullindicator){
					auto[autoindex].path[i+counter] = Auto.nullindicator;
					counter++;
				}//copy -9 to the rest digit;
				//Change path node chain;
//				System.out.print("\n");
				auto[autoindex].path_info[0] = i+ node[nodeindex].path_info[originindex][0][0];	//Change path index length;
				auto[autoindex].path_info[1] = auto[autoindex].path_info[1]-(autocost-node[nodeindex].path_info[originindex][0][1]);
				autocost = node[nodeindex].path_info[originindex][0][1];						//update path cost and current cost used for comparison;
			}
			//End for the current section of path;
		}
		//End for compare all the path along current travel route;
	}
	
	public void statistics() {
		//Statistics for travelers differentiation;
		//update link_flow, travel cost;
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				link[i][j][4]=0;
			}
		}
		for (int i=0;i<numAuto;i++){
			int routelength = (int)auto[i].route_info[0];
			for (int j=0;j<routelength-1;j++){
				link[auto[i].route[j]][auto[i].route[j+1]][4] +=1;
			}
		}
		
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				if (link[i][j][2] > 0){
					link[i][j][5] = link[i][j][3]*(1+alpha*Math.pow((link[i][j][4]/link[i][j][2]),beta));
//					System.out.println("O"+i+"D"+j+"Flow:"+link[i][j][4]+"\tcost:"+link[i][j][5]);
				}
			}
		}
	}
	
	public void resetAuto(){
		for (int i=0;i<numAuto;i++){
			if (auto[i].status != -1) auto[i].status = 0;
			auto[i].currentNode = auto[i].origin;
			auto[i].previousNode = auto[i].origin;
			auto[i].currentposition = 0;
			auto[i].path =  new int[Auto.large];
			auto[i].path_info = new double[Auto.path_info_attributes];
			auto[i].path[0] = auto[i].origin;
			for (int j=1;j<Auto.large;j++){
				auto[i].path[j] = Auto.nullindicator;
			}
			auto[i].path_info[0] = 1;
			auto[i].path_info[1] = Auto.infinite;
			int routelength = (int)auto[i].route_info[0];
			double newroutecost = 0;
			for (int j=0;j<routelength-1;j++){
				newroutecost += link[auto[i].route[j]][auto[i].route[j+1]][5];
			}
			auto[i].route_info[1] = newroutecost;
//			System.out.print("\nAuto"+i+"\troute:");
//			for (int j=0;j<routelength;j++){
//				System.out.print(""+auto[i].route[j]);
//			}
//			System.out.print("\tcost:"+auto[i].route_info[1]);
		}
		System.out.print("\n");
	}
	
	public void resetNode(){
		for (int i=0;i<numNodes;i++){
			for (int j=0;j<numNodes;j++){
				for (int k=0;k<Node.option_number;k++){
					int pathlength = (int)node[i].path_info[j][k][0];
					if (pathlength>1){
						double newpathcost=0;
						for (int l=0;l<pathlength-1;l++){
							newpathcost += link[node[i].path[j][k][l]][node[i].path[j][k][l+1]][5];
						}
						node[i].path_info[j][k][1] = newpathcost;
					}else{
						//No information is available for kth path from node j to node i;
					}
				}
				int temppath[];
				double temppathinfo[];
				temppath = new int[Node.large];
				temppathinfo = new double[Node.path_info_attributes];
				for (int k=0;k<Node.option_number-1;k++){
					for (int l=k+1;l<Node.option_number;l++){
						if (node[i].path_info[j][k][1]>node[i].path_info[j][l][1]){
							//Switch between k and l'th route option;
							for (int m=0;m<Node.path_info_attributes;m++){
								temppathinfo[m] = node[i].path_info[j][k][m];
								node[i].path_info[j][k][m] = node[i].path_info[j][l][m];
								node[i].path_info[j][l][m] = temppathinfo[m];
							}
							for (int m=0;m<Node.large;m++){
								temppath[m] = node[i].path[j][k][m];
								node[i].path[j][k][m] = node[i].path[j][l][m];
								node[i].path[j][l][m] = temppath[m];
							}
						}
					}
				}
				//End for the ith node, and path from jth node;
			}
		}
	}
	
	public boolean samepath(int[] path1, int[] path2, int pathlength){
		boolean same = true;
		int counter = 0;
		while (counter<pathlength && same == true){
			if (path1[counter] != path2[counter]){
				same = false;
			}
			counter++;
		}
		return(same);
	}
	
	public void routechoice(){
		for (int i=0;i<numAuto;i++){
			if (auto[i].destination != -1)
			{
				int origin, destination;
				origin = auto[i].origin;
				destination = auto[i].destination;
				int proposedoptionindex;
				proposedoptionindex = -1;
				int optionindex = 0;
				boolean proposedpathfound = false;
				//variable for dollar cost;
				double dollarcost[];
				int costsort[];
				double currentdollarcost = 0;
				double proposeddollarcost = 0;
				if (routechangestrategy==1 || routechangestrategy==2){
					//Route Choice without Toll;
					while ((optionindex<Node.option_number) && (proposedpathfound==false)){
						if (samepath(auto[i].route,node[destination-1].path[origin-1][optionindex],(int)auto[i].route_info[0]) == false){
							//Not the same route;
							if (node[destination-1].path_info[origin-1][optionindex][1] < Node.infinite){
								//the path exist;
								proposedoptionindex = optionindex;
								proposedpathfound = true;
							}
						}
						optionindex++;
					}
				}else if (routechangestrategy==3 || routechangestrategy==4) {
					//*******************Route choice with Toll********
					dollarcost = new double[Node.option_number];
					costsort = new int[Node.option_number];
					//Find monetory cost for each option;
					for(int j=0;j<Node.option_number;j++){
						dollarcost[j] = 0;
						costsort[j] = j;
						int pathlength;
						pathlength = (int)node[destination-1].path_info[origin-1][j][0];
						if (pathlength>1){	//Route exists
							for (int k =0;k<pathlength-1;k++){
								int oNode,dNode;
								oNode = (int)node[destination-1].path[origin-1][j][k];
								dNode = (int)node[destination-1].path[origin-1][j][k+1];
								dollarcost[j] += (auto[i].vot*link[oNode][dNode][5]+link[oNode][dNode][6]);
							}
						}else{
							dollarcost[j] = Node.infinite;
						}
					}
					//Sorted according to dollar cost;
					for(int j=0;j<(Node.option_number-1);j++){
						for (int k=j+1;k<Node.option_number;k++){
							if (dollarcost[j]>dollarcost[k]){
								double tempcost;
								int tempindex;
								tempcost = dollarcost[j];
								dollarcost[j] = dollarcost[k];
								dollarcost[k] = tempcost;
								tempindex = costsort[j];
								costsort[j] = costsort[k];
								costsort[k] = tempindex;
							}
						}
					}
					//Toll cost for current route;
					int routelength;
					routelength = (int)auto[i].route_info[0];
					for (int j=0;j<routelength-1;j++){
						int oNode,dNode;
						oNode = auto[i].route[j];
						dNode = auto[i].route[j+1];
						currentdollarcost += (auto[i].vot*link[oNode][dNode][5]+link[oNode][dNode][6]);
					}
					//Choose the proposed path: shortest in toll but not the same one;
					while ((optionindex<Node.option_number) && (proposedpathfound==false)){
						if (samepath(auto[i].route,node[destination-1].path[origin-1][costsort[optionindex]],(int)auto[i].route_info[0]) == false){
							//Not the same route;
							if (node[destination-1].path_info[origin-1][costsort[optionindex]][1] < Node.infinite){
								//the path exist;
								proposedoptionindex = costsort[optionindex];
								proposedpathfound = true;
								proposeddollarcost = dollarcost[optionindex];
							}
						}
						optionindex++;
					}
				}
				if (proposedoptionindex>=0){
					double p1;
					double sum;
					if (routechangestrategy == 1){
						sum = Math.exp(auto[i].route_info[1]*gamma+D)+Math.exp(node[destination-1].path_info[origin-1][proposedoptionindex][1]*gamma);
						p1 = Math.exp(auto[i].route_info[1]*gamma+D)/sum;
					}else if(routechangestrategy == 2){
						double shreshhold = 0.5;
						double gap;
						gap = auto[i].route_info[1] - node[destination-1].path_info[origin-1][proposedoptionindex][1];
						if (gap < shreshhold){
							p1=1;
						}else{
							sum = Math.exp(gap*gamma+D)+Math.exp(shreshhold*gamma);
							p1 = Math.exp(gap*gamma+D)/sum;
						}
					}else if (routechangestrategy == 3){
						sum = Math.exp(currentdollarcost*gamma+D)+Math.exp(proposeddollarcost*gamma);
						p1 = Math.exp(currentdollarcost*gamma+D)/sum;
					}else if (routechangestrategy == 4){
						double shreshhold = 0.1;	//1min beneficial equals 0.16$;
						double gap;
						gap = currentdollarcost -proposeddollarcost;
						if (gap < shreshhold){
							p1 = 1;
						}else{
							sum=1.0/steepness*(1-Math.exp(gamma*gap/10));
							p1=1-sum;
	//						sum = Math.exp(D*steepness/Math.pow(gap,0.5)*gamma-shreshhold)+Math.exp(shreshhold*0.5*gamma);
	//						p1 = Math.exp(shreshhold*0.5*gamma)/sum;
	//						p1 = 1-(1-p1)/Math.pow(iteration, 0.5);
	//						System.out.println("p1"+p1+"\t"+currentdollarcost+"\t"+proposeddollarcost);
						}
					}
					r = rand.nextFloat();
					if (r>p1){//change current route;
						for (int j=0;j<Auto.large;j++){
							auto[i].route[j] = node[destination-1].path[origin-1][proposedoptionindex][j];
						}
						auto[i].route_info[0] = node[destination-1].path_info[origin-1][proposedoptionindex][0];
						auto[i].route_info[1] = node[destination-1].path_info[origin-1][proposedoptionindex][1];
					}else{
						//Do not change according to stochasticity;
					}
				}else{
					//There is no alternative option and travelers do not change
				}
			}else{
				//Auto without destination;
			}
		}
	}
	//*****************************************Functions for OD choice********
	//Generate OD table for the base year based on random search;
	public void odInitialization(){
		int rNodeSeed = 1000;
		Random rNode = new Random(rNodeSeed);
		int nodeJob[];
		nodeJob = new int[numNodes+1];
		for (int i=1;i<numNodes+1;i++){
			nodeJob[i] = node[i-1].currentOppo;			//Notice the difference for coding the node and nodejob;
		}
		//In Network OD Estimator, we only use status, currentNode, Previous node, and orgin to find a destination;
		for (int i=0;i<numAuto;i++){
			auto[i].currentNode = auto[i].origin;
			auto[i].previousNode = auto[i].origin;
			auto[i].status = 0;
		}
		int freeauto = numAuto;
		int step = 0;
		while ((freeauto>max_auto_nodestination) && (step<max_step)){
//			updateTurningMatrix(nodeJob);	//update turning matrix;
			int jobdemand[];
			jobdemand = new int[numNodes+1];
			for (int i=1;i<numNodes+1;i++){
				jobdemand[i] = 0;
			}
			for (int i=0;i<numAuto;i++){
				if (auto[i].status == 0){	//Auto without a destination;
					int counter = 0;
					int currentNode = auto[i].currentNode;
					double sum = 0;
					boolean dFound = false;
					boolean backwayexist = false;
					boolean originfound = false;
					double cost[];
					cost = new double[to[currentNode-1][0]];
					int dSet[];
					dSet = new int[to[currentNode-1][0]];
					for (int j=1;j<to[currentNode-1][0]+1;j++){	//counter number of posible destination;
						if (to[currentNode-1][j] != auto[i].previousNode){	//prevent direct circle;
							if (to[currentNode-1][j] != auto[i].origin){	//Prevent Auto from returning to its origin;
								counter++;
								dSet[counter-1] = to[currentNode-1][j];						//Destination nodeID;
								cost[counter-1] = link[currentNode][to[currentNode-1][j]][5];	//Cost from currentNode to potential destination;
//								cost[counter-1] = A[auto[i].origin][to[currentNode-1][j]];		//Cost from Origin to the potential destination;
//								sum += Math.exp(mu*cost[counter-1])*nodeJob[to[currentNode-1][j]];	//No adjusting variables;
								sum += Math.exp(mu*theta*cost[counter-1])*nodeJob[to[currentNode-1][j]];	//Adjusting term;
							}else{
								originfound = true;
							}
						}else{	//At least we can back up;
							backwayexist = true;
						}
					}
					double rN;
					rN = rNode.nextDouble();	//Generate a random number;
					double upbound = 0;
					int tempcounter = 0;
					while ((dFound==false) && (tempcounter<counter)){
//						upbound += Math.exp(mu*cost[tempcounter])*nodeJob[dSet[tempcounter]]/sum;
						upbound += Math.exp(mu*theta*cost[tempcounter])*nodeJob[dSet[tempcounter]]/sum;
						if (rN < upbound){
							dFound = true;
							auto[i].previousNode = auto[i].currentNode;
							auto[i].currentNode = dSet[tempcounter];
							jobdemand[auto[i].currentNode]++;
						}
						tempcounter++;
					}
					if (dFound == false){
						//Fail to Find a Destination;
						if (counter>0){
							//Possible candidate exist;	//but dJob equals 0;
							//Randomly choose next destination but not return to origin or previous node;
							double interval;
							interval = 1.0/counter;
							upbound = 0;
							tempcounter = 0;
							while ((dFound==false) && (tempcounter<counter)){
								upbound += interval;
								if (rN < upbound){
									dFound = true;
									auto[i].previousNode = auto[i].currentNode;
									auto[i].currentNode = dSet[tempcounter];
									jobdemand[auto[i].currentNode]++;
								}
								tempcounter++;
							}
						}else{
							//In this case, either no destination or destination is the orgin;
							//However, since our target is to find a destination rather than a path, we can just move backward;
							dFound = true;
							auto[i].currentNode = auto[i].previousNode;
							jobdemand[auto[i].currentNode]++;
						}
					}
					if (dFound == false){
						System.out.println("Error in Destination finding!");
					}
				}
			}
			//Decide which Auto should stay;
			int autoindex[][];
			autoindex = new int[numNodes+1][];
			int counter[];
			counter = new int[numNodes+1];
			for (int i=1;i<numNodes+1;i++){
				autoindex[i] = new int[jobdemand[i]];
				counter[i] = 0;
			}
			for (int i=0;i<numAuto;i++){
				if (auto[i].status==0){
					autoindex[auto[i].currentNode][counter[auto[i].currentNode]] = i;
					counter[auto[i].currentNode]++;
				}
			}
			//Equal change for every auto on a Node;
			double p1;
			double rP;
			for (int i=1;i<numNodes+1;i++){
				if (jobdemand[i]>0 && nodeJob[i]>0){
					if (jobdemand[i]>nodeJob[i]){
						p1=nodeJob[i]/jobdemand[i];
					}else{
						p1=1;
					}
					for (int j=0;j<jobdemand[i];j++){
						rP = rNode.nextDouble();
						double p2;
						p2=p1*lbeta;	//For every auto, it has 30% to accept a job;
						if ((rP<p2) && (nodeJob[i]>0)){
							auto[autoindex[i][j]].status = 1;
							auto[autoindex[i][j]].destination = i;
							nodeJob[i]--;
							freeauto--;
						}
					}
				}				
			}
			//End of each step;
			step++;
		}
		//End of initial destination finding;
		int counter = 0;
		for (int i=0;i<numAuto;i++){
			if (auto[i].status == 0){
				auto[i].status = -1;
				counter++;
			}
		}
		System.out.println("End of OD initialization,"+counter+" autos without destination.");
	}
	
	public void updateTurningMatrix(int nodeJob[]){
		turningMatrix = new double[numNodes+1][];
		for (int i=1;i<numNodes+1;i++){
			int numDestination;
			numDestination = to[i-1][0];
			turningMatrix[i] = new double[numDestination];
			for (int j=0;j<numDestination;j++){
				double cost;
				int dNode;
				dNode = to[i-1][j+1];		//j+1th destination from ith node;
				cost = link[i][dNode][5];	//Take BPR travel time from link;
				turningMatrix[i][j] = Math.exp(mu*cost)*nodeJob[dNode];
			}
		}
	}
	
	public void buildTravelbudget(){
		//the simplest way is to give traveler initial travel cost;
		for (int i=0;i<numAuto;i++){
			auto[i].travelbudge = auto[i].route_info[1];	//use money cost;
		}
	}
	
	public void odInit_budget(){
		//this function is a replacement of the function of odInitialization with given travel budget;
		int rNodeSeed = 1000;
		Random rNode = new Random(rNodeSeed);
		int nodeJob[];
		nodeJob = new int[numNodes+1];
		for (int i=1;i<numNodes+1;i++){
			int sum = 0;
			for (int j=1;j<numNodes+1;j++){
				sum += od[j][i];	//sum up od for jth to i;
			}
			nodeJob[i] = sum;	//Sum of destination = avaible jobs;
		}
		//reset information for Auto;
		resetInfo();
		//Set the destination as nullindicator and replace it with chosen destination;
		for (int i=0;i<numAuto;i++){
			auto[i].destination = Auto.nullindicator;
		}
		
		//build up the information pool for nodes;
		int step = 0;
		int freeauto = numAuto;
		while (step<warmupsteps){
			for (int i=0;i<numAuto;i++){
				int currentNode, previousNode;
				currentNode = auto[i].currentNode;
				previousNode = auto[i].previousNode;
				int nextNode;
				nextNode = chooseNextNode(currentNode,previousNode);
				double linkcost;
				linkcost = link[currentNode][nextNode][5];
				boolean existCircle;
				existCircle = auto[i].addNode(nextNode, linkcost);
				if (existCircle == true){
					//If a circle exists in the path, remove it and rebuild the route;
					//the circle is removed from pathchain during addNode; the cost will be updated;
					rebuildpathcost(i);
				}
				//building information pool for Nodes, which is used for destination choice;
				exchangeinfo(i,nextNode-1);
			}
			step++;
		}
		//reset information for Auto;
		resetInfo();
		int jobdemand[];
		int autoindex[][];
		jobdemand = new int[numNodes+1];
		autoindex = new int[numNodes+1][];
		int candidateNode[];
		candidateNode = new int[numAuto];		//Store the candidates node for auto i;
		while ((freeauto>max_auto_nodestination) && (step<max_step)){
			//Initialize the demand for each node;
			for (int i=1;i<numNodes+1;i++){
				jobdemand[i] = 0;
			}
			for (int i=0;i<numAuto;i++){
				candidateNode[i] = Auto.nullindicator;
				if (auto[i].status == 0){
					int currentNode, previousNode;
					currentNode = auto[i].currentNode;
					previousNode = auto[i].previousNode;
					int nextNode;
					nextNode = chooseNextNode(currentNode,previousNode);
					double linkcost;
					linkcost = link[currentNode][nextNode][5];
					boolean existCircle;
					existCircle = auto[i].addNode(nextNode, linkcost);
					if (existCircle == true){
						//If a circle exists in the path, remove it and rebuild the route;
						//the circle is removed from pathchain during addNode; the cost will be updated;
						rebuildpathcost(i);
					}
					//building information pool for Nodes, which is used for destination choice;
					exchangeinfo(i,nextNode-1);
					//Start to decide whether to accept one node as the destination after the warmupsteps;
					if (step>=warmupsteps){	//time for building info for Nodes, currently 100 iteration;
						double currentdollarcost;
						double mincost[];
						mincost = min_generalcost(auto[i].origin,nextNode,auto[i].vot);
						currentdollarcost = mincost[1];
						double costerror;
						costerror = currentdollarcost - auto[i].travelbudge;
//						System.out.println("costerror:"+costerror);
						if ((costerror>0) && (nodeJob[nextNode]>0)){	//Stop at the first available node with a cost larger than budget;
							candidateNode[i] = nextNode;
							jobdemand[candidateNode[i]]++;
						}else{
							//continue to travel either because no available choice or not close to travelbudget;
						}
					}else{
						//within the warmup steps, no Auto will stop at a destination;
					}
				}else{
					//auto with a destination, do nothing;
				}
			}
			//Decide which auto to stay
			int counter[];
			counter = new int[numNodes+1];
			for (int i=1;i<numNodes+1;i++){
				autoindex[i] = new int[jobdemand[i]];
				counter[i]=0;
			}
			for (int i=0;i<numAuto;i++){
				if (candidateNode[i]!= Auto.nullindicator){	//If there is a candidate;
					autoindex[candidateNode[i]][counter[candidateNode[i]]]=i;
					counter[candidateNode[i]]++;
				}
			}
			double p1;
			double rP;
			for (int i=1;i<numNodes+1;i++){
				if (jobdemand[i]>0 && nodeJob[i]>0){
					if (jobdemand[i]>nodeJob[i]){
						p1 = (double)nodeJob[i]/jobdemand[i];
					}else{
						p1 =1;
					}
					for (int j=0;j<jobdemand[i];j++){
						rP = rNode.nextDouble();
						if ((rP<p1) && (nodeJob[i]>0)){
							auto[autoindex[i][j]].destination = i;	//Change the destination;
							auto[autoindex[i][j]].status = 1;
							nodeJob[i]--;
							freeauto--;
						}
					}
				}
			}
			step++;
			System.out.println("step:"+step+"\tfreeauto:"+freeauto);
		}
		System.out.println("End of OD initialization with travelbudget, auto without destination:"+freeauto);
		for (int i=0;i<numAuto;i++){
			if (auto[i].status == 0){
				int rdest;
				rdest = (int)(rNode.nextDouble()*numNodes);
				if (rdest<1)rdest=1;
				if (rdest>numNodes)rdest=numNodes;
				auto[i].destination = rdest;
				auto[i].status =1;
			}
		}
		//If travel cost is two times the tavelbudget, prefer to stay at home;
		int counter = 0;
		for (int i=0;i<numAuto;i++){
			double mincost[];
			mincost = min_generalcost(auto[i].origin,auto[i].destination,auto[i].vot);
			if (mincost[1] > (auto[i].travelbudge*2)){
				auto[i].destination = auto[i].origin;
				counter++;
			}
		}
		System.out.println("number of travelers prefer to stay at home:"+counter);
	}
	
	public double[] min_generalcost(int origin, int destination, double vot){
		double mincost[];//the first index store the index, the second store the cost in dollar;
		mincost =  new double[2];
		double dollarcost[];
		int costsort[];
		dollarcost = new double[Node.option_number];
		costsort = new int[Node.option_number];
		for(int j=0;j<Node.option_number;j++){
			dollarcost[j] = 0;
			costsort[j] = j;
			int pathlength;
			pathlength = (int)node[destination-1].path_info[origin-1][j][0];
			if (pathlength>1){	//Route exists
				for (int k =0;k<pathlength-1;k++){
					int oNode,dNode;
					oNode = (int)node[destination-1].path[origin-1][j][k];
					dNode = (int)node[destination-1].path[origin-1][j][k+1];
					dollarcost[j] += (vot*link[oNode][dNode][5]+link[oNode][dNode][6]);
				}
			}else{
				dollarcost[j] = Node.infinite;
			}
		}
		//Sorted according to dollar cost;
		for(int j=0;j<(Node.option_number-1);j++){
			for (int k=j+1;k<Node.option_number;k++){
				if (dollarcost[j]>dollarcost[k]){
					double tempcost;
					int tempindex;
					tempcost = dollarcost[j];
					dollarcost[j] = dollarcost[k];
					dollarcost[k] = tempcost;
					tempindex = costsort[j];
					costsort[j] = costsort[k];
					costsort[k] = tempindex;
				}
			}
		}
		//the first option is the best option, but it could be infinite if no option is available;
		mincost[0] = costsort[0];
		mincost[1] = dollarcost[0];
		return(mincost);
	}
	
	public void updateDestination(){
		//____________________________________________________
		//The assumption is the travel budget distribution is a natural result by combining the number of oppotunity available and the declining incentive to travel further;
		//Ten percent vacant jobs for change;
		int rNodeSeed = 1000;
		Random rNode = new Random(rNodeSeed);
		int nodeJob[];
		nodeJob =  new int[numNodes+1];
		for (int i=1;i<numNodes+1;i++){	//for destination i;
			nodeJob[i] = node[i].currentOppo;	//Sum of destination = avaible jobs;
		}
		int jobdemand[];
		int autoindex[][];
		jobdemand = new int[numNodes+1];
		autoindex = new int[numNodes+1][];
		
		int candidateNode[];
		candidateNode = new int[numAuto];		//Store the candidates node for auto i;
		double costDiffer;
		
		int iteration =1;
		int noofmoving = 0;
		double acceptableError = 0.3;	//the threshold beyond which travelers consider to change destination;
		double mincost[];
		while ((iteration < max_iteration) && (noofmoving>max_moving)){
			noofmoving = 0;
			for (int i=1;i<numNodes+1;i++){
				jobdemand[i] = 0;
			}
			for (int i=0;i<numAuto;i++){
				candidateNode[i] = Auto.nullindicator;
				mincost = min_generalcost(auto[i].origin,auto[i].destination,auto[i].vot);
				costDiffer = mincost[1] -auto[i].travelbudge;	//Only consider to change if current travel time is too large;
				if (costDiffer > acceptableError){
					int currentNode;
					currentNode = auto[i].destination;
					if (to[currentNode-1][0] >0){
						for (int j=1;j<to[currentNode-1][0]+1;j++){
							int candidateID;
							candidateID = to[currentNode-1][j];
							double tempDiffer;
							mincost = min_generalcost(auto[i].origin,candidateID,auto[i].vot);
							tempDiffer = mincost[1] - auto[i].travelbudge;
							if (tempDiffer < costDiffer){
								candidateNode[i] = candidateID;
								costDiffer = tempDiffer;
							}
						}
					}
				}else{
					//no incentive to change destinatioin;
				}
				if (candidateNode[i]!= Auto.nullindicator){	//If there is a candidate;
					jobdemand[candidateNode[i]]++;
				}
			}
			//After checking every auto, count demand for each node;
			int counter[];
			counter = new int[numNodes+1];
			for (int i=1;i<numNodes+1;i++){
				autoindex[i] = new int[jobdemand[i]];
				counter[i]=0;
			}
			for (int i=0;i<numAuto;i++){
				if (candidateNode[i]!= Auto.nullindicator){	//If there is a candidate;
					autoindex[candidateNode[i]][counter[candidateNode[i]]]=i;
					counter[candidateNode[i]]++;
				}
			}
			//Decide whether traveler will change their destination according to travel budget;
			double p1;
			double rP;
			for (int i=1;i<numNodes+1;i++){
				if (jobdemand[i]>0 && nodeJob[i]>0){
					if (jobdemand[i]>nodeJob[i]){
						p1 = (double)nodeJob[i]/jobdemand[i];
					}else{
						p1 =1;
					}
					for (int j=0;j<jobdemand[i];j++){
						rP = rNode.nextDouble();
						if ((rP<p1) && (nodeJob[i]>0)){
							nodeJob[auto[i].destination]++;
							auto[autoindex[i][j]].destination = i;	//Change the destination;
							nodeJob[i]--;
							noofmoving++;
						}
					}
				}
			}
			//after all decisions have been made;
			System.out.println("updateDestination, iteration:"+iteration+"\tnoofmoving:"+noofmoving);
			iteration++;
		}		
	}
	
	public void odEstimator(){
		estimatedOD = new int[numNodes+1][numNodes+1];
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				estimatedOD[i][j] = 0;
			}
		}
		for (int i=0;i<numAuto;i++){
			if (auto[i].destination>0) {estimatedOD[auto[i].origin][auto[i].destination]++;}
		}
		//Result of OD estimator
		System.out.println("End of OD estimator");
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				System.out.print(estimatedOD[i][j]);
				if (j < numNodes) {
					System.out.print("\t");				
				}else {
					System.out.print("\n");
				}
			}
		}
		od = estimatedOD;
	}
	public double odError(){
		double error=0;
		int oldEstimatedOD[][];
		oldEstimatedOD = new int[numNodes+1][numNodes+1];
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				oldEstimatedOD[i][j] = estimatedOD[i][j];
			}
		}
		odEstimator();
		double temperror;
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				temperror = Math.abs( estimatedOD[i][j]- oldEstimatedOD[i][j]);
				if (temperror > error){
					error = temperror;
				}
			}
		}
		return(error);
	}
	public void resetInfo(){
		//between years, after updated destination choice, erase route and path info which is no longer true;
		for (int i=0;i<numAuto;i++){
			auto[i].reset();
		}
	}
	//***********************************************
	public void printlink(){
		for (int i=0;i<numNodes;i++){
			System.out.print("From"+(i+1)+":");
			if (to[i][0]>0){
				for (int j=1;j<=to[i][0];j++){
					System.out.print("\t"+to[i][j]);
				}
			}
			System.out.print("\n");
		}
	}
	
	public void printautoroute(){
		for (int i=0;i<numAuto;i++){
			System.out.print("Auto"+(i+1)+"\tO"+auto[i].origin+"D"+auto[i].destination+"\tRoute:");
			int routelength = (int)auto[i].route_info[0];
			for (int j=0;j<routelength;j++){
				System.out.print(""+auto[i].route[j]);
			}
			System.out.print("\n");
		}
	}
	
	public void printnode(){
		for (int i=0;i<numNodes;i++){
			System.out.print("Node"+(i+1));
			for (int j=0;j<numNodes;j++){
				System.out.print("\n\tFrom"+(j+1)+"\t");
				for (int k=0;k<Node.option_number;k++){
					int pathlength;
					pathlength = (int)node[i].path_info[j][k][0];
					if (pathlength>1){
						System.out.print("\tRecord"+k+":");
						for (int l=0;l<pathlength;l++){
							System.out.print(""+node[i].path[j][k][l]);
						}
						System.out.print("\tcost:"+node[i].path_info[j][k][1]);
					}
				}
				System.out.print("\n");
			}
		}
	}
	
	public void shortestT(DirectedGraph dg){
		A = new float[numNodes+1][numNodes+1];
		float INF = 999999;
		int p[][];
		p = new int [numNodes+1][numNodes+1];
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				A[i][j] = INF;
				p[i][j] = 0;
			}
		}
		for (int i=1;i<numNodes+1;i++){
			A[i][i] = 0;
			p[i][i] = -1;
		}
		for (int i=0;i<numLinks;i++){
			int oNode;
			int dNode;
			oNode = dg.link[i].oNode;
			dNode = dg.link[i].dNode;
			A[oNode][dNode] = dg.link[i].currentT;
			p[oNode][dNode] = oNode;
		}
		for (int k=1;k<numNodes+1;k++){
			for (int i=1;i<numNodes+1;i++){
				for (int j=1;j<numNodes+1;j++){
					if (i!=k && j!=k){
						if (A[i][k]+A[k][j] < A[i][j]){
							A[i][j] = A[i][k] + A[k][j];
							p[i][j] = p[k][j];
						}
					}
				}
			}
		}
	}
	
	public void output(DirectedGraph dg){
		dg.vht = 0;
		dg.vkt = 0;
		dg.vekt = 0;
		dg.totalouttrips = 0;
		dg.acceJobs5 = 0;
		dg.acceJobs10 = 0;
		dg.acceJobs20 = 0;
		dg.acceWorkers5 = 0;
		dg.acceWorkers10 = 0;
		dg.acceWorkers20 = 0;
		dg.ODT = new float[numNodes+1][numNodes+1];
		dg.ShT = new float[numNodes+1][numNodes+1];
		dg.ODC = new int[numNodes+1][numNodes+1];
		dg.ODED = new float[numNodes+1][numNodes+1];
		dg.nodeAcceJobs5=new float[numNodes+1];
		dg.nodeAcceJobs10=new float[numNodes+1];
		dg.nodeAcceJobs20=new float[numNodes+1];
		dg.nodeAcceWorkers5 = new float[numNodes+1];
		dg.nodeAcceWorkers10 = new float[numNodes+1];
		dg.nodeAcceWorkers20 = new float[numNodes+1];
		
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				if (i==j){
					dg.ODT[i][j] = 0;
					dg.ShT[i][j] = 0;
					dg.ODC[i][j] = 0;
					dg.ODED[i][j] = 0;
				}else{
					dg.ODT[i][j] = 0;
					dg.ShT[i][j] = 9999;
					dg.ODC[i][j] = 0;
					dg.ODED[i][j] = (float)(Math.sqrt((dg.node[i].xCoord - dg.node[j].xCoord)*(dg.node[i].xCoord - dg.node[j].xCoord) + 
							(dg.node[i].yCoord - dg.node[j].yCoord)*(dg.node[i].yCoord - dg.node[j].yCoord)))/dg.LENGTHAJUSTFACTOR;
				}
			}
			dg.nodeAcceJobs5[i] = 0;
			dg.nodeAcceJobs10[i] = 0;
			dg.nodeAcceJobs20[i] = 0;
			dg.nodeAcceWorkers5[i] = 0;
			dg.nodeAcceWorkers10[i] = 0;
			dg.nodeAcceWorkers20[i] = 0;
		}
		
		for (int i=0;i<numAuto;i++){
			if (auto[i].status == 1){
				int oNode,dNode;
				oNode = auto[i].origin;
				dNode = auto[i].destination;
				float tript = 0;
				int routelength = (int)auto[i].route_info[0];
				if (routelength >0){
					dg.totalouttrips++;
				}
				for (int j=0;j<routelength-1;j++){
					tript += link[auto[i].route[j]][auto[i].route[j+1]][5];
					dg.vht += link[auto[i].route[j]][auto[i].route[j+1]][5];
					dg.vkt += link[auto[i].route[j]][auto[i].route[j+1]][1];
				}
				dg.ODT[oNode][dNode] += tript;
				dg.ODC[oNode][dNode]++;
				if (dg.ShT[oNode][dNode] > tript && routelength >0){
					dg.ShT[oNode][dNode] = tript;
				}
				dg.vekt = dg.vekt + dg.ODED[oNode][dNode]*1;	//Only one trip;
			}
		}
		
		long totalPeople = 0;
		long totalJobs = 0;
		for (int i=1;i<numNodes+1;i++){
			totalPeople = totalPeople + dg.node[i].numAuto;
			totalJobs = totalJobs + dg.node[i].numOppo;
			for (int j=1;j<numNodes+1;j++){
				if (dg.ShT[i][j]<9999)
				{
					if (dg.ShT[i][j]<=5)
					{
						dg.acceJobs5 = dg.acceJobs5 + dg.node[i].numAuto*dg.node[j].numOppo;
						dg.nodeAcceJobs5[i] = dg.nodeAcceJobs5[i] + dg.node[i].numAuto*dg.node[j].numOppo;
						dg.acceWorkers5 = dg.acceWorkers5 + dg.node[i].numOppo*dg.node[j].numAuto;
						dg.nodeAcceWorkers5[i] = dg.nodeAcceWorkers5[i] + dg.node[i].numOppo * dg.node[j].numAuto;
					}
					if (dg.ShT[i][j]<=10)
					{
						dg.acceJobs10 = dg.acceJobs10 + dg.node[i].numAuto*dg.node[j].numOppo;
						dg.nodeAcceJobs10[i] = dg.nodeAcceJobs10[i] + dg.node[i].numAuto*dg.node[j].numOppo;
						dg.acceWorkers10 = dg.acceWorkers10 + dg.node[i].numOppo*dg.node[j].numAuto;
						dg.nodeAcceWorkers10[i] = dg.nodeAcceWorkers10[i] + dg.node[i].numOppo * dg.node[j].numAuto;
					}
					if (dg.ShT[i][j]<=20)
					{
						dg.acceJobs20 = dg.acceJobs20 + dg.node[i].numAuto*dg.node[j].numOppo;
						dg.nodeAcceJobs20[i] = dg.nodeAcceJobs20[i] + dg.node[i].numAuto*dg.node[j].numOppo;
						dg.acceWorkers20 = dg.acceWorkers20 + dg.node[i].numOppo*dg.node[j].numAuto;
						dg.nodeAcceWorkers20[i] = dg.nodeAcceWorkers20[i] + dg.node[i].numOppo * dg.node[j].numAuto;
					}
				}
			}
		}
		dg.acceJobs5 = dg.acceJobs5 / totalPeople;
		dg.acceJobs10 = dg.acceJobs10 / totalPeople;
		dg.acceJobs20 = dg.acceJobs20 / totalPeople;
		dg.acceWorkers5 = dg.acceWorkers5/totalJobs;
		dg.acceWorkers10 = dg.acceWorkers10/totalJobs;
		dg.acceWorkers20 = dg.acceWorkers20/totalJobs;
		for (int i=1;i<numNodes+1;i++){
			if (dg.node[i].numAuto !=0){
				dg.nodeAcceJobs5[i] = dg.nodeAcceJobs5[i]/dg.node[i].numAuto;
				dg.nodeAcceJobs10[i] = dg.nodeAcceJobs10[i]/dg.node[i].numAuto;
				dg.nodeAcceJobs20[i] = dg.nodeAcceJobs20[i]/dg.node[i].numAuto;
			}else{
				dg.nodeAcceJobs5[i] = 0;
				dg.nodeAcceJobs10[i] = 0;
				dg.nodeAcceJobs20[i] = 0;
			}
			if (dg.node[i].numOppo!=0)
			{
				dg.nodeAcceWorkers5[i] = dg.nodeAcceWorkers5[i]/dg.node[i].numOppo;
				dg.nodeAcceWorkers10[i] = dg.nodeAcceWorkers10[i]/dg.node[i].numOppo;
				dg.nodeAcceWorkers20[i] = dg.nodeAcceWorkers20[i]/dg.node[i].numOppo;
			}else
			{
				dg.nodeAcceWorkers5[i] = 0;
				dg.nodeAcceWorkers10[i] = 0;
				dg.nodeAcceWorkers20[i] = 0;
			}
		}
		
		dg.vht = dg.vht/60; //From mins to hours;
		for (int i=1;i<numNodes+1;i++){
			for (int j=1;j<numNodes+1;j++){
				if (dg.ODC[i][j]>0){
					dg.ODT[i][j] = dg.ODT[i][j]/dg.ODC[i][j];
				}
			}
		}
		dg.averagelength = dg.vkt/dg.totalouttrips;
		dg.averagetime = dg.vht/dg.totalouttrips;
		dg.averageEuclideanLength = dg.vekt/dg.totalouttrips;
		
		for (int i=0;i<numLinks;i++){
			int oNode;
			int dNode;
			oNode = dg.link[i].oNode;
			dNode = dg.link[i].dNode;
			dg.link[i].flow = (int)link[oNode][dNode][4];
			dg.link[i].currentT = (float)link[oNode][dNode][5];
			dg.arc[oNode][dNode].flow = (int)link[oNode][dNode][4];
			dg.arc[oNode][dNode].traveltime = (float)link[oNode][dNode][5];
			if (dg.arc[oNode][dNode].capacity >0){
				dg.arc[oNode][dNode].vc = dg.arc[oNode][dNode].flow/dg.arc[oNode][dNode].capacity;
			}else{
				dg.arc[oNode][dNode].vc = 0;
			}
			dg.link[i].vc = dg.arc[oNode][dNode].vc;
		}
		
		
	}
	//*************************************************
//	Read integer or read float number
	class ReadANumber{
		
		public int end;
		
		ReadANumber() {
			end = 0;
		}
		
		int readint(InputStream f)
			throws IOException
		 {
			String msg = "";
			int i;
			do {
				i=f.read();
				//if(i != -1 && i != 13  && i!=32 && i!=9 )
				if(i>47 && i<58 || i==(int)'.'|| i==(int)'-')
				//////  32 ---- space, 13 ----- new Line
				msg += (char)i;
				
				//System.out.print("\tmsg="+msg);
			} while(i>47 && i<58 ||i==(int)'.'|| i==(int)'-');
			
			end = i;
		
			try {
				if(msg != null)  {
					//i = Integer.parseInt(msg);
					//return( i );
					if(msg.charAt( 0)=='-')return(-1*Integer.parseInt(msg.substring( 1)));
					else return(Integer.parseInt(msg) );

				}
				else
					return ( 0 );
			}	catch(NumberFormatException e) {
				System.out.println("NumberFormatException integer.");
				return ( 0 );
			}
		}
		
		
		
		float readfloat(InputStream f) 
			throws IOException
		{
			String msg = "";
			int i;
			
			do {
				
				i = f.read();
				//System.out.print("\ti="+i);
				//if(i != -1 && i != 13 && i != 32 && i!=9 && i!=10)
				if(i>47 && i<58 || i==(int)'.' ||i=='-')
				msg += (char)i;
			} while(i>47 && i<58 || i==(int)'.'||i=='-');
			
			end = i;
		
			try {
				if(msg.charAt( 0) != 0)  {
					if(msg.charAt( 0)=='-')return(-1*Float.valueOf(msg.substring( 1)).floatValue());
					else return( Float.valueOf(msg).floatValue() );
				}	
				else
					return ( 0 );
			}	catch(NumberFormatException e) {
				System.out.println("NumberFormatException float");
				return (0);
			}
		}
		
	}
//	///////////////////////    End of ReadANumber class
}
