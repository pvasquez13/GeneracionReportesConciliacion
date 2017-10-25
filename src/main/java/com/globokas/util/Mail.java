package com.globokas.util;

import com.globokas.dao.Conciliacion;
import com.globokas.dao.impl.SQLConciliacion;
import com.globokas.model.ReporteSeguimiento;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

public class Mail {

    private final Conciliacion conciliacion = new SQLConciliacion();

    public void runInterno(String subject, String body, InternetAddress[] mailAddress_TO, InternetAddress[] mailAddress_CC,
            InternetAddress[] mailAddress_BCC, List<File> archivosAttach) {
        Message message = new MimeMessage(getSession());

        try {

            message.addRecipients(RecipientType.TO, mailAddress_TO);
            message.addRecipients(RecipientType.CC, mailAddress_CC);
            message.addRecipients(RecipientType.BCC, mailAddress_BCC);
            message.addFrom(new InternetAddress[]{new InternetAddress("informacion@globokas.com")});
            message.setSubject(subject);
//            message.setContent(body, "text/html");

            //Envio archivo adjunto
            Multipart mp = new MimeMultipart();
            if (archivosAttach != null) {
                for (File archivo : archivosAttach) {
                    MimeBodyPart mbp = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(archivo);
                    mbp.setDataHandler(new DataHandler(fds));
                    mbp.setFileName(fds.getName());
                    mp.addBodyPart(mbp);
                }
            }
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(body, "text/html");
            mp.addBodyPart(mbp);
            message.setContent(mp);
            //Fin Envio archivo adjunto

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        Authenticator authenticator = new Authenticator();

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
        properties.setProperty("mail.smtp.auth", ConfigApp.getValue("auth"));

        properties.setProperty("mail.smtp.host", ConfigApp.getValue("host"));
        properties.setProperty("mail.smtp.port", ConfigApp.getValue("port"));

        properties.setProperty("mail.smtp.starttls.enable", ConfigApp.getValue("starttlsEnable"));
        properties.setProperty("mail.smtp.starttls.required", ConfigApp.getValue("starttlsRequired"));

        return Session.getInstance(properties, authenticator);
    }

    private class Authenticator extends javax.mail.Authenticator {

        private final PasswordAuthentication authentication;

        public Authenticator() {
            String username = ConfigApp.getValue("mailUserAuth");
            String password = "Informacion1";
//            String password = ConfigApp.getValue("mailUserPass");
            authentication = new PasswordAuthentication(username, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return authentication;
        }
    }

    public InternetAddress[] retornaListaCorreos(List<String> correosDestino) {
        InternetAddress[] mailAddress = new InternetAddress[correosDestino.size()];
        int indiceCorreoTO = 0;
        try {
            for (String correoDestino : correosDestino) {
                mailAddress[indiceCorreoTO] = new InternetAddress(correoDestino);
                indiceCorreoTO++;
            }

        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mailAddress;

    }

    public List<String> obtieneCorreosPorTipoCopia(List<ReporteSeguimiento> listaDestinatarios, String tipoCopia) {
        List<String> correosDestino = new ArrayList<>();
        for (ReporteSeguimiento emailDestino : listaDestinatarios) {
            if (emailDestino.getMial_ToCcBcc().trim().equals(tipoCopia)) {
                correosDestino.add(emailDestino.getMail());
            }
        }
        return correosDestino;
    }

    public void enviaCorreoPorGrupo(String asuntoCorreo, String bodyCorreo, int codigoReporte) {
        Mail m = new Mail();
        List<ReporteSeguimiento> listaDestinatarios = conciliacion.listarDestinatariosBD(codigoReporte);
        List<String> correosDestinoTO = m.obtieneCorreosPorTipoCopia(listaDestinatarios, "TO");
        List<String> correosDestinoCC = m.obtieneCorreosPorTipoCopia(listaDestinatarios, "CC");
        List<String> correosDestinoBCC = m.obtieneCorreosPorTipoCopia(listaDestinatarios, "BCC");
        InternetAddress[] mailAddress_TO = m.retornaListaCorreos(correosDestinoTO);
        InternetAddress[] mailAddress_CC = m.retornaListaCorreos(correosDestinoCC);
        InternetAddress[] mailAddress_BCC = m.retornaListaCorreos(correosDestinoBCC);
        m.runInterno(asuntoCorreo, bodyCorreo, mailAddress_TO, mailAddress_CC, mailAddress_BCC, null);
    }

    public void enviaCorreoCliente(String asuntoCorreo, String bodyCorreo, String mailAddress_TO,
            String mailAddress_CC, String mailAddress_BCC, int flagAdjuntarArchivo, String rutaFileServidor, String fecha) {

        try {
            Mail m = new Mail();
            List<File> files = new ArrayList<>();
            if (flagAdjuntarArchivo == 1) {
                File file = new File(rutaFileServidor);
                files.add(file);
            }

            m.runInterno(getReemplazarContenido(asuntoCorreo, fecha), getReemplazarContenido(bodyCorreo, fecha),
                    generarDireccionesCorreo(mailAddress_TO), generarDireccionesCorreo(mailAddress_CC),
                    generarDireccionesCorreo(mailAddress_BCC), files);
        } catch (Exception ex) {
            Logger.getLogger(Mail.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private InternetAddress[] generarDireccionesCorreo(String direcciones) throws Exception {

        InternetAddress[] arregloAdd;
        if (direcciones == null) {
            arregloAdd = new InternetAddress[0];
        } else {
            String[] arregloStr = direcciones.split(";");
            arregloAdd = new InternetAddress[arregloStr.length];

            for (int i = 0; i < arregloAdd.length; i++) {
                arregloAdd[i] = new InternetAddress(arregloStr[i]);
            }
        }

        return arregloAdd;
    }

    private String getReemplazarContenido(String contenido, String fecha) {

        String newContenido = contenido.replace("$fecha", fecha);

        return newContenido;
    }
}
