/*<html>
<body>
<applet code="Demo.class" Archive = Adam4.jar width="100" height="100" >
</applet>
</body>
</html>
*/

//Adam 3.0, Using for CE3201 Spring 2007;
//Using ARC algorithm and running on SiouxFalls Network;
//Developed by Shanjiang Zhu, Nexus, CE UMN
//April, 2007
import java.io.*;
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import java.math.*;
import java.text.*;



public class Demo extends Applet  implements ActionListener,ItemListener,WindowListener
{
///layout of the simulator interface is:
///menuframe: the whole window;
///variablesPanel(left);
///DrawArea(right-up);
///DrawPanel(right-down),including legend, status bar and buttons.

	public double belta;
	public URL url=null;
	public URL helpurl=null;
	public String currentInputFile;
	JFrame menuframe;
	VariablesPanel vp;
	public DrawArea da;
	public DrawPanel dp;
	public DirectedGraph dg;

	public boolean linkInforInclude = false;
	public float CperLane = 1800;
	public int mapscale = 2;

	boolean  graphRead = false;
	boolean  evolved = false;
	boolean  traceWorker = false; //If we have clicked the button to trace a worker
	boolean  drawVolume = true;//true when speed is drawn; false when volume is drawn
	public boolean  editNetwork = false;
	boolean  chooseLink = false;
	boolean  chooseNode = false;
	public boolean mouseclicked;
	public int chooseNodeID;
	public int chooseLinkStartNode;
	public int chooseLinkDemandID;
	public int chooseLinkID;
	public String getnetwork;
	public JFrame f;
	public Frame fw;
	public Frame fLinkEdit,fNodeEdit;
	public Frame fAddress;
	public TextArea stat;
	public Choice choiceWorkerOrigin;
	public int workerOrigin=0;
	public Choice choiceWorkerID;
	public int workerID=0;
	public Button chooseWorker;
	public boolean oppositeExist;
	public JCheckBox addOpposite;
	
	public Button changeNodeProperty;
	public Label noticeNodeProperty;
	public TextField textfieldX,textfieldY,textfieldNodeWorkers,textfieldNodeJobs;
	public TextField textfieldArcLength;
	public TextField textfieldFFT;
	public TextField textfieldNumberLanes;
	public TextField textfieldCapacity;
	public Button changeLinkProperty;
	public Label noticeLinkProperty;
	public boolean newNodeAccomplish = false;
	public Frame fDelete;
	public Button deleteConfirm,deleteCancel;
	public boolean chooseAddNode;
	public boolean chooseAddLink;
	public boolean oNodeChoosen,dNodeChoosen;
	public int addLinkOrigin,addLinkDestination;
	public boolean oNodeFound=false;
	public boolean dNodeFound=false;
	MenuBar mbar,fmbar,saveMbar;
	
	public String saveFileName;
	public JButton saveConfirm;
	public JTextField address;
	public JOptionPane confirm;
	public Frame fOverwrite;
	public Button overwriteConfirm, overwriteCancel;
	
	public String loadFileName;
	public Frame fLoad;
	public Button loadAccept;
	
	public JFileChooser chooser;
	File chooserFile;
	
	public float generationFactor,attractionFactor;
	public float theta;
	public float alphaBPR,betaBPR;
	
	public JFrame fSaveNetwork;
	public Demo demo;
	public boolean networkModified = false;
	public TextArea saveContent;
	public MenuItem saveToClipboard;
	public TextArea loadContent;
	public MenuItem loadFromTextArea;
	public JFrame fLoadNetwork;

	public void init() {
		demo = this;
		url=getCodeBase();
		vp = new VariablesPanel();
		dp = new DrawPanel(this);
		da = new DrawArea( dp, this );
		getnetwork="SiouxFalls Grid Network";

		WindowDestroyer windowKiller=new WindowDestroyer();


	//Define the main window
		menuframe = new MenuFrame("Agent-Based Demand and Assignment Model (ADAM 1.4 based on ARC and NODE)",  this )  ;
		//define the size of menuframe according to the screen size
		Dimension screensize = getToolkit().getScreenSize();

		menuframe.getContentPane().setLayout(new BorderLayout());
		menuframe.getContentPane().add("West", vp);
		menuframe.getContentPane().add("Center", da);

		menuframe.addWindowListener(this);
		menuframe.setSize ((int)(1.0*screensize.width),
					  (int)(0.99*screensize.height));

		menuframe.setVisible(true);
		//define the menu
		mbar = new MenuBar();
		menuframe.setMenuBar(mbar);
		Menu song = new Menu("Agent-Based");
		Menu help=new Menu("Help");

		MenuItem  evolve1,quit,about,instruction,saveNetwork,showNetworkFile;
		song.add(evolve1 = new MenuItem("Evolve "));
		song.add(showNetworkFile = new MenuItem("Show Network File"));
		song.add(saveNetwork = new MenuItem("Save Network to File"));
		song.add(quit = new MenuItem("Quit"));
		help.add(instruction = new MenuItem("Instructions"));
		//help.add(about=new MenuItem("About Song1.0"));

		mbar.add(song);
		mbar.add (help);

		evolve1.addActionListener(this);
		quit.addActionListener(this);
		saveNetwork.addActionListener(this);
		showNetworkFile.addActionListener(this);
		//about.addActionListener(this);
		instruction.addActionListener(this);

	//Define the result window
		f=new JFrame("Statistics");
		stat=new TextArea("");
		fw=new Frame("Choose the Worker to Trace");
		fLinkEdit = new Frame("Edit Link Property");
		fNodeEdit = new Frame("Edit Node Property");
		fDelete = new Frame("Delete Confirmation");
		fAddress = new Frame("Save as...");
		fOverwrite = new Frame("Do you want to overwrite ... ?");
		fLoad = new Frame("Load...");
		fSaveNetwork = new JFrame ("Save network: please copy the content to a .txt file");
		fLoadNetwork = new JFrame("Paste network to the Textarea and choose load");


///load the 10*10 network when the window is opened
		vp.network.select("SiouxFalls Network" );
		dp.showStatus.setText("SiouxFalls Loaded...");

		dp.evolve.setEnabled(true) ;
		dp.statistics .setEnabled(false);
//		currentInputFile = "Grid2.txt";
		currentInputFile = "SiouxFalls.txt";
		
		url = getClass().getResource(currentInputFile);
		try {
			dg = new DirectedGraph(currentInputFile,url,linkInforInclude,this);
			System.out.println("Construct new DG");
		} catch (IOException e) {
		}
//		ev.inforForDispay();
		
		vp.editProperty.setEnabled(false);
		vp.addNode.setEnabled(false);
		vp.editNetworkChoose .setEnabled(true);
		dp.removeTraceWorker .setEnabled(false) ;
		dp.traceWorker.setEnabled(false) ;
		dp.statistics .setEnabled( false);
		dp.whichAttribute.setEnabled(false) ;
		dp.scale .setEnabled( false);

		da.setMapVariables();
		graphRead = true;
		evolved = false;
		traceWorker = false;
		da.currentYear = 0;
		da.repaint();
///////////

	}




	public void paint( Graphics g ) {
		//da.paint( g);
	}


///define the events related to window
	public void windowClosing(WindowEvent e){
		Object obj = e.getSource();
		if(obj.equals( menuframe))menuframe.dispose() ;
		else if (obj.equals( f))f.dispose() ;
		else if (obj.equals(fw))fw.dispose();
//		else if (obj.equals(fLinkEdit))fLinkEdit.dispose();
//		else if (obj.equals(fNodeEdit))fNodeEdit.dispose();
//		else if (obj.equals(fDelete))fDelete.dispose();
//		else if (obj.equals(fAddress))fAddress.dispose();
//		else if (obj.equals(fOverwrite))fOverwrite.dispose();
//		else if (obj.equals(fLoad))fLoad.dispose();
//		else if (obj.equals(fSaveNetwork)) fSaveNetwork.dispose();
	}

	public void windowOpened(WindowEvent e){
		da.setVisible(true) ;

	}

	public void windowActivated(WindowEvent e){

		da.repaint() ;
	}

	public void windowDeactivated(WindowEvent e){

		da.repaint() ;
	}

	public void windowIconified(WindowEvent e){

		da.repaint() ;
	}

	public void windowDeiconified(WindowEvent e){

		da.repaint() ;
	}

	public void windowClosed(WindowEvent e){


	}
//

	public void actionPerformed( ActionEvent ae) {
		String arg = (String) ae.getActionCommand();
		Object obj = ae.getSource();

//
		  if(arg=="Evolve "){
			  if (currentInputFile == "TC_linkinfo.txt")
			  {
				  dp.showStatus.setText("The Twin Cities Network is currently for illustration only. Simulation model is not ready on this network.");  
			  }else{
				da.dp.evolve .setEnabled( false);
				vp.setEnabled( false);
				evolved = false;
				da.dp.traceWorker.setEnabled( false);
				traceWorker = false;
	
				vp.getGlobalVariable();
				dg.setGlobalVariable(alphaBPR,betaBPR,(float)belta,theta,generationFactor,attractionFactor);			
				dg.iteration();
				dp.showStatus.setText("Ready");
				
				da.dp.evolve .setEnabled( false);
				vp.setEnabled( true);
				evolved = true;		
		
				vp.editProperty.setEnabled(false);
				vp.addNode.setEnabled(false);
				vp.editNetworkChoose .setEnabled(true);
				da.dp.removeTraceWorker .setEnabled(true) ;
				da.dp.traceWorker.setEnabled(true) ;
				da.dp.statistics .setEnabled( true);
				da.dp.whichAttribute.setEnabled(true) ;
				da.dp.scale .setEnabled( true);
				
				da.repaint();
			  }
		  }


		if(arg=="Quit"){
			menuframe.dispose() ;
		}



		if(arg=="Instructions"){
			System.out.println("////////////Enter help///////////");
			url = getCodeBase();
			try {
				helpurl=new URL(url,"ADAM-helpfile.htm");
				System.out.println("-----------------helpurl:"+helpurl);
			}
			catch (MalformedURLException e) {
			System.out.println("Bad URL:" + helpurl);
			}
			getAppletContext().showDocument(helpurl,"_blank");
		}

		if(arg=="About Song1.0"){
			url = getCodeBase();
			try {
			helpurl=new URL(url,"HelpFileSONG1.0.htm"); }

			catch (MalformedURLException e) {
			System.out.println("Bad URL:" + helpurl);
			}

			getAppletContext().showDocument(helpurl,"_blank");
		}

		if(arg=="Save Statistics to File"){
			FileDialog savefile=new FileDialog(f,"Save Statistics...",FileDialog.SAVE);
			savefile.show() ;

			FileOutputStream out= null;
			File saveS= new File(savefile.getDirectory(),savefile.getFile()  );
			
			boolean success=false;		
			if (saveS.exists())
				{
					int option = JOptionPane.showConfirmDialog(null,"File exist, overwrite?","File exist Overwrite?",JOptionPane.YES_NO_OPTION);
					if (option == JOptionPane.YES_OPTION)
					{
						success = true;							
					}else{
						success = false;
					}
				}else
				{
					success = true;
				}
			if (success){
				try{
					out= new FileOutputStream(saveS);
				}catch(Exception e) {
					System.out.println("Unable to open file");
					return;
				}
				PrintStream psOut=new PrintStream(out);
				psOut.print(stat.getText());//
			}
//			try{
//			out.close();
//			}catch(IOException e){
//			System.out.println("e");
//			}
//			stat.selectAll();
////			stat.copy();
//			JOptionPane.showMessageDialog(f,"Network saved in clipboard, please save them in a txt file.","Save to Clipboard",JOptionPane.DEFAULT_OPTION);
		}

		if(arg=="Close"){
			f.dispose()  ;
		}
		
		if (arg == "Save Network to File"){
			dg.initialArc();

			chooser = new JFileChooser();
			chooser.setVisible(true);
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				chooserFile = chooser.getSelectedFile();
				saveFileName = chooserFile.getName();
				boolean success=false;
				if (chooserFile.exists())
				{
					int option = JOptionPane.showConfirmDialog(null,"File exist, overwrite?","File exist Overwrite?",JOptionPane.YES_NO_OPTION);
					if (option == JOptionPane.YES_OPTION)
					{
						success = true;							
					}else
					{
						success = false;
					}
				}else
				{
					success = true;
				}
//				
				if (success)
				{
					dg.save(chooserFile);
					fSaveNetwork.dispose();
				}
			}




//				FileDialog savefile=new FileDialog(f,"Save Network...",FileDialog.SAVE);
//
//				savefile.show() ;
//				chooserFile = new File(savefile.getDirectory(),savefile.getFile()  );
//				saveFileName = chooserFile.getName();
//				boolean success=false;		
//			if (chooserFile.exists())
//				{
//					int option = JOptionPane.showConfirmDialog(null,"File exist, overwrite?","File exist Overwrite?",JOptionPane.YES_NO_OPTION);
//					if (option == JOptionPane.YES_OPTION)
//					{
//						success = true;							
//					}else
//					{
//						success = false;
//					}
//				}else
//				{
//					success = true;
//				}
//				
//			if (success)
//			{
//				try {
//						url = chooserFile.toURL();
//						ev.save(chooserFile,url,(float)belta);
//						url=getCodeBase();
//				}catch (MalformedURLException me)
//				{
//					System.out.println("URL error");
//				}
//				
//			}
		}
		
		if (arg == "Show Network File")
		{
			System.out.println("_________________Save_________________");
			dg.initialArc();
//			Label temp;
//			fAddress.dispose();
//			fAddress = new Frame("Save as...");
//			fAddress.setLayout(new GridLayout(3,1));
//			fAddress.addWindowListener(this);
//			Dimension screensize = getToolkit().getScreenSize();
//					//define the size of menuframe according to the screen size
//			fAddress.setSize ((int)(0.30*screensize.width),
//											  (int)(0.30*screensize.height));
//											  
//			fAddress.add(temp = new Label("Save the Network as:"));
//			address = new JTextField("Input File Name Here");
//			fAddress.add(address);
//			saveConfirm = new JButton("Accept");
//			saveConfirm.addActionListener(this);
//			fAddress.add(saveConfirm);
//			
//			fAddress.setVisible(true);
//			da.setEnabled(false);

//			chooser = new JFileChooser();
//			chooser.setVisible(true);
//			int returnVal = chooser.showSaveDialog(this);
//			if (returnVal == JFileChooser.APPROVE_OPTION)
//			{
//				chooserFile = chooser.getSelectedFile();
//				saveFileName = chooserFile.getName();
//				boolean success=false;
//				if (chooserFile.exists())
//				{
//					int option = JOptionPane.showConfirmDialog(null,"File exist, overwrite?","File exist Overwrite?",JOptionPane.YES_NO_OPTION);
//					if (option == JOptionPane.YES_OPTION)
//					{
//						success = true;							
//					}else
//					{
//						success = false;
//					}
//				}else
//				{
//					success = true;
//				}
//				
//				if (success)
//				{
//					ev.save(chooserFile,(float)belta);
//				}
//			}




//				FileDialog savefile=new FileDialog(f,"Save Network...",FileDialog.SAVE);
//
//				savefile.show() ;
//				chooserFile = new File(savefile.getDirectory(),savefile.getFile()  );
//				saveFileName = chooserFile.getName();
//				boolean success=false;		
//			if (chooserFile.exists())
//				{
//					int option = JOptionPane.showConfirmDialog(null,"File exist, overwrite?","File exist Overwrite?",JOptionPane.YES_NO_OPTION);
//					if (option == JOptionPane.YES_OPTION)
//					{
//						success = true;							
//					}else
//					{
//						success = false;
//					}
//				}else
//				{
//					success = true;
//				}
//				
//			if (success)
//			{
//				try {
//						url = chooserFile.toURL();
//						ev.save(chooserFile,url,(float)belta);
//						url=getCodeBase();
//				}catch (MalformedURLException me)
//				{
//					System.out.println("URL error");
//				}
//				
//			}
			
///////Print out result to save
  			  fSaveNetwork.dispose();
			  fSaveNetwork = new JFrame("Save network: Copy and Save the content to a .txt file");
			  saveMbar = new MenuBar();
			  fSaveNetwork.setMenuBar(saveMbar);
			  Menu saveMenu = new Menu("Save Network Files");
			  MenuItem saveToFile;
			  
			  saveMenu.add(saveToClipboard = new MenuItem("Save to Clipboard"));
			  saveMenu.add(saveToFile = new MenuItem("Save Network to File"));
			  saveMbar.add(saveMenu);  
			  fSaveNetwork.addWindowListener(this);
			  saveToClipboard.addActionListener(this);
			  saveToFile.addActionListener(this);
		
			  JScrollPane saveScrollPane;
			  
			  saveContent = new TextArea("",50,50);
			  
			  Dimension screensize = getToolkit().getScreenSize();
						//define the size of menuframe according to the screen size
			  fSaveNetwork.setSize ((int)(0.35*screensize.width),
									  (int)(0.80*screensize.height));
									  
			  String temp;
				String outstring="";
			
				outstring += dg.numNodes;
				outstring +="\n";
				outstring += dg.numWorkers;
				outstring +="\n";
				outstring +=dg.beta;
				outstring +="\n";
				for (int i=1;i<dg.numNodes+1;i++)
				{
					outstring +=dg.node[i].nodeId;
					outstring +="\t";
					outstring +=dg.node[i].nodeWorkers;
					outstring +="\t";
					outstring +=dg.node[i].originalJobs;
					outstring +="\t";
					outstring +=dg.node[i].xCoord;
					outstring +="\t";
					outstring +=dg.node[i].yCoord;
					outstring +="\t";
					outstring +=dg.node[i].numDemandNodes;
				
					for (int j=0;j<dg.node[i].numDemandNodes;j++)
					{
						outstring +="\t";
						outstring +=dg.node[i].demandNodes[j];
						outstring +="\t";
						outstring +=(int)(dg.arc[i][dg.node[i].demandNodes[j]].fft);
//								System.out.println("/////////////////////arc"+i+node[i].demandNodes[j]+" "+arc[i][node[i].demandNodes[j]]);
//								outstring +="\t";				
					}
					outstring +="\n";
			
				}
					outstring +=dg.numLink;
					outstring +="\n";
						for (int i=0;i<dg.numLink;i++)
						{
							outstring +=dg.link[i].linkID;
							outstring +="\t";
							outstring +=dg.link[i].oNode;
							outstring +="\t";
							outstring +=dg.link[i].dNode;
							outstring +="\t";
							outstring +=dg.link[i].numLanes;
							outstring +="\t";
							outstring +=dg.link[i].capacity;
							outstring +="\t";
							outstring +=dg.link[i].fft;
							outstring +="\n";
						}
			  saveContent.append(outstring);
			  saveContent.setVisible(true);
			  fSaveNetwork.getContentPane().add(saveContent,"Center");
			  saveContent.setEditable(false);
			  saveScrollPane = new JScrollPane(saveContent);
			  fSaveNetwork.getContentPane().add(saveScrollPane);
			  fSaveNetwork.setVisible(true);
			  networkModified = false;
		}
		
		if (obj.equals(saveToClipboard))
		{
			saveContent.selectAll();
			JOptionPane.showMessageDialog(fSaveNetwork,"Network saved in clipboard, please save them in a txt file.","Save to Clipboard",JOptionPane.DEFAULT_OPTION);
		}

//Load Self-Define Network		
		

// Command Evolve
		if(obj.equals(dp.evolve )){
			if (networkModified)
			{
			//////////////Remind people to save before evolve
				int option = JOptionPane.showConfirmDialog(menuframe,"Network not saved!","Save before evolve?",JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION)
				{
					dg.initialArc();
/*					fSaveNetwork.dispose();
								  fSaveNetwork = new JFrame("Save network: Copy and Save the content to a .txt file");
								  fSaveNetwork.addWindowListener(this);
		
								  JTextArea saveContent;
								  JScrollPane saveScrollPane;
			  
								  saveContent = new JTextArea("",50,50);
			  
								  Dimension screensize = getToolkit().getScreenSize();
											//define the size of menuframe according to the screen size
								  fSaveNetwork.setSize ((int)(0.35*screensize.width),
														  (int)(0.80*screensize.height));
									  
								  String temp;
									String outstring="";
			
									outstring += dg.numNodes;
									outstring +="\n";
									outstring += dg.numWorkers;
									outstring +="\n";
									outstring +=dg.beta;
									outstring +="\n";
									for (int i=1;i<dg.numNodes+1;i++)
									{
										outstring +=dg.node[i].nodeId;
										outstring +="\t";
										outstring +=dg.node[i].nodeWorkers;
										outstring +="\t";
										outstring +=dg.node[i].originalJobs;
										outstring +="\t";
										outstring +=dg.node[i].xCoord;
										outstring +="\t";
										outstring +=dg.node[i].yCoord;
										outstring +="\t";
										outstring +=dg.node[i].numDemandNodes;
				
										for (int j=0;j<dg.node[i].numDemandNodes;j++)
										{
											outstring +="\t";
											outstring +=dg.node[i].demandNodes[j];
											outstring +="\t";
											outstring +=(int)(dg.arc[i][dg.node[i].demandNodes[j]].fft);
//													System.out.println("/////////////////////arc"+i+node[i].demandNodes[j]+" "+arc[i][node[i].demandNodes[j]]);
//													outstring +="\t";				
										}
										outstring +="\n";
			
									}
										outstring +=dg.numLink;
										outstring +="\n";
											for (int i=0;i<dg.numLink;i++)
											{
												outstring +=dg.link[i].linkID;
												outstring +="\t";
												outstring +=dg.link[i].oNode;
												outstring +="\t";
												outstring +=dg.link[i].dNode;
												outstring +="\t";
												outstring +=dg.link[i].numLanes;
												outstring +="\t";
												outstring +=dg.link[i].capacity;
												outstring +="\t";
												outstring +=dg.link[i].fft;
												outstring +="\n";
											}
								  saveContent.append(outstring);
								  saveContent.setVisible(true);
								  fSaveNetwork.getContentPane().add(saveContent,"Center");
								  saveScrollPane = new JScrollPane(saveContent);
								  fSaveNetwork.getContentPane().add(saveScrollPane);
								  fSaveNetwork.setVisible(true);
								  */
					chooser = new JFileChooser();
					chooser.setVisible(true);
					int returnVal = chooser.showSaveDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						chooserFile = chooser.getSelectedFile();
						saveFileName = chooserFile.getName();
						boolean success=false;
						if (chooserFile.exists())
						{
							int option1 = JOptionPane.showConfirmDialog(null,"File exist, overwrite?","File exist Overwrite?",JOptionPane.YES_NO_OPTION);
							if (option1 == JOptionPane.YES_OPTION)
							{
								success = true;							
							}else
							{
								success = false;
							}
						}else
						{
							success = true;
						}
//						
						if (success)
						{
							dg.save(chooserFile);
							fSaveNetwork.dispose();
						}
					}
					networkModified = false;
				}else if (option == JOptionPane.NO_OPTION)
				{
					//////////Evolve without save
					dp.showStatus.setText("Initialization...");
					da.dp.evolve .setEnabled( false);
					vp.setEnabled( false);
					evolved = false;
					da.dp.traceWorker.setEnabled(false);
					traceWorker = false;
					mouseclicked = false;
					editNetwork = false;
					
					dg.initialArc();
					vp.getGlobalVariable();
					dg.setGlobalVariable(alphaBPR,betaBPR,(float)belta,theta,generationFactor,attractionFactor);
					dg.iteration();
					
					//			dp.showStatus.setText("Ready");
			
					da.dp.setVisible(true);
						/////////////			
	
					da.dp.evolve .setEnabled( false);
					vp.setEnabled( true);
					evolved = true;	
	
	
					vp.editProperty.setEnabled(false);
					vp.addNode.setEnabled(false);
					vp.editNetworkChoose .setEnabled(true);
					da.dp.removeTraceWorker .setEnabled(true) ;
					da.dp.traceWorker.setEnabled(true) ;
					da.dp.statistics .setEnabled( true);
					da.dp.whichAttribute.setEnabled(true) ;
					da.dp.scale .setEnabled( true);
			
					da.repaint();
				}
			}else{
		///////////Evolve with save;
				if (currentInputFile == "TC_linkinfo.txt"){
					dp.showStatus.setText("The Twin Cities Network is currently for illustration only. Simulation model is not ready on this network.");
				}
				else{
					dp.showStatus.setText("Initialization...");
					da.dp.evolve .setEnabled( false);
					vp.setEnabled( false);
					evolved = false;
					da.dp.traceWorker.setEnabled(false);
					traceWorker = false;
					mouseclicked = false;
					editNetwork = false;
					
		
		
			///initializing...			
		//			try {
		////				nd = new NetworkDynamics( vp.variables, url,currentInputFile,this);
		//				ev = new Evolve( currentInputFile,belta,url);
		////				ev.initialization();
		//			}
		//			catch(IOException ie) {
		//			}
		//	//////////////////
		//			da.currentYear = 0;
		//			da.dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
		
			///running...	
		//			nd.NetworkDynamix(url, vp.variables);
		//			ev = new Evolve (currentInputFile,belta);
					
					vp.getGlobalVariable();
					dg.setGlobalVariable(alphaBPR,betaBPR,(float)belta,theta,generationFactor,attractionFactor);
					dg.iteration();
			//		dp.showStatus.setText("Ready");
					
					da.dp.setVisible(true);
			/////////////			
			
					da.dp.evolve .setEnabled( false);
					vp.setEnabled( true);
					evolved = true;	
			
			
					vp.editProperty.setEnabled(false);
					vp.addNode.setEnabled(false);
					vp.editNetworkChoose .setEnabled(true);
					da.dp.removeTraceWorker .setEnabled(true) ;
					da.dp.traceWorker.setEnabled(true) ;
					da.dp.statistics .setEnabled( true);
					da.dp.whichAttribute.setEnabled(true) ;
					da.dp.scale .setEnabled( true);
					
					da.repaint();
				}
			}
		}

