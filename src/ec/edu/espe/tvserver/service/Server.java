/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.espe.tvserver.service;

import java.net.ServerSocket;

/**
 *
 * @author Jonathan
 */
public class Server {

    private static final int puerto = 7000;

    public static void main(String[] args) throws Exception {
        System.out.println("Servidor OK en puerto: " + puerto);
        try (ServerSocket socketServer = new ServerSocket(puerto)) {
            while (true) {
                new ServerClient(socketServer.accept()).start();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
