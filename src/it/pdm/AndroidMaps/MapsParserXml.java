package it.pdm.AndroidMaps;

import java.util.ArrayList;

import android.util.Log;

public class MapsParserXml {
    
	static void vDebug(String debugString){         //metodi di convenienza
            Log.v("DomParsing", debugString+"\n");
    }
    static void eDebug(String debugString){
            Log.e("DomParsing", debugString+"\n");
    }
   
    private ArrayList<MapPoint> parsedData=new ArrayList<MapPoint>(); //struttura dati che immagazzinerà i dati letti
    
    public ArrayList<MapPoint> getParsedData() {  //metodo di accesso alla struttura dati
            return parsedData;
    }

  /*  public void parseXml(InputStream input){
           
    	Document doc;
        
    	try {                  
    		doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);

    		//Costruiamo il nostro documento a partire dallo stream dati fornito dall'URL
    		Element root=doc.getDocumentElement(); //nel mio caso <map>
    		//Elemento(nodo) radice del documento
                   
    		vDebug("Root element: " + root.getNodeName());
    		vDebug("");
           
    		//prediamo tutti i "figli" diretti di root. Utile se non avessimo solo "note" come figli di root
    		NodeList points=root.getChildNodes();
    		
                   
    		for(int i=0;i<points.getLength();i++){//per ogni
    			Node c= points.item(i);//nodo, nel mio caso <point>
    			
   				//controlliamo se questo è un nodo elemento (un tag) 			
    			if(c.getNodeType()==Node.ELEMENT_NODE){
        			vDebug("--------------------------------------");
                    vDebug("");
    				MapPoint newPoint=new MapPoint(); //costruiamo un oggetto MapPoint dove andremo a salvare i dati
                    NodeList noteDetails=c.getChildNodes();  //per ogni punto abbiamo i vari dettagli
                    	
                    	for(int j=0;j<noteDetails.getLength();j++){
                    		
                    		Node c1=noteDetails.item(j);
                                           
                    		if(c1.getNodeType()==Node.ELEMENT_NODE && !(c1.getNodeName().equals("owner"))){ //anche in questo caso controlliamo se si tratta di tag
                    			
                    			Element detail=(Element)c1; //cast
                                String nodeName=detail.getNodeName(); //leggo il nome del tag
                                String nodeValue=detail.getFirstChild().getNodeValue();//leggo il testo in esso contenuto
                                vDebug(nodeName+": "+nodeValue);
                                vDebug("");
                                                           
                                //a dipendenza del nome del nodo (del dettaglio) settiamo il relativo valore nell'oggetto
                                if(nodeName.equals("id")){
                                	newPoint.setId(new Integer(nodeValue));
                                }
                                
                                if(nodeName.equals("latitude")){
                                	newPoint.setLatitude(new Integer(nodeValue));
                                }
                                if(nodeName.equals("longitude")){
                                	newPoint.setLongitude(new Integer(nodeValue));
                                }                                                           
                                if(nodeName.equals("name")){
                                	newPoint.setName(nodeValue);
                                }
                                                           
                                
                    		}else{
                    			
                    			if(c1.getNodeType()==Node.ELEMENT_NODE && c1.getNodeName().equals("owner")){
                                                    		
                    				NodeList noteDetails2=c1.getChildNodes();  //per ogni nota abbiamo i vari dettagli
                                                    		
                    				for(int k=0;k<noteDetails2.getLength();k++){
                    					Node c2=noteDetails2.item(k);
                    					
                    					if(c2.getNodeType()==Node.ELEMENT_NODE){
                    						Element detail=(Element)c2; //cast
                                            String nodeName=detail.getNodeName(); //leggo il nome del tag
                                            String nodeValue=detail.getFirstChild().getNodeValue();//leggo il testo in esso contenuto
                                            vDebug("owner_"+nodeName+": "+nodeValue);
                                            vDebug("");
                                            
                                          //a dipendenza del nome del nodo (del dettaglio) settiamo il relativo valore nell'oggetto
                                            if(nodeName.equals("name")){
                                            	newPoint.setOwnerName(nodeValue);
                                            }
                                            if(nodeName.equals("email")){
                                            	newPoint.setOwnerEmail(nodeValue);
                                            }
                    					}
                    				}
                                }//else{
                                //	vDebug(" - Parte saltata -");
                                }//
                            }
                                           
                    	}
                                    vDebug("");
                           
                                    parsedData.add(newPoint); //aggiungiamo il nostro oggetto all'arraylist
    			}
                                                                                          
    		}                      
            //gestione eccezioni
    		} catch (NullPointerException e) {
                eDebug("Impossibile aprire il file");
    		} catch (SAXException e) {
                    eDebug(e.toString());
            } catch (IOException e) {
                    eDebug(e.toString());
            } catch (ParserConfigurationException e) {
                    eDebug(e.toString());
            } catch (FactoryConfigurationError e) {
                    eDebug(e.toString());
            }
           
    }*/

}
