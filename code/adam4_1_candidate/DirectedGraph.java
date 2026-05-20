import java.io.*;
import java.text.*;
import java.awt.*;
import java.applet.*;
import java.net.*;   
import java.util.*;
import java.lang.*;


public class DirectedGraph {
	
	int numNodes;
	int numWorkers;
	int numAutos;
	int numOppo;
	int numLink;
	float beta = 0;
	static final int Large = 100;
	public float LENGTHAJUSTFACTOR = 5;
	URL url;
	Evolve evolve;
	
	float alphaBPR,betaBPR,theta,generationFactor,attractionFactor;
	public Node tempnode[];
	public Arc temparc[][];
	public int deleteLinkCounter;
	final static float CperLane = 1200;
	
	float vht,vkt,vekt;	//Total OD Euclidean distance;
	float ODT[][],ShT[][];
	float ODED[][];	//OD Euclidean distance;
	int ODC[][];
	float averagelength,averagetime,averageEuclideanLength;
	int totalouttrips;
	float acceJobs5,acceJobs10,acceJobs20;
	float acceWorkers5,acceWorkers10,acceWorkers20;
	float nodeAcceJobs5[],nodeAcceJobs10[],nodeAcceJobs20[];
	float nodeAcceWorkers5[],nodeAcceWorkers10[],nodeAcceWorkers20[];
	
	public float cost;
	public float expansionLength;
	public float totalBenefit;
	public float BCRatio;
	public float budgetUsed;
	public float sCost;
	final private float VOT = (float)0.15;
	
	
	public float link_info[][];//brackets:1-link index; 2-attribute index 
	///link attributes
	//0-link ID (from 1)
	//1-Origin node ID
	//2-Destination node ID
	//3-link type (functional class)
	//4-length
	//5-free flow speed
	//6-number of lanes
	//7-capacity
	//8-traffic flow (AM peak hour)veh/hour?
	//9-BPR travel time
	//10-toll rate;

	Demo sd;
	Node node[];
	Arc arc[][];
	Link link[];
	
	public DirectedGraph(String inputFile,URL url, boolean linkInforInclude,Demo sd) throws IOException{
		this.sd = sd;
		this.url = url;
		System.out.println("Evolve loading url:"+url+" File"+inputFile);
		sd.dp.showStatus.setText("Evolve loading url:"+url);
		
		ReadANumber read = new ReadANumber();
		InputStream fin = null;
		
		////read link information
		try {
			fin = new URL(url, inputFile).openStream();
		} catch(FileNotFoundException e) {
			System.out.println("From DirectedGraph class: Exception Occured!!!!");
			sd.dp.showStatus.setText("fin open fail");
			return;
		}
		numNodes = read.readint(fin);
		System.out.println(numNodes);
		numWorkers = read.readint(fin);
		System.out.println(numWorkers);
		beta = read.readfloat(fin);
		System.out.println(beta);
		
////	  Initializing the variables
		  node = new Node[numNodes + 1];
		  arc = new Arc[numNodes + 1][numNodes + 1];
		
		  for(int i = 1; i < numNodes + 1; i++) {
			  int nodeId, nodeWorkers,nodeJobs,numDemandNodes;
			  int xCoord,yCoord;
			  int demandNodes[];
			  
			  if(read.end == -1) break;
			  nodeId= read.readint(fin);
			  System.out.print(nodeId + " ");
			  if(read.end == -1) break;
			  nodeWorkers= read.readint(fin);
			  System.out.print(nodeWorkers + " ");
			  if(read.end == -1) break;
			  nodeJobs = read.readint(fin);
			  System.out.print(nodeJobs + " ");
			  if(read.end == -1) break;
			  xCoord = read.readint(fin);
			  System.out.print(xCoord + " ");
			  if(read.end == -1) break;
			  yCoord = read.readint(fin);
			  System.out.print(yCoord + " ");

			  if(read.end == -1) break;
			  numDemandNodes = read.readint(fin);
			  System.out.print(numDemandNodes + " ");
			  demandNodes = new int[numDemandNodes];
			  for(int j =0; j<numDemandNodes; j++) {
				  if(read.end == -1) break;
				  int temp;
				  temp = read.readint(fin);
				  demandNodes[j] = temp;
				  System.out.print(demandNodes[j] + " ");
				  if(read.end == -1) break;
				  int length;
				  length = read.readint(fin);
				  System.out.print(length + " ");
				  arc[nodeId][temp] = new Arc(length);				
			  }
			  System.out.println();
			  node[i] = new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
								  numDemandNodes, demandNodes);  	
		  }
		  
