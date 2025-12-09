package proyectocope;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.JOptionPane;


/**
 *
 * @author Orlando Barreras, Juan Lizarraga, Ramses Murrieta
 */
public class juego extends JFrame {

    Timer gameLoop; // el timer chido para que corra fluido a 60fps
    
    JLabel lblFondo2;
    JLabel lblPuntaje;
    int puntaje = 0;
    
    int bg1X = 0;
    int bg2X = 570; 
    
    int birdX, birdY;
    double velocidadY = 0;
    
    // fisicas ajustadas para que no brinque a lo loco
    double gravedad = 0.6; 
    double impulso = -6; 
    
    boolean isGameOver = false;
    boolean yaGano = false;
    int anchoFondo;

    int xPar1, xPar2;
    int yPar1, yPar2;
    int gap = 140; 
    int velocidad = 3; 
    Random random = new java.util.Random();

    public juego() {
        initComponents();

        // matamos el layout de netbeans para que no mueva las cosas solo
        jPanel1.setLayout(null); 

        // configuracion del puntaje manual
        lblPuntaje = new JLabel("0");
        lblPuntaje.setFont(new Font("Arial", Font.BOLD, 48));
        lblPuntaje.setForeground(Color.WHITE);
        lblPuntaje.setBounds(20, 20, 300, 60); 
        jPanel1.add(lblPuntaje); 
        
        // configuracion del fondo infinito
        lblPajaro.setSize(50, 40);
        ImageIcon imagenOriginal = new ImageIcon(getClass().getResource("/imagenes/fondo.png"));
        int altoVentana = 440;
        
        if (imagenOriginal.getIconHeight() > 0) {
            anchoFondo = (imagenOriginal.getIconWidth() * altoVentana) / imagenOriginal.getIconHeight();
        } else {
            anchoFondo = 800; 
        }
        
        Image imgEscalada = imagenOriginal.getImage().getScaledInstance(anchoFondo, altoVentana, Image.SCALE_SMOOTH);
        ImageIcon iconoFondo = new ImageIcon(imgEscalada);
        
        lblFondo.setIcon(iconoFondo);
        lblFondo.setBounds(0, 0, anchoFondo, altoVentana);
        
        lblFondo2 = new JLabel();
        lblFondo2.setIcon(iconoFondo);
        lblFondo2.setBounds(anchoFondo, 0, anchoFondo, altoVentana);
        
        bg1X = 0;
        bg2X = anchoFondo; 
        
        jPanel1.add(lblFondo2);
        
        // mandamos los fondos al fondo xd
        jPanel1.setComponentZOrder(lblFondo, jPanel1.getComponentCount() - 1);
        jPanel1.setComponentZOrder(lblFondo2, jPanel1.getComponentCount() - 1);

        // cargamos las imagenes
        lblPajaro.setIcon(getScaledImage("/imagenes/pajaro.png", 50, 40));
        lblTuboA1.setIcon(getFlippedScaledImage("/imagenes/tubon.png", 150, 250));
        lblTuboAb1.setIcon(getScaledImage("/imagenes/tubon.png", 150, 300));
        lblTuboA2.setIcon(getFlippedScaledImage("/imagenes/tubon.png", 150, 210));
        lblTuboAb2.setIcon(getScaledImage("/imagenes/tubon.png", 150, 300));

        // controles del teclado
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    saltar();
                }
            }
        });
        setFocusable(true);

        // donde inician los tubos fuera de pantalla
        xPar1 = 600; 
        xPar2 = 900; 
        yPar1 = -100; 
        yPar2 = -50;

        birdX = lblPajaro.getX();
        birdY = 200; 
        lblPajaro.setLocation(birdX, birdY);

        // boton de jugar
        JButton btnInicio = new JButton("JUGAR");
        btnInicio.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        btnInicio.setBackground(java.awt.Color.ORANGE);
        btnInicio.setForeground(java.awt.Color.WHITE);
        btnInicio.setBounds(200, 180, 150, 60);
        
        btnInicio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnInicio.setVisible(false);
                comenzarJuego();
                juego.this.requestFocusInWindow(); // regresamos el foco para que sirva el espacio
            }
        });
        jPanel1.add(btnInicio); 
        jPanel1.setComponentZOrder(lblPuntaje, 0);
        jPanel1.setComponentZOrder(btnInicio, 1);
        
        // esto es clave: el puntaje siempre hasta arriba
        jPanel1.setComponentZOrder(lblPuntaje, 0);
        jPanel1.repaint();
    }

    private void comenzarJuego() {
        if (gameLoop != null && gameLoop.isRunning()) return; 

        // el loop principal, aqui pasa la magia cada 16ms
        gameLoop = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    moverEscenario();
                    moverPajaro();
                    moverTuberias();
                    verificarColision();
                    jPanel1.repaint(); 
                } else {
                    ((Timer)e.getSource()).stop(); 
                }
            }
        });
        gameLoop.start();
    }

    private void moverEscenario() {
        bg1X -= velocidad; 
        bg2X -= velocidad;
        
        // logica para el fondo infinito
        if (bg1X + anchoFondo <= 0) bg1X = bg2X + anchoFondo;
        if (bg2X + anchoFondo <= 0) bg2X = bg1X + anchoFondo;
        
        lblFondo.setLocation(bg1X, 0);
        lblFondo2.setLocation(bg2X, 0);
    }

    private void moverPajaro() {
        velocidadY += gravedad;
        birdY += velocidadY;

        // para que no se salga del techo o piso
        if (birdY < 0) {
            birdY = 0;
            velocidadY = 0;
        }
        if (birdY + lblPajaro.getHeight() > jPanel1.getHeight()) { 
            detenerJuego();
        }

        lblPajaro.setLocation(birdX, birdY);
    }

    private void moverTuberias() {
        xPar1 -= velocidad;
        xPar2 -= velocidad;

        // reciclo el primer par de tubos
        if (xPar1 <= -lblTuboA1.getWidth()) {
            xPar1 = xPar2 + 300; 
            yPar1 = random.nextInt(150) - 200;
            sumarPunto(); 
        }

        // reciclo el segundo par
        if (xPar2 <= -lblTuboA2.getWidth()) {
            xPar2 = xPar1 + 300;
            yPar2 = random.nextInt(150) - 200;
            sumarPunto();
        }

        // actualizo donde se ven los tubos
        lblTuboA1.setLocation(xPar1, yPar1);
        lblTuboAb1.setLocation(xPar1, yPar1 + lblTuboA1.getHeight() + gap);

        lblTuboA2.setLocation(xPar2, yPar2);
        lblTuboAb2.setLocation(xPar2, yPar2 + lblTuboA2.getHeight() + gap);
    }

    private void saltar() {
        if (!isGameOver) velocidadY = impulso;
    }

    private void sumarPunto() {
        puntaje++;
        if (puntaje >= 15) {
            yaGano = true;
            lblPuntaje.setText("WIN: " + puntaje);
            lblPuntaje.setForeground(Color.GREEN);
        } else {
            lblPuntaje.setText(String.valueOf(puntaje));
        }
    }

    private void verificarColision() {
        Rectangle rectPajaro = lblPajaro.getBounds();
        // hitbox un poco mas chica para dar chance
        Rectangle hitboxPajaro = new Rectangle(
                rectPajaro.x + 8, rectPajaro.y + 8,
                rectPajaro.width - 16, rectPajaro.height - 16
        );

        if (hitboxPajaro.intersects(obtenerHitbox(lblTuboA1))
                || hitboxPajaro.intersects(obtenerHitbox(lblTuboAb1))
                || hitboxPajaro.intersects(obtenerHitbox(lblTuboA2))
                || hitboxPajaro.intersects(obtenerHitbox(lblTuboAb2))) {

            detenerJuego();
        }
    }

    private Rectangle obtenerHitbox(JLabel tubo) {
        Rectangle r = tubo.getBounds();
        return new Rectangle(r.x + 10, r.y, r.width - 20, r.height);
    }

    private void detenerJuego() {
        if(isGameOver) return; 
        isGameOver = true;
        if (gameLoop != null) gameLoop.stop(); 

        String titulo;
        String mensaje;
        int tipoIcono;

        if (yaGano) {
            titulo = "ERES UN MAESTRO!";
            mensaje = "felicidades completaste el objetivo\npuntaje final: " + puntaje;
            tipoIcono = JOptionPane.INFORMATION_MESSAGE;
        } else {
            titulo = "GAME OVER";
            mensaje = "perdiste xddd\npuntos: " + puntaje;
            tipoIcono = JOptionPane.ERROR_MESSAGE;
        }
        
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipoIcono);
        System.exit(0);
    }

    // metodos para cargar imagenes (no moverle mucho)
    private ImageIcon getFlippedScaledImage(String ruta, int ancho, int alto) {
        try {
            java.net.URL imgUrl = getClass().getResource(ruta);
            if (imgUrl == null) return null;
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image originalImage = originalIcon.getImage();
            BufferedImage flippedImage = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = flippedImage.createGraphics();
            AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
            transform.translate(0, -alto);
            g2d.setTransform(transform);
            g2d.drawImage(originalImage, 0, 0, ancho, alto, null);
            g2d.dispose();
            return new ImageIcon(flippedImage);
        } catch (Exception e) { return null; }
    }

    private ImageIcon getScaledImage(String ruta, int ancho, int alto) {
         try {
            java.net.URL imgUrl = getClass().getResource(ruta);
            if (imgUrl == null) return null;
            return new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
         } catch (Exception e) { return null; }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblTuboA1 = new javax.swing.JLabel();
        lblTuboAb1 = new javax.swing.JLabel();
        lblTuboA2 = new javax.swing.JLabel();
        lblTuboAb2 = new javax.swing.JLabel();
        lblPajaro = new javax.swing.JLabel();
        lblFondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CopeBird (Fake FlappyBird)");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setMaximumSize(new java.awt.Dimension(570, 440));
        jPanel1.setMinimumSize(new java.awt.Dimension(570, 440));
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(570, 440));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(lblTuboA1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 0, 150, 250));
        jPanel1.add(lblTuboAb1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 244, 150, 300));
        jPanel1.add(lblTuboA2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, -30, 150, 210));
        jPanel1.add(lblTuboAb2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 180, 150, 300));
        jPanel1.add(lblPajaro, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));
        jPanel1.add(lblFondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 440));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 570, 440));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new juego().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblFondo;
    private javax.swing.JLabel lblPajaro;
    private javax.swing.JLabel lblTuboA1;
    private javax.swing.JLabel lblTuboA2;
    private javax.swing.JLabel lblTuboAb1;
    private javax.swing.JLabel lblTuboAb2;
    // End of variables declaration//GEN-END:variables
}
