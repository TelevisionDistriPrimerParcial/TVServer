/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.espe.tvserver.dao;

import ec.edu.espe.tvserver.util.DataConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Jonathan
 */
public class Peticion {

    private Connection con = null;

    public boolean login(String user, String pass) {
        boolean flag = false;

        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT USUARIO_NOMBRE, USUARIO_CLAVE FROM USUARIO";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString(1);
                String clave = rs.getString(2);
                if (name.equals(user)) {
                    if (clave.equals(pass)) {
                        flag = true;
                    } else {
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return flag;
    }

    public String getCodigoUsuario(String user, String pass) {
        String codigoUsuario = null;

        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT * FROM USUARIO";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                String codigo = rs.getString(1);
                String name = rs.getString(2);
                String clave = rs.getString(3);
                if (name.equals(user)) {
                    if (clave.equals(pass)) {
                        codigoUsuario = codigo;
                    } else {
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return codigoUsuario;
    }

    public String getNombreUsuario(String code, String sistema) {
        String name = null;

        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            if ("SERVIC".equals(sistema)) {
                String query = "SELECT CLIENTE_NOMBRE, CLIENTE_APELLIDO FROM CLIENTE"
                        + " WHERE USUARIO_CODIGO = '" + code + "'";
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    String nombre = rs.getString(1);
                    String apellido = rs.getString(2);
                    name = nombre + " " + apellido;
                }
            } else {
                String query = "SELECT EMPLEADO_NOMBRE, EMPLEADO_APELLIDO FROM EMPLEADO"
                        + " WHERE USUARIO_CODIGO = '" + code + "'";
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    String nombre = rs.getString(1);
                    String apellido = rs.getString(2);
                    name = nombre + " " + apellido;
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return name;
    }

    public String registrarCliente(String[] datos, Date d) {
        String resp = "A"; //A: exito, E: error, Y: ya existe
        String query = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement st = null;

        try {
            String codigoCliente = null;
            String codigoContrato = null;
            String codigoDetalleContrato = null;
            con = DataConnect.getConnection();
            st = con.createStatement();
            //Verifica si el usuario ingresado ya existen en la base de datos y no permite duplicados
            query = "SELECT CLIENTE_CEDULA FROM CLIENTE WHERE CLIENTE_CEDULA = '" + datos[2] + "'";
            rs = st.executeQuery(query);
            while (rs.next()) {
                return resp = "Y";
            }

            //Crea el usuario para el cliente con su cedula como nombre de usuario y contraseña
            query = "INSERT INTO USUARIO (USUARIO_NOMBRE, USUARIO_CLAVE) values (?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, datos[2]);
            ps.setString(2, datos[2]);
            ps.executeUpdate();

            //Obtiene el código de usuario del nuevo cliente
            st = con.createStatement();
            query = "SELECT USUARIO_CODIGO FROM USUARIO WHERE USUARIO_NOMBRE = '" + datos[2] + "'"
                    + " AND USUARIO_CLAVE = '" + datos[2] + "'";
            rs = st.executeQuery(query);
            while (rs.next()) {
                codigoCliente = rs.getString(1);
            }

            //Inserta el registro de los datos del cliente en la tabla CLIENTE con el código de usuario obtenido anteriormente
            query = "INSERT INTO CLIENTE values (?,?,?,?,?,?,?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, codigoCliente);
            ps.setString(2, datos[0]);
            ps.setString(3, datos[1]);
            ps.setString(4, datos[2]);
            ps.setString(5, datos[3]);
            ps.setString(6, datos[4]);
            ps.setString(7, datos[5]);
            ps.setString(8, datos[6]);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            //deshacerRegistroCliente(cont);
            resp = "E";
        } finally {
            DataConnect.close(con);
        }
        return resp;
    }

    public String buscarCedula(String cedula) {
        String cuerpo = null;
        String nombre = null;
        String apellido = null;
        String codigoCliente = null;
        String codigoContrato = null;
        String nombrePlan = null;
        List<String> listaContratos = null;
        List<String> listaPlanes = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT USUARIO_CODIGO, CLIENTE_NOMBRE, CLIENTE_APELLIDO FROM CLIENTE"
                    + " WHERE CLIENTE_CEDULA = '" + cedula + "'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                codigoCliente = rs.getString(1);
                nombre = rs.getString(2);
                apellido = rs.getString(3);
            }

            if (nombre != null) {
                cuerpo = nombre + "|" + apellido;
                listaContratos = new ArrayList<>();
                query = "SELECT CONTRATO_CODIGO FROM CONTRATO "
                        + "WHERE USUARIO_CODIGO = '" + codigoCliente + "'";
                rs = st.executeQuery(query);
                while (rs.next()) {
                    listaContratos.add(rs.getString(1));
                }

                if (listaContratos.size() > 0) {
                    listaPlanes = new ArrayList<>();
                    for (int i = 0; i < listaContratos.size(); i++) {
                        query = "SELECT PLAN_CODIGO FROM DETALLE_CONTRATO "
                                + "WHERE CONTRATO_CODIGO = '" + listaContratos.get(i) + "'";
                        rs = st.executeQuery(query);
                        while (rs.next()) {
                            listaPlanes.add(rs.getString(1));
                        }
                    }
                    cuerpo = cuerpo + "|";

                    for (int i = 0; i < listaPlanes.size(); i++) {
                        query = "SELECT PLAN_NOMBRE FROM PLAN "
                                + "WHERE PLAN_CODIGO = '" + listaPlanes.get(i) + "'";
                        rs = st.executeQuery(query);
                        while (rs.next()) {
                            nombrePlan = rs.getString(1);
                        }
                        cuerpo = cuerpo + listaContratos.get(i) + "%" + nombrePlan;
                        if (i < listaPlanes.size() - 1) {
                            cuerpo = cuerpo + "&";
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public boolean registroNuevoEquipo(String nombre, String costo) {
        boolean flag = true;
        String query = null;
        PreparedStatement ps = null;
        con = DataConnect.getConnection();

        try {
            query = "INSERT INTO EQUIPO (EQUIPO_NOMBRE, EQUIPO_COSTO) values (?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, costo);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            flag = false;
        } finally {
            DataConnect.close(con);
        }
        return flag;
    }

    public String registroNuevoCanal(String[] datos) {
        String exito = "A";
        String query = null;
        PreparedStatement ps = null;
        Statement st = null;
        ResultSet rs = null;
        con = DataConnect.getConnection();

        try {
            st = con.createStatement();
            //Verifica si el usuario ingresado ya existen en la base de datos y no permite duplicados
            query = "SELECT CANAL_CODIGO FROM CANAL";
            rs = st.executeQuery(query);
            while (rs.next()) {
                String codCanal = rs.getString(1);
                if (codCanal.equals(datos[0])) {
                    return exito = "Y";
                }
            }

            query = "INSERT INTO CANAL (CANAL_CODIGO, CANAL_NOMBRE, CANAL_TIPO, CANAL_PREMIUM) values (?,?,?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, datos[0]);
            ps.setString(2, datos[1]);
            ps.setString(3, datos[2]);
            ps.setString(4, datos[3]);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            exito = "E";
        } finally {
            DataConnect.close(con);
        }
        return exito;
    }

    public boolean actualizarUserPass(String[] datos) {
        boolean flag = true;
        PreparedStatement ps = null;
        String query = null;
        con = DataConnect.getConnection();
        try {
            query = "UPDATE USUARIO SET USUARIO_NOMBRE='" + datos[1] + "', "
                    + "USUARIO_CLAVE='" + datos[2] + "' WHERE USUARIO_CODIGO='" + datos[0] + "';";
            ps = con.prepareStatement(query);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            flag = false;
        } finally {
            DataConnect.close(con);
        }
        return flag;
    }

    public String consultaPlanesCliente(String codigoCliente) {
        String cuerpo = null;
        List<String> listanamePlanes = null;
        List<String> listacodPlanes = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "Select d.PLAN_CODIGO FROM DETALLE_CONTRATO d, "
                    + "CONTRATO c WHERE d.CONTRATO_CODIGO=c.CONTRATO_CODIGO "
                    + "AND c.USUARIO_CODIGO='" + codigoCliente + "'";
            ResultSet rs = st.executeQuery(query);
            listacodPlanes = new ArrayList<>();
            while (rs.next()) {
                listacodPlanes.add(rs.getString(1));
            }

            if (listacodPlanes.size() > 0) {
                listanamePlanes = new ArrayList<>();
                for (int i = 0; i < listacodPlanes.size(); i++) {
                    query = "SELECT PLAN_NOMBRE FROM PLAN "
                            + "WHERE PLAN_CODIGO = '" + listacodPlanes.get(i) + "'";
                    rs = st.executeQuery(query);
                    while (rs.next()) {
                        listanamePlanes.add(rs.getString(1));
                    }
                }

                for (int i = 0; i < listacodPlanes.size(); i++) {
                    if (i == 0) {
                        cuerpo = listacodPlanes.get(i) + "%" + listanamePlanes.get(i);
                    } else {
                        cuerpo = cuerpo + listacodPlanes.get(i) + "%" + listanamePlanes.get(i);
                    }
                    if (i < listacodPlanes.size() - 1) {
                        cuerpo = cuerpo + "&";
                    }
                }
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String consultaCanales() {
        String cuerpo = null;
        List<String> listanameCanales = null;
        List<String> listacodCanales = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "Select CANAL_CODIGO, CANAL_NOMBRE FROM CANAL";
            ResultSet rs = st.executeQuery(query);
            listacodCanales = new ArrayList<>();
            listanameCanales = new ArrayList<>();
            while (rs.next()) {
                listacodCanales.add(rs.getString(1));
                listanameCanales.add(rs.getString(2));
            }

            for (int i = 0; i < listacodCanales.size(); i++) {
                if (i == 0) {
                    cuerpo = listacodCanales.get(i) + "%" + listanameCanales.get(i);
                } else {
                    cuerpo = cuerpo + listacodCanales.get(i) + "%" + listanameCanales.get(i);
                }
                if (i < listacodCanales.size() - 1) {
                    cuerpo = cuerpo + "&";
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }
    
    public String consultaPlanes() {
        String cuerpo = null;
        List<String> listanamePlanes = null;
        List<String> listacodPlanes = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "Select PLAN_CODIGO, PLAN_NOMBRE FROM PLAN";
            ResultSet rs = st.executeQuery(query);
            listacodPlanes = new ArrayList<>();
            listanamePlanes = new ArrayList<>();
            while (rs.next()) {
                listacodPlanes.add(rs.getString(1));
                listanamePlanes.add(rs.getString(2));
            }

            for (int i = 0; i < listacodPlanes.size(); i++) {
                if (i == 0) {
                    cuerpo = listacodPlanes.get(i) + "%" + listanamePlanes.get(i);
                } else {
                    cuerpo = cuerpo + listacodPlanes.get(i) + "%" + listanamePlanes.get(i);
                }
                if (i < listacodPlanes.size() - 1) {
                    cuerpo = cuerpo + "&";
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }
    
    public String consultaEquipos() {
        String cuerpo = null;
        List<String> listanameEquipos = null;
        List<String> listacodEquipos = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "Select EQUIPO_CODIGO, EQUIPO_NOMBRE FROM EQUIPO";
            ResultSet rs = st.executeQuery(query);
            listacodEquipos = new ArrayList<>();
            listanameEquipos = new ArrayList<>();
            while (rs.next()) {
                listacodEquipos.add(rs.getString(1));
                listanameEquipos.add(rs.getString(2));
            }

            for (int i = 0; i < listacodEquipos.size(); i++) {
                if (i == 0) {
                    cuerpo = listacodEquipos.get(i) + "%" + listanameEquipos.get(i);
                } else {
                    cuerpo = cuerpo + listacodEquipos.get(i) + "%" + listanameEquipos.get(i);
                }
                if (i < listacodEquipos.size() - 1) {
                    cuerpo = cuerpo + "&";
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }
}