		  int linkID;
		  int oNode,dNode;
		  float capacity,fft;
		  int numLanes;
		  numLink = read.readint(fin);
		  link = new Link[numLink];
		  for (int i=0;i< numLink;i++)
		  {
			  	linkID=read.readint(fin);
			  	oNode =  read.readint(fin);
				dNode =  read.readint(fin);
				numLanes = read.readint(fin);
				capacity = read.readfloat(fin);
				fft =  read.readfloat(fin);
				link[i]=new Link(linkID,oNode,dNode,numLanes,capacity,fft);
		  }
		fin.close();
		initialArc();
		System.out.println("End of dg constructor");
	}
	
	public DirectedGraph(String inputFile,URL url, Demo sd) throws IOException{
		this.sd = sd;
		this.url = url;
				
		ReadANumber read = new ReadANumber();
		InputStream fin = null;
		
		inputFile = "TC_nodeinfo.txt";
		System.out.println("Evolve loading url:"+url+" File"+inputFile);
		sd.dp.showStatus.setText("Evolve loading url:"+url);
		////read node information
		try {
			fin = new URL(url, inputFile).openStream();
		} catch(FileNotFoundException e) {
			System.out.println("From DirectedGraph class: Exception Occured!!!!");
			sd.dp.showStatus.setText("fin open fail");
			return;
		}
		read.readint(fin);	//Original largest ID;
		numNodes = read.readint(fin);
		node = new Node[numNodes + 1];
		read.readint(fin);
		for(int i = 1; i < numNodes + 1; i++) {
			int nodeId;
			int xCoord,yCoord;
			nodeId = read.readint(fin);
			read.readint(fin);	//Original ID;
			xCoord = (int)((read.readfloat(fin)/250*106000+419230-475933)/9500*250);
			if (xCoord<0){
				xCoord = 0;
			}else if (xCoord >250){
				xCoord = 250;
			}
			yCoord = (int)((read.readfloat(fin)/250*106000+4923984-4977232)/9500*250);
			if (yCoord<0){
				yCoord = 0;
			}else if (yCoord >250){
				yCoord = 250;
			}
			node[i] = new Node(nodeId, xCoord,yCoord);
		}
		fin.close();
		//Read link info;
		inputFile = "TC_linkinfo.txt";
		System.out.println("Evolve loading url:"+url+" File"+inputFile);
		sd.dp.showStatus.setText("Evolve loading url:"+url);
		try {
			fin = new URL(url, inputFile).openStream();
		} catch(FileNotFoundException e) {
			System.out.println("From DirectedGraph class: Exception Occured!!!!");
			sd.dp.showStatus.setText("fin open fail");
			return;
		}
		
		int linkID;
		int oNode,dNode;
		float capacity,fft;
		float length,ffs;
		int functional;
		int numLanes;
		numLink = read.readint(fin);
		read.readint(fin);
		link = new Link[numLink];
		
		int tempnumofdemandNode[];
		int tempdemandNode[][];
		tempnumofdemandNode = new int[numNodes+1];
		tempdemandNode = new int[numNodes+1][10];
		for(int i = 1; i < numNodes + 1; i++) {
			tempnumofdemandNode[i]=0;
		}
		for (int i=0;i< numLink;i++)
		{
			linkID=read.readint(fin);
			oNode =  read.readint(fin);
			dNode =  read.readint(fin);
			functional = read.readint(fin); //Functional class;
			length=read.readfloat(fin); //Length in miles;
			ffs=read.readfloat(fin);	//Free flow speed;	
			numLanes = read.readint(fin);
			capacity = read.readfloat(fin);
			if (ffs>0){
				fft = length/ffs;
			}else{
				fft = 999;
			}
			link[i]=new Link(linkID,oNode,dNode,numLanes,capacity,fft,functional);
			tempnumofdemandNode[oNode]++;
			tempdemandNode[oNode][tempnumofdemandNode[oNode]-1] = dNode;
		  }
		fin.close();
		for(int i = 1; i < numNodes + 1; i++) {
			node[i].numDemandNodes = tempnumofdemandNode[i];
			node[i].demandNodes = new int[node[i].numDemandNodes];
			for (int j=0;j<node[i].numDemandNodes;j++){
				node[i].demandNodes[j] = tempdemandNode[i][j];
			}
		}
		newinitialArc();
		System.out.println("End of dg constructor");
	}
	
	public void iteration(){
		evolve = null;
		try{
			evolve = new Evolve(this,sd,theta,beta);
		}catch (IOException e) {
			System.out.println("Error in Constructor Evolve");
		}
//		evolve.shortestT(this);
		evolve.odInitialization();
		evolve.odEstimator();
		evolve.initialization();
		evolve.iteration();
		evolve.output(this);
	}
	
	public void inforForDispay()
	{
		
	}
	
	public void setGlobalVariable(float alphaBPR,float betaBPR,float beta,float theta,float generationFactor,float attractionFactor)
	{
		this.alphaBPR = alphaBPR;
		this.betaBPR = betaBPR;
		this.beta = beta;
		this.theta = theta;
		this.generationFactor = generationFactor;
		this.attractionFactor = attractionFactor;
		
		int currentAutos,numOppos;
		currentAutos =0;
		numOppos = 0;
		for (int i=1;i<numNodes+1;i++)
		{
			node[i].numAuto = Math.round(generationFactor*node[i].nodeWorkers);
			node[i].currentAuto = node[i].numAuto;
			node[i].numOppo = Math.round(attractionFactor*node[i].originalJobs);
			node[i].currentOppo = node[i].numOppo;
			
			currentAutos +=node[i].numAuto;
			numOppos = numOppos + node[i].numOppo;
		}
		numAutos = currentAutos;
		numOppo = numOppos;
	}
	
	public void reinitialization(int chooseNodeID,float beta)
	{
		System.out.println("_________________reinitialization Start_______"+numNodes+" "+numWorkers+" "+numLink);
		int nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
					numDemandNodes, demandNodes[];
		int temp, workerCounter = 0;
		int length;
		
					
		tempnode = node;
		temparc = arc;
		
		//  Initializing the variables
			  node = new Node[numNodes + 1];
			  arc = new Arc[numNodes + 1][numNodes + 1];
			  
		for(int i = 1; i < numNodes + 1; i++) 
		{
			nodeId = tempnode[i].nodeId;
			nodeWorkers = tempnode[i].nodeWorkers;
			nodeJobs = tempnode[i].originalJobs;
			xCoord = tempnode[i].xCoord;
			yCoord = tempnode[i].yCoord;
			numDemandNodes = tempnode[i].numDemandNodes;
			demandNodes = new int[numDemandNodes];
			for(int j =0; j<numDemandNodes; j++) {
				temp = tempnode[i].demandNodes[j];
				demandNodes[j] = temp;
				length = (int)Math.sqrt((tempnode[i].xCoord-tempnode[temp].xCoord)^2+(tempnode[i].yCoord-tempnode[temp].yCoord)^2);
				arc[nodeId][temp] = new Arc(length);
				
			}
			node[i] = new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
											numDemandNodes, demandNodes);
			
//			for(int k = 0; k < nodeWorkers; k++){
//				worker[workerCounter] =  new Worker(nodeId, numNodes);
//				workerCounter++;
//			}
		}
		//////ReInitial Links
		int newNumLinks = numLink - deleteLinkCounter;
		int linkCounter=0;
		Link tempLink[];
		tempLink = new Link[newNumLinks];
		int linkId,oNode, dNode, numLanes;
		float capacity, fft;
		
		for (int i=0;i< numLink;i++)
		{
			if ((link[i].oNode!=chooseNodeID) && (link[i].dNode!=chooseNodeID))
			{
				numLanes = link[i].numLanes;
				capacity = link[i].capacity;
				fft = link[i].fft;
				if (link[i].oNode >chooseNodeID)
				{
					oNode = link[i].oNode -1;
				}else
				{
					oNode = link[i].oNode;
				}
				if (link[i].dNode >chooseNodeID)
				{
					dNode = link[i].dNode -1;
				}else
				{
					dNode = link[i].dNode;
				}
				System.out.println("/////////////Reinitialization Link"+linkCounter+" "+oNode+" "+dNode+" "+numLanes+" "+capacity+" "+fft);
				tempLink[linkCounter]= new Link(linkCounter,oNode,dNode,numLanes,capacity,fft);
				linkCounter++;
			}else
			{
				///////////////////Link deleted and overwrittten;
			}
		}
		link = tempLink;
		numLink = newNumLinks;
	////////////////End of reinitialization	 
	}
	
