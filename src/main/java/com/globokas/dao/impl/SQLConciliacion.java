package com.globokas.dao.impl;

import com.globokas.dao.Conciliacion;
import com.globokas.model.EntidadConciliacion;
import com.globokas.model.ReporteSeguimiento;
import com.globokas.model.sgReporteEntidad;
import com.globokas.util.SqlConection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pvasquez
 */
public class SQLConciliacion implements Conciliacion {

    @Override
    public List<EntidadConciliacion> getEntidadConciliacionList() {

        List<EntidadConciliacion> entidadConciliacionList = new ArrayList<>();
        ResultSet rs = null;
        Connection conn = null;

        try {

            SqlConection c = new SqlConection();
            conn = c.SQLServerConnection("G");
            PreparedStatement ps;
            ps = conn.prepareStatement("{call GES_SP_LISTA_GENERACION_REPORTES()}");
            ps.setString(1, "CONCILIACION_ARCHIVOS");
            rs = ps.executeQuery();
            while (rs.next()) {
                EntidadConciliacion entidad = new EntidadConciliacion();
                entidad.setDes_ent(rs.getString("des_ent"));
                entidad.setId_ent(rs.getInt("id_ent"));
                entidad.setId_estado(rs.getInt("id_estado"));
                entidad.setVc_ruc(rs.getString("ruc"));
                entidad.setVc_razon_social(rs.getString("razonSocial"));
                entidad.setVc_tabla_log_transac(rs.getString("vc_tabla_log_transac"));
                entidad.setVc_ip_banca_desarrollo(rs.getString("vc_ip_banca_desarrollo"));
                entidad.setVc_descripcion_reporte(rs.getString(11));
                entidad.setVc_nombre_stored_sp(rs.getString("vc_nombre_stored_sp"));
                entidad.setIn_cantidad_parametros(rs.getInt("in_cantidad_parametros"));
                entidad.setVc_nombre_archivo_conciliacion(rs.getString("vc_nombre_archivo_conciliacion"));
                entidad.setIn_genera_conciliacion(rs.getInt("in_genera_conciliacion"));
                entidad.setIn_envia_conciliacion(rs.getInt("in_envia_conciliacion"));
                entidad.setIn_carga_conciliacion(rs.getInt("in_carga_conciliacion"));
                entidad.setVc_carpeta_destino(rs.getString("vc_carpeta_destino"));
                entidad.setVc_carpeta_generacion(rs.getString("vc_carpeta_generacion"));
                entidad.setVc_host_ftp(rs.getString("vc_host_ftp"));
                entidad.setVc_username_ftp(rs.getString("vc_username_ftp"));
                entidad.setVc_password_ftp(rs.getString("vc_password_ftp"));
                entidad.setVc_extension_archivo(rs.getString("vc_extension_archivo"));
                entidad.setVc_cadena_conexion(rs.getString("vc_cadena_conexion"));
                entidad.setVc_modo_conexion(rs.getString("vc_modo_conexion").trim());
                entidad.setIn_comprime_archivo(rs.getInt("in_comprime_archivo"));
                entidad.setIn_envio_correo(rs.getInt(24));
                entidadConciliacionList.add(entidad);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return entidadConciliacionList;
    }

    @Override
    public String generarArchivoConciliacion(String fecha, String nombreStored) {

        StringBuilder archivoStr = new StringBuilder();

        try {

            ResultSet rs;
            Connection conn;
            SqlConection c = new SqlConection();
            conn = c.SQLServerConnection("R");
            PreparedStatement ps;
            ps = conn.prepareStatement("{call " + nombreStored + "}");
            ps.setString(1, fecha);
            rs = ps.executeQuery();

            while (rs.next()) {
                archivoStr.append(rs.getString(1));
                archivoStr.append("\r\n");
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException ex) {
            Logger.getLogger(SQLConciliacion.class.getName()).log(Level.SEVERE, null, ex);
        }

        return archivoStr.toString();
    }

    @Override
    public List<ReporteSeguimiento> listarDestinatariosBD(int codigoReporte) {

        ArrayList<ReporteSeguimiento> listado_mail = null;
        try {
            Connection con;

            SqlConection c = new SqlConection();
            con = c.SQLServerConnection("R");
            PreparedStatement stmt;
            stmt = con.prepareStatement("{call dbo.uspDestinatarios(?)}");
            stmt.setInt(1, codigoReporte);

            try (ResultSet rs_mail = stmt.executeQuery()) {
                listado_mail = new ArrayList<>();
                while (rs_mail.next()) {

                    ReporteSeguimiento bean_mail = new ReporteSeguimiento();
                    bean_mail.setNom_mail(rs_mail.getString("vchNombre"));
                    bean_mail.setMail(rs_mail.getString("vchCorreo"));
                    bean_mail.setVer_mail(rs_mail.getString("chVersion"));
                    bean_mail.setMial_ToCcBcc(rs_mail.getString("vchCopia"));
                    listado_mail.add(bean_mail);
                }
            }
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(SQLConciliacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listado_mail;
    }

    @Override
    public List<sgReporteEntidad> getReportesEntidadList(int tipoReporte) {

        List<sgReporteEntidad> reportesList = new ArrayList<>();
        ResultSet rs = null;
        Connection conn = null;
        try {

            SqlConection c = new SqlConection();
            conn = c.SQLServerConnection("G");
            PreparedStatement ps;
            ps = conn.prepareStatement("{call sgSPReportes(?)}");
            ps.setInt(1, tipoReporte);
            rs = ps.executeQuery();
            while (rs.next()) {
                sgReporteEntidad reporte = new sgReporteEntidad();
                reporte.setEntidadID(rs.getInt("entidadID"));
                reporte.setDescripcionReporte(rs.getString("descripcionReporte"));
                reporte.setNombreReporte(rs.getString("nombreReporte"));
                reporte.setExtensionReporte(rs.getString("extensionReporte"));
                reporte.setCarpetaGeneracionReporte(rs.getString("carpetaGeneracionReporte"));
                reporte.setStoredProcedureReporte(rs.getString("storedProcedureReporte"));
                reporte.setFlagEnvioSFTP(rs.getInt("flagEnvioSFTP"));
                reporte.setHostSFTP(rs.getString("hostSFTP"));
                reporte.setUsuarioSFTP(rs.getString("usuarioSFTP"));
                reporte.setClaveSFTP(rs.getString("claveSFTP"));
                reporte.setCarpetaDestinoSFTP(rs.getString("carpetaDestinoSFTP"));
                reporte.setFlagEnvioCorreoCliente(rs.getInt("flagEnvioCorreoCliente"));
                reporte.setFlagEnvioCorreoInterno(rs.getInt("flagEnvioCorreoInterno"));
                reporte.setGrupoCorreoID(rs.getInt("grupoCorreoID"));
                reporte.setOrdenGeneracionReporte(rs.getInt("ordenGeneracionReporte"));
                reporte.setTipoReporte(rs.getInt("tipoReporte"));
                reporte.setEstadoReporteID(rs.getInt("estadoReporteID"));
                reporte.setDiasAnteriorAutomatico(rs.getInt("diasAnteriorAutomatico"));
                reporte.setAsuntoCorreoCliente(rs.getString("asuntoCorreoCliente"));
                reporte.setContenidoCorreoCliente(rs.getString("contenidoCorreoCliente"));
                reporte.setCorreoTOCliente(rs.getString("correoTOCliente"));
                reporte.setCorreoCCCliente(rs.getString("correoCCCliente"));
                reporte.setCorreoCCOCliente(rs.getString("correoCCOCliente"));
                reporte.setFlagAdjuntaReporteCliente(rs.getInt("flagAdjuntaReporteCliente"));
                reporte.setValidarRequest(rs.getInt("flagValidarRequest"));
                reporte.setValidarResponse(rs.getInt("flagValidarResponse"));

                reportesList.add(reporte);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConciliacion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return reportesList;

    }

    @Override
    public int getValidacionRequest(String fecha, int idEntidad) {
        
        int cantidad=0;

        try {

            ResultSet rs;
            Connection conn;
            SqlConection c = new SqlConection();
            conn = c.SQLServerConnection("R");
            PreparedStatement ps;
            ps = conn.prepareStatement("SELECT COUNT(*) FROM log_sw_req WHERE fecha=? AND cliente=?");
            ps.setString(1, fecha);
            ps.setInt(2, idEntidad);
            rs = ps.executeQuery();

            while (rs.next()) {
                cantidad= rs.getInt(1);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException ex) {
            Logger.getLogger(SQLConciliacion.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cantidad;
    }

    @Override
    public int getValidacionResponse(String fecha, int idEntidad) {
        int cantidad=0;

        try {

            ResultSet rs;
            Connection conn;
            SqlConection c = new SqlConection();
            conn = c.SQLServerConnection("R");
            PreparedStatement ps;
            ps = conn.prepareStatement("SELECT COUNT(*) FROM log_sw_resp WHERE fecha=? AND cliente=?");
            ps.setString(1, fecha);
            ps.setInt(2, idEntidad);
            rs = ps.executeQuery();

            while (rs.next()) {
                cantidad= rs.getInt(1);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException ex) {
            Logger.getLogger(SQLConciliacion.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cantidad;
    }

}
