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
            //System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return flag;
    }

    public boolean verificarAutorizacion(String codigoUser) {
        boolean flag = true;
        String cod = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT USUARIO_CODIGO FROM CLIENTE WHERE "
                    + "USUARIO_CODIGO = '" + codigoUser + "'";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                cod = rs.getString(1);
            }

            if (cod == null) {
                flag = false;
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            flag = false;
        } finally {
            DataConnect.close(con);
        }
        return flag;
    }
    
    public boolean verificarAutorizacionEm(String codigoUser) {
        boolean flag = true;
        String cod = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT USUARIO_CODIGO FROM EMPLEADO WHERE "
                    + "USUARIO_CODIGO = '" + codigoUser + "'";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                cod = rs.getString(1);
            }

            if (cod == null) {
                flag = false;
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            flag = false;
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
            //System.out.println(ex.getMessage());
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
            //System.out.println(ex.getMessage());
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
            //System.out.println(ex.getMessage());
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
        String nombrePlan = null;
        List<String> listaContratos = null;
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
                    cuerpo = cuerpo + "|";
                    for (int i = 0; i < listaContratos.size(); i++) {
                        query = "SELECT PLAN_NOMBRE FROM PLAN P, DETALLE_CONTRATO C "
                                + "WHERE P.PLAN_CODIGO = C.PLAN_CODIGO AND "
                                + "C.CONTRATO_CODIGO = '" + listaContratos.get(i) + "'";
                        rs = st.executeQuery(query);
                        while (rs.next()) {
                            nombrePlan = rs.getString(1);
                        }
                        cuerpo = cuerpo + listaContratos.get(i) + "%" + nombrePlan;
                        if (i < listaContratos.size() - 1) {
                            cuerpo = cuerpo + "&";
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            cuerpo = null;
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
            //System.out.println(ex.getMessage());
            flag = false;
        } finally {
            DataConnect.close(con);
        }
        return flag;
    }

    public boolean registroNuevoPlan(String[] cuerpo) {
        boolean flag = true;
        String query = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement st = null;
        String codPlan = null;
        con = DataConnect.getConnection();

        try {
            query = "INSERT INTO PLAN (PLAN_NOMBRE, PLAN_TARIFA) values (?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, cuerpo[0]);
            ps.setString(2, cuerpo[1]);
            ps.executeUpdate();

            st = con.createStatement();
            query = "SELECT MAX(PLAN_CODIGO) FROM PLAN";
            rs = st.executeQuery(query);
            while (rs.next()) {
                codPlan = rs.getString(1);
            }
            String[] canales = cuerpo[2].split("\\&");
            for (int i = 0; i < canales.length; i++) {
                query = "INSERT INTO PLAN_CANAL (PLAN_CODIGO, CANAL_CODIGO) values (?,?)";
                ps = con.prepareStatement(query);
                ps.setString(1, codPlan);
                ps.setString(2, canales[i]);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
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
            //System.out.println(ex.getMessage());
            exito = "E";
        } finally {
            DataConnect.close(con);
        }
        return exito;
    }

    public String actualizarUserPass(String[] datos) {
        String exito = "A";
        PreparedStatement ps = null;
        String query = null;
        Statement st = null;
        ResultSet rs = null;
        con = DataConnect.getConnection();
        try {
            st = con.createStatement();
            //Verifica si el usuario ingresado ya existen en la base de datos y no permite duplicados
            query = "SELECT USUARIO_NOMBRE FROM USUARIO";
            rs = st.executeQuery(query);
            while (rs.next()) {
                String nomUser = rs.getString(1);
                if (nomUser.equals(datos[1])) {
                    return exito = "Y";
                }
            }
            query = "UPDATE USUARIO SET USUARIO_NOMBRE='" + datos[1] + "', "
                    + "USUARIO_CLAVE='" + datos[2] + "' WHERE USUARIO_CODIGO='" + datos[0] + "';";
            ps = con.prepareStatement(query);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            exito = "E";
        } finally {
            DataConnect.close(con);
        }
        return exito;
    }

    public String consultaPlanesCliente(String codigoCliente) {
        String cuerpo = null;
        List<String> listaContratos = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            listaContratos = new ArrayList<>();
            String query = "SELECT CONTRATO_CODIGO FROM CONTRATO "
                    + "WHERE USUARIO_CODIGO = '" + codigoCliente + "'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                listaContratos.add(rs.getString(1));
            }

            if (listaContratos.size() > 0) {
                String nombrePlan = null;
                for (int i = 0; i < listaContratos.size(); i++) {
                    query = "SELECT PLAN_NOMBRE FROM PLAN P, DETALLE_CONTRATO C "
                            + "WHERE P.PLAN_CODIGO = C.PLAN_CODIGO AND "
                            + "C.CONTRATO_CODIGO = '" + listaContratos.get(i) + "'";
                    rs = st.executeQuery(query);
                    while (rs.next()) {
                        nombrePlan = rs.getString(1);
                    }
                    if (i == 0) {
                        cuerpo = listaContratos.get(i) + "%" + nombrePlan;
                    } else {
                        cuerpo = cuerpo + listaContratos.get(i) + "%" + nombrePlan;
                    }
                    if (i < listaContratos.size() - 1) {
                        cuerpo = cuerpo + "&";
                    }
                }
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            cuerpo = null;
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String consultarEquiposCliente(String contrato) {
        String cuerpo = null;
        String nombreEquipo = null;
        String cantidadEquipo = null;
        List<String> listaCodEquipos = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            listaCodEquipos = new ArrayList<>();
            String query = "SELECT EQUIPO_CODIGO FROM DETALLE_CONTRATO_EQUIPO D, "
                    + "DETALLE_CONTRATO C WHERE D.DETALLE_CONTRATO_CODIGO = "
                    + "C.DETALLE_CONTRATO_CODIGO AND C.CONTRATO_CODIGO = '" + contrato + "'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                listaCodEquipos.add(rs.getString(1));
            }

            if (listaCodEquipos.size() > 0) {
                for (int i = 0; i < listaCodEquipos.size(); i++) {
                    query = "SELECT EQUIPO_NOMBRE, EQUIPO_CANTIDAD FROM EQUIPO E, "
                            + "DETALLE_CONTRATO_EQUIPO D "
                            + "WHERE E.EQUIPO_CODIGO = '" + listaCodEquipos.get(i) + "' "
                            + "AND E.EQUIPO_CODIGO = D.EQUIPO_CODIGO";
                    rs = st.executeQuery(query);
                    while (rs.next()) {
                        nombreEquipo = rs.getString(1);
                        cantidadEquipo = rs.getString(2);
                    }
                    if (i == 0) {
                        cuerpo = listaCodEquipos.get(i) + "%" + nombreEquipo + "%" + cantidadEquipo;
                    } else {
                        cuerpo = cuerpo + listaCodEquipos.get(i) + "%" + nombreEquipo + "%" + cantidadEquipo;
                    }
                    if (i < listaCodEquipos.size() - 1) {
                        cuerpo = cuerpo + "&";
                    }
                }
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            cuerpo = null;
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
            //System.out.println(ex.getMessage());
            cuerpo = null;
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
            //System.out.println(ex.getMessage());
            cuerpo = null;
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
            //System.out.println(ex.getMessage());
            cuerpo = null;
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String consultarPlanes(String contrato) {
        String cuerpo = null;
        String codigoPlan = null;
        String tarifa = null;
        List<String> listaCodCanales = null;
        List<String> listaNameCanales = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT P.PLAN_CODIGO, PLAN_TARIFA FROM PLAN P, "
                    + "DETALLE_CONTRATO D WHERE P.PLAN_CODIGO = D.PLAN_CODIGO "
                    + "AND D.CONTRATO_CODIGO = '" + contrato + "'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                codigoPlan = rs.getString(1);
                tarifa = rs.getString(2);
            }
            cuerpo = tarifa + "|";
            listaCodCanales = new ArrayList<>();
            listaNameCanales = new ArrayList<>();

            query = "SELECT C.CANAL_CODIGO, C.CANAL_NOMBRE FROM CANAL C, "
                    + "PLAN_CANAL P WHERE C.CANAL_CODIGO = P.CANAL_CODIGO "
                    + "AND P.PLAN_CODIGO = '" + codigoPlan + "'";
            rs = st.executeQuery(query);
            while (rs.next()) {
                listaCodCanales.add(rs.getString(1));
                listaNameCanales.add(rs.getString(2));
            }
            for (int i = 0; i < listaCodCanales.size(); i++) {
                cuerpo = cuerpo + listaCodCanales.get(i) + "%" + listaNameCanales.get(i);
                if (i < listaCodCanales.size() - 1) {
                    cuerpo = cuerpo + "&";
                }
            }
        } catch (SQLException ex) {
            ///System.out.println(ex.getMessage());
            cuerpo = null;
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String consultarEquipos(String equipo) {
        String cuerpo = null;
        String equipoCosto = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT EQUIPO_COSTO FROM EQUIPO "
                    + "WHERE EQUIPO_CODIGO = '" + equipo + "'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                equipoCosto = rs.getString(1);
            }
            cuerpo = equipoCosto;
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            cuerpo = null;
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String consultarCanales(String canal) {
        String cuerpo = null;
        String tipoCanal = null;
        String pCanal = null;
        try {
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT CANAL_TIPO, CANAL_PREMIUM FROM CANAL "
                    + "WHERE CANAL_CODIGO = '" + canal + "'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                tipoCanal = rs.getString(1);
                pCanal = rs.getString(2);
            }
            cuerpo = tipoCanal + "|" + pCanal;
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            cuerpo = null;
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String registroPlanCliente(String[] cuerpo) {
        String query = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement st = null;
        String codContrato = null;
        String codFactura = null;
        String detCodContrato = null;
        String codUser = null;
        String total = null;
        con = DataConnect.getConnection();

        try {
            st = con.createStatement();
            query = "SELECT USUARIO_CODIGO FROM CLIENTE WHERE CLIENTE_CEDULA = '" + cuerpo[0] + "'";
            rs = st.executeQuery(query);
            while (rs.next()) {
                codUser = rs.getString(1);
            }

            if (codUser != null) {
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                query = "INSERT INTO CONTRATO (USUARIO_CODIGO, CONTRATO_FECHA, CONTRATO_ESTADO) values (?,?,?)";
                ps = con.prepareStatement(query);
                ps.setString(1, codUser);
                ps.setDate(2, sqlDate);
                ps.setString(3, "1");
                ps.executeUpdate();

                query = "SELECT MAX(CONTRATO_CODIGO) FROM CONTRATO";
                rs = st.executeQuery(query);
                while (rs.next()) {
                    codContrato = rs.getString(1);
                }

                query = "INSERT INTO DETALLE_CONTRATO (CONTRATO_CODIGO, PLAN_CODIGO) values (?,?)";
                ps = con.prepareStatement(query);
                ps.setString(1, codContrato);
                ps.setString(2, cuerpo[1]);
                ps.executeUpdate();

                query = "SELECT MAX(DETALLE_CONTRATO_CODIGO) FROM DETALLE_CONTRATO";
                rs = st.executeQuery(query);
                while (rs.next()) {
                    detCodContrato = rs.getString(1);
                }

                query = "INSERT INTO DETALLE_CONTRATO_EQUIPO (DETALLE_CONTRATO_CODIGO, EQUIPO_CODIGO, EQUIPO_CANTIDAD) values (?,?,?)";
                ps = con.prepareStatement(query);
                ps.setString(1, detCodContrato);
                ps.setString(2, cuerpo[2]);
                ps.setString(3, "1");
                ps.executeUpdate();

                if (cuerpo.length > 4) {
                    String[] extra = cuerpo[4].split("\\&");
                    for (int i = 0; i < extra.length; i++) {
                        String[] cant = extra[i].split("\\%");
                        if (cant[0].equals(cuerpo[2])) {
                            int cantidad = 1 + Integer.parseInt(cant[1]);
                            query = "UPDATE DETALLE_CONTRATO_EQUIPO SET EQUIPO_CANTIDAD = '" + cantidad + "' "
                                    + "WHERE EQUIPO_CODIGO = '" + cuerpo[2] + "'";
                            ps = con.prepareStatement(query);
                            ps.executeUpdate();
                        } else {
                            query = "INSERT INTO DETALLE_CONTRATO_EQUIPO (DETALLE_CONTRATO_CODIGO, EQUIPO_CODIGO, EQUIPO_CANTIDAD) values (?,?,?)";
                            ps = con.prepareStatement(query);
                            ps.setString(1, detCodContrato);
                            ps.setString(2, cant[0]);
                            ps.setString(3, cant[1]);
                            ps.executeUpdate();
                        }
                    }
                }

                query = "INSERT INTO FACTURA (CONTRATO_CODIGO, FACTURA_FECHA) values (?,?)";
                ps = con.prepareStatement(query);
                ps.setString(1, codContrato);
                ps.setDate(2, sqlDate);
                ps.executeUpdate();

                query = "SELECT MAX(FACTURA_CODIGO) FROM FACTURA";
                rs = st.executeQuery(query);
                while (rs.next()) {
                    codFactura = rs.getString(1);
                }

                total = calcularTotalPagado(codContrato);

                query = "INSERT INTO PAGO (FORMA_PAGO_CODIGO, FACTURA_CODIGO, PAGO_FECHA, PAGO_TOTAL) values (?,?,?,?)";
                ps = con.prepareStatement(query);
                ps.setString(1, cuerpo[3]);
                ps.setString(2, codFactura);
                ps.setDate(3, sqlDate);
                ps.setString(4, total);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            total = null;
        } finally {
            DataConnect.close(con);
        }
        return total;
    }

    public String calcularTotalPagado(String contrato) {
        String query = null;
        ResultSet rs = null;
        Statement st = null;
        double costoPlan = 0.0;
        double costoEquipo = 0.0;
        int cantidadEquipo = 0;
        double sumEquipo = 0.0;
        con = DataConnect.getConnection();

        try {
            st = con.createStatement();
            query = "SELECT P.PLAN_TARIFA FROM PLAN P, DETALLE_CONTRATO D "
                    + "WHERE P.PLAN_CODIGO = D.PLAN_CODIGO AND D.CONTRATO_CODIGO = '" + contrato + "'";
            rs = st.executeQuery(query);
            while (rs.next()) {
                costoPlan = rs.getDouble(1);
            }

            st = con.createStatement();
            query = "SELECT E.EQUIPO_COSTO, D.EQUIPO_CANTIDAD FROM EQUIPO E, "
                    + "DETALLE_CONTRATO_EQUIPO D, DETALLE_CONTRATO C "
                    + "WHERE C.DETALLE_CONTRATO_CODIGO = D.DETALLE_CONTRATO_CODIGO AND "
                    + "D.EQUIPO_CODIGO = E.EQUIPO_CODIGO AND C.CONTRATO_CODIGO = '" + contrato + "'";
            rs = st.executeQuery(query);
            while (rs.next()) {
                costoEquipo = rs.getDouble(1);
                cantidadEquipo = rs.getInt(2);
                sumEquipo += costoEquipo * cantidadEquipo;
            }
            sumEquipo += costoPlan;
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
        } 
        return Double.toString(sumEquipo);
    }

    public String consultarCedulas() {
        String cuerpo = null;
        List<String> cedulas = null;
        try {
            cedulas = new ArrayList<>();
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT CLIENTE_CEDULA FROM CLIENTE";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                cedulas.add(rs.getString(1));
            }

            for (int i = 0; i < cedulas.size(); i++) {
                if (i == 0) {
                    cuerpo = cedulas.get(i);
                } else {
                    cuerpo = cuerpo + cedulas.get(i);
                }
                if (i < cedulas.size() - 1) {
                    cuerpo = cuerpo + "|";
                }
            }

        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }

    public String consultarFormasPago() {
        String cuerpo = null;
        List<String> codForma = null;
        List<String> nameForma = null;
        try {
            codForma = new ArrayList<>();
            nameForma = new ArrayList<>();
            con = DataConnect.getConnection();
            Statement st = con.createStatement();
            String query = "SELECT * FROM FORMA_PAGO";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                codForma.add(rs.getString(1));
                nameForma.add(rs.getString(2));
            }
            for (int i = 0; i < codForma.size(); i++) {
                if (i == 0) {
                    cuerpo = codForma.get(i) + "%" + nameForma.get(i);
                } else {
                    cuerpo = cuerpo + codForma.get(i) + "%" + nameForma.get(i);
                }
                if (i < codForma.size() - 1) {
                    cuerpo = cuerpo + "&";
                }
            }
        } catch (SQLException ex) {
            ///System.out.println(ex.getMessage());
        } finally {
            DataConnect.close(con);
        }
        return cuerpo;
    }
}
