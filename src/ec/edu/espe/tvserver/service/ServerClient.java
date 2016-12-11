/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.espe.tvserver.service;

import ec.edu.espe.tvserver.dao.Peticion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Jonathan
 */
public class ServerClient extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String rq = "";
    private String nombreCliente;

    public ServerClient(Socket socket) {
        try {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            nombreCliente = in.readLine() + socket.getRemoteSocketAddress();
            System.out.println("Conexión entrante: " + nombreCliente);
        } catch (IOException ex) {
            System.err.println("Error");
        }
    }

    @Override
    public void run() {
        try {
            while ((rq = in.readLine()) != null) {
                System.out.println(nombreCliente + " dice: " + rq);
                String rs = transaccion(rq);
                if ("OUT".equals(rq.substring(33))) {
                    System.out.println("Conexión terminada: " + nombreCliente);
                    out.println(rs);
                    out.flush();
                    break;
                }
                out.println(rs);
                out.flush();
            }
        } catch (IOException es) {
            System.out.println(es.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public String transaccion(String rq) {
        String codigoSolicitante = rq.substring(8, 13); //código del usuario que hace la petición
        String tipoPeticion = rq.substring(13, 19); //código de la petición del cliente
        String sistemaConectado = rq.substring(2, 8); //sistema que hace la petición
        String rs = null; //respuesta que enviará el servidor
        String cuerpo = rq.substring(33); //cuerpo del mensaje de la petición
        String[] datosCuerpo = cuerpo.split("\\|");
        Peticion p = new Peticion();

        Date date = new Date();
        DateFormat hourdateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fecha = hourdateFormat.format(date);

        switch (tipoPeticion) {
            case "LOGIN0": //autenticación
                boolean hizoLogin = p.login(datosCuerpo[0], datosCuerpo[1]);
                if (hizoLogin) {
                    String codigoUsuario = p.getCodigoUsuario(datosCuerpo[0], datosCuerpo[1]);
                    String nombre = p.getNombreUsuario(codigoUsuario, sistemaConectado);
                    StringBuilder codigoUsuarioCabecera = concatenarCeros(codigoUsuario);
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + codigoUsuarioCabecera.toString() + nombre;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CERSES": //cerrar sesión
                rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "ACCEPT";
                System.err.println("Servidor responde: " + rs);
                break;

            case "REGCLI": //registro de cliente
                String guardado = p.registrarCliente(datosCuerpo, date);
                rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha;
                switch (guardado) {
                    case "A":
                        rs = rs + "ACCEPT";
                        break;
                    case "Y":
                        rs = rs + "DENIEDY";
                        break;
                    default:
                        rs = rs + "DENIED";
                        break;
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CLICED": //consulta de cliente por cedula, devuelve planes del cliente
                String datos = p.buscarCedula(cuerpo);
                if (datos != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + datos;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;
        }

        return rs;
    }

    public StringBuilder concatenarCeros(String code) {
        char[] arrayChar = code.toCharArray();
        int cont = arrayChar.length;
        StringBuilder sb = new StringBuilder();

        while (sb.length() < 5 - cont) {
            sb.append("0");
        }

        return sb.append(code);
    }
}
