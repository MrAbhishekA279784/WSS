package com.womensafety.ui;

import com.womensafety.config.AppConfig;
import com.womensafety.ui.components.EmergencyCard;
import com.womensafety.ui.components.NavigationBar;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.BaseModalDialog;
import com.womensafety.utils.MapUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Screen 08 — Emergency Support Screen matching reference layout:
 * Police (100), Women Helpline (181), Ambulance (108), Emergency All Services (112).
 */
public class EmergencySupportScreen extends JPanel {

    public EmergencySupportScreen(Runnable onBack, Consumer<NavigationBar.Tab> onNavigateTab) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("Emergency Support", onBack), BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 22, 20, 22));

        Map<String, String> services = AppConfig.getEmergencyNumbers();
        for (Map.Entry<String, String> entry : services.entrySet()) {
            String name = entry.getKey();
            String number = entry.getValue();
            VectorIcons.Type icon = iconForService(name);

            EmergencyCard card = new EmergencyCard(icon, name, number, () -> confirmAndCall(name, number));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(12));
        }

        listPanel.add(Box.createVerticalGlue());
        add(listPanel, BorderLayout.CENTER);
        add(new NavigationBar(null, onNavigateTab), BorderLayout.SOUTH);
    }

    private VectorIcons.Type iconForService(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("police")) {
            return VectorIcons.Type.POLICE_SHIELD;
        }
        if (lower.contains("women")) {
            return VectorIcons.Type.WOMEN_SYMBOL;
        }
        if (lower.contains("ambulance")) {
            return VectorIcons.Type.AMBULANCE;
        }
        return VectorIcons.Type.PHONE;
    }

    private void confirmAndCall(String serviceName, String number) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        boolean confirmed = BaseModalDialog.showConfirm(
                owner,
                "Confirm Emergency Call",
                "Attempt to place call to " + serviceName + " (" + number + ") using your OS phone handler?",
                "Call " + number,
                false
        );
        if (confirmed) {
            MapUtils.ActionResult res = MapUtils.initiateSystemCall(number);
            if (!res.success()) {
                BaseModalDialog.showMessage(owner, "Phone Handler Unavailable", res.message(), false);
            }
        }
    }
}
