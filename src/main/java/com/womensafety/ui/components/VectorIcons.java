package com.womensafety.ui.components;

import com.womensafety.ui.Theme;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom anti-aliased Java 2D vector icons matching the reference UI iconography.
 */
public final class VectorIcons {

    public enum Type {
        SHIELD_WOMAN,
        BACK_ARROW,
        BELL,
        EMAIL,
        LOCK,
        EYE,
        EYE_OFF,
        PLUS,
        HOME,
        LOCATION_PIN,
        CONTACTS,
        HISTORY_CLOCK,
        PHONE,
        PROFILE_USER,
        SIREN,
        CROSSHAIR,
        SHARE,
        CHEVRON_RIGHT,
        POLICE_SHIELD,
        WOMEN_SYMBOL,
        AMBULANCE,
        SETTINGS_GEAR,
        INFO,
        LOGOUT,
        CHECK_CIRCLE,
        ALERT_CIRCLE
    }

    private VectorIcons() {
    }

    public static Icon get(Type type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                paint(g2, type, size, color);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static void paint(Graphics2D g2, Type type, int s, Color color) {
        g2.setColor(color);
        float strokeW = Math.max(1.4f, s / 13f);
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case SHIELD_WOMAN -> paintShieldWoman(g2, s, color);
            case BACK_ARROW -> {
                GeneralPath p = new GeneralPath();
                p.moveTo(s * 0.65, s * 0.22);
                p.lineTo(s * 0.35, s * 0.50);
                p.lineTo(s * 0.65, s * 0.78);
                g2.draw(p);
            }
            case CHEVRON_RIGHT -> {
                GeneralPath p = new GeneralPath();
                p.moveTo(s * 0.36, s * 0.24);
                p.lineTo(s * 0.64, s * 0.50);
                p.lineTo(s * 0.36, s * 0.76);
                g2.draw(p);
            }
            case BELL -> {
                GeneralPath dome = new GeneralPath();
                dome.moveTo(s * 0.24, s * 0.68);
                dome.lineTo(s * 0.28, s * 0.42);
                dome.curveTo(s * 0.28, s * 0.22, s * 0.72, s * 0.22, s * 0.72, s * 0.42);
                dome.lineTo(s * 0.76, s * 0.68);
                dome.closePath();
                g2.draw(dome);
                g2.draw(new Line2D.Double(s * 0.18, s * 0.68, s * 0.82, s * 0.68));
                g2.draw(new Arc2D.Double(s * 0.42, s * 0.68, s * 0.16, s * 0.16, 180, 180, Arc2D.OPEN));
            }
            case EMAIL -> {
                g2.draw(new RoundRectangle2D.Double(s * 0.12, s * 0.22, s * 0.76, s * 0.56, s * 0.12, s * 0.12));
                GeneralPath flap = new GeneralPath();
                flap.moveTo(s * 0.15, s * 0.26);
                flap.lineTo(s * 0.50, s * 0.53);
                flap.lineTo(s * 0.85, s * 0.26);
                g2.draw(flap);
            }
            case LOCK -> {
                g2.draw(new RoundRectangle2D.Double(s * 0.20, s * 0.44, s * 0.60, s * 0.42, s * 0.12, s * 0.12));
                g2.draw(new Arc2D.Double(s * 0.31, s * 0.16, s * 0.38, s * 0.44, 0, 180, Arc2D.OPEN));
                g2.fill(new Ellipse2D.Double(s * 0.46, s * 0.60, s * 0.08, s * 0.08));
            }
            case EYE -> {
                GeneralPath eye = new GeneralPath();
                eye.moveTo(s * 0.10, s * 0.50);
                eye.curveTo(s * 0.28, s * 0.24, s * 0.72, s * 0.24, s * 0.90, s * 0.50);
                eye.curveTo(s * 0.72, s * 0.76, s * 0.28, s * 0.76, s * 0.10, s * 0.50);
                g2.draw(eye);
                g2.draw(new Ellipse2D.Double(s * 0.39, s * 0.39, s * 0.22, s * 0.22));
            }
            case EYE_OFF -> {
                GeneralPath eye = new GeneralPath();
                eye.moveTo(s * 0.10, s * 0.50);
                eye.curveTo(s * 0.28, s * 0.24, s * 0.72, s * 0.24, s * 0.90, s * 0.50);
                eye.curveTo(s * 0.72, s * 0.76, s * 0.28, s * 0.76, s * 0.10, s * 0.50);
                g2.draw(eye);
                g2.draw(new Ellipse2D.Double(s * 0.39, s * 0.39, s * 0.22, s * 0.22));
                g2.draw(new Line2D.Double(s * 0.16, s * 0.82, s * 0.84, s * 0.18));
            }
            case PLUS -> {
                g2.draw(new Line2D.Double(s * 0.50, s * 0.20, s * 0.50, s * 0.80));
                g2.draw(new Line2D.Double(s * 0.20, s * 0.50, s * 0.80, s * 0.50));
            }
            case HOME -> {
                GeneralPath house = new GeneralPath();
                house.moveTo(s * 0.16, s * 0.46);
                house.lineTo(s * 0.50, s * 0.16);
                house.lineTo(s * 0.84, s * 0.46);
                house.lineTo(s * 0.84, s * 0.84);
                house.lineTo(s * 0.60, s * 0.84);
                house.lineTo(s * 0.60, s * 0.60);
                house.lineTo(s * 0.40, s * 0.60);
                house.lineTo(s * 0.40, s * 0.84);
                house.lineTo(s * 0.16, s * 0.84);
                house.closePath();
                g2.draw(house);
            }
            case LOCATION_PIN -> {
                GeneralPath pin = new GeneralPath();
                pin.moveTo(s * 0.50, s * 0.88);
                pin.curveTo(s * 0.20, s * 0.60, s * 0.20, s * 0.20, s * 0.50, s * 0.14);
                pin.curveTo(s * 0.80, s * 0.20, s * 0.80, s * 0.60, s * 0.50, s * 0.88);
                pin.closePath();
                g2.draw(pin);
                g2.draw(new Ellipse2D.Double(s * 0.39, s * 0.28, s * 0.22, s * 0.22));
            }
            case CONTACTS -> {
                // Two user silhouettes
                g2.draw(new Ellipse2D.Double(s * 0.24, s * 0.22, s * 0.24, s * 0.24));
                g2.draw(new Arc2D.Double(s * 0.14, s * 0.54, s * 0.44, s * 0.34, 0, 180, Arc2D.OPEN));
                g2.draw(new Ellipse2D.Double(s * 0.56, s * 0.26, s * 0.20, s * 0.20));
                g2.draw(new Arc2D.Double(s * 0.54, s * 0.56, s * 0.32, s * 0.28, 0, 145, Arc2D.OPEN));
            }
            case HISTORY_CLOCK -> {
                g2.draw(new Ellipse2D.Double(s * 0.14, s * 0.14, s * 0.72, s * 0.72));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.30, s * 0.50, s * 0.52));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.52, s * 0.66, s * 0.62));
            }
            case PHONE -> {
                GeneralPath phone = new GeneralPath();
                phone.moveTo(s * 0.24, s * 0.22);
                phone.curveTo(s * 0.20, s * 0.45, s * 0.55, s * 0.80, s * 0.78, s * 0.76);
                phone.lineTo(s * 0.82, s * 0.64);
                phone.lineTo(s * 0.67, s * 0.57);
                phone.lineTo(s * 0.60, s * 0.63);
                phone.curveTo(s * 0.49, s * 0.58, s * 0.42, s * 0.51, s * 0.37, s * 0.40);
                phone.lineTo(s * 0.43, s * 0.33);
                phone.lineTo(s * 0.36, s * 0.18);
                phone.closePath();
                g2.draw(phone);
            }
            case PROFILE_USER -> {
                g2.draw(new Ellipse2D.Double(s * 0.34, s * 0.18, s * 0.32, s * 0.32));
                g2.draw(new Arc2D.Double(s * 0.20, s * 0.56, s * 0.60, s * 0.40, 0, 180, Arc2D.OPEN));
            }
            case SIREN -> {
                // Siren dome
                GeneralPath dome = new GeneralPath();
                dome.moveTo(s * 0.30, s * 0.68);
                dome.lineTo(s * 0.33, s * 0.40);
                dome.curveTo(s * 0.34, s * 0.26, s * 0.66, s * 0.26, s * 0.67, s * 0.40);
                dome.lineTo(s * 0.70, s * 0.68);
                dome.closePath();
                g2.draw(dome);
                g2.draw(new RoundRectangle2D.Double(s * 0.24, s * 0.68, s * 0.52, s * 0.12, s * 0.06, s * 0.06));
                // Sound waves
                g2.draw(new Arc2D.Double(s * 0.14, s * 0.24, s * 0.18, s * 0.36, 125, 110, Arc2D.OPEN));
                g2.draw(new Arc2D.Double(s * 0.68, s * 0.24, s * 0.18, s * 0.36, -55, 110, Arc2D.OPEN));
            }
            case CROSSHAIR -> {
                g2.draw(new Ellipse2D.Double(s * 0.22, s * 0.22, s * 0.56, s * 0.56));
                g2.fill(new Ellipse2D.Double(s * 0.43, s * 0.43, s * 0.14, s * 0.14));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.10, s * 0.50, s * 0.22));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.78, s * 0.50, s * 0.90));
                g2.draw(new Line2D.Double(s * 0.10, s * 0.50, s * 0.22, s * 0.50));
                g2.draw(new Line2D.Double(s * 0.78, s * 0.50, s * 0.90, s * 0.50));
            }
            case SHARE -> {
                g2.draw(new Line2D.Double(s * 0.32, s * 0.46, s * 0.68, s * 0.26));
                g2.draw(new Line2D.Double(s * 0.32, s * 0.54, s * 0.68, s * 0.74));
                g2.fill(new Ellipse2D.Double(s * 0.16, s * 0.41, s * 0.18, s * 0.18));
                g2.fill(new Ellipse2D.Double(s * 0.66, s * 0.15, s * 0.18, s * 0.18));
                g2.fill(new Ellipse2D.Double(s * 0.66, s * 0.67, s * 0.18, s * 0.18));
            }
            case POLICE_SHIELD -> {
                GeneralPath sh = new GeneralPath();
                sh.moveTo(s * 0.50, s * 0.14);
                sh.lineTo(s * 0.80, s * 0.26);
                sh.curveTo(s * 0.80, s * 0.58, s * 0.68, s * 0.76, s * 0.50, s * 0.86);
                sh.curveTo(s * 0.32, s * 0.76, s * 0.20, s * 0.58, s * 0.20, s * 0.26);
                sh.closePath();
                g2.draw(sh);
                g2.draw(new Line2D.Double(s * 0.38, s * 0.48, s * 0.62, s * 0.48));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.36, s * 0.50, s * 0.60));
            }
            case WOMEN_SYMBOL -> {
                g2.draw(new Ellipse2D.Double(s * 0.30, s * 0.18, s * 0.40, s * 0.40));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.58, s * 0.50, s * 0.86));
                g2.draw(new Line2D.Double(s * 0.36, s * 0.73, s * 0.64, s * 0.73));
            }
            case AMBULANCE -> {
                g2.draw(new RoundRectangle2D.Double(s * 0.14, s * 0.30, s * 0.50, s * 0.40, s * 0.08, s * 0.08));
                GeneralPath cab = new GeneralPath();
                cab.moveTo(s * 0.64, s * 0.40);
                cab.lineTo(s * 0.78, s * 0.40);
                cab.lineTo(s * 0.86, s * 0.54);
                cab.lineTo(s * 0.86, s * 0.70);
                cab.lineTo(s * 0.64, s * 0.70);
                g2.draw(cab);
                g2.draw(new Ellipse2D.Double(s * 0.24, s * 0.66, s * 0.14, s * 0.14));
                g2.draw(new Ellipse2D.Double(s * 0.66, s * 0.66, s * 0.14, s * 0.14));
                g2.draw(new Line2D.Double(s * 0.39, s * 0.41, s * 0.39, s * 0.57));
                g2.draw(new Line2D.Double(s * 0.31, s * 0.49, s * 0.47, s * 0.49));
            }
            case SETTINGS_GEAR -> {
                g2.draw(new Ellipse2D.Double(s * 0.36, s * 0.36, s * 0.28, s * 0.28));
                g2.draw(new Ellipse2D.Double(s * 0.18, s * 0.18, s * 0.64, s * 0.64));
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    double x1 = s * 0.50 + Math.cos(angle) * (s * 0.32);
                    double y1 = s * 0.50 + Math.sin(angle) * (s * 0.32);
                    double x2 = s * 0.50 + Math.cos(angle) * (s * 0.41);
                    double y2 = s * 0.50 + Math.sin(angle) * (s * 0.41);
                    g2.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
            case INFO -> {
                g2.draw(new Ellipse2D.Double(s * 0.14, s * 0.14, s * 0.72, s * 0.72));
                g2.fill(new Ellipse2D.Double(s * 0.46, s * 0.28, s * 0.08, s * 0.08));
                g2.draw(new Line2D.Double(s * 0.50, s * 0.45, s * 0.50, s * 0.70));
            }
            case LOGOUT -> {
                GeneralPath box = new GeneralPath();
                box.moveTo(s * 0.52, s * 0.20);
                box.lineTo(s * 0.22, s * 0.20);
                box.lineTo(s * 0.22, s * 0.80);
                box.lineTo(s * 0.52, s * 0.80);
                g2.draw(box);
                g2.draw(new Line2D.Double(s * 0.44, s * 0.50, s * 0.82, s * 0.50));
                GeneralPath arr = new GeneralPath();
                arr.moveTo(s * 0.68, s * 0.36);
                arr.lineTo(s * 0.82, s * 0.50);
                arr.lineTo(s * 0.68, s * 0.64);
                g2.draw(arr);
            }
            case CHECK_CIRCLE -> {
                GeneralPath chk = new GeneralPath();
                chk.moveTo(s * 0.28, s * 0.52);
                chk.lineTo(s * 0.44, s * 0.68);
                chk.lineTo(s * 0.74, s * 0.34);
                g2.draw(chk);
            }
            case ALERT_CIRCLE -> {
                g2.draw(new Line2D.Double(s * 0.50, s * 0.28, s * 0.50, s * 0.54));
                g2.fill(new Ellipse2D.Double(s * 0.46, s * 0.65, s * 0.08, s * 0.08));
            }
        }
    }

    private static void paintShieldWoman(Graphics2D g2, int s, Color peach) {
        // Outer shield shape in peach
        GeneralPath shield = new GeneralPath();
        shield.moveTo(s * 0.50, s * 0.08);
        shield.lineTo(s * 0.86, s * 0.22);
        shield.curveTo(s * 0.86, s * 0.60, s * 0.72, s * 0.82, s * 0.50, s * 0.94);
        shield.curveTo(s * 0.28, s * 0.82, s * 0.14, s * 0.60, s * 0.14, s * 0.22);
        shield.closePath();

        g2.setColor(peach);
        g2.fill(shield);

        // Inner dark cutout shield
        GeneralPath inner = new GeneralPath();
        inner.moveTo(s * 0.50, s * 0.16);
        inner.lineTo(s * 0.78, s * 0.27);
        inner.curveTo(s * 0.78, s * 0.58, s * 0.66, s * 0.75, s * 0.50, s * 0.85);
        inner.curveTo(s * 0.34, s * 0.75, s * 0.22, s * 0.58, s * 0.22, s * 0.27);
        inner.closePath();
        g2.setColor(Theme.BG_MAIN);
        g2.fill(inner);

        // Woman profile silhouette inside shield (in primary white/peach)
        GeneralPath profile = new GeneralPath();
        profile.moveTo(s * 0.40, s * 0.74);
        profile.curveTo(s * 0.34, s * 0.60, s * 0.34, s * 0.36, s * 0.48, s * 0.29);
        profile.curveTo(s * 0.57, s * 0.27, s * 0.62, s * 0.36, s * 0.59, s * 0.45);
        profile.lineTo(s * 0.63, s * 0.51);
        profile.lineTo(s * 0.58, s * 0.53);
        profile.lineTo(s * 0.58, s * 0.59);
        profile.curveTo(s * 0.55, s * 0.63, s * 0.51, s * 0.65, s * 0.53, s * 0.76);
        profile.closePath();
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.fill(profile);
    }
}
