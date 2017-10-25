
import com.globokas.dao.Conciliacion;
import com.globokas.dao.impl.SQLConciliacion;
import com.globokas.model.sgReporteEntidad;
import com.globokas.util.AppUtil;
import com.globokas.util.Mail;
import com.zehon.FileTransferStatus;
import com.zehon.exception.FileTransferException;
import com.zehon.sftp.SFTP;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author pvasquez
 */
public class GeneracionReportesConciliacion {

    private static final Logger LOG = Logger.getLogger(GeneracionReportesConciliacion.class);
    private static String fecha;
    private static String asuntoCorreo;
    private static String bodyCorreo;
    private static final Conciliacion conciliacion = new SQLConciliacion();
    private static String filePath;

    public static void main(String[] args) {
        int tipoReporte = Integer.parseInt(args[0]);
        ProcesarReportes(tipoReporte);
    }

    public static void ProcesarReportes(int tipoReporte) {

        List<sgReporteEntidad> reportesList = conciliacion.getReportesEntidadList(tipoReporte);
        for (sgReporteEntidad rpt : reportesList) {
            Mail mail = new Mail();
            fecha = AppUtil.getFecha(rpt.getDiasAnteriorAutomatico());
            filePath = getFilePath(rpt);
            if (AppUtil.generarReporte(rpt, fecha)) {
                mensajeGeneracionCorrecta(rpt);
                if (rpt.getFlagEnvioSFTP() == 1) {
                    envioSFTP(rpt);
                }
                //Envio a clientes
                if (rpt.getFlagEnvioCorreoCliente() == 1) {
                    mail.enviaCorreoCliente(rpt.getAsuntoCorreoCliente(), rpt.getContenidoCorreoCliente(),
                            rpt.getCorreoTOCliente(), rpt.getCorreoCCCliente(), rpt.getCorreoCCOCliente(),
                            rpt.getFlagAdjuntaReporteCliente(),filePath, fecha);
                }
            } else {
                mensajeGeneracionIncorrecta(rpt);
            }
            //Envio confirmación Interno
            if (rpt.getFlagEnvioCorreoInterno() == 1) {
                mail.enviaCorreoPorGrupo(asuntoCorreo, bodyCorreo, rpt.getGrupoCorreoID());
            }
        }
    }

    public static String getFilePath(sgReporteEntidad reporteEntidad) {
        String ruta = reporteEntidad.getCarpetaGeneracionReporte();
        String filename = reporteEntidad.getNombreReporte() + fecha + "." + reporteEntidad.getExtensionReporte();
        return ruta + filename;
    }

    public static void envioSFTP(sgReporteEntidad entidad) {
        LOG.info("Inicio transferencia Conciliacion [" + entidad.getDescripcionReporte() + "]");
        String filename = entidad.getNombreReporte() + fecha + "." + entidad.getExtensionReporte();
        String host = entidad.getHostSFTP();
        String username = entidad.getUsuarioSFTP();
        String password = entidad.getClaveSFTP();
        String destFolder = entidad.getCarpetaDestinoSFTP();

        int status = -1;

        try {
            status = SFTP.sendFile(filePath, destFolder, host, username, password);
        } catch (FileTransferException ex) {
            java.util.logging.Logger.getLogger(GeneracionReportesConciliacion.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (status != -1) {
            if (FileTransferStatus.SUCCESS == status) {
                LOG.info(filePath + " got SFTP successfully to  folder " + destFolder);
                asuntoCorreo = "FileTransferStatus - " + entidad.getDescripcionReporte() + "- SUCCESS";
                bodyCorreo = "El archivo " + filename + " ha sido transferido correctamente.";

            } else if (FileTransferStatus.FAILURE == status) {
                LOG.info("Fail to SFTP  to  folder " + destFolder);
                asuntoCorreo = "FileTransferStatus - " + entidad.getDescripcionReporte() + "- FAILURE";
                bodyCorreo = "El archivo " + filename + " no ha sido transferido correctamente.";
            }
        } else {
            LOG.info("No se ha especificado el modo de conexion" + destFolder);
            asuntoCorreo = "FileTransferStatus - " + entidad.getDescripcionReporte() + "- FAILURE";
            bodyCorreo = "El archivo " + filename + " no ha sido transferido correctamente. No se ha especificado el modo de conexion";

        }

        LOG.info("Fin transferencia Conciliacion [" + entidad.getDescripcionReporte() + "]");
    }

    public static void mensajeGeneracionCorrecta(sgReporteEntidad entidad) {
        String filename = entidad.getNombreReporte() + fecha + "." + entidad.getExtensionReporte();
        asuntoCorreo = "FileGenerateStatus - " + entidad.getDescripcionReporte() + " - SUCCESS ";
        bodyCorreo = "La generación del archivo " + filename + " ha culminado satisfactoriamente";
    }

    public static void mensajeGeneracionIncorrecta(sgReporteEntidad entidad) {
        String filename = entidad.getNombreReporte() + fecha + "." + entidad.getExtensionReporte();
        asuntoCorreo = "FileGenerateStatus - " + entidad.getDescripcionReporte() + " - FAIL ";
        bodyCorreo = "La generación del archivo " + filename + " no ha culminado satisfactoriamente, revise la BD.";
    }

}
