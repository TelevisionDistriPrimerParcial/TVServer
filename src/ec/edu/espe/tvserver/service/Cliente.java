/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.espe.tvserver.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author Jonathan
 */
public class Cliente {

    private static final String host = "localhost";
    private static final int puerto = 7000;

    public static void main(String[] args) {

        Socket clientSocket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        Scanner scn = null;

        try {
            clientSocket = new Socket(host, puerto);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            scn = new Scanner(System.in);
            out.println("ENROLL");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        System.out.println("Servidor conectado: " + clientSocket.getRemoteSocketAddress());

        Date date = new Date();
        DateFormat hourdateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fecha = hourdateFormat.format(date);

        try {
            //String request = "RQENROLL00000LOGIN020161203233018alias123|pass";
            ///String request = "RQENROLL00001REGCLI20161203233018Carlos|Proaño"
                    //+ "|1722484772|593022370455|andy_proanio@hotmail.com|Quito|Tumbaco";
                   // String request = "RQENROLL00001CERSES20161203233018OUT";
                  // String request = "RQPRODUC00000REGEQU20161203233018SM-N920G|750";
                  ///String request = "RQSERVIC00005PANCLI20161211153750CANALE";
                  //String request = "RQSERVIC00005CONCAN201612111537507";
                  String request = "RQENROLL00001CLICED201612111537501721557824";
            //while (request  != null) {
            out.println(request);
            System.out.println("Servidor dice: " + in.readLine());
            //if ("bye".equalsIgnoreCase(request)) {
            //    break;
            ///  }
            out.flush();
            //// }

            out.close();
            in.close();
            clientSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Esperando: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

}
