package com.womensafety.ui;

import com.womensafety.config.AppConfig;
import com.womensafety.services.AuthService;
import com.womensafety.services.ContactService;
import com.womensafety.services.HistoryService;
import com.womensafety.services.LocationService;
import com.womensafety.services.SOSService;
import com.womensafety.services.SessionManager;
import com.womensafety.services.UserService;
import com.womensafety.ui.components.NavigationBar;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * Main portrait-proportioned desktop JFrame using CardLayout to switch across all 9+ screens.
 */
public class AppFrame extends JFrame {

    public static final String CARD_SPLASH = "SPLASH";
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_SOS = "SOS";
    public static final String CARD_LOCATION = "LOCATION";
    public static final String CARD_CONTACTS = "CONTACTS";
    public static final String CARD_HISTORY = "HISTORY";
    public static final String CARD_SUPPORT = "SUPPORT";
    public static final String CARD_PROFILE = "PROFILE";

    private final CardLayout cardLayout;
    private final JPanel cardContainer;

    private final SplashScreen splashScreen;
    private final LoginScreen loginScreen;
    private final RegisterScreen registerScreen;
    private final DashboardScreen dashboardScreen;
    private final SOSActivationScreen sosScreen;
    private final LocationScreen locationScreen;
    private final ContactsScreen contactsScreen;
    private final HistoryScreen historyScreen;
    private final EmergencySupportScreen supportScreen;
    private final ProfileScreen profileScreen;

    public AppFrame() {
        super(AppConfig.APP_NAME + " — " + AppConfig.APP_SUBTITLE);

        // Configure dark FlatLaf window title bar
        getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.BG_MAIN);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT_PRIMARY);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (Boolean.getBoolean("wss.embedded")) {
            setUndecorated(true);
        }
        setSize(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(390, 760));
        setResizable(false);
        setLocation(0, 0);
        if (!Boolean.getBoolean("wss.embedded")) {
            setLocationRelativeTo(null);
        }
        getContentPane().setBackground(Theme.BG_MAIN);

        // Services
        AuthService authService = new AuthService();
        UserService userService = new UserService();
        ContactService contactService = new ContactService();
        LocationService locationService = new LocationService();
        HistoryService historyService = new HistoryService();
        SOSService sosService = new SOSService();

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(Theme.BG_MAIN);

        splashScreen = new SplashScreen(() -> {
            if (SessionManager.isAuthenticated()) {
                showDashboard();
            } else {
                showLogin();
            }
        });

        loginScreen = new LoginScreen(
                authService,
                this::showDashboard,
                this::showRegister,
                () -> {
                    cardLayout.show(cardContainer, CARD_SPLASH);
                    splashScreen.startInitialization();
                }
        );

        registerScreen = new RegisterScreen(
                authService,
                this::showDashboard,
                this::showLogin
        );

        dashboardScreen = new DashboardScreen(
                contactService,
                historyService,
                this::startSOS,
                this::showSupport,
                this::navigateTab
        );

        sosScreen = new SOSActivationScreen(
                sosService,
                this::showDashboard
        );

        locationScreen = new LocationScreen(
                locationService,
                this::showDashboard,
                this::navigateTab
        );

        contactsScreen = new ContactsScreen(
                contactService,
                this::showDashboard,
                this::navigateTab
        );

        historyScreen = new HistoryScreen(
                historyService,
                this::showDashboard,
                this::navigateTab
        );

        supportScreen = new EmergencySupportScreen(
                this::showDashboard,
                this::navigateTab
        );

        profileScreen = new ProfileScreen(
                userService,
                authService,
                this::showDashboard,
                this::showLogin,
                this::navigateTab
        );

        cardContainer.add(splashScreen, CARD_SPLASH);
        cardContainer.add(loginScreen, CARD_LOGIN);
        cardContainer.add(registerScreen, CARD_REGISTER);
        cardContainer.add(dashboardScreen, CARD_DASHBOARD);
        cardContainer.add(sosScreen, CARD_SOS);
        cardContainer.add(locationScreen, CARD_LOCATION);
        cardContainer.add(contactsScreen, CARD_CONTACTS);
        cardContainer.add(historyScreen, CARD_HISTORY);
        cardContainer.add(supportScreen, CARD_SUPPORT);
        cardContainer.add(profileScreen, CARD_PROFILE);

        setContentPane(cardContainer);
    }

    public void launch() {
        setVisible(true);
        cardLayout.show(cardContainer, CARD_SPLASH);
        splashScreen.startInitialization();
    }

    public void showLogin() {
        loginScreen.resetForm();
        cardLayout.show(cardContainer, CARD_LOGIN);
    }

    public void showRegister() {
        registerScreen.resetForm();
        cardLayout.show(cardContainer, CARD_REGISTER);
    }

    public void showDashboard() {
        dashboardScreen.refreshData();
        cardLayout.show(cardContainer, CARD_DASHBOARD);
    }

    public void startSOS() {
        cardLayout.show(cardContainer, CARD_SOS);
        sosScreen.startSOSWorkflow();
    }

    public void showLocation() {
        locationScreen.refreshData();
        cardLayout.show(cardContainer, CARD_LOCATION);
    }

    public void showContacts() {
        contactsScreen.refreshData();
        cardLayout.show(cardContainer, CARD_CONTACTS);
    }

    public void showHistory() {
        historyScreen.refreshData();
        cardLayout.show(cardContainer, CARD_HISTORY);
    }

    public void showSupport() {
        cardLayout.show(cardContainer, CARD_SUPPORT);
    }

    public void showProfile() {
        profileScreen.refreshData();
        cardLayout.show(cardContainer, CARD_PROFILE);
    }

    private void navigateTab(NavigationBar.Tab tab) {
        switch (tab) {
            case HOME -> showDashboard();
            case LOCATION -> showLocation();
            case CONTACTS -> showContacts();
            case HISTORY -> showHistory();
            case PROFILE -> showProfile();
        }
    }
}
