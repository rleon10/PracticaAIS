package raf.servidor;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;

import raf.agentes.*;
import raf.principal.*;


/**
 * Esta clase implementa un lanzador de entorno RAF con interfaz de usuario.
 */
public class GRaLauncher extends JFrame implements ActionListener,
                                            ListSelectionListener,
                                            AgencyListener{
    /**
     * Donde esta la configuracion del servidor.
     */
    String strConfigFile =  "C:\\Users\\almig\\OneDrive\\Escritorio\\AISP2\\src\\raf\\config\\movil.config";

    JFrame frame2;
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem;
    ImageIcon icon = new ImageIcon("images/middle.gif");
    //JFileChooser fileChooser;
    JPanel panel = new JPanel();

    JList list;
    DefaultListModel listModel;
    JScrollPane listScroller;

    /**
     * Maneja los byte codes de las clases cargadas.
     */
    ClassManager classManager;

    /**
     * Nombre del agente que fue seleccionado en la lista.
     */
    String selectedRa = null;

	/**
	 * La agencia que maneja todos los agentes.
	 */
	private RaAgency raAgency;

    /**
     * Direccion del servidor que registra todos los servidores de agentes del dominio.
     */
    RaAddress raServer;

    /**
     * N� puerto para las conexiones, por defecto 10101.
     */
    int port;

    /**
     * Crea un nuevo lanzador GRaLauncher.

     */
    public GRaLauncher (){
        super("GRaLauncher");

        long byteCodeDelay;
        String strRaServer = null;
        int raPort;
        Properties props = new Properties ();

        // lee las propiedades desde el fichero
        try {
            FileInputStream in = new FileInputStream (strConfigFile);
            props.load (in);
            in.close();
        }
        catch (FileNotFoundException e){
            System.err.println ("GraLauncher: No se puede abrir el fichero de configuraci�n!");
        }
        catch (IOException e){
            System.err.println ("GRaLauncher: Ha fallado la lectura del fichero!");
        }

        try {
            port = Integer.parseInt(props.getProperty("port", "10101"));
        }
        catch (NumberFormatException e){
            port = 10101;
        }
        try {
            byteCodeDelay = Long.parseLong(props.getProperty("byteCodeDelay", "100000"));
        }
        catch (NumberFormatException e){
            byteCodeDelay = 100000;
        }
        try {
            strRaServer = props.getProperty("raServer");
            raPort = Integer.parseInt(props.getProperty("raPort", "10102"));
        }
        catch (NumberFormatException e){
            raPort = 10102;
        }

        try{
            if (strRaServer == null){
                raServer = null;
            }
            else{
                InetAddress server = InetAddress.getByName (strRaServer);
                raServer = new RaAddress (server, raPort, null);
            }
        }
        catch (UnknownHostException e){
            System.out.println("! GRaLauncher: raServer no valido." + e);
            raServer = null;
        }

        System.out.println ("puerto: " + port);

        // lanza una nuva agencia
        classManager = new ClassManager (byteCodeDelay, props.getProperty("agentsPath"));

        raAgency = new RaAgency (this, classManager);
        raAgency.addAgencyListener (this);
	setVisible(false);

       	// crea el menu principal
       	menuBar = new JMenuBar();
       	setJMenuBar (menuBar);

       	// menu fichero
       	menu = new JMenu ("Fichero");
       	menuItem = new JMenuItem ("Cargar...");
       	menuItem.setActionCommand ("Cargar...");
       	menuItem.addActionListener (this);
       	menu.add (menuItem);
       	menuBar.add (menu);

       	// Editar menu
       	menu = new JMenu ("Editar");
       	menuItem = new JMenuItem ("Enviar A...");
       	menuItem.setActionCommand ("editSendTo");
       	menuItem.addActionListener (this);
       	menu.add (menuItem);
       	menuItem = new JMenuItem ("Eliminar");
       	menuItem.setActionCommand ("editDestroy");
       	menuItem.addActionListener (this);
       	menu.add (menuItem);
       	menuBar.add (menu);

      

      
        // contenidos del Frame 
        getContentPane().add (panel);
        panel.setLayout (new GridLayout(1,1));
        panel.setPreferredSize(new java.awt.Dimension(500, 300));

        listModel = new DefaultListModel();
        list = new JList (listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        listScroller = new JScrollPane (list);

        panel.add (listScroller);

        startAgency();
    }


    /**
     *  Llamada para el limpiado de las conexiones de red.
     */
    public void dispose() {
        stopAgency();
    }


    /**
     * Crea un nuevo servidor GraLauncher
     */
    public static void main(String[] args){

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("No se ha podido establecer el look and feel multiplataforma: " + e);
        }

        JFrame frame = new GRaLauncher();
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ((GRaLauncher) e.getWindow()).dispose();
                System.exit(0);
            }
        };
        frame.addWindowListener(l);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Maneja los eventos de menu
     */
    public void actionPerformed (ActionEvent e){
        if ( e.getActionCommand().equals ("Cargar...") ) {
           
	    
            File agentsPath = new File ( "src\\raf\\agentes" );
         //   cargar los nombred de los agentes
           String[] lista = agentsPath.list();
           System.out.println (lista);
           Object[] lis = (Object[]) agentsPath.list();
           System.out.println("LIS: " + lis);
           String s = (String) JOptionPane.showInputDialog(
                     frame2,
                     "Elige un Agente",
                     "Agentes Moviles",
                     JOptionPane.PLAIN_MESSAGE,
		     icon,
                     lis,
                     lis[0]);
                     if (s != null) {
                         s = s.trim();
                         if (s.length() >0 ) {
                             loadRa ("HelloDomain");
                         }
                     }
  
        }
        if ( e.getActionCommand().equals ("editSendTo") ) {
         int i;
              i=0; 
                 Object[] v = new Object[50];
                 Enumeration enumer = raAgency.getServers(this).elements();
                 while (enumer.hasMoreElements()){
                 v[i] = (Object) enumer.nextElement();
                   i = i + 1;
                 }
		RaAddress address =  (RaAddress) JOptionPane.showInputDialog(
                     frame2,
                     "Elige una Agencia",
                     "Agencia Destino",
                     JOptionPane.PLAIN_MESSAGE,
		     icon,
		     v,
                     v[0]);
                String s = address.toString();
                     if (s != null) {
                         s = s.trim();
                         if (s.length() >0 ) {
                           System.out.println (s);
                             editSendTo (address.port, address.host, address.name);
                             }
                     }
        }
        if ( e.getActionCommand().equals ("editDestroy") ) {
            editDestroy();
        }
       

    }

    /**
     * carga un agente desde un fichero (poner un string como parametro)
     */
    public void loadRa(String s){
      
        String nombre;
        System.out.println(this.getClass().toString());
        nombre = "raf.agentes." + s;
        try{
            Class result;
            RaClassLoader loader = new RaClassLoader(classManager, null, null);
            result = loader.loadClass(nombre);
            if (result == null){
                System.err.println ("GRaLauncher: No se pudo cargar la clase! clase no encontrada!");
                return;
            }

            Constructor cons[] = result.getConstructors();
            Object obs[] = {raAgency.generateName()};
	        Ra agent = (Ra) cons[0].newInstance(obs);
            raAgency.addRaOnCreation (agent, null);
        }
        catch (InvocationTargetException e){
            System.err.println ("! GRaLauncher: No se ha podidio cargar la clase " + e);
        }
        catch (SecurityException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase! " + e);
        }
        catch (ClassNotFoundException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase!  " + e);
        }
        catch (IllegalAccessException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase! " + e);
        }
        catch (InstantiationException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase! " + e);
        }
    }

    /**
     * Envia el agente seleccionado a otra agencia.
     */
    void editSendTo(int port, InetAddress host, String name){
        InetAddress destination = null;
        String server = null;
        String servername = null;
        String strLoPort = null;
        int pos = list.getSelectedIndex();
        System.out.println("POSICION . " + pos);
        selectedRa = (String) listModel.elementAt (pos);
        System.out.println ("seleccionado: " + selectedRa);
        int loPort = port;
            raAgency.dispatchRa (this, selectedRa, new RaAddress (host, port, name));

    }


    /**
     * Borra el agente seleccionado.
     */
    void editDestroy(){
        raAgency.destroyRa (this, selectedRa);
    }

    /**
     * Inicializa el Thread del RaAgency.
     */
    void startAgency(){
	System.out.println ("Inicializando la Agencia");
        raAgency.startAgency (this, port, raServer);
    }

    /**
     * Para el Thread de la agencia.
     */
    void stopAgency(){
	System.out.println ("Parando la Agencia");
        raAgency.stopAgency (this);
    }

   
    /**
     * Recuerda el agente que se selecciono de la lista.
     */
    public synchronized void valueChanged(ListSelectionEvent e){
        
    }

    /**
     * Reaccion cuando se ha puesto a un agente en el estado onCreate

     */
    public void agencyRaCreated (AgencyEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaccion cuando se ha puesto a un agente en el estado onArrival
     

     */
    public void agencyRaArrived (AgencyEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaccion cuando un agente abandona la agencia.
     */
    public void agencyRaLeft (AgencyEvent e){
        listModel.removeElement ((String)e.getName());
    }
    
    /**
     * Reaccion cuando se ha borrado un agente.
     */
    public void agencyRaDestroyed (AgencyEvent e){
        listModel.removeElement ((String)e.getName());
    }


}