//Command Statistics
		if(obj.equals(dp.statistics)){
			//JOptionPane.showMessageDialog(menuframe,"The Moe's Results: avgSpeed="+nd.avgSpeed+"; avgVolume="+nd.avgVolume+"; vkt="+nd.vkt+"; vht="+nd.vht);
			
			DecimalFormat dFmt = new DecimalFormat("#.###");
			DecimalFormat dFmtT = new DecimalFormat("#.######");
			
			f.dispose();
			f=new JFrame("Statistics");
			f.addWindowListener(this);
			
			JScrollPane scrollPane;

			stat=new TextArea("",50,50);
			Dimension screensize = getToolkit().getScreenSize();
			//define the size of menuframe according to the screen size
			f.setSize ((int)(0.35*screensize.width),
						  (int)(0.80*screensize.height));
			//define the menu
			fmbar = new MenuBar();
			f.setMenuBar(fmbar);
			Menu file = new Menu("File");

			MenuItem  save,close;
			file.add(save = new MenuItem("Save Statistics to File"));
			file.add(close = new MenuItem("Close"));

			fmbar.add(file);

			save.addActionListener(this);
			close.addActionListener(this);
			stat.setText( "");
			f.setVisible( false);
			stat.setVisible(false);
			
			//ScrollBars
//			JScrollPane scrollpane;
//			scrollpane = new JScrollPane(stat);
//			getContentPane().add(scrollpane);

			String temp="";

			stat.append(new String("\n\n---Network Summary---\n\n"));

			stat.append(new String("       Item                    Description/Value\n\n"));

			stat.append(new String("1.  Network Type       \t"+vp.network .getSelectedItem() +"\n"));
			stat.append(new String("2.  Global Variables:\t"+"\n"));
			stat.append(new String("2.1 Travel Length Coefficient\t"+vp.beltaScroll.value() +"\n"));
			stat.append(new String("2.2 Theta\t"+vp.thetaScroll.value() +"\n"));
			stat.append(new String("2.3 Alpha for BPR Fucntion\t"+vp.alphaScroll.value() +"\n"));
			stat.append(new String("2.4 Beta for BPR Fucntion\t"+vp.betaScroll.value() +"\n"));
			stat.append(new String("2.5 Trip Production Rate\t"+vp.tripGRateScroll.value() +"\n"));
			stat.append(new String("2.6 Trip Attraction Rate\t"+vp.tripARateScroll.value() +"\n"));
			stat.append(new String("2.7 Peak Hour Rate\t"+vp.peakHourRateScroll.value() +"\n"));
			stat.append(new String("2.8 Auto Mode Share\t"+vp.tripByAutoScroll.value() +"\n"));
			stat.append(new String("2.9 Auto Occupancy\t"+vp.autoOccupancyScroll.value() +"\n"));
			stat.append(new String("2.10 Cost $/lane*mile\t"+vp.constructionCostScroll.value() +"\n"));
			stat.append(new String("Number of Nodes:")+dg.numNodes+"\n");
			stat.append(new String("Total Traffic Production:")+dg.numAutos+"\n");
			stat.append(new String("Total Traffic Attraction:")+dg.numOppo+"\n");
//			stat.append(new String("\nActual Number of Iterations   \t"+(nd.endyear +1) +"\n"));

			stat.append(new String("\n---MOEs Results---\n\n"));

			stat.append(new String("  MOE       Value\n\n"));
	//Load standard Information for output;		
			
			url = getClass().getResource("sStatistics.txt");	//Path to load the standard file;
			System.out.println("*************STAT:"+"\t"+vp.constructionCostScroll.value()+"\t"+vp.autoOccupancyScroll.value()+"\t"+vp.peakHourRateScroll.value());
			//dg.benefitCost((float)vp.constructionCostScroll.value(),(float)vp.autoOccupancyScroll.value(),(float)vp.peakHourRateScroll.value(),url);

			//It is hard to calculate benefit and cost without prespecified scenario;
			
//			stat.append(new String("Benefit (vehicle*kilomter):"+dg.benefit+"\n"));
			stat.append(new String("Vehicle Hour Travel:"+dg.vht+"(vehicle*hour)\n"));
//			stat.append(new String("Average Travel Time:"+dg.vhtPerVehicle+"(min)\n"));
			stat.append(new String("Vehicle Kilometer Travel:"+dg.vkt+"(vehicle*kilometer)\n"));
			stat.append(new String("Vehicle Euclidean Kilometer Travel:"+dg.vekt+"(vehicle*kilometer)\n"));
			stat.append(new String("Traffic Production:"+dg.numAutos+"(vehicle)\n"));
			stat.append(new String("Traffic Attraction:"+dg.numOppo+"(vehicle)\n"));
			stat.append(new String("Total Trips:"+dg.totalouttrips+"\n"));
//			stat.append(new String("Interzonal Trips:"+ev.quantity+"\n"));
//			stat.append(new String("Number of Vehicles without Destination:"+dg.noJobAuto+"\n"));
//			stat.append(new String("Average Travel Length for Interzonal Trips:"+ev.averageTravelLength+"(node)\n"));
//			stat.append(new String("Average Travel Length for Interzonal Trips:"+ev.vkt/ev.quantity+"(kilometer)\n"));
//			stat.append(new String("Network Cost($):"+ev.cost+"\n"));


			//stat.append(new String("\n---Benefits & Cost Analysis---\n\n"));
			//stat.append(new String("Benefit from Road Expansions:"+dg.totalBenefit+"($)"+"\n"));
			//stat.append(new String("Amount of Road Expansions:"+dg.expansionLength+"(kilometer)"+"\n"));
			//stat.append(new String("Initial Investment of Road Expansions:"+dg.budgetUsed+"($)"+"\n"));
//			stat.append(new String("Current Benefit Cost Ratio:"+dFmt.format(dg.BCRatio)+"\n"));
			stat.append(new String("\n---Accessibility Measure---\n\n"));
			stat.append(new String("Average Number of Jobs Accessible in 5 Minutes"+dg.acceJobs5+"\n"));
			stat.append(new String("Average Number of Jobs Accessible in 10 Minutes"+dg.acceJobs10+"\n"));
			stat.append(new String("Average Number of Jobs Accessible in 20 Minutes"+dg.acceJobs20+"\n"));
			stat.append(new String("Average Number of Workers Accessible in 5 Minutes"+dg.acceWorkers5+"\n"));
			stat.append(new String("Average Number of Workers Accessible in 10 Minutes"+dg.acceWorkers10+"\n"));
			stat.append(new String("Average Number of Workers Accessible in 20 Minutes"+dg.acceWorkers20+"\n"));
			
			stat.append(new String("\n-----Network Performance Detail-----\n"));

			stat.setFont(new Font("Times New Roman",Font.PLAIN|Font.ROMAN_BASELINE |Font.BOLD ,12));
//			stat.append(new String("AvgSpeed\t"+nd.avgSpeed +"\n"));
//			stat.append(new String("AvgFlow\t"+nd.avgVolume  +"\n"));
//			stat.append(new String("vkt\t"+nd.vkt +"\n"));
//			stat.append(new String("vht\t"+nd.vht +"\n"));
//			stat.append(new String("Total Cost\t"+nd.totalCost +"\n"));
//			stat.append(new String("Total Revenue\t"+nd.totalRevenue  +"\n"));
///			stat.append(new String("Cumulative Cost\t"+nd.cumulativeCost  +"\n"));
//			stat.append(new String("Cumulative Revenue\t"+nd.cumulativeRevenue  +"\n"));
//			stat.append(new String("Improvement Term\t")+nd.ImproveTerm +"\n");
			
			stat.append(new String("  OD Table \n\n"));
			stat.append(new String("Node\t"));
			for(int i=1;i<dg.numNodes+1;i++)
			{
				if (i>0 && i<10){
					stat.append(new String("00"+i+"\t"));				
				}else if (i>=10 && i<100){
					stat.append(new String("0"+i+"\t"));
				}else{
					stat.append(new String(""+i+"\t"));
				}				
			}
			stat.append(new String("\n"));
				for (int i=1;i<dg.numNodes+1;i++)
				{
					stat.append(new String(""+i+"\t"));
					for(int j=1;j<dg.numNodes+1;j++)
					{
						stat.append(new String(""+dg.evolve.od[i][j]+"\t"));
					}
					stat.append(new String(" \n"));
				}
/*
			stat.append(new String("  OD Shortest Travel Time (in minutes) \n\n"));
			stat.append(new String("Node\t"));
			for(int i=1;i<dg.numNodes+1;i++)
			{
				if (i>0 && i<10){
					stat.append(new String("00"+i+"\t"));				
				}else if (i>=10 && i<100){
					stat.append(new String("0"+i+"\t"));
				}else{
					stat.append(new String(""+i+"\t"));
				}				
			}
			stat.append(new String("\n"));
				for (int i=1;i<dg.numNodes+1;i++)
				{
					stat.append(new String(""+i+"\t"));
					for(int j=1;j<dg.numNodes+1;j++)
					{
						stat.append(new String(""+dg.ShT[i][j]+"\t"));
					}
				stat.append(new String(" \n"));
				}
			stat.append(new String("  OD Average Travel Time (in minute) \n\n"));
			stat.append(new String("Node\t"));
			for(int i=1;i<dg.numNodes+1;i++)
			{
				if (i>0 && i<10){
					stat.append(new String("00"+i+"\t"));				
				}else if (i>=10 && i<100){
					stat.append(new String("0"+i+"\t"));
				}else{
					stat.append(new String(""+i+"\t"));
				}				
			}
			stat.append(new String("\n"));
				for (int i=1;i<dg.numNodes+1;i++)
				{
					stat.append(new String(""+i+"\t"));
					for(int j=1;j<dg.numNodes+1;j++)
					{
						stat.append(new String(""+dg.ODT[i][j]+"\t"));
					}
					stat.append(new String(" \n"));
				}
*/
			stat.append(new String("\n     Link Performance \n"));
			stat.append(new String("Number of Links:"+dg.numLink));
			stat.append(new String("\n"));
			stat.append(new String("Link\t"+"ONode\t"+"DNode\t"+"Flow\t"+"Time\t"+"FFT\t"+"V/C\t"+"Lanes\t"+"Capacity"+"Length"+"\n"));
			for (int i=0;i<dg.numLink;i++)
			{
				stat.append(new String(""+dg.link[i].linkID+"\t"));
				stat.append(new String(""+dg.link[i].oNode+"\t"));
				stat.append(new String(""+dg.link[i].dNode+"\t"));
				stat.append(new String(""+dg.link[i].flow+"\t"));
				stat.append(new String(""+dFmtT.format(dg.link[i].currentT)+"\t"));
				stat.append(new String(""+dFmt.format(dg.link[i].fft)+"\t\t"));
				stat.append(new String(""+dFmt.format(dg.link[i].vc)+"\t\t"));
				stat.append(new String(""+dg.link[i].numLanes+"\t"));
				stat.append(new String(""+dg.link[i].capacity+"\t"));
				stat.append(new String(""+dFmt.format(dg.link[i].length)+"\t"));
				stat.append(new String("\n"));
			}
			
		//////////Calculate Benefit/Cost;
			
			stat.setVisible( true);
			stat.setEditable(true);
			f.getContentPane().add(stat,"Center");
			scrollPane = new JScrollPane(stat);
			f.getContentPane().add(scrollPane);
			f.setVisible( true);

		}

/////////////////////////////////////////////
//      Input the Worker Your want to Trace		
/*		if(arg.equals("Trace Worker"))
		{
		  if (!traceWorker)
		  {
			System.out.println("_______Choose Worker Window_______");
			Label temp;
			workerID=0;
			workerOrigin=0;
			traceWorker=false;
			fw.dispose();
			fw=new Frame("Trace Vehicle");
			fw.setLayout(new GridLayout(3,2));
			fw.addWindowListener(this);
			Dimension screensize = getToolkit().getScreenSize();
				//define the size of menuframe according to the screen size
			fw.setSize ((int)(0.30*screensize.width),
									  (int)(0.30*screensize.height));
									  
									  
			choiceWorkerOrigin=new Choice();
			choiceWorkerID=new Choice();
			for (int i=1;i<dg.numNodes+1;i++)
			{
				choiceWorkerOrigin.addItem(""+i);
			}
//			choiceWorkerOrigin.select(0);
			workerOrigin=choiceWorkerOrigin.getSelectedIndex()+1;
			fw.add(temp=new Label("Origin of Vehicle to Trace"));
			fw.add(choiceWorkerOrigin);
			choiceWorkerOrigin.addItemListener(this);
//			System.out.println("_______workerOrigin_______"+workerOrigin);
//			System.out.println("_______numWorkers_______"+ev.node[workerOrigin].nodeWorkers);			
			for (int i=1;i<dg.node[workerOrigin].numAuto+1;i++)
			{
				choiceWorkerID.addItem(""+i);
			}
//			choiceWorkerID.select(0);
			workerID=choiceWorkerID.getSelectedIndex();
			fw.add(temp=new Label("Vehicle to Trace"));
			fw.add(choiceWorkerID);
			choiceWorkerID.addItemListener(this);
			System.out.println("_______workerID_______"+workerID);			
			
			chooseWorker=new Button("Choose");
			fw.add(chooseWorker);
			chooseWorker.addActionListener(this);		
			
			fw.setVisible(true);
		  }else
		  {
		  	traceWorker = false;
			dp.removeTraceWorker.setEnabled(false);
			da.repaint();
		  }
		}
// Respond to the Button Action		
		if(obj.equals(chooseWorker))
		{
			System.out.println("__________________Button chooseWorker Down_______________");
			workerOrigin=choiceWorkerOrigin.getSelectedIndex()+1;
			workerID=choiceWorkerID.getSelectedIndex()+1;
			fw.dispose();
			traceWorker = true;
			dp.removeTraceWorker.setEnabled(true);
			vp.setEnabled(false);
			da.repaint();
			System.out.println("____________Button chooseWorker Finished____"+workerOrigin+" "+workerID);
		}
//Remove Worker Trace From the Draw Panal;
		if(obj.equals(dp.removeTraceWorker))
		{
			traceWorker = false;
			vp.setEnabled(true);
			dp.removeTraceWorker.setEnabled(false);
			da.repaint();
		}
*/		


//Command << < > >>
		  if(arg.equals("<<")) {
			  da.currentYear = 0;
			  da.repaint();
		  } else if( arg.equals("<") ) {
			  if( da.currentYear > 0 ) {
				  da.currentYear--;
				  da.repaint();
			  }  else {
//				  da.currentYear = nd.endyear ;
				  da.repaint();
			  }
//		  } else if(arg.equals(">") ) {
//			  if(da.currentYear < nd.endyear ) {
//				  da.currentYear ++;
//				  da.repaint();
//			  } else if(da.currentYear == nd.endyear){
//				  da.currentYear = 0;
//				  da.repaint();
//			  }
		  } else if(arg.equals(">>") ) {
//			  da.currentYear = nd.endyear;
			  da.repaint();
		  }