//	Delete a Link
	public void deleteLink(int originNodeID, int demandNodeID, float beta)
	{
		System.out.println("___________Delete Link Start____________"+"O"+originNodeID+"D"+demandNodeID);
		int nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
							numDemandNodes, demandNodes[];
				int endID = node[originNodeID].demandNodes[demandNodeID];
				nodeId=node[originNodeID].nodeId;
				nodeWorkers=node[originNodeID].nodeWorkers;
				nodeJobs = node[originNodeID].originalJobs;
				xCoord = node[originNodeID].xCoord;
				yCoord = node[originNodeID].yCoord;
				numDemandNodes = node[originNodeID].numDemandNodes;
				demandNodes = new int[numDemandNodes-1];
				int counter = 0;
				for (int j=0;j<numDemandNodes;j++)
				{
					if (j != demandNodeID)
					{
//						System.out.println("counter"+counter+"j"+j+"demandNode"+node[originNodeID].demandNodes[j]);
						demandNodes[counter]=node[originNodeID].demandNodes[j];
						counter++;
					}
				}
				numDemandNodes=numDemandNodes-1;
				node[originNodeID] = new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
															numDemandNodes, demandNodes);
		System.out.println("deleteLink from"+originNodeID+" numDemmandNodes"+numDemandNodes+" demandNodes"+demandNodes);
		//////////Delete one link
		int newNumLinks = numLink -1;
		int linkCounter = 0;
		Link tempLink [];
		tempLink = new Link[newNumLinks];
		for (int i=0;i<numLink;i++)
		{
			if ((link[i].oNode == originNodeID) && (link[i].dNode == endID))
			{
				System.out.println("///////////////////////Delete Link Found////////Link"+i+" "+link[i].oNode+" "+link[i].dNode);
			}else
			{
				tempLink[linkCounter]=link[i];
				linkCounter++;
			}
			
		}
		link = tempLink;
		numLink = newNumLinks;
	}
	
	public void deleteNode(int chooseNodeID,float beta)
	{
		System.out.println("_____________________deleteNode Start_________"+chooseNodeID);
		int nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
							numDemandNodes, demandNodes[];
		int temp, workerCounter = 0;
		int length;
		int newnumNodes,newnumWorkers;
		
		newnumNodes=numNodes-1;
		newnumWorkers=numWorkers-node[chooseNodeID].nodeWorkers;
		tempnode=node;
		node = new Node[newnumNodes + 1]; 
		int newNodeCounter=1,newWorkerCounter=0;
		deleteLinkCounter = 0;
		
		for (int i=1;i<numNodes+1;i++)
		{
			if(i!=chooseNodeID)
			{
				nodeId = tempnode[i].nodeId;
			//The node Id has to be rearranged	
				if (nodeId>chooseNodeID)
				{
					nodeId=nodeId-1;
				}
				nodeWorkers = tempnode[i].nodeWorkers;
				nodeJobs = tempnode[i].originalJobs;
				xCoord = tempnode[i].xCoord;
				yCoord = tempnode[i].yCoord;
				numDemandNodes = tempnode[i].numDemandNodes;
				
				boolean linkcancel=false;
				for (int j=0;j<numDemandNodes;j++)
				{
					if (tempnode[i].demandNodes[j]==chooseNodeID)
					{
						linkcancel=true;
						deleteLinkCounter = deleteLinkCounter + 1;
					}
				}
				if (linkcancel)
				{
					numDemandNodes=numDemandNodes-1;
					demandNodes = new int[numDemandNodes];
					int counter=0;
					for(int j=0;j<numDemandNodes+1;j++)
					{
						if (tempnode[i].demandNodes[j]!=chooseNodeID)
						{
							demandNodes[counter]=tempnode[i].demandNodes[j];
							if (demandNodes[counter]>chooseNodeID)
							{
								demandNodes[counter]=demandNodes[counter]-1;
							}
							counter++;
						}
					}
				}
				else
				{
					demandNodes = new int[numDemandNodes];
					demandNodes = tempnode[i].demandNodes;
					for (int j=0;j<numDemandNodes;j++)
					{
						if (demandNodes[j]>chooseNodeID)
						{
							demandNodes[j]=demandNodes[j]-1;
						}
					}
				}
				node[newNodeCounter] = new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
															numDemandNodes, demandNodes);
				newNodeCounter++;
			}else
			{
				deleteLinkCounter = deleteLinkCounter+tempnode[i].numDemandNodes;
			}
		}
		numNodes=newnumNodes;
		numWorkers=newnumWorkers;
		System.out.println("___________________Delete Node End____________"+"numNode"+numNodes+"numWorkers"+numWorkers);
	}
	
