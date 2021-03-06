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
                    //comprar codigo con las tablas para ver el tipo de ususari permitido en sistema
                    if (sistemaConectado.equals("SERVIC")) {
                        if (p.verificarAutorizacion(codigoUsuario)) {
                            String nombre = p.getNombreUsuario(codigoUsuario, sistemaConectado);
                            StringBuilder codigoUsuarioCabecera = concatenarCeros(codigoUsuario);
                            rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                                    + "ACCEPT" + codigoUsuarioCabecera.toString() + nombre;
                        } else {
                            rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIEDN";
                        }
                    } else {
                        if (p.verificarAutorizacionEm(codigoUsuario)) {
                            String nombre = p.getNombreUsuario(codigoUsuario, sistemaConectado);
                            StringBuilder codigoUsuarioCabecera = concatenarCeros(codigoUsuario);
                            rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                                    + "ACCEPT" + codigoUsuarioCabecera.toString() + nombre;
                        } else {
                            rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIEDN";
                        }
                    }

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

            case "REGEQU": //registro nuevo equipo
                boolean nuevoEquipo = p.registroNuevoEquipo(datosCuerpo[0], datosCuerpo[1]);
                if (nuevoEquipo) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT";
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "REGCAN": //registro nuevo canal
                String nuevoCanal = p.registroNuevoCanal(datosCuerpo);
                rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha;
                switch (nuevoCanal) {
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

            case "REGPAN": //registro nuevo plan
                boolean nuevoPlan = p.registroNuevoPlan(datosCuerpo);
                if (nuevoPlan) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT";
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "ACTCLI": //actualización de nombre de usuario, y contraseña
                String cambioDatos = p.actualizarUserPass(datosCuerpo);
                rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha;
                switch (cambioDatos) {
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

            case "PANCLI": //consulta los planes del cliente
                String planes = p.consultaPlanesCliente(cuerpo);
                if (planes != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + planes;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "EQUCLI": //consulta los equipos por plan de cliente
                String equipos = p.consultarEquiposCliente(cuerpo);
                if (equipos != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + equipos;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONCAN": //consulta todos los canales
                String canales = p.consultaCanales();
                if (canales != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + canales;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONPLA": //consulta todos los canales
                String planes2 = p.consultaPlanes();
                if (planes2 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + planes2;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONEQU": //consulta todos los canales
                String equipos2 = p.consultaEquipos();
                if (equipos2 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + equipos2;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "DATPAN": //consulta datos plan
                String planes3 = p.consultarPlanes(cuerpo);
                if (planes3 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + planes3;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "DATEQU": //consulta datos plan
                String equipos3 = p.consultarEquipos(cuerpo);
                if (equipos3 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + equipos3;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "DATCAN": //consulta datos plan
                String canales2 = p.consultarCanales(cuerpo);
                if (canales2 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + canales2;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "NUEPLA": //registro nuevo plan del cliente
                String nuevoPlan2 = p.registroPlanCliente(datosCuerpo);
                if (nuevoPlan2 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + nuevoPlan2;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CEDUID": //consulta datos plan
                String ced = p.consultarCedulas();
                if (ced != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + ced;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONPAG": //consulta datos plan
                String pagos = p.consultarFormasPago();
                if (pagos != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + pagos;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONTOT": //pago de la factura
                String total = p.totalPago(cuerpo);
                if (total != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + total;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "SERTEC": //registro nuevo plan
                boolean nuevoSoport = p.registroNuevoSoporte(datosCuerpo);
                if (nuevoSoport) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT";
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONADI": //consulta todos los servicios adicionales
                String adicional = p.consultaSerAdi();
                if (adicional != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + adicional;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CONPRE": //consulta todos los canales especial (premium)
                String canalespre = p.consultaCanalesPremium();
                if (canalespre != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + canalespre;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "PAGFAC": //registro nuevo plan del cliente
                boolean nuevoPago = p.registroPagoCliente(datosCuerpo);
                if (nuevoPago) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT";
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "REGSOP": //registro nuevo tipo de servicio tecnico
                boolean nuevoServicio = p.registroNuevoServicioTec(datosCuerpo[0], datosCuerpo[1]);
                if (nuevoServicio) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT";
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "CLICOD": //consulta de cliente por cedula, devuelve planes del cliente
                String datos2 = p.buscarCodigoCliente(cuerpo);
                if (datos2 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + datos2;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "FACCON": //consulta de de facturas por contrato de cliente
                String datos3 = p.buscarFacturasPorContrato(cuerpo);
                if (datos3 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + datos3;
                } else {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha + "DENIED";
                }
                System.err.println("Servidor responde: " + rs);
                break;

            case "DATFAC": //consulta de datos de la factura
                String datos4 = p.buscarDatosFactura(cuerpo);
                if (datos4 != null) {
                    rs = "RS" + sistemaConectado + codigoSolicitante + tipoPeticion + fecha
                            + "ACCEPT" + datos4;
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