//		dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );

	}

	public void itemStateChanged( ItemEvent ie) {
		String arg = (String) ie.getItem();
		Object obj = ie.getSource();
		if (obj.equals(dp.whichAttribute)){
				if(dp.scale .getSelectedItem() =="Absolute"){
					if(arg.equals( "Volume")){
						drawVolume = true;
						dp.unit.setText("");
						dp.bluefor.setText("0~" + Integer.toString(400));
						dp.greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
						dp.yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
						dp.orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
						dp.redfor.setText(Integer.toString(1600) +"~"+ "  ");
						da.repaint();
					}
					else{
						drawVolume = false;
						dp.unit.setText("");
						dp.bluefor.setText("0~" +0.2);
						dp.greenfor.setText(""+0.2+"~" + 0.4);
						dp.yellowfor.setText(""+0.4+"~" + 0.6);
						dp.orangefor.setText(""+0.6+"~" + 0.8);
						dp.redfor.setText(""+0.8+"~");
						da.repaint();
					}

				}

				else{
					if(arg.equals( "Volume")){
						drawVolume = true;
						dp.unit.setText("");
						da.repaint();
					}
					else{
						drawVolume = false;
						dp.unit.setText("");
						da.repaint();
					}
				}
		}

		else if (obj.equals(dp.scale)){
			if(arg.equals( "Relative")){
				dp.unit.setText("");
				dp.bluefor.setText("Lowest");
				dp.greenfor.setText("Lower");
				dp.yellowfor.setText("Middle");
				dp.orangefor.setText("Higher");
				dp.redfor.setText("Highest");
				da.repaint() ;
			}
			else{
				if(dp.whichAttribute.getSelectedItem() .equals( "Volume")){
					drawVolume = true;
					dp.unit.setText("");
					dp.bluefor.setText("0~" + Integer.toString(400));
					dp.greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
					dp.yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
					dp.orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
					dp.redfor.setText(Integer.toString(1600) +"~"+ "  ");
					da.repaint();
				}
				else{
					drawVolume = false;
					dp.unit.setText("");
					dp.bluefor.setText("0~" +0.2);
					dp.greenfor.setText(""+0.2+"~" + 0.4);
					dp.yellowfor.setText(""+0.4+"~" + 0.6);
					dp.orangefor.setText(""+0.6+"~" + 0.8);
					dp.redfor.setText(""+0.8+"~");
					da.repaint();
				}
			}
			if (obj.equals(vp.network))
			{
				if(arg.equals( "2X2 Grid Network"))
				{
					vp.editNetworkChoose.setEnabled(false);
					currentInputFile="Grid2.txt";
					getnetwork = "2X2 Grid Network";
				}
				else if(arg.equals( "5X5 Grid Network"))
				{
					System.out.println("____________________________________Load_________________5x5");
					vp.editNetworkChoose.setEnabled(false);
					currentInputFile="Grid5.txt";
					getnetwork = "5X5 Grid Network";
				}
				else if(arg.equals("SiouxFalls Network"))
				{
					vp.editNetworkChoose.setEnabled(true);
					currentInputFile = "SiouxFalls.txt";
					getnetwork="SiouxFalls Network" ;
				}
				else if(arg.equals("Load"))
				{
					System.out.println("______________________________________Load_________________");
					Label temp;
					fLoad.dispose();
					fLoad = new Frame("Load...");
					fLoad.setLayout(new GridLayout(3,1));
					fLoad.addWindowListener(this);
					Dimension screensize = getToolkit().getScreenSize();
							//define the size of menuframe according to the screen size
					fLoad.setSize ((int)(0.30*screensize.width),
												  (int)(0.30*screensize.height));
					temp = new Label("Input the name of file to load");
					fLoad.add(temp);							
					address = new JTextField("File Name");
					fLoad.add(address);
					loadAccept = new Button("Accept");
					loadAccept.addActionListener(this);
					fLoad.add(loadAccept);
					
					fLoad.setVisible(true);
					da.setEnabled(false);
					vp.setEnabled(false);
					return; 							  
				}
				url = getClass().getResource(currentInputFile);
				System.out.println("Right Location-22-------------"+url);
				try {
						dg = new DirectedGraph(currentInputFile,url,linkInforInclude,this);
						System.out.println("Construct new Evolve");
					} catch (IOException e) {
					}
				da.repaint();
			}


		}
		if (obj.equals(choiceWorkerOrigin))
		{
			workerOrigin=choiceWorkerOrigin.getSelectedIndex()+1;
			System.out.println("_______Choose Worker Window_______");
						Label temp;
						fw.dispose();
						fw=new Frame("Trace Worker");
						fw.setLayout(new GridLayout(3,2));
						fw.addWindowListener(this);
						Dimension screensize = getToolkit().getScreenSize();
							//define the size of menuframe according to the screen size
						fw.setSize ((int)(0.30*screensize.width),
												  (int)(0.30*screensize.height));
									  
									  
						choiceWorkerOrigin=new Choice();
						choiceWorkerID=new Choice();
						for (int i=1;i<dg.numNodes+1;i++)
						{
							choiceWorkerOrigin.addItem(""+i);
						}
						choiceWorkerOrigin.select(workerOrigin-1);
						fw.add(temp=new Label("Origin of Worker to Trace"));
						fw.add(choiceWorkerOrigin);
						choiceWorkerOrigin.addItemListener(this);
//						System.out.println("_______workerOrigin_______"+workerOrigin);
//						System.out.println("_______numWorkers_______"+dg.node[workerOrigin].nodeWorkers);			
						for (int i=1;i<dg.node[workerOrigin].numAuto+1;i++)
						{
							choiceWorkerID.addItem(""+i);
						}
//						choiceWorkerID.select(0);
						workerID=choiceWorkerID.getSelectedIndex()+1;
						fw.add(temp=new Label("Worker to Trace"));
						fw.add(choiceWorkerID);
						choiceWorkerID.addItemListener(this);
						System.out.println("_______workerID_______"+workerID);			
			
						chooseWorker=new Button("Choose");
						fw.add(chooseWorker);
						chooseWorker.addActionListener(this);		
			
						fw.setVisible(true);
			
		}else if (obj.equals(choiceWorkerID))
		{
			workerID=choiceWorkerID.getSelectedIndex()+1;
		}
		
	}

	

	public void writeStat(URL url){

		PrintWriter out=null;

		try
			{
				out=new PrintWriter(new FileOutputStream("Stat.htm") );
			}
		catch(IOException e)
			{

				System.out.print("Error opening the files!");
				System.exit(0);
			}
		dp.showStatus.setText("right!");
									dp.repaint() ;
		out.print("<html>"+
//		"<head>"+
//		"<meta http-equiv=\"Content-Language\" content=\"en-us\">"+
//		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\">"+
//		"<meta name=\"GENERATOR\" content=\"Microsoft FrontPage 4.0\">"+
//		"<meta name=\"ProgId\" content=\"FrontPage.Editor.Document\">"+
		"<title>Statistics</title>"+
//		"</head>"+
		"<body>"+
		"<p align=\"left\"><font face=\"Times New Roman\" size=\"4\"><b>Statistics</b></font></p>"+
		"<p align=\"left\"><font face=\"Times New Roman\" size=\"3\"><b><u>MOE's Results</u></b></font></p>"+
		"<table border=\"1\">"+
		" <tr>"+
			"<td align=\"center\"><font face=\"Times New Roman\" size=\"3\">MOE</font></td>"+
			"<td align=\"center\"><font face=\"Times New Roman\" size=\"3\">Value</font></td>"+
		  "</tr>"+
//		"<tr><td align=\"center\"><font face=\"Times New Roman\" size=\"3\">Average Speed</font></td><td><p align=\"center\">　<font face=\"Times New Roman\" size=\"3\">"+nd.avgSpeed+"</font></p></td></tr>"+
//		"<tr><td align=\"center\"><font face=\"Times New Roman\" size=\"3\">Average Flow</font></td><td><p align=\"center\">　<font face=\"Times New Roman\" size=\"3\">"+nd.avgVolume+"</font></p></td></tr>"+
//		"<tr><td align=\"center\"><font face=\"Times New Roman\" size=\"3\">vkt</font></td><td><p align=\"center\">　<font face=\"Times New Roman\" size=\"3\">"+nd.vkt+"</font></p></td></tr>"+
//		"<tr><td align=\"center\"><font face=\"Times New Roman\" size=\"3\">vht</font></td><td><p align=\"center\">　<font face=\"Times New Roman\" size=\"3\">"+nd.vht+"</font></p></td></tr>"+
		"</table>"+
		"<p><font face=\"Times New Roman\" size=\"3\"><b><u>Network Summary</u></b></font></p>"+
		"<table border=\"1\">"+
		  "<tr><td></td><td><font face=\"Times New Roman\" size=\"3\">Description or Value</font></td></tr>"+
		"<tr><td><font face=\"Times New Roman\" size=\"3\">0. Network Type</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.network .getSelectedItem() +"</font></td></tr>"+
		"<tr><td><font face=\"Times New Roman\" size=\"3\">1. Speed Distribution</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.speed .getSelectedItem() +"</font></td></tr>"+
		"<tr><td><font face=\"Times New Roman\" size=\"3\">2. Land use Distribution</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.landuse.getSelectedItem() +"</font></td></tr>"+
		"<tr><td><font face=\"Times New Roman\" size=\"3\">5. Travel Demand Model</font></td><td><font face=\"Times New Roman\" size=\"3\">"+"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">5.1 Value of Time</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v6.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">5.2 Friction Factor</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v10.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">6. Revenue Model</font></td><td><font face=\"Times New Roman\" size=\"3\">"+"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">6.1 Toll rate</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v13.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">6.2 Coeff. of length</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v14.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">6.3 Coeff. of speed</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v15.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">7. Cost Model</font></td><td><font face=\"Times New Roman\" size=\"3\">"+"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">7.1 Coeff. of length</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v17.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">7.2 Coeff. of flow</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v18.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">7.3 Coeff. of speed</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.v19.value() +"</font></td></tr>"+
//		"<tr><td><font face=\"Times New Roman\" size=\"3\">8 Investment Model</font></td><td><font face=\"Times New Roman\" size=\"3\">"+"</font></td></tr>"+

		
		"<tr><td><font face=\"Times New Roman\" size=\"3\">8.1 Travel Length Coeff.</font></td><td><font face=\"Times New Roman\" size=\"3\">"+vp.beltaScroll.value() +"</font></td></tr>"+
		"</table></body></html>");
		out.print("<html>"+
		"<tr><td><font face=\"Times New Roman\" size=\"3\">OD Table</font></td><td><font face=\"Times New Roman\" size=\"3\">"+"</font></td></tr>"+
		"</table></body></html>");
					for (int i=1;i<dg.numNodes+1;i++)
					{
						for (int j=1;j<dg.numNodes+1;j++)
						{
							out.print("<html>"+
							"<tr><td><font face=\"Times New Roman\" size=\"3\">"+dg.evolve.od[i][j]+"</font></td><td><font face=\"Times New Roman\" size=\"3\">"+"</font></td>"+
							"</table></body></html>");
						}
						out.print("<html>"+"</tr>"+"</table></body></html>");
					}
		out.close();

	}

