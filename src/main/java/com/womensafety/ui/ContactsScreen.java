package com.womensafety.ui;

import com.womensafety.models.EmergencyContact;
import com.womensafety.models.User;
import com.womensafety.services.ContactService;
import com.womensafety.services.ServiceResult;
import com.womensafety.services.SessionManager;
import com.womensafety.ui.components.ContactCard;
import com.womensafety.ui.components.NavigationBar;
import com.womensafety.ui.components.PrimaryButton;
import com.womensafety.ui.components.ScreenHeader;
import com.womensafety.ui.components.VectorIcons;
import com.womensafety.ui.dialogs.AddContactDialog;
import com.womensafety.ui.dialogs.BaseModalDialog;
import com.womensafety.ui.dialogs.EditContactDialog;
import com.womensafety.utils.MapUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen 06 — Trusted Contacts Screen matching reference layout:
 * SQLite-backed vertical contact cards, empty state when none exist, Add Contact button, NavigationBar.
 */
public class ContactsScreen extends JPanel {

    private final ContactService contactService;
    private final JPanel listContainer;

    public ContactsScreen(ContactService contactService,
                          Runnable onBack,
                          Consumer<NavigationBar.Tab> onNavigateTab) {
        this.contactService = contactService;

        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);

        add(new ScreenHeader("Trusted Contacts", onBack), BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBorder(BorderFactory.createEmptyBorder(8, 22, 16, 22));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel addBtnWrap = new JPanel(new BorderLayout());
        addBtnWrap.setOpaque(false);
        addBtnWrap.setBorder(BorderFactory.createEmptyBorder(10, 22, 18, 22));

        PrimaryButton addContactBtn = new PrimaryButton(
                "Add Contact",
                PrimaryButton.Style.PRIMARY_PEACH,
                VectorIcons.get(VectorIcons.Type.PLUS, 17, Theme.TEXT_ON_PEACH)
        );
        addContactBtn.addActionListener(e -> openAddDialog());
        addBtnWrap.add(addContactBtn, BorderLayout.CENTER);

        centerWrapper.add(addBtnWrap, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);
        add(new NavigationBar(NavigationBar.Tab.CONTACTS, onNavigateTab), BorderLayout.SOUTH);
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        long userId = user.getId();
        new SwingWorker<ServiceResult<List<EmergencyContact>>, Void>() {
            @Override
            protected ServiceResult<List<EmergencyContact>> doInBackground() {
                return contactService.getContacts(userId);
            }

            @Override
            protected void done() {
                try {
                    ServiceResult<List<EmergencyContact>> res = get();
                    if (res.success() && res.data() != null) {
                        renderContacts(res.data());
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void renderContacts(List<EmergencyContact> contacts) {
        listContainer.removeAll();

        if (contacts.isEmpty()) {
            JPanel emptyBox = new JPanel();
            emptyBox.setOpaque(false);
            emptyBox.setLayout(new BoxLayout(emptyBox, BoxLayout.Y_AXIS));
            emptyBox.setBorder(BorderFactory.createEmptyBorder(70, 12, 20, 12));

            JLabel icon = new JLabel(VectorIcons.get(VectorIcons.Type.CONTACTS, 42, Theme.TEXT_MUTED));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel title = new JLabel("No Trusted Contacts Yet", SwingConstants.CENTER);
            title.setFont(Theme.bold(15f));
            title.setForeground(Theme.TEXT_PRIMARY);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel desc = new JLabel(
                    "<html><center>Add family members or friends to build your emergency contact list.</center></html>",
                    SwingConstants.CENTER
            );
            desc.setFont(Theme.regular(12.5f));
            desc.setForeground(Theme.TEXT_SECONDARY);
            desc.setAlignmentX(Component.CENTER_ALIGNMENT);
            desc.setMaximumSize(new Dimension(280, 48));

            emptyBox.add(icon);
            emptyBox.add(Box.createVerticalStrut(12));
            emptyBox.add(title);
            emptyBox.add(Box.createVerticalStrut(6));
            emptyBox.add(desc);
            listContainer.add(emptyBox);
        } else {
            for (EmergencyContact contact : contacts) {
                ContactCard card = new ContactCard(
                        contact,
                        () -> openEditDialog(contact),
                        () -> confirmAndCall(contact)
                );
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                listContainer.add(card);
                listContainer.add(Box.createVerticalStrut(12));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void openAddDialog() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        AddContactDialog dialog = new AddContactDialog(owner, user.getId(), contactService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void openEditDialog(EmergencyContact contact) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditContactDialog dialog = new EditContactDialog(owner, contact, contactService);
        dialog.setVisible(true);
        if (dialog.isChanged()) {
            refreshData();
        }
    }

    private void confirmAndCall(EmergencyContact contact) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        boolean confirmed = BaseModalDialog.showConfirm(
                owner,
                "Call Trusted Contact",
                "Initiate system call to " + contact.getContactName() + " (" + contact.getPhoneNumber() + ")?",
                "Call",
                false
        );
        if (confirmed) {
            MapUtils.ActionResult res = MapUtils.initiateSystemCall(contact.getPhoneNumber());
            if (!res.success()) {
                BaseModalDialog.showMessage(owner, "Call Handler Notice", res.message(), false);
            }
        }
    }
}
