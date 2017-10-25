package com.globokas.util;

import com.globokas.dao.Conciliacion;
import com.globokas.dao.impl.SQLConciliacion;
import com.globokas.model.sgReporteEntidad;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author pvasquez
 */
public class AppUtil {

    private static final Conciliacion conciliacion = new SQLConciliacion();
    private static final Logger LOG = Logger.getLogger(AppUtil.class);

    public static String getFecha(int diasAutomatico) {
        String modo_automatico = ConfigApp.getValue("MODO_AUTOMATICO");
        if (modo_automatico.equals("FALSE")) {
            return ConfigApp.getValue("FECHA_OPERACION");
        } else {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -diasAutomatico);
            return df.format(c.getTime());
        }
    }

    public static boolean generarReporte(sgReporteEntidad entidad, String fecha) {
        BufferedWriter out = null;
        try {
            LOG.info("Inicio Generacion Reporte [" + entidad.getDescripcionReporte() + "]");

            if (entidad.getValidarRequest() == 1) {
                int cantidadReq = conciliacion.getValidacionRequest(fecha, entidad.getEntidadID());
                LOG.info("cantidadReq=" + cantidadReq + " Entidad: " + entidad.getEntidadID());
                if (cantidadReq == 0) {
                    return false;
                }
            }
            if (entidad.getValidarResponse() == 1) {
                int cantidadRes = conciliacion.getValidacionResponse(fecha, entidad.getEntidadID());
                LOG.info("cantidadRes=" + cantidadRes + " Entidad: " + entidad.getEntidadID());
                if (cantidadRes == 0) {
                    return false;
                }
            }

            String ruta = entidad.getCarpetaGeneracionReporte();
            String filename = entidad.getNombreReporte() + fecha + "." + entidad.getExtensionReporte();
            String archivo = conciliacion.generarArchivoConciliacion(fecha, entidad.getStoredProcedureReporte());
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruta + filename), "latin1"));
            out.write(archivo);
            LOG.info("Fin Generacion Reporte [" + entidad.getDescripcionReporte() + "]");
            return true;
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(AppUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(AppUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AppUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (out!=null) {
                    out.close();
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(AppUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
}