///	total 23 variable are allocated to get the parameters of models
/// some of them are 'visible' in the interface
/// the others are 'invisible' and are fixed by default
/// this method is used to give the values of some 'invisible' variables

	public void writeVariables(){
		belta = vp.beltaScroll.value;
	}


	class DrawPanel extends Panel {

		Demo sd;

		Panel legend=new Panel();
		Panel button=new Panel();;
		Panel status=new Panel();;
		
///////////////////////////////////////////////
		public Choice whichAttribute = new Choice ();
		public Choice scale = new Choice ();
		//Button help=new Button("Help");
		Label blank=new Label("    ");
		Button evolve = new Button("Evolve");

		Button statistics=new Button("Statistics");

		
		Button removeTraceWorker = new Button("Remove Trace");
		Button traceWorker = new Button("Trace Worker");
		

////////////////////////////////////////////////

		public Label unit=new Label("");

		Label blue=new Label("    ");
		Label green=new Label("    ");
		Label yellow=new Label("    ");
		Label orange=new Label("    ");
		Label red=new Label("    ");

		public Label bluefor=new Label("            ");
		public Label greenfor=new Label("            ");
		public Label yellowfor=new Label("            ");
		public Label orangefor=new Label("            ");
		public Label redfor=new Label("            ");

////////////////////////////////////////////////
		public Label showStatus=new Label("");


		public DrawPanel( Demo sd) {
			showStatus.setFont(new Font("",Font.BOLD,12));
			this.sd = sd;
			setLayout(new BorderLayout());

//			button panel

			whichAttribute.addItem("Volume");
			whichAttribute.addItem("V/C");
			whichAttribute.select("Volume");
			drawVolume=true;
			whichAttribute.addItemListener(this.sd);

			scale.addItem("Absolute");
			scale.addItem("Relative");
			scale.select("Absolute");
			scale.addItemListener(this.sd);



			evolve.addActionListener(this.sd);
			
			removeTraceWorker.addActionListener(this.sd);
			traceWorker.addActionListener(this.sd);
			statistics.addActionListener( this.sd);

			evolve.setEnabled(false);
			statistics.setEnabled(false);

			removeTraceWorker .setEnabled(false) ;
			traceWorker.setEnabled(false) ;
			scale.setEnabled( false);
			whichAttribute.setEnabled(false) ;



			button.add(evolve);
			button.add(blank);
			button.add(scale);
			button.add(whichAttribute);
			
			
//			button.add(editIndicator);
//			button.add( removeTraceWorker );
//			button.add( traceWorker);
			button.add(new Label("   "));
			button.add(statistics);

			add(button,"South");
			

						
//			legend panel
			legend.setLayout( new GridLayout(1,11));

			if (currentInputFile == "TC_linkinfo.txt" || true){
				red.setBackground(new Color(200, 20, 20));
				legend.add(red);
				legend.add(redfor);
				redfor.setText("Freeway");
				
				orange.setBackground(new Color(250, 125, 0));
				legend.add(orange);
				legend.add(orangefor);
				orangefor.setText("Divided Arterial");
				
				yellow.setBackground(Color.YELLOW );
				legend.add(yellow);
				legend.add(yellowfor);
				yellowfor.setText("Undivided Arterial");
				
				green.setBackground(new Color(8, 140, 14));
				legend.add(green);
				legend.add(greenfor);
				greenfor.setText("Collector");
				
				blue.setBackground(new Color(60, 100, 250));
				legend.add(blue);
				legend.add(bluefor);
				bluefor.setText("Ramps");
				
			}else{
				blue.setBackground(new Color(60, 100, 250));
				legend.add(blue);
				legend.add(bluefor);
	
				green.setBackground(new Color(8, 140, 14));
				legend.add(green);
				legend.add(greenfor);
	
				yellow.setBackground(Color.YELLOW );
				legend.add(yellow);
				legend.add(yellowfor);
	
				orange.setBackground(new Color(250, 125, 0));
				legend.add(orange);
				legend.add(orangefor);
	
	
				red.setBackground(new Color(200, 20, 20));
				legend.add(red);
				legend.add(redfor);
				if (scale.getSelectedItem() =="Absolute"){
					if (whichAttribute.getSelectedIndex() ==0)
					{
						unit.setText("");
						bluefor.setText("0~" + Integer.toString(400));
						greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
						yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
						orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
						redfor.setText(Integer.toString(1600) +"~"+ "  ");
						repaint();
					}
					else
					{
						unit.setText("");
						drawVolume = false;
						bluefor.setText("0~" + 0.2);
						greenfor.setText(""+0.2+"~" + 0.4);
						yellowfor.setText(""+0.4+"~" + 0.6);
						orangefor.setText(""+0.6+"~" + 0.8);
						redfor.setText(""+0.8+"~" );
						repaint();
					}
	
				}
				else{
	
					unit.setText("");
					bluefor.setText("Lowest");
					greenfor.setText("Lower");
					yellowfor.setText("Middle");
					orangefor.setText("Higher");
					redfor.setText("Highest");
					repaint() ;
	
				}
			}


			add(legend,"North");


//			status	panel
			status.setLayout( new GridLayout(1,1));
			status.add(showStatus);
			add(status,"Center");


		}
	}

	class DrawArea extends Panel implements MouseListener {

		DrawPanel dp;
		Demo dm;
//		Panel editButtonPanel = new Panel();

		int Scale;	// Scale of magnification or diminision; scale=dim/Max
		int Trans;	// translation
		int dim;      // size of the DrawArea,, which is equal to the number of pixes of the draw area
		int radius;   //Radius of circle that represents a node
		Dimension d;  //Current Dimension of the DrawArea (dynamic variable)
		Dimension sd;
		int Max;   // Maximum number of cells
//		boolean mouseclicked;
		int mouseX;
		int mouseY;
		
		Frame fPopup;
		boolean popupShow;
		
		boolean shift;
		
		int n;
		int currentYear = 0;

		float c1,c2,c3,c4; //used to decide which color to use

		public DrawArea(DrawPanel dp, Demo dm) {
			addMouseListener(this);
			this.dp = dp;
			this.dm = dm;
			
			setLayout(new BorderLayout() );
			add("South", dp );
//			editPanel arranges all button for edit the draw area;
//			editButtonPanel.setLayout(new GridLayout(25,1,1,5));
			
			
//Arrange Buttons for Map Editing
//			Label temp = new Label("");
//			editButtonPanel.add( temp);
//			temp = new Label("");
//			editButtonPanel.add( temp);
//			Label title = new Label("    Map Editing Panel       ", Label.CENTER);
//			title.setFont(new Font("",Font.BOLD,18));
//			editButtonPanel.add( title);
//						
//			editButtonPanel.add(delete);
//			editButtonPanel.add(addNode);
//			editButtonPanel.add(addLink);					
//			editButtonPanel.add( editProperty);
//			editButtonPanel.add( editNetworkChoose );
//			editButtonPanel.add(editIndicator);
//			add("East",editButtonPanel);
//			System.out.println("/////////////////////////////////////Start Load SiouxFalls.jpg");
//			ImageIcon icon = new ImageIcon("SiouxFalls.jpeg");
//			JLabel label = new JLabel(icon,JLabel.CENTER);
//			add("Center",label);
			

		}


		void setMapVariables() {
				Max =5;
				int maxX=5;
				int maxY=5;
				for (int i=1;i<dg.numNodes+1;i++)
				{
					if (dg.node[i].xCoord>maxX)
					{
						maxX=dg.node[i].xCoord;
					}
					if (dg.node[i].yCoord>maxY)
					{
						maxY=dg.node[i].yCoord;
					}
				}
				if (maxX+4>Max)
				{
					Max=maxX+4;
				}
				if (maxY+4>Max)
				{
					Max = maxY+4;
				}
				
			System.out.println("Max:"+Max);
			sd = getToolkit().getScreenSize();
			//System.out.print(sd.width +"\t"+sd.height);
			d=getSize() ;

			//System.out.println(" Dimension of the DrawArea: width =  "+d.width + "  height = " + d.height );

			dim = (int)    (      (d.width<d.height) ? (0.90*d.width) : (0.90*d.height)        );

			//System.out.println("dim = "+ dim);

			if(Max != 0){
				Scale = (int)(dim/Max);
			} else {
				System.out.println("From DrawArea class Max variable is 0. Erorr!!!!!");
				Scale = 2;
			}
			if(Scale == 0)
				Scale = 1;

			Trans = (int) (0.05*dim);

			radius = (int) (Scale);

			if(radius == 0)
				radius = 1;
			//System.out.println("Trans = "+Trans+";  radius = "+ radius);
			//System.out.println("End of setScale()!!!!!");

		}


//// network will be drawn for the current year
		private void drawLinks_Speed(Graphics g) {
			float factor;
			int min, max;
			min=0;
			max=5;
			float maxVC,minVC;
			minVC=1;
			maxVC=0;
			if (evolved)
			{	
				////////find smallest and largest flow
				for (int i=1;i<dg.numNodes+1;i++)
					{
						for (int j=1;j<dg.numNodes+1;j++)
						{
							if(dg.arc[i][j] != null){
								if (dg.arc[i][j].flow>max)
								{
									max=dg.arc[i][j].flow;	
								}
								if(dg.arc[i][j].flow<min)
								{
									min=dg.arc[i][j].flow;
								}
							}
						}
					}
				//////find smallest and largest vc
				for (int i=0;i<dg.numLink;i++)
				{
					if (dg.arc[dg.link[i].oNode][dg.link[i].dNode].vc > maxVC)
					{
						maxVC = dg.arc[dg.link[i].oNode][dg.link[i].dNode].vc;
					}
					if (dg.arc[dg.link[i].oNode][dg.link[i].dNode].vc < minVC)
					{
						minVC = dg.arc[dg.link[i].oNode][dg.link[i].dNode].vc;
					}
				}
			}
			
			int xcoord[] = new int[5];
			int ycoord[] = new int[5];
			int endID;
			for (int i=1;i<dg.numNodes+1;i++)
			{
//				System.out.println("Draw links from Node"+i+"////////////////////");
				for (int j=0;j<dg.node[i].numDemandNodes;j++)
				{
//					System.out.println("links"+i+ev.node[i].demandNodes[j]);
					endID=dg.node[i].demandNodes[j];
//					System.out.println("Current Trans"+Trans+"Scale"+Scale+"Max"+Max);
					int startx, starty, endx, endy;
						startx = Trans+(int)(Scale*2) + (int)((dg.node[i].xCoord-1)*Scale);
						starty =   Trans- (int)(Scale*2)+ (int)(Scale*Max) - (int)((dg.node[i].yCoord-1)*Scale);
//											int k = nd.dg.EndNodeNumbers(i+1, j+1);
						endx = Trans+(int)(Scale*2) + (int)((dg.node[endID].xCoord-1)*Scale);
						endy =  Trans - (int)(Scale*2)+(int) (Scale* Max) - (int)((dg.node[endID].yCoord-1)*Scale);
					
					if(dg.arc[i][endID].numLanes == 1)
					{
						factor = (float)(0.30*Scale);						
					}else if(dg.arc[i][endID].numLanes == 2)
					{
						factor = (float)(0.50*Scale);
					}else if(dg.arc[i][endID].numLanes == 3)
					{
						factor = (float)(0.75*Scale);
					}else if(dg.arc[i][endID].numLanes >= 4)
					{
						factor = (float)(1.0*Scale);
					}else
					{
						factor = (float)(0.5*Scale);
					}
					
					if (currentInputFile ==	 "TC_linkinfo.txt"){
						if ((i==1618 && endID == 1622) || (i==1632 && endID == 1621)){
							factor = 10;
						}
					}
					
					if(evolved)
					{
					
						if ((dp.scale .getSelectedItem() =="Absolute") && (dp.whichAttribute.getSelectedItem() == "Volume"))
						{
							///absolute scale
							
							c1=400;
							c2=800;
							c3=1200;
							c4=1600;
							 if( dg.arc[i][endID].flow <=  c1  ) {
								g.setColor(new Color(60, 100, 250) );  /////Blue
								//g.setColor(new Color(150, 150, 150) );
//								factor = (float) (0.5*Scale);
								//count1++;
								}
							else if ( dg.arc[i][endID].flow <=  c2  ) {
								g.setColor(new Color(8, 140, 14) );   ////Green
								//g.setColor(new Color( 115, 115, 115) );
//								factor = (float) (0.75*Scale);
								//count2++;
								}
							else if ( dg.arc[i][endID].flow <=  c3  ) {
								g.setColor(Color.yellow);    ////// Yellow
								//g.setColor(new Color(70, 70, 70) );
//								factor = (float) (Scale);
								//count3++;
								}
							else if ( dg.arc[i][endID].flow <=  c4  ) {
								g.setColor(new Color(250, 125, 0));    ////// Oringe
								//g.setColor(new Color(70, 70, 70) );
//								factor = (float) (Scale);
								//count3++;
								}
							else {
								g.setColor(new Color (200, 20, 20) );   //// Red
								//g.setColor(new Color(25, 25, 25) );
//								factor = (float) (1.25*Scale);
								//count4++;
								}

						}
						else if ((dp.scale .getSelectedItem() =="Relative") && (dp.whichAttribute.getSelectedItem() == "Volume")){
						////relative scale
						float step = (max-min)/5;
						if( dg.arc[i][endID].flow <=  min+step  ) {
							  g.setColor(new Color(60, 100, 250) );  /////Blue
						  	//g.setColor(new Color(150, 150, 150) );sc
//							   factor = (float) (0.5*Scale);
							   //count1++;
						   }
						else if ( dg.arc[i][endID].flow <=  min+2*step  ) {
							   g.setColor(new Color(8, 140, 14) );   ////Green
							   //g.setColor(new Color( 115, 115, 115) );
//							  factor = (float) (0.75*Scale);
							   //count2++;
						   }
						else if ( dg.arc[i][endID].flow <=  min+3*step  ) {
							  g.setColor(Color.yellow);    ////// Yellow
							  //g.setColor(new Color(70, 70, 70) );
//							  factor = (float) (Scale);
							  //count3++;
						   }
						else if ( dg.arc[i][endID].flow <=  min+4*step  ) {
							   g.setColor(new Color(250, 125, 0));    ////// Oringe
							   //g.setColor(new Color(70, 70, 70) );
//							   factor = (float) (Scale);
							   //count3++;
						   }
						else {
							   g.setColor(new Color (200, 20, 20) );   //// Red
							   //g.setColor(new Color(25, 25, 25) );
//							   factor = (float) (1.25*Scale);
							   //count4++;
						   }
						}else if ((dp.scale .getSelectedItem() =="Absolute") && (dp.whichAttribute.getSelectedItem() == "V/C"))
						{
							float c1,c2,c3,c4;
							c1 = (float)0.2;
							c2 = (float)0.4;
							c3 = (float)0.6;
							c4 = (float)0.8;
							if( dg.arc[i][endID].vc <=  c1  ) {
								g.setColor(new Color(60, 100, 250) );  /////Blue
								//g.setColor(new Color(150, 150, 150) );
//								factor = (float) (0.5*Scale);
								//count1++;
							}
							else if ( dg.arc[i][endID].vc <=  c2  ) {
								g.setColor(new Color(8, 140, 14) );   ////Green
								//g.setColor(new Color( 115, 115, 115) );
//								factor = (float) (0.75*Scale);
								//count2++;
							}
							else if ( dg.arc[i][endID].vc <=  c3  ) {
								g.setColor(Color.yellow);    ////// Yellow
								//g.setColor(new Color(70, 70, 70) );
//								factor = (float) (Scale);
								//count3++;
							}
							else if ( dg.arc[i][endID].vc <=  c4  ) {
								g.setColor(new Color(250, 125, 0));    ////// Oringe
								//g.setColor(new Color(70, 70, 70) );
//								factor = (float) (Scale);
								//count3++;
							}
							else {
								g.setColor(new Color (200, 20, 20) );   //// Red
								//g.setColor(new Color(25, 25, 25) );
//								factor = (float) (1.25*Scale);
								//count4++;
							}
						}else if ((dp.scale .getSelectedItem() =="Relative") && (dp.whichAttribute.getSelectedItem() == "V/C"))
						{
							float step = (maxVC-minVC)/5;
							if( dg.arc[i][endID].vc <=  minVC+step ) {
								g.setColor(new Color(60, 100, 250) );  /////Blue
							}else if( dg.arc[i][endID].vc <=  minVC+step*2 ) {
								g.setColor(new Color(8, 140, 14) );   ////Green
							}else if( dg.arc[i][endID].vc <=  minVC+step*3 ) {
								g.setColor(Color.yellow);    ////// Yellow
							}else if( dg.arc[i][endID].vc <=  minVC+step*4 ) {
								g.setColor(new Color(250, 125, 0));    ////// Oringe
							}else {
								g.setColor(new Color (200, 20, 20) );   //// Red
							}
						}
					}else if ((!evolved) && (editNetwork) )
					{
						System.out.println("//////////////////////////////Edit Mode "+i+" "+endID+" "+dg.arc[i][endID].numLanes);
						if (dg.arc[i][endID].numLanes == 1)
						{
							g.setColor(new Color(60, 100, 250) );  /////Blue
						}else if (dg.arc[i][endID].numLanes == 2)
						{
							g.setColor(new Color(8, 140, 14) );   ////Green
						}else if (dg.arc[i][endID].numLanes == 3)
						{
							g.setColor(Color.yellow);    ////// Yellow
						}else if (dg.arc[i][endID].numLanes == 4)
						{
							g.setColor(new Color(250, 125, 0));    ////// Oringe
						}else
						{
							g.setColor(new Color (200, 20, 20) );   //// Red
						}
					}else if ((!evolved) && (!editNetwork) )
					{
//						factor = (float)(0.5*Scale);
						g.setColor(new Color(60, 100, 250) );    /////Blue
						if (currentInputFile == "TC_linkinfo.txt"){
							if (currentInputFile == "TC_linkinfo.txt"){
								if (dg.arc[i][endID].functional==1 || dg.arc[i][endID].functional==2){
									g.setColor(new Color(200, 20, 20));
								}else if (dg.arc[i][endID].functional==5){
									g.setColor(new Color(250, 125, 0));
								}else if (dg.arc[i][endID].functional==6){
									g.setColor(Color.YELLOW);
								}else if (dg.arc[i][endID].functional==7){
									g.setColor(Color.green);
								}else{
									g.setColor(Color.blue);
								}
								if ((i==1618 && endID == 1622) || (i==1632 && endID == 1621)){
									g.setColor(Color.black);
								}
							}
						}
					}
					int xerror, yerror;
					int x = endx - startx;
					int y = endy - starty;


					xerror = (int) (-factor*y/Math.sqrt(x*x+y*y));
					yerror = (int)(factor*x/Math.sqrt(x*x+y*y));

					int endxadd = endx+xerror, startxadd = startx+xerror;
					int endyadd = endy+yerror, startyadd = starty+yerror;

					xcoord[0] = startx-1;
					xcoord[1] = endx-1;
					xcoord[2] = endxadd;
					xcoord[3] = startxadd;
					xcoord[4] = startx-1;

					ycoord[0] = starty-1;
					ycoord[1] = endy-1;
					ycoord[2] = endyadd;
					ycoord[3] = startyadd;
					ycoord[4] = starty-1;

//					if((mouseclicked == true) && ((currentInputFile != "Grid2.txt") && (currentInputFile != "Grid5.txt")) && (!chooseAddNode))
					if((mouseclicked == true) && (!chooseAddNode))
					{
						int bStartX,bStartY,bStartXAdd,bStartYAdd,bEndX,bEndY,bEndXAdd,bEndYAdd;
						bStartX = startx+(int)(radius*x/Math.sqrt(x*x+y*y));
						bEndX = endx-(int)(radius*x/Math.sqrt(x*x+y*y));
						bStartXAdd = startxadd+(int)(radius*x/Math.sqrt(x*x+y*y));
						bEndXAdd = endxadd-(int)(radius*x/Math.sqrt(x*x+y*y));
						bStartY = starty+(int)(radius*y/Math.sqrt(x*x+y*y));
						bEndY = endy-(int)(radius*y/Math.sqrt(x*x+y*y));
						bStartYAdd = startyadd+(int)(radius*y/Math.sqrt(x*x+y*y));
						bEndYAdd = endyadd-(int)(radius*y/Math.sqrt(x*x+y*y));
						int tempYLow,tempYAbove,tempXLow,tempXAbove;
						
//						System.out.println("i"+i+"j"+j+"endId"+endID+"endx"+endx+"startx"+startx);
						if (startx==endx)
						{
							if (((bEndY-mouseY)*(mouseY-bStartY)>0) && ((bStartXAdd-mouseX)*(bStartX-mouseX)<0))
							{
								chooseLink = true;
								chooseNode = false;
								g.setColor(Color.red);
								chooseLinkStartNode = i;
								chooseLinkDemandID = j;
								vp.editProperty.setEnabled(true);
								System.out.println("/////////////////////////////////////////////// get one///");
							}
						}else if (starty == endy)
						{
							if (((bStartYAdd-mouseY)*(mouseY-bStartY)>0) && ((bStartX-mouseX)*(bEndX-mouseX)<0))
							{
								chooseLink = true;
								chooseNode = false;
								g.setColor(Color.red);
								chooseLinkStartNode = i;
								chooseLinkDemandID = j;
								vp.editProperty.setEnabled(true);
								System.out.println("/////////////////////////////////////////////// get one///");
							}
							
						}else
						{
							tempYLow = bStartY + (mouseX-bStartX)*(bEndY-bStartY)/(bEndX-bStartX);
							tempYAbove = bStartYAdd + (mouseX-bStartXAdd)*(bEndYAdd-bStartYAdd)/(bEndXAdd-bStartXAdd);
							tempXLow = bStartX + (mouseY-bStartY)*(bEndX-bStartX)/(bEndY-bStartY);
							tempXAbove = bStartXAdd + (mouseY-bStartYAdd)*(bEndXAdd-bStartXAdd)/(bEndYAdd-bStartYAdd);
							 if (((tempYLow-mouseY)*(tempYAbove-mouseY)<0) && ((tempXLow-mouseX)*(tempXAbove-mouseX)<0) && ((bStartX-mouseX)*(bEndXAdd-mouseX)<0))
							{
								chooseLink = true;
								chooseNode = false;
								g.setColor(Color.red);
								chooseLinkStartNode = i;
								chooseLinkDemandID = j;
								vp.editProperty.setEnabled(true);
								System.out.println("/////////////////////////////////////////////// get one///");
							}
						}
					}
					if (currentInputFile == "TC_linkinfo.txt"){
						if (dg.node[i].xCoord>0 && dg.node[i].xCoord<250 && dg.node[i].yCoord>0 && dg.node[i].yCoord<250 && dg.node[endID].xCoord>0 && dg.node[endID].xCoord<250 && dg.node[endID].yCoord>0 && dg.node[endID].yCoord<250){
							if ((i==1618 && endID == 1622) || (i==1632 && endID == 1621)){
								
							}else{
								g.fillPolygon(xcoord, ycoord, 5);
								g.setColor(Color.white);
								g.drawLine(startx, starty, endx, endy);
							}
						}
					}else{
						g.fillPolygon(xcoord, ycoord, 5);
						g.setColor(Color.white);
						g.drawLine(startx, starty, endx, endy);
					}
				}
			}
		}
		
		private void drawWorker_Trace(Graphics g)
		{
			float factor;
			int xcoord[] = new int[5];
			int ycoord[] = new int[5];
			
			if (traceWorker)
			{
				int uniformWorkerID=0;
				for(int i=1;i<workerOrigin;i++)
				{
					uniformWorkerID=uniformWorkerID+dg.node[i].numAuto;
				}
				uniformWorkerID=uniformWorkerID+workerID;
				uniformWorkerID=uniformWorkerID-1;
				int startx, starty, endx, endy;
				int oID,dID;
		//Paint the Trace of Worker
				for (int i=0;i<(int)dg.evolve.auto[uniformWorkerID].path_info[0]-1;i++)
				{
					oID=dg.evolve.auto[uniformWorkerID].path[i];
					dID=dg.evolve.auto[uniformWorkerID].path[i+1];
//					if ((currentInputFile != "Grid2.txt") && (currentInputFile != "Grid5.txt"))
//					{
						
						startx = Trans+(int)(Scale*2) + (int)((dg.node[oID].xCoord-1)*Scale);
						starty =   Trans- (int)(Scale*2)+ (int)(Scale*Max) - (int)((dg.node[oID].yCoord-1)*Scale);
//											int k = nd.dg.EndNodeNumbers(i+1, j+1);
						endx = Trans+(int)(Scale*2) + (int)((dg.node[dID].xCoord-1)*Scale);
						endy =  Trans - (int)(Scale*2)+(int) (Scale* Max) - (int)((dg.node[dID].yCoord-1)*Scale);
//											System.out.println("Startx"+startx+"Starty"+starty+"endx"+endx+"endy"+endy);						
//					}else
//					{
//						startx = Trans+(int)(Scale/2*3) + (int)((ev.node[oID].xCoord-1)*Scale*5);
//						starty =   Trans- (int)(Scale/2*3)+ (int)(Scale*Max) - (int)((ev.node[oID].yCoord-1)*Scale*5);
////											int k = nd.dg.EndNodeNumbers(i+1, j+1);
//						endx = Trans+(int)(Scale/2*3) + (int)((ev.node[dID].xCoord-1)*Scale*5);
//						endy =  Trans - (int)(Scale/2*3)+(int) (Scale* Max) - (int)((ev.node[dID].yCoord-1)*Scale*5);
////											System.out.println("Startx"+startx+"Starty"+starty+"endx"+endx+"endy"+endy);
//					}
					factor = (float)0.2*Scale;
					int xerror, yerror;
										int x = endx - startx;
										int y = endy - starty;


										xerror = (int) (factor*y/Math.sqrt(x*x+y*y));
										yerror = (int)(-factor*x/Math.sqrt(x*x+y*y));

										int endxadd = endx+xerror, startxadd = startx+xerror;
										int endyadd = endy+yerror, startyadd = starty+yerror;

										xcoord[0] = startx-1;
										xcoord[1] = endx-1;
										xcoord[2] = endxadd;
										xcoord[3] = startxadd;
										xcoord[4] = startx-1;

										ycoord[0] = starty-1;
										ycoord[1] = endy-1;
										ycoord[2] = endyadd;
										ycoord[3] = startyadd;
										ycoord[4] = starty-1;

										g.setColor(Color.black);
										g.fillPolygon(xcoord, ycoord, 5);
										g.setColor(Color.white);
										g.drawLine(startx, starty, endx, endy);
				}
				if ((int)dg.evolve.auto[uniformWorkerID].path_info[0] <=1)
				{
					JOptionPane.showMessageDialog(menuframe,"Auto Stay at Origin","Auto Stay at Origin",JOptionPane.OK_OPTION);
					traceWorker = false;
					dp.traceWorker.setEnabled(true);
					dp.removeTraceWorker.setEnabled(false);
					vp.setEnabled(true);
				}
			}
		}


		private void paintCells(Graphics g) {
			//int noOfLines;
			float sizeofcell;
			int sizeOfGrid;

			g.setColor(new Color(220, 220, 220) );

			sizeofcell = (Scale);
			sizeOfGrid = (int) (Scale * (Max));
			System.out.println("sizeofcell:"+sizeofcell);
			System.out.println("sizeofGrid:"+sizeOfGrid);
			System.out.println("Max:"+Max);
			int iteration;
	//		if (currentInputFile=="SiouxFalls.txt")
//			if ((currentInputFile != "Grid2.txt") && (currentInputFile != "Grid5.txt"))
//			{
				iteration=Max;
//			}else
//			{
//				iteration=Max+1;
//			}
			for(int i=1; i<=iteration; i++) {
				g.drawLine(Trans, sizeOfGrid+(int)(Trans-(i-1)*sizeofcell), Trans+sizeOfGrid, sizeOfGrid+(int) (Trans-(i-1)*sizeofcell) );   /// draw lines parallel to x-axis
				g.drawLine((int)(Trans+(i-1)*sizeofcell),  Trans,  (int)(Trans+(i-1)*sizeofcell),  sizeOfGrid+Trans);
			}

		}

		private void paintDG(Graphics g) {
			int dummyNodeId = 0;
			////  Draw Speed boxes
			g.setColor(Color.black);
			drawLinks_Speed(g);
			drawWorker_Trace(g);
			System.out.println("Draw the Nodes start");
//			System.out.println("Scale:"+Scale);
//			System.out.println("Trans:"+Trans);
			///// Draw Nodes
			if (evolved)
			{
				int minOrigin,maxOrigin;
				int origin[]=new int[dg.numNodes+1];
				for (int i=1;i<dg.numNodes+1;i++)
				{
					origin[i]=0;
				}
				for (int i=1;i<dg.numNodes+1;i++)
				{
					for (int j=1;j<dg.numNodes+1;j++)
					{
//						System.out.println("OD"+i+j+":"+dg.od[i][j]);
						origin[i]=origin[i]+dg.evolve.od[i][j];
					}
				}
				minOrigin=0;
				maxOrigin=5;
				for (int i=1;i<dg.numNodes+1;i++)
				{
					if (origin[i]>maxOrigin)
					{
						maxOrigin = origin[i];
					}
					if (origin[i]< minOrigin)
					{
						minOrigin = origin[i];
					}
				}
			//	if (currentInputFile=="SiouxFalls.txt")
//				if ((currentInputFile != "Grid2.txt") && (currentInputFile != "Grid5.txt"))
//				{
				for(int i = 1; i< dg.numNodes+1; i++) {
					g.setColor(Color.black);
					int newx, newy;
					newx = (int)(Scale*2)+Trans + (int)((dg.node[i].xCoord-1)*Scale);
					newy  = (int)(Scale*Max)-(int)(Scale*2) - (int)((dg.node[i].yCoord-1)*Scale) + Trans;
					int odRadius;
//					System.out.println("minOrigin"+minOrigin+"maxOrigin"+maxOrigin);
					odRadius = (int)((origin[i]-minOrigin)*radius/2/(maxOrigin-minOrigin)+radius/2);
//					System.out.println("Node"+i+":"+"x"+newx+"y"+newy+"odRadius"+odRadius);
					g.fillOval(newx-(int)(odRadius/2) , newy-(int)(odRadius/2), odRadius, odRadius);
				}
			}
			else if (editNetwork)   //not evolved
			{
			//	if (currentInputFile=="SiouxFalls.txt")
//				if ((currentInputFile != "Grid2.txt") && (currentInputFile != "Grid5.txt"))
//				{
					
				for(int i = 1; i< dg.numNodes+1; i++) {
					g.setColor(Color.black);
					
					int newx, newy;
					newx = (int)(Scale*2)+Trans + (int)((dg.node[i].xCoord-1)*Scale);
					newy  = (int)(Scale*Max)-(int)(Scale*2) - (int)((dg.node[i].yCoord-1)*Scale) + Trans;
					//Choose Node or Link without adding
					if (mouseclicked && (chooseLink == false) && (!chooseAddNode) && (!chooseAddLink))
					{
						if ((Math.abs(mouseX-newx)<radius) && (Math.abs(mouseY-newy)<radius) )
						{
							g.setColor(Color.red);
							chooseNodeID = i;
							chooseNode = true;
							vp.editProperty.setEnabled(true);
						}
					}
					//Choose Origin and Destination for link to add
					else if ( mouseclicked && chooseAddLink && (!chooseAddNode))
					{
						//Choose the origin
						if (!shift)
						{
							if ((Math.abs(mouseX-newx)<radius) && (Math.abs(mouseY-newy)<radius) )
								{
									g.setColor(Color.ORANGE);
									addLinkOrigin = i;
									addLinkDestination = 0;
									oNodeFound=true;
									dNodeFound = false;
									dNodeChoosen = false;
									vp.editProperty.setEnabled(false);							
								}
						}
						//Choose the destination
						else if (oNodeFound && (shift))
						{
							//Keep the Origin
							if (shift && i==addLinkOrigin)
							{
								g.setColor(Color.ORANGE);
							}
							if ((Math.abs(mouseX-newx)<radius) && (Math.abs(mouseY-newy)<radius) )
							{
								if (i!=addLinkOrigin)
								{
									g.setColor(Color.ORANGE);
									addLinkDestination = i;
									dNodeFound = true;
									vp.editProperty.setEnabled(true);
								}
							}
						}
						//Rechoose orgin
						else if ((!oNodeFound) && shift)
						{
							if ((Math.abs(mouseX-newx)<radius) && (Math.abs(mouseY-newy)<radius) )
							{
								g.setColor(Color.ORANGE);
								addLinkOrigin = i;
								addLinkDestination = 0;
								oNodeFound=true;
								dNodeFound = false;
								dNodeChoosen = false;
								vp.editProperty.setEnabled(false);
							}
						}
					}else if (mouseclicked && chooseAddNode && (!chooseAddLink))
					{
						if ((Math.abs(mouseX-newx)<radius) && (Math.abs(mouseY-newy)<radius) )
						{
							dummyNodeId = i;
						}
					}
					g.fillOval(newx-(int)(radius/2) , newy-(int)(radius/2), radius, radius);
				}
///////////////End of for;
					if (chooseAddLink && (oNodeFound == false) && (dNodeChoosen == false))
					{
						oNodeChoosen = false;
						vp.editIndicator.setText("Please choose the origin for new link");
						
					}else if (chooseAddLink && (oNodeFound == true) && (dNodeFound == false))
					{
						dNodeChoosen = false;
						vp.editIndicator.setText("Please choose the Destination for new link");
					}else if (chooseAddLink && (oNodeFound == true) && (dNodeFound == true))
					{
						oNodeChoosen = false;
						dNodeChoosen = false;
						vp.editProperty.setEnabled(true);
						vp.editIndicator.setText("Press Property to accomplish");
					}
					
					
					
					//Add temperary Node for furthing clarify its property;
					if (mouseclicked && chooseAddNode && (!chooseAddLink) && (dummyNodeId == 0))
					{
						int newx = mouseX;
						int newy  = mouseY;
						g.setColor(Color.CYAN);
						g.fillOval(newx-(int)(radius/2) , newy-(int)(radius/2), radius, radius);
						vp.editProperty.setEnabled(true);
						vp.editIndicator.setText("Press Property to accomplish");
					}
			}else   ////Neither evolved nor editNetwork
			{
				if (currentInputFile == "TC_linkinfo.txt"){
					for(int i = 1; i< dg.numNodes+1; i++) {
						if (dg.node[i].xCoord>0 && dg.node[i].xCoord<250 && dg.node[i].yCoord>0 && dg.node[i].yCoord<250){
							g.setColor(Color.black);
							int newx, newy;
							newx = (int)(Scale*2)+Trans + (int)((dg.node[i].xCoord-1)*Scale);
							newy  = (int)(Scale*Max)-(int)(Scale*2) - (int)((dg.node[i].yCoord-1)*Scale) + Trans;
							g.fillOval(newx-(int)(radius/2/2) , newy-(int)(radius/2/2), radius/2, radius/2);
						}
					}
				}else{
					for(int i = 1; i< dg.numNodes+1; i++) {
						g.setColor(Color.black);
						int newx, newy;
						newx = (int)(Scale*2)+Trans + (int)((dg.node[i].xCoord-1)*Scale);
						newy  = (int)(Scale*Max)-(int)(Scale*2) - (int)((dg.node[i].yCoord-1)*Scale) + Trans;
						g.fillOval(newx-(int)(radius/2) , newy-(int)(radius/2), radius, radius);
					}
				}
			}

		}



		public void paint(Graphics g) {
//			System.out.println("/////////////////////////////////////Start Load SiouxFalls.jpg");
//						ImageIcon icon = new ImageIcon("SiouxFalls.jpeg");
//						JLabel label = new JLabel(icon,JLabel.CENTER);
//						da.add("Center",label);
////////////////Load Image for SiouxFalls network
//			if (currentInputFile != "Grid2.txt" && currentInputFile != "Grid5.txt")
//			{
//			
//			d=getSize() ;
//
//						//System.out.println(" Dimension of the DrawArea: width =  "+d.width + "  height = " + d.height );
//
//						dim = (int)    (      (d.width<d.height) ? (0.90*d.width) : (0.90*d.height)        );
////			Image picture = getToolkit().getImage("SiouxFalls.jpg");
//			Image picture = getImage(url,"SiouxFalls.jpg");
//			int x,y,height,width;
//			height = picture.getHeight(this);
//			width = picture.getWidth(this);
//			x=(int)(0.1*d.width);
//			y=(int)(0.1*d.height);
//			System.out.println("///////////x"+x+"y"+y);
//			g.drawImage(picture,x,y,this);
//			}
			
////////////Draw network
			if  (graphRead) {
				
				paintCells(g);
				paintDG(g);
			}

		}
//		///////////////////////////////////////////////////////////
//		Reaction to Mouse Action
		  public void mouseClicked(MouseEvent me)
		  {
		  	shift = false;
		  	if ((me.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK)
		  	{
		  		shift=true;
		  	}
		  	
//		  	if ((editNetwork == true) && (chooseAddNode == false) && (chooseAddLink == false))
//		  	{
//		  		mouseclicked = true;
//		  		chooseLink = false;
//		  		chooseNode = false;
//		  		mouseX = me.getX();
//		  		mouseY = me.getY();
//		  		da.setMapVariables();
//		  		da.repaint();
//		  	}else if ((editNetwork == true) && (chooseAddNode == true) && (chooseAddLink == false))
//		  	{
//		  		mouseclicked = true;
//		  		mouseX=me.getX();
//		  		mouseY=me.getY();
//		  		vp.editProperty.setEnabled(true);
//				da.setMapVariables();
//				da.repaint();
//		  	}else if ((editNetwork == true) && (chooseAddNode == false) && (chooseAddLink == true))
//		  	{
//				
//				if (oNodeChoosen == false)
//				{
//					mouseclicked = true;
//					mouseX=me.getX();
//					mouseY=me.getY();
//					vp.editIndicator.setText("Choose the destination of the link");
//					oNodeChoosen = true;
//				}else if ( (oNodeChoosen == true) && (shift == true) && (dNodeChoosen == false) )
//				{
//					mouseclicked = true;
//					mouseX=me.getX();
//					mouseY=me.getY();
//					dNodeChoosen = true;
//				}
//				da.setMapVariables();
//				da.repaint();
//		  	}
			 if (editNetwork && (me.getButton() == MouseEvent.BUTTON1))
			 {
			 	chooseLink = false;
			 	chooseNode = false;
			 	mouseclicked = true;
			 	mouseX = me.getX();
			 	mouseY = me.getY();
			 	da.setMapVariables();
			 	da.repaint();
			 }
			 
			 
			 			 	  	
		  }
	
		  public void mouseEntered(MouseEvent me)
		  {
		  }
	
		  public void mouseExited (MouseEvent me)
			  {
			  }
		
		  public void mousePressed (MouseEvent me)
		  {
//			System.out.println("/////////////////////////Component:"+me.getComponent().getName());
//			System.out.println("/////////////////////////Parent:"+me.getComponent().getParent().getName());
//			System.out.println("/////////////////////////Location:"+me.getComponent().getLocation().getX()+" "+me.getComponent().getLocation().getY());
			if (me.getButton() == MouseEvent.BUTTON3)
						 {
							System.out.println("//////////////////////////////Enter Right Clicked");
							setMapVariables();
							int rightX = me.getX();
							int rightY = me.getY();
							boolean rightNode = false;
							boolean rightLink = false;
							int nodeID = -1;
							int linkOID = -1, linkDID = -1;
							int linkID = -1;
				
							///////Find if rightclick for a Node;
							for (int i=1;i<dg.numNodes+1;i++)
							{
								int newx, newy;
								newx = (int)(Scale*2)+Trans + (int)((dg.node[i].xCoord-1)*Scale);
								newy  = (int)(Scale*Max)-(int)(Scale*2) - (int)((dg.node[i].yCoord-1)*Scale) + Trans;
								if ((Math.abs(rightX-newx)<radius) && (Math.abs(rightY-newy)<radius) )
								{
									nodeID = i;
									rightNode = true;
									break;
								}	
							}
							///////Find if rightclick for a Link;
							if (rightNode == false)
							{
								int startID,endID;
								int startx, starty, endx, endy;
								float factor;
								int xerror, yerror;
								int x,y;
								int startxadd,endxadd,startyadd,endyadd;
								int xcoord[] = new int[5];
								int ycoord[] = new int[5];
								int bStartX,bStartY,bStartXAdd,bStartYAdd,bEndX,bEndY,bEndXAdd,bEndYAdd;
								int tempYLow,tempYAbove,tempXLow,tempXAbove;
					
								for (int i=0;i<dg.numLink;i++)
								{
									startID = dg.link[i].oNode;
									endID = dg.link[i].dNode;
									startx = Trans+(int)(Scale*2) + (int)((dg.node[startID].xCoord-1)*Scale);
									starty =   Trans- (int)(Scale*2)+ (int)(Scale*Max) - (int)((dg.node[startID].yCoord-1)*Scale);
									endx = Trans+(int)(Scale*2) + (int)((dg.node[endID].xCoord-1)*Scale);
									endy =  Trans - (int)(Scale*2)+(int) (Scale* Max) - (int)((dg.node[endID].yCoord-1)*Scale);
						
									if(dg.arc[startID][endID].numLanes == 1)
									{
										factor = (float)(0.25*Scale);						
									}else if(dg.arc[startID][endID].numLanes == 2)
									{
										factor = (float)(0.50*Scale);
									}else if(dg.arc[startID][endID].numLanes == 3)
									{
										factor = (float)(0.75*Scale);
									}else if(dg.arc[startID][endID].numLanes >= 4)
									{
										factor = (float)(1.0*Scale);
									}else
									{
										factor = (float)(0.5*Scale);
									}
						
									x = endx - startx;
									y = endy - starty;
									xerror = (int) (-factor*y/Math.sqrt(x*x+y*y));
									yerror = (int)(factor*x/Math.sqrt(x*x+y*y));
									endxadd = endx+xerror;
									startxadd = startx+xerror;
									endyadd = endy+yerror;
									startyadd = starty+yerror;
						
									xcoord[0] = startx-1;
									xcoord[1] = endx-1;
									xcoord[2] = endxadd;
									xcoord[3] = startxadd;
									xcoord[4] = startx-1;

									ycoord[0] = starty-1;
									ycoord[1] = endy-1;
									ycoord[2] = endyadd;
									ycoord[3] = startyadd;
									ycoord[4] = starty-1;
						
									bStartX = startx+(int)(radius*x/Math.sqrt(x*x+y*y));
									bEndX = endx-(int)(radius*x/Math.sqrt(x*x+y*y));
									bStartXAdd = startxadd+(int)(radius*x/Math.sqrt(x*x+y*y));
									bEndXAdd = endxadd-(int)(radius*x/Math.sqrt(x*x+y*y));
									bStartY = starty+(int)(radius*y/Math.sqrt(x*x+y*y));
									bEndY = endy-(int)(radius*y/Math.sqrt(x*x+y*y));
									bStartYAdd = startyadd+(int)(radius*y/Math.sqrt(x*x+y*y));
									bEndYAdd = endyadd-(int)(radius*y/Math.sqrt(x*x+y*y));
						
									if (startx==endx)
									{
										if (((bEndY-rightY)*(rightY-bStartY)>0) && ((bStartXAdd-rightX)*(bStartX-rightX)<0))
										{
											linkOID = startID;
											linkDID = endID;
											linkID = i;
											rightLink = true;
											System.out.println("///////////////////////////////////right click// get one///");
											break;
										}
									}else if (starty == endy)
									{
										if (((bStartYAdd-rightY)*(rightY-bStartY)>0) && ((bStartX-rightX)*(bEndX-rightX)<0))
										{
											linkOID = startID;
											linkDID = endID;
											linkID = i;
											rightLink = true;
											System.out.println("///////////////////////////////////right click// get one///");
											break;
										}
									}else
									{
										tempYLow = bStartY + (rightX-bStartX)*(bEndY-bStartY)/(bEndX-bStartX);
										tempYAbove = bStartYAdd + (rightX-bStartXAdd)*(bEndYAdd-bStartYAdd)/(bEndXAdd-bStartXAdd);
										tempXLow = bStartX + (rightY-bStartY)*(bEndX-bStartX)/(bEndY-bStartY);
										tempXAbove = bStartXAdd + (rightY-bStartYAdd)*(bEndXAdd-bStartXAdd)/(bEndYAdd-bStartYAdd);
										if (((tempYLow-rightY)*(tempYAbove-rightY)<0) && ((tempXLow-rightX)*(tempXAbove-rightX)<0) && ((bStartX-rightX)*(bEndXAdd-rightX)<0))
										{
											linkOID = startID;
											linkDID = endID;
											linkID = i;
											rightLink = true;
											System.out.println("///////////////////////////////////right click// get one///");
											break;
										}
									}
								}
							}
							////////////PopupMenu
							if (rightNode)
							{
								PopupMenu mRight;
								MenuItem mIRight;
								mRight = new PopupMenu();
								try{
									mIRight = new MenuItem("Node:"+dg.node[nodeID].nodeId);
									mRight.add(mIRight);
									mIRight = new MenuItem("X:"+dg.node[nodeID].xCoord);
									mRight.add(mIRight);
									mIRight = new MenuItem("Y:"+dg.node[nodeID].yCoord);
									mRight.add(mIRight);
									mIRight = new MenuItem("Number of Workers:"+dg.node[nodeID].nodeWorkers);
									mRight.add(mIRight);
									mIRight = new MenuItem("Number of Jobs:"+dg.node[nodeID].originalJobs);
									mRight.add(mIRight);
									if (evolved)
									{
										mIRight = new MenuItem("Traffic Production:"+dg.node[nodeID].numAuto);
										mRight.add(mIRight);
										mIRight = new MenuItem("Traffic Attraction:"+dg.node[nodeID].numOppo);
										mRight.add(mIRight);
										mIRight = new MenuItem("Jobs Accessible in 5 mins:"+dg.nodeAcceJobs5[nodeID]);
										mRight.add(mIRight);
										mIRight = new MenuItem("Jobs Accessible in 10 mins:"+dg.nodeAcceJobs10[nodeID]);
										mRight.add(mIRight);
										mIRight = new MenuItem("Jobs Accessible in 20 mins:"+dg.nodeAcceJobs20[nodeID]);
										mRight.add(mIRight);
										mIRight = new MenuItem("Workers Accessible in 5 mins:"+dg.nodeAcceWorkers5[nodeID]);
										mRight.add(mIRight);
										mIRight = new MenuItem("Workers Accessible in 10 mins:"+dg.nodeAcceWorkers10[nodeID]);
										mRight.add(mIRight);
										mIRight = new MenuItem("Workers Accessible in 20 mins:"+dg.nodeAcceWorkers20[nodeID]);
										mRight.add(mIRight);
									}
								}catch (NullPointerException npe)
								{
									System.out.println("////////////////////NullpointerException node not found");
								}
//								System.out.println("/////////////////////////Component:"+me.getComponent().getName());
								da.add(mRight);
								mRight.show(me.getComponent(),me.getX(),me.getY());
							}else if (rightLink)
							{
								PopupMenu mRight;
								MenuItem mIRight;
								mRight = new PopupMenu();
								try{
									mIRight = new MenuItem("Link"+dg.link[linkID].linkID);
									mRight.add(mIRight);
									mIRight = new MenuItem("Origin Node:"+dg.link[linkID].oNode);
									mRight.add(mIRight);
									mIRight = new MenuItem("Destination Node:"+dg.link[linkID].dNode);
									mRight.add(mIRight);
									mIRight = new MenuItem("Capacity:"+dg.link[linkID].capacity+"(veh/h)");
									mRight.add(mIRight);
									mIRight = new MenuItem("Number of Lanes:"+dg.link[linkID].numLanes);
									mRight.add(mIRight);
									mIRight = new MenuItem("Free Flow Travel Time:"+dg.link[linkID].fft+"(min)");
									mRight.add(mIRight);
								//	mIRight = new MenuItem("Length:"+dg.link[linkID].length);
									mIRight = new MenuItem("Length:"+dg.arc[linkOID][linkDID].length+"(km)");
									mRight.add(mIRight);
									if (evolved)
									{
										mIRight = new MenuItem("Traffic Flow:"+dg.link[linkID].flow+"(veh)");
										mRight.add(mIRight);
										mIRight = new MenuItem("Volume Capacity Ratio:"+dg.link[linkID].vc);
										mRight.add(mIRight);
										mIRight = new MenuItem("Travel Time:"+dg.link[linkID].currentT+"(min)");
										mRight.add(mIRight);
									}
								}catch (NullPointerException npe)
								{
									System.out.println("////////////////////NullpointerException node not found");
								}
//								System.out.println("/////////////////////////Component:"+me.getComponent().getName());
								da.add(mRight);
								mRight.show(me.getComponent(),me.getX(),me.getY());
							}
							//////////////End of rightclick;
						 }
		  }
		
		  public void mouseReleased (MouseEvent me)
			  {
			  }


	}


///scrollPanel is used to define the scroll bars embeded in the variablePanel
	class ScrollPanel extends Panel implements AdjustmentListener{

		public double value;
		double maxvalue;
		double minvalue;
		int x;
		int y;
		int index;
		Label lvalue=new Label("");
		JScrollBar sb=new JScrollBar(JScrollBar.HORIZONTAL,0,1,0,101);
		DecimalFormat dFmt = new DecimalFormat("#.###");

		public ScrollPanel(double minvalue, double maxvalue,double defaultvalue,int index){

		setLayout(new GridBagLayout());
		value =defaultvalue;
		this.maxvalue =maxvalue;
		this.minvalue=minvalue;
		this.index=index;
		
		if(index==22)lvalue=new Label(Integer.toString((int)defaultvalue));	
		else if(index==99||index==100)lvalue.setText(Integer.toString((int) (100*defaultvalue))+"%");
		else lvalue=new Label(Double.toString(defaultvalue));
		
		sb.setValue ((int)Math.round(100*(defaultvalue-minvalue)/(maxvalue-minvalue)));

		sb.addAdjustmentListener( this);
		}


		public void adjustmentValueChanged(AdjustmentEvent ame){
		Object obj=ame.getSource() ;
		int arg=ame.getAdjustmentType() ;

			if(obj.equals(this.sb)){
				if(arg==AdjustmentEvent.TRACK){
					value=minvalue+(maxvalue-minvalue)*(double)sb.getValue()/100.0;
				}
				else if(arg==AdjustmentEvent.UNIT_INCREMENT){
					value+=(double)(maxvalue-minvalue) /100.0;
					}
				else if(arg==AdjustmentEvent.UNIT_DECREMENT){
					value-=(double)(maxvalue-minvalue) /100.0;
					}
				else if(arg==AdjustmentEvent.BLOCK_INCREMENT){
					value+=(double)(maxvalue-minvalue) /10.0;
					}
				else if(arg==AdjustmentEvent.BLOCK_DECREMENT){
					value-=(double)(maxvalue-minvalue) /10.0;
					}
			}

			////some limitations by the model
			//the toll rate can't be zero, or the revenue will be zero
			if(index==13)
				{
					if (value==0.0)value+=(double)(maxvalue-minvalue) /100.0;
				}

	/////update corresponding vp.variables[]...			
			value=Math.round(value*100.0)/100.0;
			lvalue.setText(dFmt.format(value));
			evolved = false;
			dp.evolve.setEnabled(true);
			dp.statistics.setEnabled(false);
			dp.traceWorker.setEnabled(false);
			this.repaint() ;


/////any changes in scroll bars will repaint the network

			//reset right-hand panel	
//			dp.scale.select( "Absolute");		
//			dp.whichAttribute.select ("Volume");
//			drawVolume=true;
//				
//			dp.bluefor.setText("0~" + Integer.toString(400));
//			dp.greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
//			dp.yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
//			dp.orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
//			dp.redfor.setText(Integer.toString(1600) +"~"+ "  ");
//			
//			//all variables in the right-hand panel, except "evolve" are set disabled			
//			vp.editProperty.setEnabled(false);
////			dp.editNetworkChoose .setEnabled(false);
//			dp.removeTraceWorker .setEnabled(false) ;
//			dp.traceWorker.setEnabled(false) ;
//			dp.statistics .setEnabled( false);
//			dp.whichAttribute.setEnabled(false) ;
//			dp.scale .setEnabled( false);
//			da.dp.evolve .setEnabled( true);
//			
//			da.repaint();
//			writeVariables();
			//System.out.print("ScrollBars Changed,writeVariables:\n");
			//for(int i=0;i<23;i++){
			//	System.out.print(i+"\t"+vp.variables [i]+"\n");
			//}
////////////////////End of Item changed;			
		}

		public float value(){
			return (float)value;
		}

	}


	class VariablesPanel extends Panel implements ActionListener, ItemListener ,WindowListener {

//		float variables[] = new float[23];
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints constraints=new GridBagConstraints();
		Label temp,temp2;

		Panel help=new Panel();
		Button forhelp=new Button();
		Button restore=new Button();
		Choice network=new Choice();

		Label yellowfor=new Label("            ");
		Choice speed=new Choice();

		Choice landuse=new Choice();

		Choice investment=new Choice();

		Choice pricing=new Choice();
//Edit Button		
		JButton editProperty;
		JButton addNode;
		JButton addLink;
		JButton delete;
		JButton editNetworkChoose;
		Label editIndicator;

//		ScrollPanel v6,v13,v10,v14,v15,v17,v18,v19,v20,v99,v100;
		ScrollPanel beltaScroll;
		ScrollPanel alphaScroll,betaScroll;
		ScrollPanel tripGRateScroll;
		ScrollPanel tripARateScroll;
		ScrollPanel peakHourRateScroll;
		ScrollPanel tripByAutoScroll;
		ScrollPanel autoOccupancyScroll;
		ScrollPanel constructionCostScroll;
		ScrollPanel thetaScroll;
		//// Constructor
		public VariablesPanel() {

			defaultVars();

			//setSize(250,1000);
			setLayout(gbl);

			help.setLayout(new FlowLayout() );
			forhelp=new Button("?");
			forhelp.addActionListener( this);
			forhelp.setFont( new Font("",Font.BOLD ,11));

			help.add(temp=new Label("--Please click"));
			temp.setAlignment( Label.RIGHT );
			temp.setFont(new Font("",Font.PLAIN|Font.ITALIC|Font.BOLD ,11));
			temp.setForeground( Color.black );
			help.add(forhelp);
			help.add(temp2=new Label("for HELP!--"));
			temp2.setFont(new Font("",Font.PLAIN|Font.ITALIC|Font.BOLD ,11));
			temp2.setForeground( Color.black  );

			restore=new Button("Restore");
			restore.addActionListener( this);
			
			editProperty = new JButton("Edit Property");
			editNetworkChoose = new JButton(" editNetwork ");
			editNetworkChoose.setFont(new Font("",Font.BOLD,18));
			editIndicator = new Label(  "          Network Fixed                           " , Label.CENTER );
			addNode = new JButton("   Add Node   ");
			addLink = new JButton("    Add Link   ");
			delete = new JButton("     Delete     ");
			
			delete.addActionListener(this);
			editProperty.addActionListener(this);
			editNetworkChoose.addActionListener(this);
			addNode.addActionListener(this);
			addLink.addActionListener(this);

			constraints.weightx =1.0;
			constraints.weighty=1.0;

			constraints.anchor=GridBagConstraints.WEST;
			constraints.fill=GridBagConstraints.HORIZONTAL ;
			//addComponent(0,0,4,1,help);

			addComponent(0,0,2,1,temp=new Label("1. Network Type"));
			temp.setFont(new Font("",Font.BOLD,14));

			//network.addItem(" ");
//			network.addItem("2X2 Grid Network");
//			network.addItem("5X5 Grid Network");
//			network.addItem("10X10 Grid Network");
			network.addItem("SiouxFalls Network");
			network.addItem("Twin Cities Network");
			network.addItem("Load from Clipboard...");
			network.addItem("Load from File...");
//			network.addItem("2X2 Grid Network");
//			network.addItem("A  Network  with  River");

			addComponent(2,1,2,1,network);
			network.addItemListener( this);


//			addComponent(0,2,2,1,temp=new Label("1. Speed Distribution"));
//			temp.setFont(new Font("",Font.BOLD,13));
//
//
//
//			speed.addItem("Uniform");
//			speed.addItem("Random");
//			speed.addItem("Prespecified Random");
//			addComponent(2,2,2,1,speed);
//			speed.select( "Uniform");
//			speed.addItemListener(this);
//
//			v99=new ScrollPanel(0.2,2.2,1,99);
//			addComponent(0,3,1,1,temp=new Label("     Speed Multiplier:"));
//			addComponent(2,3,2,1,v99.sb);
//			addComponent(1,3,1,1,v99.lvalue );
//			v99.lvalue .setAlignment( Label.RIGHT );
//
//
//			addComponent(0,4,2,1,temp=new Label("2. Land use Distribution"));
//			temp.setFont(new Font("",Font.BOLD,13));
//
//			landuse.addItem("Uniform");
//			landuse.addItem("Random");
//			landuse.addItem("Downtown");
//			landuse.addItem("Prespecified Random");
//
//			addComponent(2,4,2,1,landuse);
//			landuse.select("Uniform");
//			landuse.addItemListener(this);
//
//			v100=new ScrollPanel(0.2,2.2,1,100);
//			addComponent(0,5,1,1,temp=new Label("     Land Use Multiplier:"));
//			addComponent(2,5,2,1,v100.sb);
//			addComponent(1,5,1,1,v100.lvalue );
//			v100.lvalue .setAlignment( Label.RIGHT );
//
//			//addComponent(0,4,2,1,temp=new Label("3. Investment Rules"));
//			//temp.setFont(new Font("",Font.BOLD,13));
//
//			investment.addItem("Self Consumption");
//			investment.addItem("Revenue         Sharing");
//			//addComponent(2,4,2,1,investment);
//			investment.select("Self Consumption");
//			investment.addItemListener(this);
//			investment.setEnabled(false) ;
//
//			//addComponent(0,5,2,1,temp=new Label("4. Pricing Policies"));
//			//temp.setFont(new Font("",Font.BOLD,13));
//
//			pricing.addItem("Decreasing Toll");
//			pricing.addItem("Decreasing    Parking");
//			//addComponent(2,5,2,1,pricing);
//			pricing.select("Decreasing Toll");
//			pricing.addItemListener(this);
//			pricing.setEnabled(false);
//
//
//			
//			constraints.fill=GridBagConstraints.HORIZONTAL ;
//			constraints.anchor=GridBagConstraints.WEST;
//			addComponent(0,7,2,1,temp=new Label("3. Travel Demand Model"));
//			temp.setFont(new Font("",Font.BOLD,13));
//			addComponent(2,7,2,1,temp=new Label(""));
//
//			v6=new ScrollPanel(0,5,1,6);
//			addComponent(0,8,1,1,new Label("     3.1 Value of time"));
//			addComponent(2,8,2,1,v6.sb);
//			addComponent(1,8,1,1,v6.lvalue);
//			v6.lvalue .setAlignment( Label.RIGHT );
//
//			v10=new ScrollPanel(0,1,0.01,10);
//			addComponent(0,9,1,1,new Label("     3.2 Friction factor"));
//			addComponent(2,9,2,1,v10.sb);
//			addComponent(1,9,1,1,v10.lvalue);
//			v10.lvalue .setAlignment( Label.RIGHT );
//
//			addComponent(0,11,4,1,temp=new Label("4. Revenue Model"));
//			temp.setFont(new Font("",Font.BOLD,13));
//
//			v13=new ScrollPanel(0.5,1.5,1,13);
//			addComponent(0,12,1,1,new Label("     4.1 Toll rate"));
//			addComponent(2,12,2,1,v13.sb);
//			addComponent(1,12,1,1,v13.lvalue);
//			v13.lvalue .setAlignment( Label.RIGHT );
//			
//			v14=new ScrollPanel(0,1.5,1,14);
//			addComponent(0,13,1,1,new Label("     4.2 Coeff. of length"));
//			addComponent(2,13,2,1,v14.sb);
//			addComponent(1,13,1,1,v14.lvalue);
//			v14.lvalue .setAlignment( Label.RIGHT );
//			
//
//			v15=new ScrollPanel(0,1,0,15);
//			addComponent(0,14,1,1,new Label("     4.3 Coeff. of speed"));
//			addComponent(2,14,2,1,v15.sb);
//			addComponent(1,14,1,1,v15.lvalue);
//			v15.lvalue .setAlignment( Label.RIGHT );
//			
//			addComponent(0,17,4,1,temp=new Label("5. Cost Model"));
//			temp.setFont(new Font("",Font.BOLD,13));
//
//			v17=new ScrollPanel(0,1.2,1,17);
//			addComponent(0,18,1,1,new Label("     5.1 Coeff. of length"));
//			addComponent(2,18,2,1,v17.sb);
//			addComponent(1,18,1,1,v17.lvalue);
//			v17.lvalue .setAlignment( Label.RIGHT );
//			
//
//			v18=new ScrollPanel(0,1.2,0.75,18);
//			addComponent(0,19,1,1,new Label("     5.2 Coeff. of flow"));
//			addComponent(2,19,2,1,v18.sb);
//			addComponent(1,19,1,1,v18.lvalue);
//			v18.lvalue .setAlignment( Label.RIGHT );
//			
//
//			v19=new ScrollPanel(0,1.2,0.75,19);
//			addComponent(0,20,1,1,new Label("     5.3 Coeff. of speed"));
//			addComponent(2,20,2,1,v19.sb);
//			addComponent(1,20,1,1,v19.lvalue);
//			v19.lvalue .setAlignment( Label.RIGHT );
			
			addComponent(0,21,1,1,new Label("       "));
			addComponent(1,21,1,1,new Label("                   "));
			addComponent(2,21,1,1,new Label("       "));
			
			
			addComponent(0,22,4,1,temp=new Label("3. Global Variables:"));
			temp.setFont(new Font("",Font.BOLD,14));

//			v20=new ScrollPanel(0,1,1,20);

			tripGRateScroll = new ScrollPanel(0,2,1.0,23);
			addComponent(0,23,1,1,temp = new Label("3.1 Trip Production Rate"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,23,2,1,tripGRateScroll.sb);
			addComponent(1,23,1,1,tripGRateScroll.lvalue);
			tripGRateScroll.lvalue.setAlignment(Label.RIGHT);
						
			tripARateScroll = new ScrollPanel(0,2,1.0,24);
			addComponent(0,24,1,1,temp = new Label("3.2 Trip Attraction Rate"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,24,2,1,tripARateScroll.sb);
			addComponent(1,24,1,1,tripARateScroll.lvalue);
			tripARateScroll.lvalue.setAlignment(Label.RIGHT);
			
			peakHourRateScroll = new ScrollPanel(0.01,1,0.15,25);
			addComponent(0,25,1,1,temp = new Label("3.3 Peak Hour Rate"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,25,2,1,peakHourRateScroll.sb);
			addComponent(1,25,1,1,peakHourRateScroll.lvalue);
			peakHourRateScroll.lvalue.setAlignment(Label.RIGHT);


			beltaScroll = new ScrollPanel(0,1,0.15,20);
			addComponent(0,26,1,1,temp = new Label("3.4 Travel Length Coefficient"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,26,2,1,beltaScroll.sb);
			addComponent(1,26,1,1,beltaScroll.lvalue);
			beltaScroll.lvalue .setAlignment( Label.RIGHT );
			
			thetaScroll = new ScrollPanel(0,10,1,29);
			addComponent(0,27,1,1,temp = new Label("3.5 theta"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,27,2,1,thetaScroll.sb);
			addComponent(1,27,1,1,thetaScroll.lvalue);
			thetaScroll.lvalue.setAlignment(Label.RIGHT);
			
			tripByAutoScroll = new ScrollPanel(0,1,0.8,26);
			addComponent(0,28,1,1,temp = new Label("3.6 Auto Mode Share"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,28,2,1,tripByAutoScroll.sb);
			addComponent(1,28,1,1,tripByAutoScroll.lvalue);
			tripByAutoScroll.lvalue.setAlignment(Label.RIGHT);
			
			autoOccupancyScroll = new ScrollPanel(1,5,1.2,27);
			addComponent(0,29,1,1,temp = new Label("3.7 Auto Occupancy"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,29,2,1,autoOccupancyScroll.sb);
			addComponent(1,29,1,1,autoOccupancyScroll.lvalue);
			autoOccupancyScroll.lvalue.setAlignment(Label.RIGHT);
			
			alphaScroll = new ScrollPanel(0,1,0.15,21);
			addComponent(0,30,1,1,temp = new Label("3.8 Alpha for BPR Function"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,30,2,1,alphaScroll.sb);
			addComponent(1,30,1,1,alphaScroll.lvalue);
			alphaScroll.lvalue.setAlignment(Label.RIGHT);
			
			betaScroll = new ScrollPanel(0,10,4,22);
			addComponent(0,31,1,1,temp = new Label("3.9 Beta for BPR Function"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,31,2,1,betaScroll.sb);
			addComponent(1,31,1,1,betaScroll.lvalue);
			betaScroll.lvalue.setAlignment(Label.RIGHT);
								
			constructionCostScroll = new ScrollPanel(0,10000000,3000000,28);
			addComponent(0,32,1,1,temp = new Label("3.10 Cost $/lane*kilometer"));
			temp.setFont(new Font("",Font.ITALIC,12));
			addComponent(2,32,2,1,constructionCostScroll.sb);
			addComponent(1,32,1,1,constructionCostScroll.lvalue);
			constructionCostScroll.lvalue.setAlignment(Label.RIGHT);
			
						
			addComponent(0,13,2,1,temp=new Label("2. Editing Option"));
			temp.setFont(new Font("",Font.BOLD,14));
			
//			addComponent(0,4,4,1,temp=new Label("3. Choose Worker to Trace"));
//			temp.setFont(new Font("",Font.BOLD,13));
//			addComponent(0,5,1,1,new Label("Origin Node of Worker"));
//			choiceWorkerOrigin=new Choice();
//			choiceWorkerID=new Choice();
//			for (int i=1;i<dg.numNodes+1;i++)
//			{
//				choiceWorkerOrigin.addItem(""+i);
//			}
//			addComponent(1,5,1,1,choiceWorkerOrigin);
			
				
			constraints.fill=GridBagConstraints.NONE;
			constraints.anchor=GridBagConstraints.CENTER;		
			
			addComponent(0,14,1,1,editNetworkChoose);
			
			addComponent(0,16,1,1,addNode);
			addComponent(0,17,1,1,addLink);
			addComponent(0,18,1,1,editProperty);
			addComponent(0,19,1,1,delete);
			addComponent(0,20,6,1,editIndicator);
			
			constraints.fill=GridBagConstraints.NONE;
			constraints.anchor=GridBagConstraints.WEST;
			addComponent(0,35,2,1,temp=new Label("4. Default Value"));
			temp.setFont(new Font("",Font.BOLD,14));
			addComponent(0,36,2,1,restore);
			addComponent(3,36,1,1,temp=new Label(""));
			//addComponent(0,23,2,1,new Label(""));
			//addComponent(2,23,2,1,new Label(""));

			//addComponent(0,23,2,1,new Label(""));
			//addComponent(2,23,2,1,restore);

			//addComponent(0,25,2,1,new Label(""));
			//addComponent(2,25,2,1,new Label(""));

			editProperty.setEnabled(false);
			delete.setEnabled(false);
			addNode.setEnabled(false);
			addLink.setEnabled(false);
			
//			beltaScroll.sb.setEnabled(false);
//			alphaScroll.sb.setEnabled(false);
//			betaScroll.sb.setEnabled(false);
//			tripGRateScroll.sb.setEnabled(false);
//			tripARateScroll.sb.setEnabled(false);
//			peakHourRateScroll.sb.setEnabled(false);
//			tripByAutoScroll.sb.setEnabled(false);
//			autoOccupancyScroll.sb.setEnabled(false);
//			constructionCostScroll.sb.setEnabled(false);
//			thetaScroll.sb.setEnabled(false);
//			restore.setEnabled(false);
		}

		public void addComponent(int x, int y, int w, int h, Component c)
		{
		constraints.gridx=x;
			constraints.gridy=y;
			constraints.gridwidth=w;
			constraints.gridheight=h;

			gbl.setConstraints( c,constraints);

			add(c);
		}



		void defaultVars() {
			belta = 0.2;
//			variables[0] = (float) 5;	//speedmin
//			variables[1] = (float) 5;	//speedmax
//			variables[2] = (float) 0;  ///
//			variables[3] = (float) 10;	//landmin
//			variables[4] = (float) 10;	//landmax
//			variables[5] = (float) 0.0;   //// downtown?
//			variables[6] = (float) 1.0;		//volue of time
//			variables[7] = (float) 1.0;		//tax rate
//			variables[8] = (float) 1.0;		//length rate
//			variables[9] = (float) 0.0;		//speed rate
//			variables[10] = (float) 0.01;	//friction factor
//			variables[11] = (float) 1;	//symmetry?
//			variables[12] = (float) 1;	//avg speed?
//			variables[13] = (float) 1.0; //tax rate(toll rate)
//			variables[14] = (float) 1.0; //length
//			variables[15] = (float) 0;	//speed
//			variables[16] = (float) 365;	//cost rate
//			variables[17] = (float) 1.0;	//length coefficient
//			variables[18] = (float) 0.75;	//flow coefficient
//			variables[19] = (float) 0.75;	// speed coefficient
//			variables[20] = (float) 1.0;	//speed reduction factor
//			variables[21] = (float) 0;	//X
//			variables[22] = (float) 20;	//time period

		}




		public void actionPerformed( ActionEvent ae) {
			String arg=(String) ae.getActionCommand();
			Object obj = ae.getSource();
			
//			Choose to edit the network
				if(obj.equals(loadFromTextArea))
					{
						String s;
						s=loadContent.getText();
						boolean loadsuccess;
						loadsuccess = dg.loadNetwork(s);
						if (loadsuccess)
						{
							fLoadNetwork.dispose();
							networkModified = false;
							da.setMapVariables();
							graphRead = true;
							evolved = false;
							da.repaint();
						}else{
							JOptionPane.showMessageDialog(fLoadNetwork,"Network loading fails, please chech input file","Network Loading Error",JOptionPane.DEFAULT_OPTION);
						}
				  }
										
				  if(obj.equals(vp.editNetworkChoose))
				  {
					  networkModified = true;
					  dg.initialArc();
					  if (editNetwork == false)
					  {
						  evolved = false;
						  editNetwork = true;
						  chooseNode = false;
						  traceWorker = false;
						  chooseLink = false;
						  mouseclicked = false;
						  chooseAddNode = false;
						  chooseAddLink = false;
						  oNodeChoosen = false;
						  dNodeChoosen = false;
						  oNodeFound = false;
						  dNodeFound = false;
						  newNodeAccomplish = false;
						  vp.beltaScroll.sb.setEnabled(false);
						  vp.alphaScroll.sb.setEnabled(false);
						  vp.betaScroll.sb.setEnabled(false);
						  vp.tripGRateScroll.sb.setEnabled(false);
						  vp.tripARateScroll.sb.setEnabled(false);
						  vp.peakHourRateScroll.sb.setEnabled(false);
						  vp.tripByAutoScroll.sb.setEnabled(false);
						  vp.autoOccupancyScroll.sb.setEnabled(false);
						  vp.constructionCostScroll.sb.setEnabled(false);
						  vp.thetaScroll.sb.setEnabled(false);
									  dp.evolve.setEnabled(false);
									  dp.traceWorker.setEnabled(false);
									  dp.statistics.setEnabled(false);
									  vp.editProperty.setEnabled(true);
									  vp.addNode.setEnabled(true);
									  vp.addLink.setEnabled(true);
									  vp.delete.setEnabled(true);
									  vp.network.setEnabled(false);
									  vp.editIndicator.setText("Network Editing, Press again to quit editing state");
									  vp.editNetworkChoose.setForeground(Color.RED);
//									  vp.setEnabled(false);
						              dp.unit.setText("Lane");
									  dp.bluefor.setText("1");
									  dp.greenfor.setText("2");
									  dp.yellowfor.setText("3");
									  dp.orangefor.setText("4");
									  dp.redfor.setText(">=4");
									  dp.repaint() ;
									  da.repaint();
									  dp.setEnabled(false);						  
					  }else
					  {
						  editNetwork = false;
						  mouseclicked = false;
						  chooseAddNode = false;
						  chooseAddLink = false;
						  oNodeChoosen = false;
						  dNodeChoosen = false;
						  oNodeFound = false;
						  dNodeFound = false;
						  newNodeAccomplish = false;
								vp.beltaScroll.sb.setEnabled(true);
								vp.alphaScroll.sb.setEnabled(true);
								vp.betaScroll.sb.setEnabled(true);
								vp.tripGRateScroll.sb.setEnabled(true);
								vp.tripARateScroll.sb.setEnabled(true);
								vp.peakHourRateScroll.sb.setEnabled(true);
								vp.tripByAutoScroll.sb.setEnabled(true);
								vp.autoOccupancyScroll.sb.setEnabled(true);
								vp.constructionCostScroll.sb.setEnabled(true);
								vp.thetaScroll.sb.setEnabled(true);
						  vp.setEnabled(true);
						  dp.setEnabled(true);
						  dp.evolve.setEnabled(true);
						  vp.editProperty.setEnabled(false);
						  vp.addNode.setEnabled(false);
						  vp.addLink.setEnabled(false);
						  vp.delete.setEnabled(false);
						vp.network.setEnabled(true);
						  vp.addNode.setForeground(Color.BLACK);
						  vp.addLink.setForeground(Color.BLACK);
						  vp.editNetworkChoose.setForeground(Color.BLACK);
						  vp.editIndicator.setText("Network Fixed");
						dp.setEnabled(true);  
						if (dp.scale.getSelectedItem() =="Absolute" || currentInputFile != "TC_linkinfo.txt"){
										if (dp.whichAttribute.getSelectedIndex() ==0)
										{
											drawVolume = true;
											dp.unit.setText("");
											dp.bluefor.setText("0~" + Integer.toString(400));
											dp.greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
											dp.yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
											dp.orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
											dp.redfor.setText(Integer.toString(1600) +"~"+ "  ");
											dp.repaint();
										}
										else
										{
											dp.unit.setText("");
											drawVolume = false;
											dp.bluefor.setText("0~" + 0.2);
											dp.greenfor.setText(""+0.2+"~" + 0.4);
											dp.yellowfor.setText(""+0.4+"~" + 0.6);
											dp.orangefor.setText(""+0.6+"~" + 0.8);
											dp.redfor.setText(""+0.8+"~" );
											dp.repaint();
										}

									}
									else{

										dp.unit.setText("");
										dp.bluefor.setText("Lowest");
										dp.greenfor.setText("Lower");
										dp.yellowfor.setText("Middle");
										dp.orangefor.setText("Higher");
										dp.redfor.setText("Highest");
										dp.repaint() ;

									}
						  da.repaint();
					  }
			
				  }
//			/////////////////////////////////////////////
//			Edit the Property of Links
		
				  if(obj.equals(vp.editProperty))
				  {
					  Label temp;
			
					  if (chooseNode && (!chooseAddNode) )
					  {
						  fNodeEdit.dispose();
						  fNodeEdit = new Frame("Edit the Property of Node"+chooseNodeID);
						  fNodeEdit.setLayout(new GridLayout(6,2));
						  fNodeEdit.addWindowListener(this);
						  Dimension screensize = getToolkit().getScreenSize();
										  //define the size of menuframe according to the screen size
						  fNodeEdit.setSize ((int)(0.30*screensize.width),
																(int)(0.30*screensize.height));
				
						  fNodeEdit.add(temp = new Label("Node"+chooseNodeID+":"));
						  fNodeEdit.add(temp = new Label("",Label.CENTER));
						  fNodeEdit.add(temp = new Label("X:",Label.RIGHT));
						  textfieldX = new TextField(4);
						  textfieldX.setText(""+dg.node[chooseNodeID].xCoord);
						  fNodeEdit.add(textfieldX);
				
						  fNodeEdit.add(temp = new Label("Y:",Label.RIGHT));
						  textfieldY = new TextField(4);
						  textfieldY.setText(""+dg.node[chooseNodeID].yCoord);
						  fNodeEdit.add(textfieldY);
				
						  fNodeEdit.add(temp = new Label("Number of Workers",Label.RIGHT));
						  textfieldNodeWorkers = new TextField(4);
						  textfieldNodeWorkers.setText(""+dg.node[chooseNodeID].nodeWorkers);
						  fNodeEdit.add(textfieldNodeWorkers);
				
						  fNodeEdit.add(temp = new Label("Number of Jobs",Label.RIGHT));
						  textfieldNodeJobs = new TextField(4);
						  textfieldNodeJobs.setText(""+dg.node[chooseNodeID].originalJobs);
						  fNodeEdit.add(textfieldNodeJobs);
				
						  changeNodeProperty = new Button("Change");
						  fNodeEdit.add(changeNodeProperty);
						  changeNodeProperty.addActionListener(this);
				
						  noticeNodeProperty = new Label("Change properties and Press the Button");
						  fNodeEdit.add(noticeNodeProperty);
				
						  vp.editIndicator.setText("Edit Property and Press Button");
						  mouseclicked = false;
						  fNodeEdit.setVisible(true);
						  da.setEnabled(false);
				
				
					  }else if (chooseLink && (!chooseAddLink) )
					  {
						  fLinkEdit.dispose();
						  fLinkEdit = new Frame("Edit the Property of Link from Node"+chooseLinkStartNode+"to Node"+dg.node[chooseLinkStartNode].demandNodes[chooseLinkDemandID]);
						  fLinkEdit.setLayout(new GridLayout(5,2));
						  fLinkEdit.addWindowListener(this);
						  Dimension screensize = getToolkit().getScreenSize();
								  //define the size of menuframe according to the screen size
						  fLinkEdit.setSize ((int)(0.30*screensize.width),  (int)(0.30*screensize.height));
						  if (!chooseAddLink)
						  {
						  		fLinkEdit.add(temp = new Label("Link from Node"+chooseLinkStartNode+"to Node"+dg.node[chooseLinkStartNode].demandNodes[chooseLinkDemandID]));
						  		fLinkEdit.add(temp = new Label(""));
							
								int i=0;
								boolean linkfound = false;
							
								while (i<dg.numLink && linkfound == false)
								{
									if ((dg.link[i].oNode == chooseLinkStartNode) && (dg.link[i].dNode == dg.node[chooseLinkStartNode].demandNodes[chooseLinkDemandID]))
									{
										chooseLinkID = i;
										System.out.println("_____________________chooseLinkID"+chooseLinkID);
										linkfound = true;
									}
									i++;
								}
//						  fLinkEdit.add(temp = new Label("Length:",Label.RIGHT));
//						  textfieldArcLength = new TextField(4);
//						  textfieldArcLength.setText(""+ev.arc[chooseLinkStartNode][ev.node[chooseLinkStartNode].demandNodes[chooseLinkDemandID]].length);
//						  fLinkEdit.add(textfieldArcLength);
//				

							fLinkEdit.add(temp = new Label("Number of Lanes in Each Direction:",Label.RIGHT));
							textfieldNumberLanes = new TextField(8);
							textfieldNumberLanes.setText(""+dg.link[chooseLinkID].numLanes);
							fLinkEdit.add(textfieldNumberLanes);
							
							fLinkEdit.add(temp = new Label("Free Flow Travel Time:",Label.RIGHT));
							textfieldFFT = new TextField(8);
							textfieldFFT.setText(""+dg.link[chooseLinkID].fft);
							fLinkEdit.add(textfieldFFT);
						  }
						  else
						  {
							fLinkEdit.add(temp = new Label("Link from Node"+addLinkOrigin+"to Node"+addLinkDestination));
							fLinkEdit.add(temp = new Label(""));
							
							fLinkEdit.add(temp = new Label("Number of Lanes in This Direction:",Label.RIGHT));
							textfieldNumberLanes = new TextField(8);
							textfieldNumberLanes.setText("Number of lanes for new link");
							fLinkEdit.add(textfieldNumberLanes);
							
							fLinkEdit.add(temp = new Label("Free Flow Travel Time:",Label.RIGHT));
							textfieldFFT = new TextField(8);
							textfieldFFT.setText("Free Flow Travel time for new link");
							fLinkEdit.add(textfieldFFT);
							
						  }
						  changeLinkProperty = new Button("Change");
						  fLinkEdit.add(changeLinkProperty);
						  changeLinkProperty.addActionListener(this);
				
						  noticeLinkProperty = new Label("Change Link's Property and Press the Button");
						  fLinkEdit.add(noticeLinkProperty);
				
						  vp.editIndicator.setText("Edit Property and Press Button");
				
						  mouseclicked = false;
						  fLinkEdit.setVisible(true);
						  da.setEnabled(false);
					  }else if (chooseAddNode && (!newNodeAccomplish))
					  {
						  fNodeEdit.dispose();
						  fNodeEdit = new Frame("Edit the Property of New Node"+(dg.numNodes+1));
						  fNodeEdit.setLayout(new GridLayout(6,2));
						  fNodeEdit.addWindowListener(this);
						  Dimension screensize = getToolkit().getScreenSize();
							  //define the size of menuframe according to the screen size
						  fNodeEdit.setSize ((int)(0.30*screensize.width),
											(int)(0.30*screensize.height));
				
						  fNodeEdit.add(temp = new Label("Node"+(dg.numNodes+1)+":"));
						  fNodeEdit.add(temp = new Label("",Label.CENTER));
						  fNodeEdit.add(temp = new Label("X:",Label.RIGHT));
						  textfieldX = new TextField(4);
						  int xCoord = (int)((da.mouseX-da.Scale*2-da.Trans)/da.Scale)+1;
						  textfieldX.setText(""+xCoord);
						  fNodeEdit.add(textfieldX);
				
						  fNodeEdit.add(temp = new Label("Y:",Label.RIGHT));
						  textfieldY = new TextField(4);
						  int yCoord = (int)((da.Scale*da.Max-da.Scale*2+da.Trans-da.mouseY)/da.Scale)+1; 
						  textfieldY.setText(""+yCoord);
						  fNodeEdit.add(textfieldY);
				
						  fNodeEdit.add(temp = new Label("Number of Workers",Label.RIGHT));
						  textfieldNodeWorkers = new TextField(4);
						  textfieldNodeWorkers.setText(""+0);
						  fNodeEdit.add(textfieldNodeWorkers);
				
						  fNodeEdit.add(temp = new Label("Number of Jobs",Label.RIGHT));
						  textfieldNodeJobs = new TextField(4);
						  textfieldNodeJobs.setText(""+0);
						  fNodeEdit.add(textfieldNodeJobs);
				
						  changeNodeProperty = new Button("Change");
						  fNodeEdit.add(changeNodeProperty);
						  changeNodeProperty.addActionListener(this);
				
						  noticeNodeProperty = new Label("Change properties and Press the Button");
						  fNodeEdit.add(noticeNodeProperty);
				
						  vp.editIndicator.setText("Edit Property to Accomplish Adding Node");
						  fNodeEdit.setVisible(true);
						  da.setEnabled(false);
					  }else if (chooseAddLink && (oNodeFound == true) && (dNodeFound == true) )
					  {
						  boolean linkexist=false;
						  oppositeExist = false;
					//////Check if the link exist;
						  for (int i=0;i<dg.node[addLinkOrigin].numDemandNodes;i++)
						  {
							  if (dg.node[addLinkOrigin].demandNodes[i] == addLinkDestination)
							  {
								  linkexist=true;
								  vp.editIndicator.setText("Link Existed");
//								  da.editButtonPanel.setEnabled(false);
								  oNodeFound = false;
								  dNodeFound = false;
								  oNodeChoosen = false;
								  dNodeChoosen = false;
								  mouseclicked = false;
							  }
						  }
					//////Check if the opposite direction link exist;
						  for (int i=0;i<dg.node[addLinkDestination].numDemandNodes;i++)
						  {
						  	if(dg.node[addLinkDestination].demandNodes[i] == addLinkOrigin)
						  	{
						  		oppositeExist = true;
						  	}
						  }
						  if (linkexist == false)
						  {
							  fLinkEdit.dispose();
							  fLinkEdit = new Frame("Edit the Property of Link from Node"+addLinkOrigin+"to Node"+addLinkDestination);
							  fLinkEdit.setLayout(new GridLayout(6,2));
							  fLinkEdit.addWindowListener(this);
							  Dimension screensize = getToolkit().getScreenSize();
								  //define the size of menuframe according to the screen size
							  fLinkEdit.setSize ((int)(0.30*screensize.width),  (int)(0.30*screensize.height));
							  fLinkEdit.add(temp = new Label("Link from Node"+addLinkOrigin+"to Node"+addLinkDestination));
							  fLinkEdit.add(temp = new Label(""));
				
//							  fLinkEdit.add(temp = new Label("Length:",Label.RIGHT));
//							  textfieldArcLength = new TextField(4);
//						  System.out.println("________Add Link_____________Origin"+addLinkOrigin+"x"+dg.node[addLinkOrigin].xCoord+"y"+dg.node[addLinkOrigin].yCoord+"Destination"+addLinkDestination+"x"+dg.node[addLinkDestination].xCoord+"y"+dg.node[addLinkDestination].yCoord);
							  int l;
							  int x = dg.node[addLinkOrigin].xCoord-dg.node[addLinkDestination].xCoord;
							  int y = dg.node[addLinkOrigin].yCoord-dg.node[addLinkDestination].yCoord;
							  l = (int)Math.sqrt(x*x+y*y);
//						  System.out.println("__________________________________________________________l"+l);
//							  textfieldArcLength.setText(""+l);
//							  fLinkEdit.add(textfieldArcLength);
								
							fLinkEdit.add(temp = new Label("Number of Lanes:",Label.RIGHT));
							textfieldNumberLanes = new TextField(8);
							textfieldNumberLanes.setText("Number of lanes for new link");
							fLinkEdit.add(textfieldNumberLanes);
							
							float tempfft = (float)l/mapscale;
							fLinkEdit.add(temp = new Label("Free Flow Travel Time:",Label.RIGHT));
							textfieldFFT = new TextField(8);
							textfieldFFT.setText(""+tempfft);
							fLinkEdit.add(textfieldFFT);
							
							fLinkEdit.add(new Label(""));
							addOpposite = new JCheckBox("Add two-direction link?",false);
							addOpposite.setSelected(true);
							fLinkEdit.add(addOpposite);
								
							  changeLinkProperty = new Button("Change");
							  fLinkEdit.add(changeLinkProperty);
							  changeLinkProperty.addActionListener(this);
				
							  noticeLinkProperty = new Label("Change Link's Property and Press the Button");
							  fLinkEdit.add(noticeLinkProperty);
					
							  mouseclicked = false;
							  fLinkEdit.setVisible(true);
							  da.setEnabled(false);
						  }
							
								
					  }
				  }
		
				  if (obj.equals(changeNodeProperty))
				  {
					  boolean nodePropertyEditSuccess = true;
					  String msg;
					  int x=-1;
					  int y=-1;
					  int workers = -1;
					  int jobs = -1;
					  try 
					  {
						  msg=textfieldX.getText();
						  if (msg == null)
						  {
							  noticeNodeProperty.setText("X ordinate needed");
							  nodePropertyEditSuccess = false;
						  }else
						  {
							  x=Integer.parseInt(msg);
							  if (x>100 || x<0)
							  {
								  noticeNodeProperty.setText("X ordinate should be 0~100");
								  nodePropertyEditSuccess = false;
							  }
					
						  }	
				
					  }catch (NumberFormatException e)
					  {
						  noticeNodeProperty.setText("X ordinate should be Interger");
						  nodePropertyEditSuccess = false;
					  }
			
					  if (nodePropertyEditSuccess == true)
					  {
						  try{
							  msg=textfieldY.getText();
											  if (msg==null)
											  {
												  noticeNodeProperty.setText("Y ordinate needed");
												  nodePropertyEditSuccess = false;
											  }else
											  {
												  y=Integer.parseInt(msg);
												  if (y>100 || y<0)
												  {
													  noticeNodeProperty.setText("Y ordinate should be 0~100");
													  nodePropertyEditSuccess = false;
												  }
											  } 
						  }catch(NumberFormatException e)
						  {
							  noticeNodeProperty.setText("Y ordinate should be Interger");
							  nodePropertyEditSuccess = false;
						  }
				
					  }

						  if (nodePropertyEditSuccess == true)
								  {
									  try{
										  msg=textfieldNodeWorkers.getText();
														  if (msg==null)
														  {
															  noticeNodeProperty.setText("Workers of the Node needed");
															  nodePropertyEditSuccess = false;
														  }else
														  {
															  workers=Integer.parseInt(msg);
															  if (workers>100000 || workers<0)
															  {
																  noticeNodeProperty.setText("Number of Workers should be 0~100000");
																  nodePropertyEditSuccess = false;
															  }
														  } 
									  }catch(NumberFormatException e)
									  {
										  noticeNodeProperty.setText("Number of Workers should be Interger");
										  nodePropertyEditSuccess = false;
									  }
				
								  }
						  if (nodePropertyEditSuccess == true)
								  {
									  try{
											  msg=textfieldNodeJobs.getText();
											  if (msg==null)
											  {
												  noticeNodeProperty.setText("Jobs of the Node needed");
												  nodePropertyEditSuccess = false;
											  }else
											  {
												  jobs=Integer.parseInt(msg);
												  if (jobs>100000 || jobs<0)
												  {
													  System.out.println("///////////////////Acutal input jobs:"+jobs);
													  noticeNodeProperty.setText("Number of Jobs should be 0~100000");
													  nodePropertyEditSuccess = false;
												  }
											  } 
										  }catch(NumberFormatException e)
											  {
												  noticeNodeProperty.setText("Number of Jobs should be Interger");
												  nodePropertyEditSuccess = false;
											  }
				
								  }
					  if (nodePropertyEditSuccess && (!chooseAddNode))
					  {
						  evolved = false;
//						  dp.evolve.setEnabled(true);
				
						  dg.numWorkers = dg.numWorkers + workers - dg.node[chooseNodeID].nodeWorkers;
						  dg.node[chooseNodeID].xCoord=x;
						  dg.node[chooseNodeID].yCoord=y;
						  dg.node[chooseNodeID].nodeWorkers=workers;
						  dg.node[chooseNodeID].originalJobs=jobs;
//						  dg.reinitialization(chooseNodeID,(float)belta);
				
						  vp.editProperty.setEnabled(true);
						  vp.editIndicator.setText("Editing Accomplished");
				
//			Close the Property Editing Window
						  fNodeEdit.dispose();
						  da.setEnabled(true);
						  graphRead = true;
						  chooseNode = false;
						  da.setMapVariables();
						  da.repaint(); 			
					  }else if (nodePropertyEditSuccess && chooseAddNode)
					  {
						  evolved = false;
						  dg.addNode(x,y,workers,jobs,(float)belta);
						  newNodeAccomplish=false;
						  mouseclicked=false;
						  vp.editProperty.setEnabled(false);
				
						  fNodeEdit.dispose();
						  da.setEnabled(true);
						  chooseNode = false;
//						  graphRead = true;
						  da.setMapVariables();
						  da.repaint();
					  }
				
				  }
		
//			Change Link Properties
				  if (obj.equals(changeLinkProperty))
				  {
					boolean linkPropertyEditSuccess = true;
				  	String msg;
				  	int numLane =-1;
				  	float fft = -1;
				  	boolean addOppo;
				  	
					try{
							msg=textfieldNumberLanes.getText();
							if (msg == null)
							{
							  noticeLinkProperty.setText("Number of lanes needed");
							  linkPropertyEditSuccess = false;
							}else
							{
						  		numLane=Integer.parseInt(msg);
								if (numLane>10 || numLane<1)
						 		{
						  			noticeLinkProperty.setText("Number of lanes should be 1~99");
									linkPropertyEditSuccess = false;
						 		}
							}	
					  }catch (NumberFormatException e)
						 {
						  noticeLinkProperty.setText("Number of lanes should be Interger");
						  linkPropertyEditSuccess = false;
					 	 }
					 	 
					if (linkPropertyEditSuccess == true)
					{
					  try{
						  msg=textfieldFFT.getText();
						  if (msg==null)
						  {
							  noticeLinkProperty.setText("Free Flow Travel Time is needed");
							  linkPropertyEditSuccess = false;
						  }else
						  {
							  fft=(float)Float.parseFloat(msg);
							  if (fft>100 || fft<=0)
							  {
								  noticeLinkProperty.setText("Free Flow Travel Time should be 0~100");
								  linkPropertyEditSuccess = false;
							  }
						  } 
					  }catch(NumberFormatException e)
						  {
							  noticeLinkProperty.setText("Free Flow Travel Time should be float");
							  linkPropertyEditSuccess = false;
						  }
				
					}
					
				if (linkPropertyEditSuccess && (!chooseAddLink))
				{
					evolved = false;
			
				  	dg.link[chooseLinkID].numLanes = numLane;
				  	dg.link[chooseLinkID].fft = fft;
				  	dg.link[chooseLinkID].capacity = (float)CperLane*numLane;
				  					
//					vp.editProperty.setEnabled(false);
					vp.editIndicator.setText("Editing Accomplished");
				
//								Close the Property Editing Window
					fLinkEdit.dispose();
					da.setEnabled(true);
					graphRead = true;
					chooseLink = false;
					dg.initialArc();
					da.setMapVariables();
					da.repaint(); 			
				}else if (linkPropertyEditSuccess && chooseAddLink && oNodeFound && dNodeFound)
				 {
					evolved = false;
					dg.addLink(addLinkOrigin,addLinkDestination,(float)belta,numLane,fft);
					addOppo = addOpposite.isSelected();
					if (addOppo && (!oppositeExist))
					{
						dg.addLink(addLinkDestination,addLinkOrigin,(float)belta,numLane,fft);
					}
					oNodeFound = false;
					dNodeFound = false;
					oNodeChoosen = false;
					dNodeChoosen = false;
					mouseclicked = false;
					chooseLink = false;
					addLinkOrigin = 0;
					addLinkDestination = 0;
					vp.editProperty.setEnabled(false);
					da.setEnabled(true);
					fLinkEdit.dispose();
					da.setMapVariables();
					da.repaint();
				  }
					
			  }
		
//			Delete node or links
				  if (obj.equals(vp.delete))
				  {
					  if (chooseLink)
					  {
						  fDelete.dispose();
						  fDelete = new Frame("Link Delete Confirmation");
						  fDelete.addWindowListener(this);
						  fDelete.setLayout(new GridLayout(2,2));
						  Dimension screensize = getToolkit().getScreenSize();
														  //define the size of menuframe according to the screen size
										  fDelete.setSize ((int)(0.30*screensize.width),
																				(int)(0.30*screensize.height));
						  Label temp = new Label("Are you sure to delelte the link?",Label.CENTER);
						  temp.setFont(new Font("",Font.BOLD,18));
						  fDelete.add(temp);
						  fDelete.add(new Label(""));
						  deleteConfirm = new Button("OK");
						  deleteConfirm.setFont(new Font("",Font.BOLD,24));
						  deleteCancel = new Button("Cancel");
						  deleteCancel.setFont(new Font("",Font.BOLD,24));
						  deleteConfirm.addActionListener(this);
						  deleteCancel.addActionListener(this);
						  fDelete.add(deleteConfirm);
						  fDelete.add(deleteCancel);
				
						  fDelete.setVisible(true);											
						  da.setEnabled(false);
					  }else if(chooseNode)
					  {
						  fDelete.dispose();
						  fDelete = new Frame("Node Delete Confirmation");
						  fDelete.addWindowListener(this);
						  fDelete.setLayout(new GridLayout(2,2));
										  Dimension screensize = getToolkit().getScreenSize();
																		  //define the size of menuframe according to the screen size
														  fDelete.setSize ((int)(0.30*screensize.width),
																								(int)(0.30*screensize.height));
						  Label temp = new Label("Are you sure to delelte the Node?",Label.CENTER);
						  temp.setFont(new Font("",Font.BOLD,18));
						  fDelete.add(temp);
						  fDelete.add(new Label(""));
						  deleteConfirm = new Button("OK");
						  deleteConfirm.setFont(new Font("",Font.BOLD,24));
						  deleteCancel = new Button("Cancel");
						  deleteCancel.setFont(new Font("",Font.BOLD,24));
						  deleteConfirm.addActionListener(this);
						  deleteCancel.addActionListener(this);
						  fDelete.add(deleteConfirm);
						  fDelete.add(deleteCancel);
				
						  fDelete.setVisible(true);											
						  da.setEnabled(false);																	  
					  }
				  }
		
				  if (obj.equals(deleteConfirm))
				  {
					  if(chooseLink)
					  {
						  dg.deleteLink(chooseLinkStartNode,chooseLinkDemandID,(float)belta);
						  chooseNodeID = -1;
//						  dg.reinitialization(chooseNodeID,(float)belta);
						  dg.initialArc();
						  fDelete.dispose();
						  da.setEnabled(true);
						  chooseLink = false;
						  da.repaint();
					  }else if(chooseNode)
					  {
						  dg.deleteNode(chooseNodeID, (float) belta);
						  dg.reinitialization(chooseNodeID,(float)belta);
						  dg.initialArc();
						  fDelete.dispose();
						  chooseNode = false;
						  da.setEnabled(true);
						  da.repaint();
					  }
		
				  }
		
				  if (obj.equals(deleteCancel))
				  {
					  fDelete.dispose();
					  chooseNode = false;
					  chooseLink = false;
					  da.setEnabled(true);
				  }
		
//			Add Node in the network
				  if(obj.equals(vp.addNode))
				  {
					  if (chooseAddNode == false)
					  {
						  vp.delete.setEnabled(false);
						  vp.editProperty.setEnabled(false);
						  vp.addLink.setEnabled(false);
						  chooseAddNode=true;
						  newNodeAccomplish = false;
						  chooseAddLink = false;
						  mouseclicked = false;
						  vp.addNode.setForeground(Color.cyan);
						  vp.editIndicator.setText("Choose Property to Accomplish adding Node");
					  }else
					  {
						  vp.delete.setEnabled(true);
						  vp.editProperty.setEnabled(true);
						  vp.addLink.setEnabled(true);
						  chooseAddNode = false;
						  newNodeAccomplish = false;
						  mouseclicked=false;
						  vp.addNode.setForeground(Color.black);
						  vp.editIndicator.setText("Network Editing");
					  }
			
			
				  }
		
//			Add Link in the network
				  if(obj.equals(vp.addLink))
				  {
					  if(chooseAddLink == false)
					  {
						  vp.delete.setEnabled(false);
						  vp.editProperty.setEnabled(false);
						  vp.addNode.setEnabled(false);
						  chooseAddNode=false;
						  chooseAddLink=true;
						  mouseclicked = false;
						  oNodeChoosen=false;
						  dNodeChoosen = false;
						  oNodeFound = false;
						  dNodeFound = false;
						  vp.addLink.setForeground(Color.cyan);
						  vp.editIndicator.setText("Choose the Origin of Link");
					  }else
					  {
						  vp.delete.setEnabled(true);
						  vp.editProperty.setEnabled(true);
						  vp.addLink.setEnabled(true);
						  vp.addNode.setEnabled(true);
						  chooseAddNode=false;
						  chooseAddLink=false;
						  mouseclicked = false;
						  oNodeChoosen=false;
						  dNodeChoosen = false;
						  oNodeFound = false;
						  dNodeFound = false;
						  vp.addLink.setForeground(Color.black);
						  vp.editIndicator.setText("Editing");
					  }
			
				  }

			if(obj==forhelp){
				try { helpurl=new URL(url,"HelpFileSONG1.0.htm");

				 }

				catch (MalformedURLException e) {

				  System.out.println("Bad URL:" + helpurl);

				 }
				getAppletContext().showDocument(helpurl,"_blank");

			}
			
			else if (obj.equals(loadAccept))
					{
						loadFileName = address.getText();
						url=getCodeBase();
						InputStream fin = null;
						try{
							  //fin = new FileInputStream(inputFile);
							  fin = new URL(url, loadFileName).openStream();
						}catch(FileNotFoundException e) {
							fLoad.dispose();
							da.setEnabled(true);
							vp.setEnabled(true);
							return;
						}catch(IOException ioe){
		  		
						}
						currentInputFile = loadFileName;
						getnetwork = loadFileName;
						try {
								dg = new DirectedGraph(currentInputFile,url,linkInforInclude,demo);
								System.out.println("Construct new Evolve");
						} catch (IOException e) {
				
						}
						fLoad.dispose();
						da.setEnabled(true);
						vp.setEnabled(true);
						da.repaint();
						da.dp.evolve.setEnabled(true) ;
						da.dp.statistics .setEnabled(false);
						da.dp.traceWorker.setEnabled(false);
						vp.editNetworkChoose.setEnabled(true);
						da.setMapVariables();
						graphRead = true;
						evolved = false;
						da.currentYear = 0;
			}

			else if(obj==restore){
//				v99.value =(float)1;
//				v99.lvalue.setText ("100%");
//				v99.sb.setValue ((int)Math.round(100*(1.0-v99.minvalue)/(v99.maxvalue-vp.v99.minvalue)));
//
//				v100.value =(float)1;
//				v100.lvalue.setText ("100%");
//				v100.sb.setValue ((int)Math.round(100*(1.0-v100.minvalue)/(v100.maxvalue-vp.v100.minvalue)));
//				
//				v6.value =vp.variables [6]=1;
//				v6.lvalue.setText (Double.toString(1.0));
//				v6.sb.setValue ((int)Math.round(100*(1.0-v6.minvalue)/(v6.maxvalue-vp.v6.minvalue)));
//
//				v10.value =vp.variables [10]=(float)0.01;
//				v10.lvalue.setText (Double.toString(0.01));
//				v10.sb.setValue ((int)Math.round(100*(0.01-v10.minvalue)/(v10.maxvalue-vp.v10.minvalue)));
//
//				v13.value =vp.variables [13]=1;
//				vp.v13.lvalue.setText(Double.toString(1.0));
//				vp.v13.sb.setValue ((int)Math.round(100*(1.0-v13.minvalue)/(v13.maxvalue-vp.v13.minvalue)));
//				vp.v13.repaint() ;
//
//				v14.value =vp.variables [14]=1;
//				v14.lvalue.setText(Double.toString(1.0));
//				v14.sb.setValue ((int)Math.round(100*(1.0-v14.minvalue)/(v14.maxvalue-v14.minvalue)));
//
//				v15.value =vp.variables [15]=0;
//				v15.lvalue.setText(Double.toString(0.0));
//				v15.sb.setValue ((int)Math.round(100*(0.0-v15.minvalue)/(v15.maxvalue-v15.minvalue)));
//
//				v17.value =vp.variables [17]=1;
//				v17.lvalue.setText(Double.toString(1.0));
//				v17.sb.setValue ((int)Math.round(100*(1.0-v17.minvalue)/(v17.maxvalue-v17.minvalue)));
//
//				v18.value =vp.variables [18]=(float)0.75;
//				v18.lvalue.setText(Double.toString(0.75));
//				v18.sb.setValue ((int)Math.round(100*(0.75-v18.minvalue)/(v18.maxvalue-v18.minvalue)));
//
//				v19.value =vp.variables [19]=(float)0.75;
//				v19.lvalue.setText(Double.toString(0.75));
//				v19.sb.setValue ((int)Math.round(100*(0.75-v19.minvalue)/(v19.maxvalue-v19.minvalue)));

				beltaScroll.value = belta =0.15;
				beltaScroll.lvalue.setText(Double.toString(0.15));
				beltaScroll.sb.setValue ((int)Math.round(100*(0.15-beltaScroll.minvalue)/(beltaScroll.maxvalue-beltaScroll.minvalue)));
				
				alphaScroll.value = 0.15;
				alphaScroll.lvalue.setText(""+0.15);
				alphaScroll.sb.setValue((int)Math.round(100*(0.15-alphaScroll.minvalue)/(alphaScroll.maxvalue-alphaScroll.minvalue)));
				
				betaScroll.value = 4;
				betaScroll.lvalue.setText(""+4);
				betaScroll.sb.setValue((int)Math.round(100*(4-betaScroll.minvalue)/(betaScroll.maxvalue-betaScroll.minvalue)));
				
				tripGRateScroll.value = 1;
				tripGRateScroll.lvalue.setText(""+1);
				tripGRateScroll.sb.setValue((int)Math.round(100*(1-tripGRateScroll.minvalue)/(tripGRateScroll.maxvalue-tripGRateScroll.minvalue)));
				
				tripARateScroll.value = 1;
				tripARateScroll.lvalue.setText(""+1);
				tripARateScroll.sb.setValue((int)Math.round(100*(1-tripARateScroll.minvalue)/(tripARateScroll.maxvalue-tripARateScroll.minvalue)));
				
				peakHourRateScroll.value = 0.15;
				peakHourRateScroll.lvalue.setText(""+0.15);
				peakHourRateScroll.sb.setValue((int)Math.round(100*(0.15-peakHourRateScroll.minvalue)/(peakHourRateScroll.maxvalue-peakHourRateScroll.minvalue)));
				
				tripByAutoScroll.value = 0.8;
				tripByAutoScroll.lvalue.setText(""+0.8);
				tripByAutoScroll.sb.setValue((int)Math.round(100*(0.8-tripByAutoScroll.minvalue)/(tripByAutoScroll.maxvalue-tripByAutoScroll.minvalue)));
				
				autoOccupancyScroll.value = 1.2;
				autoOccupancyScroll.lvalue.setText(""+1.2);
				autoOccupancyScroll.sb.setValue((int)Math.round(100*(1.2-autoOccupancyScroll.minvalue)/(autoOccupancyScroll.maxvalue-autoOccupancyScroll.minvalue)));
				
				thetaScroll.value = 1;
				thetaScroll.lvalue.setText(""+1);
				thetaScroll.sb.setValue((int)Math.round(100*(1-thetaScroll.minvalue)/(thetaScroll.maxvalue-thetaScroll.minvalue)));
				
				constructionCostScroll.value = 3000000;
				constructionCostScroll.lvalue.setText(""+3000000);
				constructionCostScroll.sb.setValue((int)Math.round(100*(3000000-constructionCostScroll.minvalue)/(constructionCostScroll.maxvalue-constructionCostScroll.minvalue)));
	//reset right-hand panel	
				da.dp.scale.select( "Absolute");		
				da.dp.whichAttribute.select ("Volume");
				drawVolume=true;
				
				da.dp.bluefor.setText("0~" + Integer.toString(400));
				da.dp.greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
				da.dp.yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
				da.dp.orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
				da.dp.redfor.setText(Integer.toString(1600) +"~"+ "  ");
			
				//all variables in the right-hand panel, except "evolve" are set disabled			
				vp.editProperty.setEnabled(false);
				vp.editNetworkChoose .setEnabled(false);
				da.dp.removeTraceWorker .setEnabled(false) ;
				da.dp.traceWorker.setEnabled(false) ;
				da.dp.statistics .setEnabled( false);
				da.dp.whichAttribute.setEnabled(false) ;
				da.dp.scale .setEnabled( false);
				da.dp.evolve .setEnabled( true);

	///reload the network
	////
//				if(vp.network.getSelectedItem() .equals(" "))
//					{dp.evolve.setEnabled(false);}
//				else if(vp.network.getSelectedItem() .equals("10X10 Grid Network")){
//					dp.showStatus.setText("10X10 Network Loaded...");
//					dp.evolve.setEnabled(true) ;
//					dp.statistics .setEnabled(false);
//					vp.editNetworkChoose.setEnabled(false);
//					currentInputFile = "Grid10.txt";
//					getnetwork="10X10 Grid Network" ;
//					try {
//						dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//					} catch (IOException e) {
//					}
//
//				
//					da.setMapVariables();
//					graphRead = true;
//					evolved = false;
//					da.currentYear = 0;
////					da.dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//					da.repaint();
//				}
//				else if(vp.network.getSelectedItem() .equals("SiouxFalls Network")){
//					da.dp.showStatus.setText("SiouxFalls Network Loaded...");
//					da.dp.evolve.setEnabled(true) ;
//					da.dp.statistics .setEnabled(false);
//					da.dp.traceWorker.setEnabled(false);
//					vp.editNetworkChoose.setEnabled(true);
//					currentInputFile = "SiouxFalls.txt";
//					getnetwork="SiouxFalls Network" ;
//					try {
//						dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//					} catch (IOException e) {
//					}
//					networkModified = false;
//					da.setMapVariables();
//					graphRead = true;
//					evolved = false;
//					da.currentYear = 0;
////					da.dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//					da.repaint();
//				}
//				else if(vp.network.getSelectedItem().equals("2X2 Grid Network")){
//					da.dp.showStatus.setText("2X2 Network Loaded...");
//					da.dp.evolve.setEnabled(true) ;
//					da.dp.statistics .setEnabled(false);
//					da.dp.traceWorker.setEnabled(false);
//					vp.editNetworkChoose.setEnabled(false);
//					currentInputFile = "Grid2.txt";
//					getnetwork="2X2 Grid Network" ;
//				try {
//					dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//					} catch (IOException e) {
//					}
//					networkModified = false;
//					da.setMapVariables();
//					graphRead = true;
//					evolved = false;
//					da.currentYear = 0;
////					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//					da.repaint();
//				}
//
//				else if(vp.network.getSelectedItem() .equals("5X5 Grid Network")){
//					dp.showStatus.setText("5X5 Network Loaded...");
//					dp.evolve.setEnabled(true) ;
//					dp.statistics .setEnabled(false);
//					da.dp.traceWorker.setEnabled(false);
//					vp.editNetworkChoose.setEnabled(false);
//					currentInputFile = "Grid5.txt";
//					getnetwork="5X5 Grid Network" ;
//				try {
//					dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//					} catch (IOException e) {
//					}
//					networkModified = false;
//					da.setMapVariables();
//					graphRead = true;
//					evolved = false;
//					da.currentYear = 0;
////					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//					da.repaint();
//				}
//
//				else if(vp.network.getSelectedItem().equals("A  Network  with  River")){
//					dp.showStatus.setText("A  Network  with  River Loaded...");
//					dp.evolve.setEnabled(true) ;
//					dp.statistics .setEnabled(false);
//					da.dp.traceWorker.setEnabled(false);
//					vp.editNetworkChoose.setEnabled(false);
//					currentInputFile = "River.txt";
//					getnetwork="A  Network  with  River" ;
//					try {
//						dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//					} catch (IOException e) {
//					}
//
//					da.setMapVariables();
//					graphRead = true;
//					evolved = false;
//					da.currentYear = 0;
////					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//					da.repaint();
//				}
//				else if (arg.equals("Load"))
//				{
////									System.out.println("______________________________________Load_________________");
////														Label temp;
////														fLoad.dispose();
////														fLoad = new Frame("Load...");
////														fLoad.setLayout(new GridLayout(3,1));
////														fLoad.addWindowListener(this);
////														Dimension screensize = getToolkit().getScreenSize();
////																//define the size of menuframe according to the screen size
////														fLoad.setSize ((int)(0.30*screensize.width),
////																					  (int)(0.30*screensize.height));
////														temp = new Label("Input the name of file to load");
////														fLoad.add(temp);							
////														address = new JTextField("File Name");
////														fLoad.add(address);
////														loadAccept = new Button("Accept");
////														loadAccept.addActionListener(this);
////														fLoad.add(loadAccept);
////					
////														fLoad.setVisible(true);
////														da.setEnabled(false);
////														vp.setEnabled(false);
////														return;
//
////					chooser = new JFileChooser();
////					chooser.setCurrentDirectory(new File(""+url+"SiouxFalls.txt"));
////										int returnVal = chooser.showOpenDialog(this);
////										if (returnVal == JFileChooser.APPROVE_OPTION)
////										{
////											chooserFile = chooser.getSelectedFile();
//						FileDialog loadfile=new FileDialog(f,"Load Network...",FileDialog.LOAD);
//						loadfile.show() ;
//						chooserFile = new File(loadfile.getDirectory(),loadfile.getFile()  );
//											currentInputFile = chooserFile.getName();
//											try {
//													url = chooserFile.toURL();
//													dp.showStatus.setText("get url:"+url);
//											}catch (MalformedURLException me)
//											{
//												System.out.println("URL error");
//												dp.showStatus.setText("url error1");
//											}
//											linkInforInclude = true;
//											//evolved = false;
//																				
//											dp.showStatus.setText(currentInputFile+" Loaded...");
//											dp.evolve.setEnabled(true) ;
//											dp.statistics .setEnabled(false);
//											vp.editNetworkChoose.setEnabled(true);
//											getnetwork="Load" ;
////										if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-15;}
////										if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-15;}
//
//										try {
//											dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//										} catch (IOException e) {
//										}
//										networkModified = false;
//										da.setMapVariables();
//										graphRead = true;
//										evolved = false;
//										url=getCodeBase();
//										da.currentYear = 0;
////										dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//										da.repaint();
////										}
//				}

				writeVariables();
				//System.out.print("command restore,writeVariables:\n");
				//for(int i=0;i<23;i++){
				//	System.out.print(i+"\t"+vp.variables [i]+"\n");
				//}

			}
		}

		public void itemStateChanged( ItemEvent ie) {
			
			evolved = false;
			dp.evolve.setEnabled(true);
			dp.statistics.setEnabled(false);
			da.repaint();
			String arg=(String) ie.getItem();
			Object obj=ie.getSource();

			//reset right-hand panel	
			
			
			if (currentInputFile == "TC_linkinfo.txt" || true){
				
			}else{
				da.dp.scale.select( "Absolute");		
				da.dp.whichAttribute.select ("Volume");
				drawVolume=true;
				
				da.dp.bluefor.setText("0~" + Integer.toString(400));
				da.dp.greenfor.setText(Integer.toString(400) +"~" + Integer.toString(800));
				da.dp.yellowfor.setText(Integer.toString(800)+"~" + Integer.toString(1200));
				da.dp.orangefor.setText(Integer.toString(1200) +"~"+ Integer.toString(1600));
				da.dp.redfor.setText(Integer.toString(1600) +"~"+ "  ");
			}
			
			//all variables in the right-hand panel, except "evolve" are set disabled			
			vp.editProperty.setEnabled(false);
//			da.dp.editNetworkChoose .setEnabled(false);
			da.dp.removeTraceWorker .setEnabled(false) ;
			da.dp.traceWorker.setEnabled(false) ;
			da.dp.statistics .setEnabled( false);
			da.dp.whichAttribute.setEnabled(false) ;
			da.dp.scale .setEnabled( false);
			da.dp.evolve .setEnabled( true);


		/////Network
			if(obj.equals(vp.network)){
				if(arg.equals(" "))
					{dp.evolve.setEnabled(false);}
				else if(arg.equals("10X10 Grid Network")){
					dp.showStatus.setText("10X10 Network Loaded...");
					dp.evolve.setEnabled(true) ;
					dp.statistics .setEnabled(false);
					vp.editNetworkChoose.setEnabled(true);
					currentInputFile = "Grid10.txt";
					getnetwork="10X10 Grid Network" ;
//					if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-10;}
//					if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-10;}
					url = getClass().getResource(currentInputFile);
					try {
						dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
					} catch (IOException e) {
					}
					networkModified = false;
					da.setMapVariables();
					graphRead = true;
					evolved = false;
					da.currentYear = 0;
//					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
					da.repaint();
				}
				else if(arg.equals("SiouxFalls Network")){
					dp.showStatus.setText("SiouxFalls Network Loaded...");
					dp.evolve.setEnabled(true) ;
					dp.statistics .setEnabled(false);
					vp.editNetworkChoose.setEnabled(true);
					currentInputFile = "SiouxFalls.txt";
					getnetwork="SiouxFalls Network" ;
//					if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-15;}
//					if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-15;}
					url = getClass().getResource(currentInputFile);
					System.out.println("Right Location--------------"+url);
					try {
						dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
					} catch (IOException e) {
					}
					networkModified = false;
					da.setMapVariables();
					graphRead = true;
					evolved = false;
					da.currentYear = 0;
//					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
					da.repaint();
				}
				else if(arg.equals("2X2 Grid Network")){
					dp.showStatus.setText("2X2 Network Loaded...");
					dp.evolve.setEnabled(true) ;
					dp.statistics .setEnabled(false);
					vp.editNetworkChoose.setEnabled(true);
					currentInputFile = "Grid2.txt";
					getnetwork="2X2 Grid Network" ;
//					if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-20;}
//					if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-20;}
					url = getClass().getResource(currentInputFile);
					try {
						dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
					} catch (IOException e) {
					}
					networkModified = false;
					da.setMapVariables();
					graphRead = true;
					evolved = false;
					da.currentYear = 0;
//					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
					da.repaint();
				}

				else if(arg.equals("5X5 Grid Network")){
					dp.showStatus.setText("5X5 Network Loaded...");
					dp.evolve.setEnabled(true) ;
					dp.statistics .setEnabled(false);
					vp.editNetworkChoose.setEnabled(true);
					currentInputFile = "Grid5.txt";
					getnetwork="5X5 Grid Network" ;
//					if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-5;}
//					if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-5;}
					url = getClass().getResource(currentInputFile);
					try {
						dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
					} catch (IOException e) {
					}
					networkModified = false;
					da.setMapVariables();
					graphRead = true;
					evolved = false;
					da.currentYear = 0;
//					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
					da.repaint();
				}

				else if(arg.equals("A  Network  with  River")){
					dp.showStatus.setText("A  Network  with  River Loaded...");
					dp.evolve.setEnabled(true) ;
					dp.statistics .setEnabled(false);
					vp.editNetworkChoose.setEnabled(false);
					currentInputFile = "River.txt";
					getnetwork="A  Network  with  River" ;
//					if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-99;}
//					if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-99;}
					url = getClass().getResource(currentInputFile);
					try {
						dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
					} catch (IOException e) {
					}

					da.setMapVariables();
					graphRead = true;
					evolved = false;
					da.currentYear = 0;
//					dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
					da.repaint();
				}
				
				else if (arg.equals("Load from Clipboard..."))
				{
					
  						fLoadNetwork.dispose();
						fLoadNetwork = new JFrame("Paste the network to the Textarea and choose load");
						MenuBar loadMbar = new MenuBar();
						fLoadNetwork.setMenuBar(loadMbar);
						Menu loadMenu = new Menu("Load");
			  
						loadMenu.add(loadFromTextArea = new MenuItem("Load from TextArea"));
						loadMbar.add(loadMenu);  
						fLoadNetwork.addWindowListener(this);
						loadFromTextArea.addActionListener(this);
		
						JScrollPane loadScrollPane;
			  
						loadContent = new TextArea("",50,50);
			  
						Dimension screensize = getToolkit().getScreenSize();
						loadContent.setVisible(true);
						fLoadNetwork.getContentPane().add(loadContent,"Center");
						loadContent.setEditable(true);
						loadScrollPane = new JScrollPane(loadContent);
						fLoadNetwork.getContentPane().add(loadScrollPane);
						screensize = getToolkit().getScreenSize();
						//define the size of menuframe according to the screen size
						fLoadNetwork.setSize ((int)(0.35*screensize.width),
										  		(int)(0.80*screensize.height));
						fLoadNetwork.setVisible(true);
								  		
						JOptionPane.showMessageDialog(fLoadNetwork,"Paste the network to the TextArea and click Load to load the network","Load from TextArea",JOptionPane.DEFAULT_OPTION);
	/////////////////////////////////////////////								
						  //define the size of menuframe according to the screen size
//										fSaveNetwork.setSize ((int)(0.35*screensize.width),
//																	(int)(0.80*screensize.height));
//										try {
//											dg = new Evolve( currentInputFile,belta,url,linkInforInclude,demo);
//										} catch (IOException e) {
//										}
//										networkModified = false;
//										da.setMapVariables();
//										graphRead = true;
//										evolved = false;
										url=getCodeBase();
										da.currentYear = 0;
//										dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
//										da.repaint();
//										}
				}else if (arg.equals("Load from File...")){
					FileDialog loadfile=new FileDialog(f,"Load Network...",FileDialog.LOAD);
					loadfile.show() ;
					chooserFile = new File(loadfile.getDirectory(),loadfile.getFile()  );
					loadFileName = chooserFile.getName();
					try {
						url = chooserFile.toURL();
					}catch (MalformedURLException me)
					{
						System.out.println("URL error");
						return;
					}
					dp.showStatus.setText(loadFileName+" Loading...");
					dp.evolve.setEnabled(true) ;
					dp.statistics .setEnabled(false);
					vp.editNetworkChoose.setEnabled(true);
					currentInputFile = loadFileName;
					getnetwork="Loaded Network" ;
				
					try {
						dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
					} catch (IOException e) {
						dp.showStatus.setText("Loading File Error!");
						return;
					}
					networkModified = false;
					da.setMapVariables();
					graphRead = true;
					evolved = false;
					da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
					da.repaint();
				
//				chooser = new JFileChooser();
//				chooser.setVisible(true);
//				int returnVal = chooser.showSaveDialog(this);
//				if (returnVal == JFileChooser.APPROVE_OPTION)
//				{
//					chooserFile = chooser.getSelectedFile();
//					loadFileName = chooserFile.getName();
//				}
			}else if (arg.equals("Twin Cities Network")){
				currentInputFile = "TC_linkinfo.txt";
				dp.showStatus.setText("SiouxFalls Network Loaded...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(true);
				getnetwork="TwinCities Network" ;
				url = getClass().getResource(currentInputFile);
				System.out.println("Right Location--------------"+url);
				try {
					dg = new DirectedGraph( currentInputFile,url,demo);
				} catch (IOException e) {
				}
				networkModified = false;
				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			}
		}else{

			///others
				if(obj.equals(vp.investment)){
					belta = beltaScroll.value;
				}
				if(obj.equals(vp.pricing)){}


	////any changes in any pull-down boxes other than the network pull-down will also rapaint the network
			
			writeVariables();
			//System.out.print("Choices ItemChanged,writeVariables:\n");
			//for(int i=0;i<23;i++){
			//	System.out.print(i+"\t"+vp.variables [i]+"\n");
			//}

			////
/*			if(vp.network.getSelectedItem() .equals(" "))
				{dp.evolve.setEnabled(false);}
			else if(vp.network.getSelectedItem() .equals("10X10 Grid Network")){
				dp.showStatus.setText("10X10 Network Loaded...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(true);
				currentInputFile = "Grid10.txt";
				getnetwork="10X10 Grid Network" ;
				url = getClass().getResource(currentInputFile);			
				try {
					dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
				} catch (IOException e) {
				}
				
				
				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			
			}
			else if(vp.network.getSelectedItem() .equals("SiouxFalls Network")){
				dp.showStatus.setText("SiouxFalls Network Loaded...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(true);
				currentInputFile = "SiouxFalls.txt";
				getnetwork="SiouxFalls Network" ;
				url = getClass().getResource(currentInputFile);
				System.out.println("Right Location--------------"+url);
				try {
					dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
				} catch (IOException e) {
				}

				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			}
			else if(vp.network.getSelectedItem().equals("2X2 Grid Network")){
				dp.showStatus.setText("2X2 Network Loaded...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(true);
				currentInputFile = "Grid2.txt";
				getnetwork="2X2 Grid Network" ;
				url = getClass().getResource(currentInputFile);
				try {
					dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
				} catch (IOException e) {
				}
				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			}

			else if(vp.network.getSelectedItem() .equals("5X5 Grid Network")){
				dp.showStatus.setText("5X5 Network Loaded...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(true);
				currentInputFile = "Grid5.txt";
				getnetwork="5X5 Grid Network" ;
				url = getClass().getResource(currentInputFile);
			try {
				dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
				} catch (IOException e) {
				}
				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			}

			else if(vp.network.getSelectedItem().equals("A  Network  with  River")){
				dp.showStatus.setText("A  Network  with  River Loaded...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(false);
				currentInputFile = "River.txt";
				getnetwork="A  Network  with  River" ;
				url = getCodeBase();
				try {
					dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
				} catch (IOException e) {
				}

				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//				dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			}
			
			else if (vp.network.getSelectedItem().equals("Load"))
			{
//								System.out.println("______________________________________Load_________________");
//													Label temp;
//													fLoad.dispose();
//													fLoad = new Frame("Load...");
//													fLoad.setLayout(new GridLayout(3,1));
//													fLoad.addWindowListener(this);
//													Dimension screensize = getToolkit().getScreenSize();
//															//define the size of menuframe according to the screen size
//													fLoad.setSize ((int)(0.30*screensize.width),
//																				  (int)(0.30*screensize.height));
//													temp = new Label("Input the name of file to load");
//													fLoad.add(temp);							
//													address = new JTextField("File Name");
//													fLoad.add(address);
//													loadAccept = new Button("Accept");
//													loadAccept.addActionListener(this);
//													fLoad.add(loadAccept);
//					
//													fLoad.setVisible(true);
//													da.setEnabled(false);
//													vp.setEnabled(false);
//													return;
//								chooser = new JFileChooser();
//								chooser.setCurrentDirectory(new File(""+url+"SiouxFalls.txt"));
								FileDialog loadfile=new FileDialog(f,"Load Network...",FileDialog.LOAD);
								loadfile.show() ;
								chooserFile = new File(loadfile.getDirectory(),loadfile.getFile()  );
								
//										int returnVal = chooser.showOpenDialog(this);
//										if (returnVal == JFileChooser.APPROVE_OPTION)
//										{
//											chooserFile = chooser.getSelectedFile();
											currentInputFile = chooserFile.getName();
											try {
												url = chooserFile.toURL();
												dp.showStatus.setText("get url:"+url);
											}catch (MalformedURLException me)
											{
												System.out.println("URL error");
												dp.showStatus.setText("url error 3");
											}
											linkInforInclude = true;
											//evolved = false;
																				
											dp.showStatus.setText(currentInputFile+" Loaded...");
											dp.evolve.setEnabled(true) ;
											dp.statistics .setEnabled(false);
											vp.editNetworkChoose.setEnabled(true);
											getnetwork="Load" ;
//										if(vp.speed .getSelectedItem() =="Prespecified Random"){variables[0]=variables[1]=-15;}
//										if(vp.landuse .getSelectedItem() =="Prespecified Random"){variables[3]=variables[4]=-15;}

										try {
											dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
										} catch (IOException e) {
										}

										da.setMapVariables();
										graphRead = true;
										evolved = false;
										url=getCodeBase();
										da.currentYear = 0;
//										dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
										da.repaint();
//										}
			}else if (vp.network.getSelectedItem().equals("Load from File...")){
				FileDialog loadfile=new FileDialog(f,"Load Network...",FileDialog.LOAD);
				loadfile.show() ;
				chooserFile = new File(loadfile.getDirectory(),loadfile.getFile()  );
				loadFileName = chooserFile.getName();
				try {
					url = chooserFile.toURL();
				}catch (MalformedURLException me)
				{
					System.out.println("URL error");
					return;
				}
				dp.showStatus.setText(loadFileName+" Loading...");
				dp.evolve.setEnabled(true) ;
				dp.statistics .setEnabled(false);
				vp.editNetworkChoose.setEnabled(true);
				currentInputFile = loadFileName;
				getnetwork="Loaded Network" ;
			
				try {
					dg = new DirectedGraph( currentInputFile,url,linkInforInclude,demo);
				} catch (IOException e) {
					dp.showStatus.setText("Loading File Error!");
					return;
				}
				networkModified = false;
				da.setMapVariables();
				graphRead = true;
				evolved = false;
				da.currentYear = 0;
//			dp.year.setText( "   Year "+ Integer.toString( da.currentYear ) + "   " );
				da.repaint();
			
//			chooser = new JFileChooser();
//			chooser.setVisible(true);
//			int returnVal = chooser.showSaveDialog(this);
//			if (returnVal == JFileChooser.APPROVE_OPTION)
//			{
//				chooserFile = chooser.getSelectedFile();
//				loadFileName = chooserFile.getName();
//			}
			}
*/
		 }//end of else


		}///End of public void ()
		
		public void windowClosing(WindowEvent e){
				Object obj = e.getSource();
				if(obj.equals( menuframe))menuframe.dispose() ;
				else if (obj.equals( f))f.dispose() ;
				else if (obj.equals(fw))fw.dispose();
//				else if (obj.equals(fLinkEdit))fLinkEdit.dispose();
//				else if (obj.equals(fNodeEdit))fNodeEdit.dispose();
//				else if (obj.equals(fDelete))fDelete.dispose();
//				else if (obj.equals(fAddress))fAddress.dispose();
//				else if (obj.equals(fOverwrite))fOverwrite.dispose();
//				else if (obj.equals(fLoad))fLoad.dispose();
			}

			public void windowOpened(WindowEvent e){
				da.setVisible(true) ;

			}

			public void windowActivated(WindowEvent e){

				da.repaint() ;
			}

			public void windowDeactivated(WindowEvent e){

				da.repaint() ;
			}

			public void windowIconified(WindowEvent e){

				da.repaint() ;
			}

			public void windowDeiconified(WindowEvent e){

				da.repaint() ;
			}

			public void windowClosed(WindowEvent e){


			}
			
			public void getGlobalVariable()
			{
				alphaBPR = alphaScroll.value();
				betaBPR = betaScroll.value();
				theta = thetaScroll.value();
				belta = beltaScroll.value();
				float temp;
				temp = tripGRateScroll.value();
				temp = temp*tripByAutoScroll.value();
				temp = temp*peakHourRateScroll.value();
				temp = temp/autoOccupancyScroll.value();
				generationFactor = temp;
				// calculate generation factor;
				temp = tripARateScroll.value();
				temp = temp*tripByAutoScroll.value();
				temp = temp*peakHourRateScroll.value();
				temp = temp/autoOccupancyScroll.value();
				attractionFactor = temp;
			}

	}
	////// End of class VariablesPanel



}

///////  End of Demo Class



	