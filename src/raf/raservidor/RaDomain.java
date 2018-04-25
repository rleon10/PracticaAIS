package raf.raservidor;

import java.io.*;
import java.util.Properties;

/**
 * La clase principal del servidor que gestiona todos los servidores del dominio.
 */
public class RaDomain{

    /**
     * Donde esta la configuracion del servidor.
     */
    String strConfigFile = "C:\\Users\\almig\\OneDrive\\Escritorio\\AISP2\\src\\raf\\config\\radomain.config";

    /**
     * Puerto en el que escucha.
     */
    int port;

    /**
     * El modelo del servidor que realiza el trabajo realmente.
     */
    RaModel raModel;

    /**
     * Crea un nuevo RaModel e inicia el servicio que escucha en la red.
     */
    public RaDomain() throws IOException{
        Properties props = new Properties ();

        // lee las propiedades del fichero
        try {
            FileInputStream in = new FileInputStream (strConfigFile);
            props.load (in);
            in.close();
        }
        catch (FileNotFoundException e){
            System.err.println ("RaDomain: No se puede abrir el fichero de propiedades!");
        }
        catch (IOException e){
            System.err.println ("RaDomain: Ha fallado la lectura del fichero!");
        }

        // inicializa las propiedades
        try {
            port = Integer.parseInt(props.getProperty("port", "10104"));
        }
        catch (NumberFormatException e){
            port = 10104;
        }

        // Lanza el servidor RaModel
        raModel = new RaModel();
        raModel.startService(port);
    }

    /**
     * Crea un nuevo servidor para gestionar el dominio.
     */
    public static void main (String[] args) throws IOException{
        new RaDomain();
    }


}
