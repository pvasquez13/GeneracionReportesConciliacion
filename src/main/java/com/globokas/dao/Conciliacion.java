package com.globokas.dao;

import com.globokas.model.EntidadConciliacion;
import com.globokas.model.ReporteSeguimiento;
import com.globokas.model.sgReporteEntidad;
import java.util.List;

/**
 *
 * @author pvasquez
 */
public interface Conciliacion {

    public List<EntidadConciliacion> getEntidadConciliacionList();

    public String generarArchivoConciliacion(String fecha, String nombreStored);

    public List<ReporteSeguimiento> listarDestinatariosBD(int codigoReporte);

    public List<sgReporteEntidad> getReportesEntidadList(int tipoReporte);

    public int getValidacionRequest(String fecha, int idEntidad);

    public int getValidacionResponse(String fecha, int idEntidad);

}