//	Add a node in the network
	public void addNode(int xCoord,int yCoord,int nodeWorkers, int nodeJobs,float beta)
	{
		int newnumNodes = numNodes+1;
		int nodeId = newnumNodes;
		int numDemandNodes=0;
		int demandNodes[]=null;
		
		tempnode = node;
		node = new Node[newnumNodes+1];
		for (int i=1;i< numNodes+1;i++)
		{
			node[i]=tempnode[i];
		}
		node[newnumNodes]=new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
												numDemandNodes, demandNodes);
		numNodes=newnumNodes;
		numWorkers=numWorkers+nodeWorkers;
		///////////////Rebuild Arc;
		initialArc();
		
	System.out.println("__________________End addNode_____________"+numNodes+"X"+node[numNodes].xCoord+"Y"+node[numNodes].yCoord);	
	}
//	Add a link in the network
	public void addLink(int origin, int destination, float beta,int numLane,float fft)
	{
		int nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
									numDemandNodes, demandNodes[];
		Node temp = node[origin];
		nodeId = temp.nodeId;
		nodeWorkers=temp.nodeWorkers;
		nodeJobs = temp.originalJobs;
		xCoord = temp.xCoord;
		yCoord = temp.yCoord;
		numDemandNodes = temp.numDemandNodes+1;
		demandNodes = new int[numDemandNodes];
		for (int i=0;i<temp.numDemandNodes;i++)
		{
			demandNodes[i]=temp.demandNodes[i];
		}
		demandNodes[numDemandNodes-1]=destination;
		
		node[origin] = new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
																	numDemandNodes, demandNodes);
		int tempnumLink;
		tempnumLink = numLink;
		numLink = numLink+1;
		Link tempLink[];
		tempLink = link;
		link = new Link[numLink];
		for (int i=0;i<numLink-1;i++)
		{
			link[i]=tempLink[i];
		}
		int linkID = numLink-1;
		float capacity = (float)CperLane*numLane;
		link[numLink-1] = new Link(linkID,origin,destination,numLane,capacity,fft);
		
		//////////////Rebuild Arc
		initialArc();
	}
	
	public void initialArc()
	{
		arc = new Arc[numNodes+1][numNodes+1];
		for (int i=0;i<numLink;i++)
		{
			arc[link[i].oNode][link[i].dNode] = new Arc(link[i].capacity,link[i].fft,link[i].numLanes,link[i].length);
		}
	}
	
	public void newinitialArc()
	{
		arc = new Arc[numNodes+1][numNodes+1];
		for (int i=0;i<numLink;i++)
		{
			arc[link[i].oNode][link[i].dNode] = new Arc(link[i].capacity,link[i].fft,link[i].numLanes,link[i].length,link[i].functional);
		}
	}
	
	public void benefitCost(float constructionCost,float autoOccupancy,float peakHourRate,URL url)
	{
//////////Total Cost;
		if (numLink == 76){
			System.out.println("////////////////////////////////////Benefit Cost Analysis");
			try {
					loadStandardPerformance(url);
			} catch (IOException ioe){
					System.out.println("Load Standard Information Wrong");
			}
			cost = 0;
			totalBenefit = 0;
			budgetUsed = 0;
			BCRatio = 0;
			for (int i=0;i<numLink;i++)
			{
				cost = cost+ link[i].length*link[i].numLanes*constructionCost;
				totalBenefit = totalBenefit + (link[i].sTime - link[i].currentT)*(link[i].flow + link[i].sFlow)/2;
	//			totalBenefit = totalBenefit + (link[i].sTime*link[i].sFlow-link[i].currentT*link[i].flow);
	//			System.out.println("Link"+i+" "+link[i].currentT+" "+link[i].sTime+" "+link[i].flow+" "+link[i].sFlow);
			}
			budgetUsed = cost - sCost;
			expansionLength = budgetUsed/constructionCost;
			totalBenefit = totalBenefit*VOT*autoOccupancy;   ////Peak Hour Benefit;
			totalBenefit = totalBenefit*6*365*20; ////20 years benefit;(surpose 6 peak hours);
			if (budgetUsed>0){
				BCRatio = totalBenefit/budgetUsed;
			}else{
				BCRatio = -1;
			}
			System.out.println("Total Expense:"+cost+"\tCost"+budgetUsed+"\tRatio"+BCRatio);
		}else{
			cost = 0;
			totalBenefit = 0;
			budgetUsed = 0;
			BCRatio = 0;
			System.out.println("Benefit Cost Analysis canceled because the total number of link has been modified.");
		}
	}
	
	public void loadStandardPerformance(URL url) throws IOException
	{
		String fileStandard = "sStatistics.txt";
		URL surl;
		surl = url;
		InputStream fin = null;
		ReadANumber read = new ReadANumber();
		try{
			fin = new URL(surl, fileStandard).openStream();
		  }catch (FileNotFoundException e) {
			System.out.println("Load Standard Performance File Error");
			sd.dp.showStatus.setText("standard performance file not found");
			  return;
		  }
		
		  int oNode,dNode;
			int sFlow;
			float sTime;
			boolean linkFound;
			for (int i=0;i<numLink;i++)
			{
				oNode = read.readint(fin);
				dNode = read.readint(fin);
				sFlow = read.readint(fin);
				sTime = read.readfloat(fin);
				int j=0;
				linkFound = false;
				while ((linkFound == false) && (j<numLink))
				{
					if ((link[j].oNode == oNode) &&(link[j].dNode == dNode))
					{
						link[j].sFlow = sFlow;
						link[j].sTime = sTime;
						linkFound = true;
					}
					j++;
				}
				if (linkFound == false)
				{
					sd.dp.showStatus.setText("standard performance file: searching link failure");
					return;
				}
			}
			sCost = read.readfloat(fin);
//			System.out.print("Loading Standard Construction Cost:"+sCost);
	}
	
	public boolean loadNetwork(String s)
	{
		boolean loadsuccess = false;
		int nodeId,xCoord,yCoord,nodeWorkers,nodeJobs,numDemandNodes,length;
		int [] demandNodes;
		int temp;
		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(s);
		try{
			numNodes = Integer.parseInt(tokenizer.nextToken());
			System.out.println(numNodes);
			numWorkers = Integer.parseInt(tokenizer.nextToken());
			System.out.println(numWorkers);
			beta = Float.valueOf(tokenizer.nextToken()).floatValue();
			System.out.println(beta);
			
			node = new Node[numNodes + 1];
			arc = new Arc[numNodes + 1][numNodes + 1];
			
			for(int i = 1; i < numNodes + 1; i++) {
					  nodeId= Integer.parseInt(tokenizer.nextToken());
					  System.out.print(nodeId + " ");
					  nodeWorkers= Integer.parseInt(tokenizer.nextToken());
					  System.out.print(nodeWorkers + " ");
					  nodeJobs = Integer.parseInt(tokenizer.nextToken());
					  System.out.print(nodeJobs + " ");
					  xCoord = Integer.parseInt(tokenizer.nextToken());
					  System.out.print(xCoord + " ");
					  yCoord = Integer.parseInt(tokenizer.nextToken());
					  System.out.print(yCoord + " ");

					  numDemandNodes = Integer.parseInt(tokenizer.nextToken());
					  System.out.print(numDemandNodes + " ");
					  demandNodes = new int[numDemandNodes];
					  for(int j =0; j<numDemandNodes; j++) {
						  temp = Integer.parseInt(tokenizer.nextToken());
						  demandNodes[j] = temp;
						  System.out.print(demandNodes[j] + " ");
						  length = Integer.parseInt(tokenizer.nextToken());
						  System.out.print(length + " ");
						  arc[nodeId][temp] = new Arc(length);				
					  }
					  System.out.println();
					  node[i] = new Node(nodeId, nodeWorkers, nodeJobs, xCoord, yCoord, 
										  numDemandNodes, demandNodes);
				}
				
			int linkID;
			int oNode,dNode;
			float capacity,fft;
			int numLanes;
			numLink = Integer.parseInt(tokenizer.nextToken());
			System.out.println("________________________numLink"+numLink);
			link = new Link[numLink];
				for (int i=0;i< numLink;i++)
				{
					linkID=Integer.parseInt(tokenizer.nextToken());
					oNode =  Integer.parseInt(tokenizer.nextToken());
					dNode =  Integer.parseInt(tokenizer.nextToken());
					numLanes = Integer.parseInt(tokenizer.nextToken());
					capacity = Float.valueOf(tokenizer.nextToken()).floatValue();
					fft =  Float.valueOf(tokenizer.nextToken()).floatValue();
									//	capacity = (float) CperLane*numLanes;
					link[i]=new Link(linkID,oNode,dNode,numLanes,capacity,fft);
					System.out.println(""+linkID+" "+oNode+" "+dNode+" "+numLanes+" "+capacity+" "+fft);
				}
				
				loadsuccess = true;
		}catch (Exception e){
			System.out.println(""+e.getMessage());
			loadsuccess = false;
		}
		return(loadsuccess);
	}
	
	public void save(File saveFile)
	{
		FileOutputStream out= null;
		try{
			out= new FileOutputStream(saveFile);
		}catch(Exception e)
		{
			System.out.println("Unable to open file");
			return;
		}
		PrintStream psOut=new PrintStream(out);
		
		String outstring="";
		outstring += numNodes;
		outstring +="\n";
		outstring += numWorkers;
		outstring +="\n";
		outstring +=beta;
		outstring +="\n";
		for (int i=1;i<numNodes+1;i++)
		{
			outstring +=node[i].nodeId;
			outstring +="\t";
			outstring +=node[i].nodeWorkers;
			outstring +="\t";
			outstring +=node[i].originalJobs;
			outstring +="\t";
			outstring +=node[i].xCoord;
			outstring +="\t";
			outstring +=node[i].yCoord;
			outstring +="\t";
			outstring +=node[i].numDemandNodes;
			
			for (int j=0;j<node[i].numDemandNodes;j++)
			{
				outstring +="\t";
				outstring +=node[i].demandNodes[j];
				outstring +="\t";
				outstring +=(int)(arc[i][node[i].demandNodes[j]].fft);
//				System.out.println("/////////////////////arc"+i+node[i].demandNodes[j]+" "+arc[i][node[i].demandNodes[j]]);
//				outstring +="\t";				
			}
			outstring +="\n";
		
		}
	outstring +=numLink;
	outstring +="\n";
		for (int i=0;i<numLink;i++)
		{
			outstring +=link[i].linkID;
			outstring +="\t";
			outstring +=link[i].oNode;
			outstring +="\t";
			outstring +=link[i].dNode;
			outstring +="\t";
			outstring +=link[i].numLanes;
			outstring +="\t";
			outstring +=link[i].capacity;
			outstring +="\t";
			outstring +=link[i].fft;
			outstring +="\n";
		}
		psOut.print(outstring);
		psOut.close();
	}
	
	class Node {
		public int nodeWorkers;
		public int originalWorkers;
		public int originalJobs;
		public int xCoord;
		public int yCoord;
		
		public int nodeId;
		public int currentWorkers;
		public int currentJobs;
		public int numDemandNodes;
		public int demandNodes[];
		
		public int numAuto;
		public int currentAuto;
		public int numOppo;
		public int currentOppo;
		
		public Node(int nodeId, int nodeWorkers,int nodeJobs, int xCoord, int yCoord,
				 int numDemandNodes, int[] demandNodes){
			this.nodeWorkers=nodeWorkers;
			this.nodeId = nodeId;
			this.originalJobs = nodeJobs;
			this.currentJobs = nodeJobs;
			this.xCoord = xCoord;
			this.yCoord = yCoord;
			this.numDemandNodes = numDemandNodes;
			
			this.demandNodes = new int[numDemandNodes];
			for(int i = 0; i < numDemandNodes; i++){
				this.demandNodes[i] = demandNodes[i];
			}			
		}
		
		public Node(int nodeId, int xCoord, int yCoord){
			this.nodeId = nodeId;
			this.xCoord = xCoord;
			this.yCoord = yCoord;
		}
	}
	
	class Arc {
		public int flow;
		public float traveltime;
		public float capacity;
		public float fft;
		public float length;
		public int numLanes;
		static final int CperLane = 1200;
		public float vc;
		public float tempfft;
		public int functional;

		static final int MULTIPLIER = 2;
		public Arc(int length){
			this.length = length;
		}
		public Arc(float capacity, float fft, int numLanes, float length)
		{
			this.flow =0;
			this.capacity = capacity;
			this.fft = fft;
			this.traveltime = fft;
//			this.length = (int)(fft*MULTIPLIER);
			this.length = length;
//			this.numLanes = Math.round(capacity/CperLane);
			this.numLanes = numLanes;
			this.vc = 0;
			this.tempfft = 0;  
		}
		
		public Arc(float capacity, float fft, int numLanes, float length,int functional)
		{
			this.flow =0;
			this.capacity = capacity;
			this.fft = fft;
			this.traveltime = fft;
//			this.length = (int)(fft*MULTIPLIER);
			this.length = length;
//			this.numLanes = Math.round(capacity/CperLane);
			this.numLanes = numLanes;
			this.vc = 0;
			this.tempfft = 0;
			this.functional = functional;
		}
	}
	
	class Link extends LinkedList	{
		public int linkID,oNode,dNode;
		public float capacity;
		public float fft;
		public float currentT;
		public int numLanes;
		public int flow;
		public float length;
		public float vc;
		public int sFlow;
		public float sTime;
		public int functional;
		
		
		public Link (int linkID, int oNode,int dNode,int numLanes,float capacity,float fft)
		{
			this.linkID = linkID;
			this.oNode = oNode;
			this.dNode = dNode;
			this.numLanes = numLanes;
			this.capacity = capacity;
			this.fft = fft;
			this.currentT = fft;
			this.flow = 0;
			this.vc = 0;
			this.sFlow = 0;
			this.sTime = 0;
			this.length = (float) Math.sqrt((node[oNode].xCoord-node[dNode].xCoord)*(node[oNode].xCoord-node[dNode].xCoord)
								+(node[oNode].yCoord-node[dNode].yCoord)*(node[oNode].yCoord-node[dNode].yCoord))/LENGTHAJUSTFACTOR;
		}
		
		public Link (int linkID, int oNode,int dNode,int numLanes,float capacity,float fft, int functional)
		{
			this.linkID = linkID;
			this.oNode = oNode;
			this.dNode = dNode;
			this.numLanes = numLanes;
			this.capacity = capacity;
			this.fft = fft;
			this.currentT = fft;
			this.flow = 0;
			this.vc = 0;
			this.sFlow = 0;
			this.sTime = 0;
			this.functional = functional;
			this.length = (float) Math.sqrt((node[oNode].xCoord-node[dNode].xCoord)*(node[oNode].xCoord-node[dNode].xCoord)
								+(node[oNode].yCoord-node[dNode].yCoord)*(node[oNode].yCoord-node[dNode].yCoord))/LENGTHAJUSTFACTOR;
		}
		
	}
	
	//Read integer or read float number
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
